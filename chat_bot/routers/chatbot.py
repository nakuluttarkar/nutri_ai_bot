from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Dict, Any
from agents.nutri_ai_agent import run_agent

router = APIRouter()

class ChatRequest(BaseModel):
    user_input: str
    chat_history: List[Dict[str, Any]] = []
    summary: str = ""

class ChatResponse(BaseModel):
    response: str
    chat_history: List[Dict[str, Any]]
    summary: str

@router.post("/chat", response_model=ChatResponse)
def chat_endpoint(request: ChatRequest):
    reply, updated_history, updated_summary = run_agent(
        request.user_input, request.chat_history, request.summary
    )
    return ChatResponse(response=reply, chat_history=updated_history, summary=updated_summary)

# import io
# from fastapi import APIRouter, File, Form, UploadFile
# from fastapi.responses import JSONResponse
# from typing import Optional
# import json
# from utils.image_utils import encode_image_to_base64
# from utils.classifier import classify_image
# from utils.fallback_agent import handle_symptom_query, run_agent_fallback
# import requests
# import os
# from agents.nutri_ai_agent import run_agent
# from fastapi import Depends
# from pydantic import BaseModel
# from typing import List, Dict, Any

# router = APIRouter()

# API_URL = "http://localhost:8000"

# def get_optional_upload_file(file: Optional[UploadFile]) -> Optional[UploadFile]:
#     # Swagger UI sends an empty string when no file is uploaded
#     if file is None or (hasattr(file, "filename") and file.filename == ""):
#         return None
#     return file

# class ChatResponse(BaseModel):
#     response: str
#     chat_history: List[Dict[str, Any]]
#     summary: str

# @router.post("/chat", response_model=ChatResponse)
# async def chat_endpoint(user_input: Optional[str] = Form(""),
#                         chat_history: Optional[str] = Form("[]"),
#                         summary: Optional[str] = Form(""),
#                         image: Optional[UploadFile] = File(None)):
    
#     try:
#         chat_history_list = json.loads(chat_history or "[]")
#     except json.JSONDecodeError:
#         chat_history_list = [] 
#     if image:
#         image_bytes = await image.read()
#         image_file = io.BytesIO(image_bytes)
#         image_base64 = encode_image_to_base64(image_bytes)
#         image_type = classify_image(image_base64)
#         print("Image uploaded", image_type)
#         if image_type not in ["food", "label"]:
#             return JSONResponse({
#                 "response": f"🤔 Couldn't confidently classify the image. Model said: `{image_type}`",
#                 "chat_history": chat_history_list,
#                 "summary": summary
#             })
        
#         endpoint = "/analyze-food/" if image_type == "food" else "/rate-label/"

#         try:
#             print("Sending request to LLM for image, endpont - ", endpoint)
#             print(f"{API_URL}{endpoint}")
#             response = requests.post(
#                 f"{API_URL}{endpoint}",
#                 files={"image": image_base64},
#                 data={"user_query": user_input or ""}
#             )
#             print(response)
#             response.raise_for_status()
#             data = response.json()
#             print("Data = " + data)
#             chat_history_list.append({"role": "user", "content": user_input or "[Uploaded Image]"})
#             chat_history_list.append({"role": "assistant", "content": json.dumps(data)})
#             print("chat_history_list = ", chat_history_list)
#             return JSONResponse({
#                 "response": data,
#                 "chat_history": chat_history,
#                 "summary": summary
#             })
#         except Exception as e:
#             return JSONResponse({
#                 "response": f"❌ Error calling {endpoint}: {e}",
#                 "chat_history": chat_history,
#                 "summary": summary
#             })
#     else:
#         reply, updated_history, updated_summary = run_agent(
#             user_input, chat_history_list, summary
#         )
#         return ChatResponse(response=reply, chat_history=updated_history, summary=updated_summary)

# # @router.post("/chat-bot")
# # async def chat_bot(
# #     user_input: Optional[str] = Form(None),
# #     chat_history: Optional[str] = Form("[]"),
# #     summary: Optional[str] = Form(""),
# #     image: Optional[UploadFile] = Depends(get_optional_upload_file)
# # ):
    
# #     try:
# #         chat_history_list = json.loads(chat_history or "[]")
# #     except json.JSONDecodeError:
# #         chat_history_list = []

# #     if image:
# #         image_bytes = await image.read()
# #         image_base64 = encode_image_to_base64(image_bytes)
# #         image_type = classify_image(image_base64)

# #         if image_type not in ["food", "label"]:
# #             return JSONResponse({
# #                 "response": f"🤔 Couldn't confidently classify the image. Model said: `{image_type}`",
# #                 "chat_history": chat_history,
# #                 "summary": summary
# #             })

# #         # Route to respective downstream API
# #         endpoint = "/analyze-food/" if image_type == "food" else "/rate-label/"
# #         try:
# #             response = requests.post(
# #                 f"{API_URL}{endpoint}",
# #                 files={"image": image_bytes},
# #                 data={"user_query": user_input or ""}
# #             )
# #             response.raise_for_status()
# #             data = response.json()

# #             chat_history.append({"role": "user", "content": user_input or "[Uploaded Image]"})
# #             chat_history.append({"role": "assistant", "content": data})

# #             return JSONResponse({
# #                 "response": data,
# #                 "chat_history": chat_history,
# #                 "summary": summary
# #             })
# #         except Exception as e:
# #             return JSONResponse({
# #                 "response": f"❌ Error calling {endpoint}: {e}",
# #                 "chat_history": chat_history,
# #                 "summary": summary
# #             })

# #     # No image uploaded — handle text only
# #     try:
# #         reply, updated_history, updated_summary = run_agent(user_input, chat_history_list, summary)
# #         return JSONResponse({
# #             "response": reply,
# #             "chat_history": updated_history,
# #             "summary": updated_summary
# #         })
# #     except Exception as e:
# #         return JSONResponse({
# #             "response": f"❌ Error during chat: {e}",
# #             "chat_history": chat_history_list,
# #             "summary": summary
# #         })
    

