#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Whisper API 服务
提供语音转文字接口
"""

from fastapi import FastAPI, File, UploadFile, Form
from fastapi.responses import PlainTextResponse
from faster_whisper import WhisperModel
import tempfile
import os
import logging

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(title="Whisper API", version="1.0.0")

# 全局变量
model = None

def load_model():
    """加载 Whisper 模型"""
    global model
    if model is None:
        logger.info("正在加载 Whisper 模型...")
        # 使用 small 模型，平衡速度和准确率
        # 可选: tiny, base, small, medium, large-v3
        model = WhisperModel("small", device="cpu", compute_type="int8")
        logger.info("Whisper 模型加载完成")
    return model

@app.on_event("startup")
async def startup_event():
    """应用启动时加载模型"""
    load_model()

@app.get("/")
def root():
    """根路径"""
    return {"message": "Whisper API Server", "version": "1.0.0"}

@app.get("/health")
def health():
    """健康检查"""
    return {"status": "ok", "model": "small"}

@app.post("/v1/audio/transcriptions", response_class=PlainTextResponse)
async def transcribe(
    file: UploadFile = File(...),
    model_name: str = Form("whisper-1"),
    language: str = Form("zh")
):
    """
    转录音频文件为文本
    
    参数:
        file: 音频文件 (wav, mp3, mp4, etc.)
        model_name: 模型名称 (兼容性参数，实际使用 faster-whisper)
        language: 语言代码 (zh=中文, en=英文)
    
    返回:
        转录的文本内容
    """
    temp_path = None
    try:
        logger.info(f"收到转录请求: {file.filename}, 语言: {language}")
        
        # 保存上传的文件到临时文件
        suffix = os.path.splitext(file.filename)[1] or ".wav"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            content = await file.read()
            tmp.write(content)
            temp_path = tmp.name
        
        logger.info(f"文件已保存到临时路径: {temp_path}")
        
        # 加载模型
        whisper_model = load_model()
        
        # 转录音频
        logger.info("开始转录音频...")
        segments, info = whisper_model.transcribe(
            temp_path,
            language=language,
            beam_size=5,
            vad_filter=True,  # 使用 VAD 过滤静音
            vad_parameters=dict(min_silence_duration_ms=500)
        )
        
        # 拼接所有文本段落
        text_parts = []
        for segment in segments:
            text_parts.append(segment.text.strip())
        
        result_text = " ".join(text_parts)
        logger.info(f"转录完成，文本长度: {len(result_text)} 字符")
        
        return result_text
        
    except Exception as e:
        logger.error(f"转录失败: {str(e)}", exc_info=True)
        return f"转录失败: {str(e)}"
    
    finally:
        # 清理临时文件
        if temp_path and os.path.exists(temp_path):
            try:
                os.unlink(temp_path)
                logger.info(f"已删除临时文件: {temp_path}")
            except Exception as e:
                logger.warning(f"删除临时文件失败: {e}")

if __name__ == "__main__":
    import uvicorn
    
    print("=" * 60)
    print("Whisper API 服务启动中...")
    print("=" * 60)
    print(f"访问地址: http://localhost:8000")
    print(f"健康检查: http://localhost:8000/health")
    print(f"API 文档: http://localhost:8000/docs")
    print("=" * 60)
    
    # 启动服务
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        log_level="info"
    )
