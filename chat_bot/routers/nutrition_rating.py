from fastapi import APIRouter, UploadFile, File, Form
from utils.openai_client import rate_nutrition_label

router = APIRouter(prefix="/rate-label", tags=["Nutrition Rating"])

@router.post("/")
async def rate_label(image: UploadFile = File(...), user_query: str = Form(...)):
    print("Analysing nutrition label.....")
    return await rate_nutrition_label(image, user_query)
