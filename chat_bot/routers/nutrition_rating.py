import logging
from fastapi import APIRouter, Form
from fastapi.responses import JSONResponse
from utils.openai_client import rate_nutrition_label

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/rate-label", tags=["Nutrition Rating"])

@router.post("/")
async def rate_label(
    image_base64: str = Form(...),
    user_query: str = Form(...)
):
    logger.info("🔍 Analyzing nutrition label for query: %s", user_query)

    try:
        response = await rate_nutrition_label(image_base64, user_query)
        logger.info("✅ Analysis complete.")
        return JSONResponse(content=response)
    except Exception as e:
        logger.error("❌ Error during nutrition label analysis: %s", str(e), exc_info=True)
        return JSONResponse(content={"error": "Failed to analyze label."}, status_code=500)
