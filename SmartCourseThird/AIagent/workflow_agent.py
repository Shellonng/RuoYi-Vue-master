from __future__ import annotations

import json
import os
import threading
import uuid
from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Callable, Dict, Generator, List, Optional, Protocol, Tuple

from assembly import AssemblyResult, PaperAssembler, PaperSpec
from db import KnowledgePoint, QUESTION_TYPES, QuestionBankRepository
from llm import LLMClient
from publisher import AssignmentPublisher, PublishRequest


# 流式事件类型
@dataclass
class StreamEvent:
    event_type: str  # "thinking", "chunk", "action", "result", "done", "error"
    data: Any = None
    message: str = ""


class SessionRecorder(Protocol):
    def create_session(self, spec: PaperSpec, result: AssemblyResult) -> str:  # pragma: no cover - protocol hint
        ...


@dataclass
class SpecState:
    knowledge_points: Optional[List[str]] = None
    target_difficulty: Optional[float] = None
    question_type_counts: Dict[str, int] = field(default_factory=dict)
    total_score: Optional[int] = None
    chapter_ids: Optional[List[int]] = None

    def update(self, payload: Dict[str, Any]):
        if not payload:
            return
        if "knowledge_points" in payload and payload["knowledge_points"]:
            values = []
            for item in payload["knowledge_points"]:
                norm = self._normalize_kp(item)
                if norm:
                    values.append(norm)
            if values:
                self.knowledge_points = values
        if "target_difficulty" in payload:
            value = self._parse_difficulty(payload["target_difficulty"])
            if value is not None:
                self.target_difficulty = value
        if "question_type_counts" in payload and isinstance(payload["question_type_counts"], dict):
            counts: Dict[str, int] = {}
            for key, val in payload["question_type_counts"].items():
                if key not in QUESTION_TYPES:
                    continue
                try:
                    number = int(val)
                except (TypeError, ValueError):
                    continue
                if number > 0:
                    counts[key] = number
            if counts:
                self.question_type_counts = counts
        if "total_score" in payload:
            try:
                total = int(payload["total_score"])
            except (TypeError, ValueError):
                total = None
            if total and total > 0:
                self.total_score = total
        if "chapter_ids" in payload and payload["chapter_ids"] is not None:
            chapters: List[int] = []
            for value in payload["chapter_ids"]:
                try:
                    chapters.append(int(value))
                except (TypeError, ValueError):
                    continue
            if chapters:
                self.chapter_ids = chapters

    @staticmethod
    def _normalize_kp(item: Any) -> Optional[str]:
        if item is None:
            return None
        if isinstance(item, dict):
            if item.get("title"):
                return str(item["title"]).strip()
            if item.get("id") is not None:
                return str(item["id"]).strip()
        return str(item).strip()

    @staticmethod
    def _parse_difficulty(value: Any) -> Optional[float]:
        if value is None:
            return None
        mapping = {
            "basic": 2.0,
            "beginner": 2.0,
            "低": 2.0,
            "intermediate": 3.0,
            "medium": 3.0,
            "中": 3.0,
            "中等": 3.0,
            "advanced": 4.0,
            "hard": 4.0,
            "高": 4.0,
        }
        if isinstance(value, str):
            cleaned = value.strip()
            if not cleaned:
                return None
            lower = cleaned.lower()
            if lower in mapping:
                return mapping[lower]
            try:
                value = float(cleaned)
            except ValueError:
                return None
        try:
            number = float(value)
        except (TypeError, ValueError):
            return None
        if number <= 0:
            return None
        return max(1.0, min(5.0, number))

    def is_complete(self) -> bool:
        return bool(self.knowledge_points) and bool(self.question_type_counts) and self.target_difficulty is not None

    def missing_fields(self) -> List[str]:
        missing: List[str] = []
        if not self.knowledge_points:
            missing.append("knowledge_points")
        if self.target_difficulty is None:
            missing.append("target_difficulty")
        if not self.question_type_counts:
            missing.append("question_type_counts")
        if self.total_score is None:
            missing.append("total_score")
        return missing

    def to_spec(self, course_id: int) -> PaperSpec:
        if not self.is_complete():
            raise ValueError("spec is incomplete")
        return PaperSpec(
            course_id=course_id,
            knowledge_points=self.knowledge_points or [],
            target_difficulty=self.target_difficulty or 3.0,
            question_type_counts=self.question_type_counts,
            total_score=self.total_score or 100,
            chapter_ids=self.chapter_ids,
        )

    def to_dict(self) -> Dict[str, Any]:
        return {
            "knowledge_points": self.knowledge_points,
            "target_difficulty": self.target_difficulty,
            "question_type_counts": self.question_type_counts,
            "total_score": self.total_score,
            "chapter_ids": self.chapter_ids,
        }


