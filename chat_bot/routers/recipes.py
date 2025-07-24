from fastapi import APIRouter
from pydantic import BaseModel
from utils.openai_client import get_recipes
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/recipes", tags=["Recipes"])

class RecipeRequest(BaseModel):
    age_months: int
    allergies: str | None = None
    illness: str | None = None

@router.post("/")
async def suggest_recipes(request: RecipeRequest):
    print(f"Received recipe request: {request}")
    print("Calling get recipes...")
    return await get_recipes(request.age_months, request.allergies, request.illness)
