from __future__ import annotations

import asyncio
import threading
import uuid
from dataclasses import dataclass
from datetime import datetime, timedelta
from typing import AsyncGenerator, Dict, List, Optional

from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect
from fastapi.concurrency import run_in_threadpool
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field, ValidationError

from assembly import AssemblyResult, PaperAssembler, PaperSpec
from config import load_configs
from db import QUESTION_TYPES, QuestionBankRepository
from llm import LocalEchoClient, LLMClient, OpenAIStreamClient
from publisher import AssignmentPublisher, PublishRequest
from workflow_agent import ConversationManager, ConversationReply, WorkflowAgent, StreamEvent


@dataclass
class SessionData:
    spec: PaperSpec
    result: AssemblyResult
    created_at: datetime


class SessionStore:
    """In-memory session store so the frontend can publish the last assembly result."""

    def __init__(self, ttl_minutes: int = 30):
        self.ttl = timedelta(minutes=ttl_minutes)
        self._sessions: Dict[str, SessionData] = {}
        self._lock = threading.Lock()

    def create_session(self, spec: PaperSpec, result: AssemblyResult) -> str:
        session_id = uuid.uuid4().hex
        with self._lock:
            self._cleanup_locked()
            self._sessions[session_id] = SessionData(spec=spec, result=result, created_at=datetime.utcnow())
        return session_id

    def get_session(self, session_id: str) -> SessionData:
        with self._lock:
            self._cleanup_locked()
            data = self._sessions.get(session_id)
            if not data:
                raise KeyError(session_id)
            if datetime.utcnow() - data.created_at > self.ttl:
                del self._sessions[session_id]
                raise KeyError(session_id)
            return data

    def _cleanup_locked(self):
        expired = [
            key
            for key, data in self._sessions.items()
            if datetime.utcnow() - data.created_at > self.ttl
        ]
        for key in expired:
            del self._sessions[key]


class AssembleRequest(BaseModel):
    course_id: int
    knowledge_points: List[str]
    target_difficulty: float = Field(ge=1.0, le=5.0)
    question_type_counts: Dict[str, int]
    total_score: int = Field(default=100, ge=1)
    chapter_ids: Optional[List[int]] = None


class AssembleResponse(BaseModel):
    session_id: str
    narrative: str
    result: Dict
    complete: bool


class PublishPayload(BaseModel):
    title: str
    publisher_user_id: int
    assignment_type: str = Field(default="exam", pattern="^(homework|exam)$")
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None
    description: Optional[str] = None
    mode: str = "question"
    time_limit: Optional[int] = None  # 时间限制（分钟），仅考试类型有效

    def to_dataclass(self) -> PublishRequest:
        return PublishRequest(
            title=self.title,
            publisher_user_id=self.publisher_user_id,
            assignment_type=self.assignment_type,
            start_time=self.start_time,
            end_time=self.end_time,
            description=self.description,
            mode=self.mode,
            time_limit=self.time_limit,
        )


class ConversationStartPayload(BaseModel):
    course_id: int


class ConversationMessagePayload(BaseModel):
    message: str = Field(min_length=1)


db_config, llm_config = load_configs()
repository = QuestionBankRepository(db_config)
assembler = PaperAssembler(repository)
publisher = AssignmentPublisher(db_config)
llm: LLMClient = OpenAIStreamClient(llm_config) if llm_config.enabled else LocalEchoClient()
session_store = SessionStore()
workflow_agent = WorkflowAgent(repository, assembler, llm, publisher, session_store)
conversation_manager = ConversationManager(workflow_agent)

