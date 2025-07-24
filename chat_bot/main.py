from fastapi import FastAPI
from fastapi.requests import Request
from routers import food_analysis, nutrition_rating, recipes, pediatricians
from fastapi.exceptions import RequestValidationError
from fastapi.exception_handlers import request_validation_exception_handler
app = FastAPI(title="NutriAI - Baby Health Backend")

app.include_router(food_analysis.router)
app.include_router(nutrition_rating.router)
app.include_router(recipes.router)
app.include_router(pediatricians.router)
app.include_router(pediatricians.router)

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    print(f"Validation Error: {exc}")
    return await request_validation_exception_handler(request, exc)