@dataclass
class ConversationReply:
    message: str
    stage: str
    action: str
    spec: Dict[str, Any]
    missing_fields: List[str]
    course_id: int
    session_id: Optional[str] = None
    result: Optional[Dict[str, Any]] = None
    assignment_id: Optional[int] = None
    publish_request: Optional[Dict[str, Any]] = None
    error: Optional[str] = None


@dataclass
class WorkflowConversation:
    course_id: int
    knowledge_points: List[KnowledgePoint]
    question_summary: Dict[str, int]
    llm_context: str
    messages: List[Dict[str, str]] = field(default_factory=list)
    spec_state: SpecState = field(default_factory=SpecState)
    publish_payload: Dict[str, Any] = field(default_factory=dict)
    result: Optional[AssemblyResult] = None
    session_id: Optional[str] = None
    assignment_id: Optional[int] = None
    stage: str = "collecting"
    last_complete_spec: Optional[PaperSpec] = None

    def result_dict(self) -> Optional[Dict[str, Any]]:
        return self.result.to_dict() if self.result else None

    def snapshot(self) -> Dict[str, Any]:
        return {
            "course_id": self.course_id,
            "spec": self.spec_state.to_dict(),
            "stage": self.stage,
            "session_id": self.session_id,
            "assignment_id": self.assignment_id,
            "publish_payload": self.publish_payload or None,
            "result": self.result_dict(),
            "missing_fields": self.spec_state.missing_fields(),
        }


@dataclass
class LLMStructuredResponse:
    reply: str
    action: str
    slots: Dict[str, Any] = field(default_factory=dict)
    publish_payload: Dict[str, Any] = field(default_factory=dict)


def _load_default_publisher_id() -> Optional[int]:
    raw_value = os.getenv("DEFAULT_PUBLISHER_USER_ID", "").strip()
    if not raw_value:
        # 默认使用演示账号 20001，便于 CLI 快速体验
        raw_value = "20001"
    try:
        return int(raw_value)
    except ValueError:
        return None


