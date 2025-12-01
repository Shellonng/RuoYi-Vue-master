from __future__ import annotations

from typing import Optional

from workflow_agent import ConversationReply, WorkflowAgent


class CLIWorkflowSession:
    """Simple CLI wrapper around the workflow agent."""

    def __init__(self, agent: WorkflowAgent, default_course_id: Optional[int] = None):
        self.agent = agent
        self.default_course_id = default_course_id

    def run(self):
        print("=== 智能组卷工作流 ===")
        course_id = self._prompt_int("请输入课程ID", self.default_course_id)
        try:
            conversation, reply = self.agent.create_conversation(course_id)
        except RuntimeError as exc:
            print(f"无法启动会话：{exc}")
            return

        self._display_reply(reply)
        while True:
            user_input = input("你> ").strip()
            if not user_input:
                continue
            if user_input.lower() in {"exit", "quit"}:
                print("已退出对话。")
                break
            reply = self.agent.process_turn(conversation, user_input)
            self._display_reply(reply)
            if reply.stage in {"completed", "published"}:
                print("对话已完成，可输入 exit 退出。")

    def _display_reply(self, reply: ConversationReply):
        print(f"助手> {reply.message}")
        if reply.result:
            avg = reply.result.get("average_difficulty")
            try:
                avg_text = f"{float(avg):.2f}" if avg is not None else "未知"
            except (TypeError, ValueError):
                avg_text = "未知"
            sid = reply.session_id or "--"
            print(f"(最新组卷平均难度 {avg_text}，session_id={sid})")
        if reply.assignment_id:
            print(f"(发布成功 assignment_id={reply.assignment_id})")

    @staticmethod
    def _prompt_int(label: str, default: Optional[int] = None) -> int:
        while True:
            raw = input(f"{label}{' [' + str(default) + ']' if default is not None else ''}：").strip()
            if not raw and default is not None:
                return default
            try:
                return int(raw)
            except ValueError:
                print("请输入整数。")
