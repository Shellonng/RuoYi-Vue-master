import argparse

from assembly import PaperAssembler
from config import load_configs
from db import QuestionBankRepository
from llm import LocalEchoClient, OpenAIStreamClient
from publisher import AssignmentPublisher
from session import CLIWorkflowSession
from workflow_agent import WorkflowAgent


def parse_args():
    parser = argparse.ArgumentParser(description="SmartCourse AI intelligent paper assembly agent.")
    parser.add_argument("--course-id", type=int, help="Default course ID for the session.")
    return parser.parse_args()


def main():
    args = parse_args()
    db_config, llm_config = load_configs()
    repository = QuestionBankRepository(db_config)
    assembler = PaperAssembler(repository)
    publisher = AssignmentPublisher(db_config)

    if llm_config.enabled:
        llm = OpenAIStreamClient(llm_config)
    else:
        llm = LocalEchoClient()

    agent = WorkflowAgent(repository, assembler, llm, publisher)
    session = CLIWorkflowSession(agent=agent, default_course_id=args.course_id)
    try:
        session.run()
    except RuntimeError as exc:
        print(f"[智能agent错误] {exc}")


if __name__ == "__main__":
    main()