class WorkflowAgent:
    def __init__(
        self,
        repository: QuestionBankRepository,
        assembler: PaperAssembler,
        llm: LLMClient,
        publisher: AssignmentPublisher,
        session_recorder: Optional[SessionRecorder] = None,
        default_publisher_user_id: Optional[int] = None,
    ):
        self.repository = repository
        self.assembler = assembler
        self.llm = llm
        self.publisher = publisher
        self.session_recorder = session_recorder
        self.default_publisher_user_id = (
            default_publisher_user_id
            if default_publisher_user_id is not None
            else _load_default_publisher_id()
        )
        self._system_message = {
            "role": "system",
            "content": (
                "你是 SmartCourse 的智能组卷工作流助手，需主动引导教师逐步完成需求收集、组卷和发布。\n"
                "- 所有回复都必须是 JSON，且至少包含 reply/action 字段。\n"
                "- action 仅可取值: ask_info, assemble, publish, done, idle。\n"
                "- ask_info: 继续和教师对话收集信息。\n"
                "- assemble: 说明信息已齐全，提示系统执行组卷。\n"
                "- publish: 教师已经给出发布意图或信息。\n"
                "- done: 已经完成发布或教师主动结束。\n"
                "reply 字段请使用中文自然表达。\n"
                "filled_slots 字段用于抽取教师提供的数据，如 knowledge_points、target_difficulty、question_type_counts、total_score、chapter_ids。\n"
                "当教师提供发布信息时，写入 publish_payload，包含 title、publisher_user_id、assignment_type、start_time、end_time、description。\n"
                "如果信息不足，请在 reply 中明确告知缺口。\n"
                "user 提供的 JSON 中含 intent 字段：greeting、conversation、result、publish_success。\n"
                "- 当 intent=greeting 时，先向教师问好并给出可选信息提示。\n"
                "- intent=result 时，需要总结最新组卷结果并询问是否需要调整或发布。\n"
                "- intent=publish_success 时，祝贺教师发布完成并结束对话。"
            ),
        }

    def create_conversation(self, course_id: int) -> Tuple[WorkflowConversation, ConversationReply]:
        kp_list = self.repository.list_knowledge_points(course_id)
        if not kp_list:
            raise RuntimeError("未找到该课程的知识点，无法启动会话。")
        summary = self.repository.summarize_question_types(course_id)
        context_message = self._build_context_prompt(course_id, kp_list, summary)
        conversation = WorkflowConversation(
            course_id=course_id,
            knowledge_points=kp_list,
            question_summary=summary,
            llm_context=context_message,
        )
        conversation.spec_state.total_score = 100
        greeting = self._build_greeting_text(course_id, kp_list, summary)
        conversation.messages.append({"role": "assistant", "content": greeting})
        reply = ConversationReply(
            message=greeting,
            stage=conversation.stage,
            action="ask_info",
            spec=conversation.spec_state.to_dict(),
            missing_fields=conversation.spec_state.missing_fields(),
            course_id=conversation.course_id,
        )
        return conversation, reply

    def process_turn(self, conversation: WorkflowConversation, user_input: str) -> ConversationReply:
        conversation.messages.append({"role": "user", "content": user_input})
        interpret = self._call_llm(conversation, user_input=user_input, intent="conversation")
        conversation.spec_state.update(interpret.slots)
        conversation.publish_payload.update({k: v for k, v in interpret.publish_payload.items() if v is not None})

        if conversation.spec_state.is_complete():
            try:
                conversation.last_complete_spec = conversation.spec_state.to_spec(conversation.course_id)
            except Exception:
                conversation.last_complete_spec = None

        follow_up: Optional[LLMStructuredResponse] = None
        action = interpret.action
        manual_message = None

        override_action = self._detect_manual_action(user_input, conversation)
        if override_action and override_action != action:
            action = override_action
            manual_message = self._manual_action_message(action)

        final_reply = manual_message or interpret.reply

        if action == "done":
            conversation.stage = "completed"

        if action == "assemble":
            spec_to_use: Optional[PaperSpec] = None
            if conversation.spec_state.is_complete():
                spec_to_use = conversation.spec_state.to_spec(conversation.course_id)
            elif conversation.last_complete_spec:
                spec_to_use = conversation.last_complete_spec

            if spec_to_use:
                try:
                    result = self.assembler.assemble(spec_to_use)
                    conversation.result = result
                    conversation.stage = "assembled"
                    if self.session_recorder:
                        conversation.session_id = self.session_recorder.create_session(spec_to_use, result)
                    conversation.last_complete_spec = spec_to_use
                    follow_up = self._call_llm(conversation, intent="result")
                except Exception as exc:  # pragma: no cover - runtime safety
                    final_reply = f"组卷失败：{exc}"
            else:
                final_reply = self._missing_fields_notice(conversation)
        elif action == "publish":
            publish_request = self._build_publish_request(conversation)
            if conversation.result and publish_request:
                try:
                    assignment_id = self.publisher.publish(
                        conversation.spec_state.to_spec(conversation.course_id),
                        conversation.result,
                        publish_request,
                    )
                    conversation.assignment_id = assignment_id
                    conversation.stage = "published"
                    follow_up = self._call_llm(conversation, intent="publish_success")
                except Exception as exc:  # pragma: no cover - runtime safety
                    final_reply = f"发布失败：{exc}"
            elif not conversation.result:
                final_reply = "需要先完成组卷才能发布。"
            else:
                final_reply = "发布信息还不完整，请继续提供。"

        if follow_up:
            conversation.spec_state.update(follow_up.slots)
            conversation.publish_payload.update(follow_up.publish_payload)
            final_reply = "\n\n".join(part for part in [final_reply, follow_up.reply] if part)

        conversation.messages.append({"role": "assistant", "content": final_reply})
        response_payload = follow_up or LLMStructuredResponse(
            reply=final_reply,
            action=action,
            slots=interpret.slots,
            publish_payload=interpret.publish_payload,
        )
        return self._build_reply(conversation, response_payload)

    def _call_llm(
        self,
        conversation: WorkflowConversation,
        user_input: Optional[str] = None,
        intent: str = "conversation",
    ) -> LLMStructuredResponse:
        state_payload = {
            "intent": intent,
            "course_id": conversation.course_id,
            "spec": conversation.spec_state.to_dict(),
            "missing_fields": conversation.spec_state.missing_fields(),
            "stage": conversation.stage,
            "session_id": conversation.session_id,
            "assignment_id": conversation.assignment_id,
            "publish_payload": conversation.publish_payload or None,
            "result": conversation.result_dict(),
            "user_input": user_input or "",
        }
        payload = json.dumps(state_payload, ensure_ascii=False)
        messages: List[Dict[str, str]] = [self._system_message, {"role": "system", "content": conversation.llm_context}]
        messages.extend(conversation.messages)
        messages.append({"role": "user", "content": payload})
        try:
            raw = self.llm.complete(messages)
        except Exception as exc:  # pragma: no cover - runtime safety
            return LLMStructuredResponse(reply=f"[LLM调用失败] {exc}", action="idle")
        return self._parse_response(raw)

    def _parse_response(self, raw: str) -> LLMStructuredResponse:
        cleaned = self._clean_json_block(raw)
        try:
            data = json.loads(cleaned)
        except json.JSONDecodeError:
            return LLMStructuredResponse(reply=raw.strip(), action="idle")
        reply = str(data.get("reply") or "")
        action = str(data.get("action") or "idle")
        slots = data.get("filled_slots") or {}
        publish_payload = data.get("publish_payload") or {}
        if not isinstance(slots, dict):
            slots = {}
        if not isinstance(publish_payload, dict):
            publish_payload = {}
        return LLMStructuredResponse(reply=reply, action=action, slots=slots, publish_payload=publish_payload)

    @staticmethod
    def _clean_json_block(raw: str) -> str:
        """
        Normalize the LLM raw response so we can decode JSON even if it is wrapped
        inside Markdown fences or includes explanatory text.
        """
        text = raw.strip()
        if text.startswith("```"):
            lines = text.splitlines()
            filtered = [line for line in lines if not line.strip().startswith("```")]
            text = "\n".join(filtered).strip()
        if not text.startswith("{"):
            start = text.find("{")
            end = text.rfind("}")
            if start != -1 and end != -1 and end > start:
                text = text[start : end + 1]
        return text

    def _build_reply(self, conversation: WorkflowConversation, response: LLMStructuredResponse) -> ConversationReply:
        return ConversationReply(
            message=response.reply,
            stage=conversation.stage,
            action=response.action,
            spec=conversation.spec_state.to_dict(),
            missing_fields=conversation.spec_state.missing_fields(),
            course_id=conversation.course_id,
            session_id=conversation.session_id,
            result=conversation.result_dict(),
            assignment_id=conversation.assignment_id,
            publish_request=conversation.publish_payload or None,
        )

    def process_turn_streaming(
        self, conversation: WorkflowConversation, user_input: str
    ) -> Generator[StreamEvent, None, ConversationReply]:
        """
        流式处理用户输入，通过 Generator 产出中途事件。
        最终返回 ConversationReply。
        """
        conversation.messages.append({"role": "user", "content": user_input})
        
        # 发送思考中事件
        yield StreamEvent(event_type="thinking", message="正在分析需求...")
        
        # 流式调用 LLM
        gen, chunks = self._call_llm_streaming(conversation, user_input=user_input, intent="conversation")
        for event in gen:
            yield event
        interpret = self._get_llm_response_from_chunks(chunks)
        
        conversation.spec_state.update(interpret.slots)
        conversation.publish_payload.update({k: v for k, v in interpret.publish_payload.items() if v is not None})

        if conversation.spec_state.is_complete():
            try:
                conversation.last_complete_spec = conversation.spec_state.to_spec(conversation.course_id)
            except Exception:
                conversation.last_complete_spec = None

        follow_up: Optional[LLMStructuredResponse] = None
        action = interpret.action
        manual_message = None

        override_action = self._detect_manual_action(user_input, conversation)
        if override_action and override_action != action:
            action = override_action
            manual_message = self._manual_action_message(action)

        final_reply = manual_message or interpret.reply

        if action == "done":
            conversation.stage = "completed"

        if action == "assemble":
            yield StreamEvent(event_type="action", data={"action": "assemble"}, message="开始组卷...")
            spec_to_use: Optional[PaperSpec] = None
            if conversation.spec_state.is_complete():
                spec_to_use = conversation.spec_state.to_spec(conversation.course_id)
            elif conversation.last_complete_spec:
                spec_to_use = conversation.last_complete_spec

            if spec_to_use:
                try:
                    result = self.assembler.assemble(spec_to_use)
                    conversation.result = result
                    conversation.stage = "assembled"
                    if self.session_recorder:
                        conversation.session_id = self.session_recorder.create_session(spec_to_use, result)
                    conversation.last_complete_spec = spec_to_use
                    
                    # 发送组卷结果事件
                    yield StreamEvent(
                        event_type="result", 
                        data=conversation.result_dict(),
                        message="组卷完成！"
                    )
                    
                    # 流式获取后续回复
                    yield StreamEvent(event_type="thinking", message="正在总结组卷结果...")
                    gen2, chunks2 = self._call_llm_streaming(conversation, intent="result")
                    for event in gen2:
                        yield event
                    follow_up = self._get_llm_response_from_chunks(chunks2)
                except Exception as exc:
                    final_reply = f"组卷失败：{exc}"
                    yield StreamEvent(event_type="error", message=final_reply)
            else:
                final_reply = self._missing_fields_notice(conversation)
                yield StreamEvent(event_type="chunk", data=final_reply, message=final_reply)
                
        elif action == "publish":
            yield StreamEvent(event_type="action", data={"action": "publish"}, message="开始发布...")
            publish_request = self._build_publish_request(conversation)
            if conversation.result and publish_request:
                try:
                    assignment_id = self.publisher.publish(
                        conversation.spec_state.to_spec(conversation.course_id),
                        conversation.result,
                        publish_request,
                    )
                    conversation.assignment_id = assignment_id
                    conversation.stage = "published"
                    
                    yield StreamEvent(
                        event_type="published",
                        data={"assignment_id": assignment_id},
                        message=f"发布成功！作业 ID: {assignment_id}"
                    )
                    
                    yield StreamEvent(event_type="thinking", message="正在生成发布总结...")
                    gen3, chunks3 = self._call_llm_streaming(conversation, intent="publish_success")
                    for event in gen3:
                        yield event
                    follow_up = self._get_llm_response_from_chunks(chunks3)
                except Exception as exc:
                    final_reply = f"发布失败：{exc}"
                    yield StreamEvent(event_type="error", message=final_reply)
            elif not conversation.result:
                final_reply = "需要先完成组卷才能发布。"
                yield StreamEvent(event_type="chunk", data=final_reply, message=final_reply)
            else:
                final_reply = "发布信息还不完整，请继续提供。"
                yield StreamEvent(event_type="chunk", data=final_reply, message=final_reply)

        if follow_up:
            conversation.spec_state.update(follow_up.slots)
            conversation.publish_payload.update(follow_up.publish_payload)
            final_reply = "\n\n".join(part for part in [final_reply, follow_up.reply] if part)

        conversation.messages.append({"role": "assistant", "content": final_reply})
        response_payload = follow_up or LLMStructuredResponse(
            reply=final_reply,
            action=action,
            slots=interpret.slots,
            publish_payload=interpret.publish_payload,
        )
        
        # 发送完成事件
        reply = self._build_reply(conversation, response_payload)
        yield StreamEvent(event_type="done", data=reply.__dict__, message="处理完成")
        return reply

    def _call_llm_streaming(
        self,
        conversation: WorkflowConversation,
        user_input: Optional[str] = None,
        intent: str = "conversation",
    ) -> Tuple[Generator[StreamEvent, None, None], List[str]]:
        """
        流式调用 LLM，返回 (事件生成器, 响应收集列表)。
        调用者需要迭代生成器，然后从响应列表中取结果。
        """
        state_payload = {
            "intent": intent,
            "course_id": conversation.course_id,
            "spec": conversation.spec_state.to_dict(),
            "missing_fields": conversation.spec_state.missing_fields(),
            "stage": conversation.stage,
            "session_id": conversation.session_id,
            "assignment_id": conversation.assignment_id,
            "publish_payload": conversation.publish_payload or None,
            "result": conversation.result_dict(),
            "user_input": user_input or "",
        }
        payload = json.dumps(state_payload, ensure_ascii=False)
        messages: List[Dict[str, str]] = [self._system_message, {"role": "system", "content": conversation.llm_context}]
        messages.extend(conversation.messages)
        messages.append({"role": "user", "content": payload})
        
        # 用列表来收集响应片段
        response_chunks: List[str] = []
        
        def generator():
            try:
                for chunk in self.llm.stream_response(messages):
                    response_chunks.append(chunk)
                    yield StreamEvent(event_type="chunk", data=chunk, message=chunk)
            except Exception as exc:
                yield StreamEvent(event_type="error", message=f"[LLM调用失败] {exc}")
                response_chunks.append(f"[LLM调用失败] {exc}")
        
        return generator(), response_chunks
    
    def _get_llm_response_from_chunks(self, chunks: List[str]) -> LLMStructuredResponse:
        """从收集的响应片段构建 LLMStructuredResponse"""
        full_response = "".join(chunks)
        if full_response.startswith("[LLM调用失败]"):
            return LLMStructuredResponse(reply=full_response, action="idle")
        return self._parse_response(full_response)

    def _build_context_prompt(
        self, course_id: int, kp_list: List[KnowledgePoint], summary: Dict[str, int]
    ) -> str:
        kp_preview = [
            {
                "id": kp.id,
                "title": kp.title,
                "level": kp.level,
            }
            for kp in kp_list[:25]
        ]
        return (
            f"课程ID: {course_id}\n"
            f"可选知识点示例: {json.dumps(kp_preview, ensure_ascii=False)}\n"
            f"题型库存: {json.dumps(summary, ensure_ascii=False)}\n"
            "需要你打招呼并提示教师提供知识点、题型数量、目标难度、总分等信息。"
        )

    def _build_greeting_text(
        self, course_id: int, kp_list: List[KnowledgePoint], summary: Dict[str, int]
    ) -> str:
        kp_lines = "\n".join(
            f"- {kp.id}: {kp.title}" for kp in kp_list[:10]
        ) or "- 暂无可用知识点"
        type_lines = "\n".join(
            f"- {q_type}: {summary.get(q_type, 0)} 道可用" for q_type in QUESTION_TYPES
        )
        return (
            f"您好！我是SmartCourse的智能组卷助手。我将协助您为课程ID {course_id}创建试卷。"
            "目前我需要以下信息来开始：\n"
            "1) 要涵盖的知识点，可选的知识点包括：\n"
            f"{{{kp_lines}}};\n"
            "2) 目标难度：在1-5五个整数中选择，数字越大难度越高；\n"
            "3) 各题型题目数量，题型概览：\n"
            f"{{{type_lines}}};\n"
            "4) 总分（默认100分）。\n"
            "您可以从这些方面开始提供信息吗？"
        )

    def _missing_fields_notice(self, conversation: WorkflowConversation) -> str:
        missing = conversation.spec_state.missing_fields()
        if not missing:
            return "信息已齐全。"
        return ("仍缺少以下信息，请补充：" + ", ".join(missing))

    def _build_publish_request(self, conversation: WorkflowConversation) -> Optional[PublishRequest]:
        payload = conversation.publish_payload
        if not payload:
            if self.default_publisher_user_id is None:
                return None
            required = self.default_publisher_user_id
        else:
            required = payload.get("publisher_user_id")
            if required is None and self.default_publisher_user_id is not None:
                required = self.default_publisher_user_id
        if required is None:
            return None
        try:
            publisher_user_id = int(required)
        except (TypeError, ValueError):
            return None

        title = payload.get("title") or "AI 智能组卷"
        assignment_type = payload.get("assignment_type") or "exam"
        start_time = self._parse_datetime(payload.get("start_time"))
        end_time = self._parse_datetime(payload.get("end_time"))
        description = payload.get("description")
        mode = payload.get("mode") or "question"
        return PublishRequest(
            title=title,
            publisher_user_id=publisher_user_id,
            assignment_type=assignment_type,
            start_time=start_time,
            end_time=end_time,
            description=description,
            mode=mode,
        )

    @staticmethod
    def _parse_datetime(raw_value: Optional[str]) -> Optional[datetime]:
        if not raw_value:
            return None
        try:
            return datetime.fromisoformat(str(raw_value))
        except ValueError:
            return None

    def _detect_manual_action(self, user_input: str, conversation: WorkflowConversation) -> Optional[str]:
        normalized = user_input.lower()
        assemble_keywords = ["开始组卷", "生成试卷", "开始", "生成", "组卷", "start", "go", "ready"]
        if any(keyword in normalized for keyword in assemble_keywords):
            return "assemble"
        if conversation.result:
            if any(keyword in normalized for keyword in ["发布", "提交", "上架", "publish", "release"]):
                return "publish"
        if any(keyword in normalized for keyword in ["结束", "退出", "不用", "算了"]):
            return "done"
        return None

    @staticmethod
    def _manual_action_message(action: str) -> str:
        mapping = {
            "assemble": "好的，我将根据当前需求开始组卷，请稍候...",
            "publish": "收到，现在根据当前结果发起发布流程。",
            "done": "好的，已结束本次智能组卷对话，如需继续可随时再来。",
        }
        return mapping.get(action, "")


