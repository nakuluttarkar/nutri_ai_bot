import logging

from fastapi import APIRouter, UploadFile, File, Form
from utils.openai_client import rate_nutrition_label

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

router = APIRouter(prefix="/rate-label", tags=["Nutrition Rating"])

@router.post("/")
async def rate_label(image_base64: str = Form(...), user_query: str = Form(...)):
    logger.info("🔍 Analysing nutrition label for query: %s", user_query)
    
    try:
        response = await rate_nutrition_label(image_base64, user_query)
        logger.info("✅ Analysis complete.")
        return response
    except Exception as e:
        logger.error("❌ Error during nutrition label analysis: %s", str(e), exc_info=True)
        return {"error": "Failed to analyze label."}
