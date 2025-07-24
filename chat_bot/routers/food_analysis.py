from fastapi import APIRouter, UploadFile, File, Form
from utils.openai_client import analyze_food_image

router = APIRouter(prefix="/analyze-food", tags=["Food Analysis"])

@router.get("/")
async def get_analyze_food(image: UploadFile = File(...), user_query: str = Form(...)):
    return await analyze_food_image(image, user_query)

@router.post("/")
async def analyze_food(image: UploadFile = File(...), user_query: str = Form(...)):
    print("Analysing Food.....")
    return await analyze_food_image(image, user_query)