class ConversationManager:
    def __init__(self, agent: WorkflowAgent):
        self.agent = agent
        self._conversations: Dict[str, WorkflowConversation] = {}
        self._aborted: Dict[str, bool] = {}  # 跟踪中断状态
        self._lock = threading.Lock()

    def create(self, course_id: int) -> Tuple[str, ConversationReply]:
        conversation, reply = self.agent.create_conversation(course_id)
        conversation_id = uuid.uuid4().hex
        with self._lock:
            self._conversations[conversation_id] = conversation
            self._aborted[conversation_id] = False
        return conversation_id, reply

    def get(self, conversation_id: str) -> WorkflowConversation:
        with self._lock:
            conversation = self._conversations.get(conversation_id)
            if not conversation:
                raise KeyError(conversation_id)
            return conversation

    def is_aborted(self, conversation_id: str) -> bool:
        with self._lock:
            return self._aborted.get(conversation_id, False)

    def abort(self, conversation_id: str) -> bool:
        """中断对话，返回是否成功"""
        with self._lock:
            if conversation_id not in self._conversations:
                raise KeyError(conversation_id)
            self._aborted[conversation_id] = True
            return True

    def reset_abort(self, conversation_id: str):
        """重置中断状态"""
        with self._lock:
            if conversation_id in self._aborted:
                self._aborted[conversation_id] = False

    def handle_message(self, conversation_id: str, message: str) -> ConversationReply:
        # 检查是否已中断
        if self.is_aborted(conversation_id):
            self.reset_abort(conversation_id)
            raise InterruptedError("对话已被中断")
        
        with self._lock:
            conversation = self._conversations.get(conversation_id)
            if not conversation:
                raise KeyError(conversation_id)
        
        # 在处理过程中可以检查中断状态
        reply = self.agent.process_turn(conversation, message)
        
        with self._lock:
            self._conversations[conversation_id] = conversation
        return reply

    def handle_message_streaming(
        self, conversation_id: str, message: str
    ) -> Generator[StreamEvent, None, ConversationReply]:
        """流式处理消息，产出中途事件"""
        # 检查是否已中断
        if self.is_aborted(conversation_id):
            self.reset_abort(conversation_id)
            raise InterruptedError("对话已被中断")
        
        with self._lock:
            conversation = self._conversations.get(conversation_id)
            if not conversation:
                raise KeyError(conversation_id)
        
        # 流式处理
        gen = self.agent.process_turn_streaming(conversation, message)
        reply = None
        try:
            while True:
                # 检查中断状态
                if self.is_aborted(conversation_id):
                    yield StreamEvent(event_type="aborted", message="对话已被中断")
                    break
                event = next(gen)
                yield event
        except StopIteration as e:
            reply = e.value
        
        with self._lock:
            self._conversations[conversation_id] = conversation
        
        return reply

    def snapshot(self, conversation_id: str) -> ConversationReply:
        conversation = self.get(conversation_id)
        last_text = conversation.messages[-1]["content"] if conversation.messages else ""
        response = LLMStructuredResponse(reply=last_text, action="idle")
        return self.agent._build_reply(conversation, response)
