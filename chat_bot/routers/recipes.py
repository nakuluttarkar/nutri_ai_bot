from fastapi import APIRouter
from pydantic import BaseModel
from utils.openai_client import get_recipes

router = APIRouter(prefix="/recipes", tags=["Recipes"])

class RecipeRequest(BaseModel):
    age_months: int
    allergies: str | None = None

@router.post("/")
async def suggest_recipes(request: RecipeRequest):
    return await get_recipes(request.age_months, request.allergies)
