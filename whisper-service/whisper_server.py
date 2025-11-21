"""
Whisper 语音识别 API 服务
支持中文语音转文字
"""
from fastapi import FastAPI, File, UploadFile, Form, HTTPException
from fastapi.responses import PlainTextResponse
from faster_whisper import WhisperModel
import tempfile
import os
import logging
from pathlib import Path

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Whisper API Service",
    description="语音识别服务 - 支持中文",
    version="1.0.0"
)

# 全局模型实例
model = None
MODEL_SIZE = "base"  # tiny, base, small, medium, large-v3

def load_model():
    """加载 Whisper 模型（首次会自动下载）"""
    global model
    try:
        logger.info(f"正在加载 Whisper 模型: {MODEL_SIZE}...")
        model = WhisperModel(
            MODEL_SIZE,
            device="cpu",
            compute_type="int8",  # CPU 使用 int8 更快
            download_root=str(Path.home() / ".cache" / "whisper")
        )
        logger.info("模型加载成功！")
    except Exception as e:
        logger.error(f"模型加载失败: {e}")
        raise

@app.on_event("startup")
async def startup_event():
    """应用启动时加载模型"""
    load_model()

@app.get("/")
def root():
    """根路径"""
    return {
        "service": "Whisper API",
        "status": "running",
        "model": MODEL_SIZE,
        "endpoints": [
            "/health",
            "/v1/audio/transcriptions"
        ]
    }

@app.get("/health")
def health():
    """健康检查"""
    return {
        "status": "ok",
        "model_loaded": model is not None,
        "model_size": MODEL_SIZE
    }

@app.post("/v1/audio/transcriptions", response_class=PlainTextResponse)
async def transcribe(
    file: UploadFile = File(...),
    model_name: str = Form("whisper-1"),
    language: str = Form("zh"),
    response_format: str = Form("text")
):
    """
    语音转文字接口
    
    参数:
    - file: 音频文件 (WAV, MP3, M4A 等)
    - model_name: 模型名称（兼容 OpenAI API）
    - language: 语言代码，默认 zh（中文）
    - response_format: 返回格式，默认 text
    
    返回: 识别出的文本
    """
    if model is None:
        raise HTTPException(status_code=503, detail="模型未加载")
    
    tmp_path = None
    try:
        # 保存上传的文件到临时目录
        suffix = Path(file.filename).suffix or ".wav"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            content = await file.read()
            tmp.write(content)
            tmp_path = tmp.name
        
        logger.info(f"开始转录: {file.filename} ({len(content)} bytes), 语言: {language}")
        
        # 执行转录
        segments, info = model.transcribe(
            tmp_path,
            language=language if language else None,
            beam_size=5,
            vad_filter=True,  # 语音活动检测
            vad_parameters=dict(
                min_silence_duration_ms=500  # 最小静音时长
            )
        )
        
        # 收集所有转录文本
        texts = []
        for segment in segments:
            texts.append(segment.text.strip())
            logger.info(f"[{segment.start:.2f}s - {segment.end:.2f}s] {segment.text}")
        
        # 拼接文本
        full_text = " ".join(texts)
        
        logger.info(f"转录完成，检测语言: {info.language}, 概率: {info.language_probability:.2f}")
        logger.info(f"识别文本长度: {len(full_text)} 字符")
        
        return full_text
        
    except Exception as e:
        logger.error(f"转录失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"转录失败: {str(e)}")
    
    finally:
        # 清理临时文件
        if tmp_path and os.path.exists(tmp_path):
            try:
                os.unlink(tmp_path)
                logger.info(f"已清理临时文件: {tmp_path}")
            except Exception as e:
                logger.warning(f"清理临时文件失败: {e}")

@app.get("/models")
def list_models():
    """列出可用模型"""
    return {
        "models": [
            {"id": "whisper-1", "size": MODEL_SIZE, "languages": ["zh", "en", "auto"]}
        ]
    }

if __name__ == "__main__":
    import uvicorn
    
    # 启动服务
    logger.info("=" * 50)
    logger.info("Whisper API 服务启动中...")
    logger.info(f"模型大小: {MODEL_SIZE}")
    logger.info("访问地址: http://localhost:8000")
    logger.info("API 文档: http://localhost:8000/docs")
    logger.info("=" * 50)
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        log_level="info"
    )
