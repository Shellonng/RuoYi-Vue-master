import os
from dataclasses import dataclass
from typing import Optional, Tuple


@dataclass
class DBConfig:
    host: str
    port: int
    user: str
    password: str
    database: str
    charset: str = "utf8mb4"


@dataclass
class LLMConfig:
    api_key: Optional[str]
    base_url: str = "https://api.siliconflow.cn"
    model: str = "deepseek-ai/DeepSeek-V3"
    temperature: float = 0.5

    @property
    def enabled(self) -> bool:
        return bool(self.api_key)


def load_db_config() -> DBConfig:
    """Load database configuration from environment variables."""
    host = os.getenv("DB_HOST", "8.131.109.137")
    port = int(os.getenv("DB_PORT", "3306"))
    user = os.getenv("DB_USER", "admin")
    password = os.getenv("DB_PASSWORD", "Neu123456.")
    database = os.getenv("DB_NAME", "education_platform_v1")
    return DBConfig(
        host=host,
        port=port,
        user=user,
        password=password,
        database=database,
    )


def load_llm_config() -> LLMConfig:
    """Load LLM configuration from environment variables."""
    api_key = "sk-qgkybeepfrqegxsfvczpvzgrzinmuphuwtrfnratalkskitk"  # os.getenv("OPENAI_API_KEY")
    base_url = os.getenv("OPENAI_BASE_URL", "https://api.siliconflow.cn")
    model = os.getenv("OPENAI_MODEL", "deepseek-ai/DeepSeek-V3")
    temperature = float(os.getenv("OPENAI_TEMPERATURE", "0.5"))
    return LLMConfig(api_key=api_key, base_url=base_url, model=model, temperature=temperature)


def load_configs() -> Tuple[DBConfig, LLMConfig]:
    """Convenience helper to load both DB and LLM configuration."""
    return load_db_config(), load_llm_config()