app = FastAPI(title="SmartCourse AI Agent API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health_check():
    return {"status": "ok", "llm_enabled": llm_config.enabled}


@app.get("/courses/{course_id}/knowledge-points")
def list_knowledge_points(course_id: int):
    kps = repository.list_knowledge_points(course_id)
    return [kp.__dict__ for kp in kps]


@app.get("/courses/{course_id}/question-types")
def summarize_question_types(course_id: int):
    return repository.summarize_question_types(course_id)


@app.post("/assemble", response_model=AssembleResponse)
def assemble_paper(payload: AssembleRequest):
    _validate_question_types(payload.question_type_counts)
    if not payload.knowledge_points:
        raise HTTPException(status_code=400, detail="knowledge_points 不能为空")

    spec = PaperSpec(
        course_id=payload.course_id,
        knowledge_points=payload.knowledge_points,
        target_difficulty=payload.target_difficulty,
        question_type_counts=payload.question_type_counts,
        total_score=payload.total_score,
        chapter_ids=payload.chapter_ids,
    )
    result = assembler.assemble(spec)
    narrative = _generate_narrative(spec, result)
    session_id = session_store.create_session(spec, result)
    return AssembleResponse(
        session_id=session_id,
        narrative=narrative,
        result=result.to_dict(),
        complete=result.is_complete(),
    )


@app.get("/sessions/{session_id}")
def get_session_result(session_id: str):
    try:
        data = session_store.get_session(session_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="session 不存在或已过期")
    return {
        "session_id": session_id,
        "created_at": data.created_at.isoformat(),
        "spec": {
            "course_id": data.spec.course_id,
            "knowledge_points": data.spec.knowledge_points,
            "target_difficulty": data.spec.target_difficulty,
            "question_type_counts": data.spec.question_type_counts,
            "total_score": data.spec.total_score,
            "chapter_ids": data.spec.chapter_ids,
        },
        "result": data.result.to_dict(),
        "complete": data.result.is_complete(),
    }


@app.post("/sessions/{session_id}/publish")
def publish_assignment(session_id: str, payload: PublishPayload):
    try:
        data = session_store.get_session(session_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="session 不存在或已过期")
    assignment_id = publisher.publish(data.spec, data.result, payload.to_dataclass())
    return {"assignment_id": assignment_id}


@app.post("/workflow/conversations")
def start_conversation(payload: ConversationStartPayload):
    try:
        conversation_id, reply = conversation_manager.create(payload.course_id)
    except RuntimeError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    return _conversation_to_dict(conversation_id, reply)


@app.post("/workflow/conversations/{conversation_id}/messages")
def continue_conversation(conversation_id: str, payload: ConversationMessagePayload):
    try:
        reply = conversation_manager.handle_message(conversation_id, payload.message)
    except KeyError:
        raise HTTPException(status_code=404, detail="conversation 不存在")
    except InterruptedError as exc:
        raise HTTPException(status_code=409, detail=str(exc))
    return _conversation_to_dict(conversation_id, reply)


@app.get("/workflow/conversations/{conversation_id}")
def fetch_conversation(conversation_id: str):
    try:
        reply = conversation_manager.snapshot(conversation_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="conversation 不存在")
    return _conversation_to_dict(conversation_id, reply)


@app.post("/workflow/conversations/{conversation_id}/abort")
def abort_conversation(conversation_id: str):
    """中断对话"""
    try:
        conversation_manager.abort(conversation_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="conversation 不存在")
    return {"status": "aborted", "conversation_id": conversation_id}


@app.websocket("/ws/workflow/conversations/{conversation_id}/messages")
async def workflow_conversation_websocket(websocket: WebSocket, conversation_id: str):
    """
    WebSocket 端点用于流式对话。
    客户端发送 JSON: {"message": "用户输入"}
    服务端流式返回事件:
    - {"event": "thinking", "message": "..."} - AI 正在思考
    - {"event": "chunk", "data": "...", "message": "..."} - LLM 输出片段
    - {"event": "action", "data": {...}, "message": "..."} - 执行动作（如组卷、发布）
    - {"event": "result", "data": {...}, "message": "..."} - 组卷结果
    - {"event": "published", "data": {...}, "message": "..."} - 发布成功
    - {"event": "done", "data": {...}, "message": "..."} - 处理完成，包含完整回复
    - {"event": "error", "message": "..."} - 错误
    """
    await websocket.accept()
    
    try:
        # 验证 conversation_id 存在
        try:
            conversation_manager.get(conversation_id)
        except KeyError:
            await websocket.send_json({"event": "error", "message": "conversation 不存在"})
            await websocket.close()
            return
        
        await websocket.send_json({"event": "ready", "conversation_id": conversation_id})
        
        while True:
            # 等待客户端消息
            try:
                request_data = await websocket.receive_json()
            except WebSocketDisconnect:
                break
            
            message = request_data.get("message", "").strip()
            if not message:
                await websocket.send_json({"event": "error", "message": "message 不能为空"})
                continue
            
            try:
                # 使用异步队列来处理流式输出
                event_queue: asyncio.Queue = asyncio.Queue()
                loop = asyncio.get_running_loop()
                
                def run_generator():
                    """在线程中运行同步的流式处理"""
                    try:
                        gen = conversation_manager.handle_message_streaming(conversation_id, message)
                        for event in gen:
                            asyncio.run_coroutine_threadsafe(event_queue.put(event), loop)
                    except Exception as exc:
                        asyncio.run_coroutine_threadsafe(event_queue.put(exc), loop)
                    finally:
                        asyncio.run_coroutine_threadsafe(event_queue.put(None), loop)
                
                # 在线程池中启动生成器
                threading.Thread(target=run_generator, daemon=True).start()
                
                # 从队列中读取事件并发送给客户端
                while True:
                    event = await event_queue.get()
                    
                    if event is None:
                        # 处理完成
                        break
                    
                    if isinstance(event, Exception):
                        await websocket.send_json({"event": "error", "message": str(event)})
                        break
                    
                    event_data = {
                        "event": event.event_type,
                        "data": event.data,
                        "message": event.message,
                    }
                    await websocket.send_json(event_data)
                    
                    # 如果是中断事件，退出循环
                    if event.event_type == "aborted":
                        break
                        
            except KeyError:
                await websocket.send_json({"event": "error", "message": "conversation 不存在"})
                break
            except InterruptedError as exc:
                await websocket.send_json({"event": "aborted", "message": str(exc)})
                break
            except Exception as exc:
                await websocket.send_json({"event": "error", "message": str(exc)})
                
    except WebSocketDisconnect:
        pass
    except Exception as exc:
        try:
            await websocket.send_json({"event": "error", "message": str(exc)})
        except:
            pass
    finally:
        try:
            await websocket.close()
        except:
            pass


@app.websocket("/ws/assemble")
async def assemble_websocket(websocket: WebSocket):
    await websocket.accept()
    await websocket.send_json({"event": "ready"})
    try:
        request_data = await websocket.receive_json()
        try:
            payload = AssembleRequest(**request_data)
        except ValidationError as exc:
            await websocket.send_json({"event": "error", "detail": exc.errors()})
            await websocket.close()
            return

        _validate_question_types(payload.question_type_counts)
        if not payload.knowledge_points:
            await websocket.send_json({"event": "error", "detail": "knowledge_points 不能为空"})
            await websocket.close()
            return

        spec = PaperSpec(
            course_id=payload.course_id,
            knowledge_points=payload.knowledge_points,
            target_difficulty=payload.target_difficulty,
            question_type_counts=payload.question_type_counts,
            total_score=payload.total_score,
            chapter_ids=payload.chapter_ids,
        )

        result = await run_in_threadpool(assembler.assemble, spec)
        session_id = session_store.create_session(spec, result)
        await websocket.send_json(
            {
                "event": "result",
                "session_id": session_id,
                "result": result.to_dict(),
                "complete": result.is_complete(),
            }
        )

        async for chunk in _stream_narrative(spec, result):
            await websocket.send_json({"event": "narrative_chunk", "data": chunk})
        await websocket.send_json({"event": "narrative_done"})
        await websocket.close()
    except WebSocketDisconnect:
        return
    except Exception as exc:
        await websocket.send_json({"event": "error", "detail": str(exc)})
        await websocket.close()


def _generate_narrative(spec: PaperSpec, result: AssemblyResult) -> str:
    messages = _build_narrative_messages(spec, result)
    try:
        return "".join(llm.stream_response(messages))
    except Exception as exc:
        return f"[LLM调用失败] {exc}"


async def _stream_narrative(spec: PaperSpec, result: AssemblyResult) -> AsyncGenerator[str, None]:
    loop = asyncio.get_running_loop()
    queue: asyncio.Queue = asyncio.Queue()
    messages = _build_narrative_messages(spec, result)

    def producer():
        try:
            for chunk in llm.stream_response(messages):
                asyncio.run_coroutine_threadsafe(queue.put(chunk), loop)
        except Exception as exc:
            asyncio.run_coroutine_threadsafe(queue.put(exc), loop)
        finally:
            asyncio.run_coroutine_threadsafe(queue.put(None), loop)

    threading.Thread(target=producer, daemon=True).start()

    while True:
        item = await queue.get()
        if item is None:
            break
        if isinstance(item, Exception):
            raise item
        yield item


def _build_narrative_messages(spec: PaperSpec, result: AssemblyResult) -> List[dict]:
    payload = {
        "spec": {
            "course_id": spec.course_id,
            "knowledge_points": spec.knowledge_points,
            "target_difficulty": spec.target_difficulty,
            "question_type_counts": spec.question_type_counts,
            "total_score": spec.total_score,
        },
        "result": result.to_dict(),
    }
    return [
        {
            "role": "system",
            "content": (
                "你是一名智慧课堂助手，需要用简洁中文说明组卷结果，概括题型比例、知识点覆盖"
                "与整体难度，并为教师给出建议。"
            ),
        },
        {"role": "user", "content": f"分析以下JSON：{payload}"},
    ]


def _validate_question_types(question_type_counts: Dict[str, int]):
    invalid = [key for key in question_type_counts if key not in QUESTION_TYPES]
    if invalid:
        raise HTTPException(status_code=400, detail=f"不支持的题型: {', '.join(invalid)}")
    if not question_type_counts:
        raise HTTPException(status_code=400, detail="question_type_counts 不能为空")
    if any(count <= 0 for count in question_type_counts.values()):
        raise HTTPException(status_code=400, detail="题型数量必须大于0")


def _conversation_to_dict(conversation_id: str, reply: ConversationReply) -> Dict:
    return {
        "conversation_id": conversation_id,
        "message": reply.message,
        "stage": reply.stage,
        "action": reply.action,
        "course_id": reply.course_id,
        "spec": reply.spec,
        "missing_fields": reply.missing_fields,
        "session_id": reply.session_id,
        "result": reply.result,
        "assignment_id": reply.assignment_id,
        "publish_request": reply.publish_request,
    }
