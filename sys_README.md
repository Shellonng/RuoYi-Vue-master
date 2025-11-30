## 智慧课程--子任务三

- 一个前端两个后端服务

## sprintboot后端（port：8083）
在根目录下找到src/main/java/com/exampl/smartcourse/SmartCourseApplication.java，运行springboot服务

## AIagent 服务后端 (端口：8000)
首先配置config
```bash
cd AIagent
```
找到config.py打开
```python
def load_db_config() -> DBConfig:
    """Load database configuration from environment variables."""
    host = os.getenv("DB_HOST", "8.131.109.137")
    port = int(os.getenv("DB_PORT", "3306"))
    user = os.getenv("DB_USER", "admin")
    password = os.getenv("DB_PASSWORD", "Neu123456.")
    database = os.getenv("DB_NAME", "education_platform")
    return DBConfig(
        host=host,
        port=port,
        user=user,
        password=password,
        database=database,
    )


def load_llm_config() -> LLMConfig:
    """Load LLM configuration from environment variables."""
    api_key = ""  # os.getenv("OPENAI_API_KEY")
    base_url = os.getenv("OPENAI_BASE_URL", "https://api.siliconflow.cn")
    model = os.getenv("OPENAI_MODEL", "deepseek-ai/DeepSeek-V3")
    temperature = float(os.getenv("OPENAI_TEMPERATURE", "0.5"))
    return LLMConfig(api_key=api_key, base_url=base_url, model=model, temperature=temperature)
```

主要是这两个配置，我们选择硅基流动，在api_key输入自己的key，可调用对应的LLM模型作智能体服务。（仅智能组卷使用智能体服务实现）

配置python环境（测试python环境3.10，3.12通过）
```bash
cd AIagent
pip install -r requirements.txt

# run
uvicorn server:app --host 0.0.0.0 --port 8000 --reload
```

## 前端(在node 22 测试通过。port:5173)：
```bash
cd frontend
npm install
npn run dev
```


启动后可输入账户密码进入主页
teacher_zhang
pwd