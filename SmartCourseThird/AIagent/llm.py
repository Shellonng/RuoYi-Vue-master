from __future__ import annotations

import json
from typing import Iterable, List, Optional

import requests

from config import LLMConfig


class LLMClient:
    """Base class for LLM clients."""

    def stream_response(self, messages: List[dict], **kwargs) -> Iterable[str]:
        raise NotImplementedError

    def complete(self, messages: List[dict], **kwargs) -> str:
        return "".join(self.stream_response(messages, **kwargs))


class OpenAIStreamClient(LLMClient):
    """Thin wrapper around the OpenAI-compatible Chat Completions API."""

    def __init__(self, config: LLMConfig):
        if not config.api_key:
            raise ValueError("OPENAI_API_KEY is required to enable the AI agent.")
        self.config = config

    def stream_response(self, messages: List[dict], **kwargs) -> Iterable[str]:
        payload = {
            "model": self.config.model,
            "messages": messages,
            "temperature": kwargs.get("temperature", self.config.temperature),
            "stream": True,
        }
        payload.update({key: val for key, val in kwargs.items() if key not in {"temperature"}})

        response = requests.post(
            f"{self.config.base_url.rstrip('/')}/v1/chat/completions",
            headers={
                "Authorization": f"Bearer {self.config.api_key}",
                "Content-Type": "application/json",
            },
            stream=True,
            json=payload,
            timeout=60,
        )
        response.raise_for_status()

        for line in response.iter_lines():
            if not line:
                continue
            if line.startswith(b"data: "):
                data = line[len(b"data: ") :].strip()
                if data == b"[DONE]":
                    break
                chunk = json.loads(data.decode("utf-8"))
                delta = chunk["choices"][0]["delta"].get("content")
                if delta:
                    yield delta


class LocalEchoClient(LLMClient):
    """Fallback LLM that simply echoes instructions when API keys are absent."""

    def stream_response(self, messages: List[dict], **kwargs) -> Iterable[str]:
        content = messages[-1]["content"] if messages else ""
        yield f"[LLM disabled] {content}"
