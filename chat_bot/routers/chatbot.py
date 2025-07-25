# from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Dict, Any
import httpx
import re
from utils.openai_client import rate_nutrition_label, analyze_food_image
# from agents.nutri_ai_agent import run_agent

# router = APIRouter()

# class ChatRequest(BaseModel):
#     user_input: str
#     chat_history: List[Dict[str, Any]] = []
#     summary: str = ""

class ChatResponse(BaseModel):
    response: str

class ChatRequest(BaseModel):
    user_input: str


# @router.post("/chat", response_model=ChatResponse)
# def chat_endpoint(request: ChatRequest):
#     reply, updated_history, updated_summary = run_agent(
#         request.user_input, request.chat_history, request.summary
#     )
#     return ChatResponse(response=reply, chat_history=updated_history, summary=updated_summary)

import io
import json
import requests
from fastapi import APIRouter, UploadFile, File, Form, Request
from fastapi.responses import JSONResponse
from typing import Optional

from agents.nutri_ai_agent import run_agent
from utils.image_utils import encode_image_to_base64
from utils.classifier import classify_image 

API_URL = "http://localhost:8000"
router = APIRouter()

@router.post("/chat", response_model=ChatResponse)
def chat_endpoint(
    request: Request,
    data: ChatRequest
   
):
    
    chat_history = request.session.get("chat_history", "[]")
    summary = request.session.get("summary", "")
    if not isinstance(chat_history, list):
        try:
            chat_history_list = json.loads(chat_history)
        except json.JSONDecodeError:
            chat_history_list = []
    else:
        chat_history_list = chat_history

    print("User Input = ", data.user_input)
        # No image, process text query with LLM agent
    reply, updated_history, updated_summary = run_agent(
        data.user_input, chat_history_list, summary
    )
    request.session["chat_history"] = updated_history
    request.session["chat_summary"] = updated_summary
    reply = re.sub(r"```(?:json)?\s*([\s\S]*?)```", r"\1", reply).strip()
    print("Reply = ", ChatResponse(response=reply))

    return ChatResponse(response=reply)
