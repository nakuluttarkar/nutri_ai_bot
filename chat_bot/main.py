from fastapi import FastAPI
from fastapi.requests import Request
from routers import food_analysis, nutrition_rating, recipes, pediatricians, chatbot
from fastapi.exceptions import RequestValidationError
from fastapi.exception_handlers import request_validation_exception_handler
from dotenv import load_dotenv
import os
import secrets
from starlette.middleware.sessions import SessionMiddleware

load_dotenv()
temp_secret_key = secrets.token_urlsafe(32)

app = FastAPI(title="NutriAI - Baby Health Backend")
app.add_middleware(SessionMiddleware, secret_key=temp_secret_key)

app.include_router(food_analysis.router)
app.include_router(nutrition_rating.router)
app.include_router(recipes.router)
app.include_router(pediatricians.router)
app.include_router(chatbot.router)

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    print(f"Validation Error: {exc}")
    return await request_validation_exception_handler(request, exc)