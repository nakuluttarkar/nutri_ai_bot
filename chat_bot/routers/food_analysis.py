import logging
from fastapi import APIRouter, Form
from fastapi.responses import JSONResponse
from utils.openai_client import analyze_food_image

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/analyze-food", tags=["Food Analysis"])

@router.post("/")
async def analyze_food(
    image_base64: str = Form(...)
):
    logger.info("🧠 Analyzing food image for query: %s")

    try:
        response = await analyze_food_image(image_base64)
        logger.info("✅ Analysis complete.")
        return JSONResponse(content=response)
    except Exception as e:
        logger.error("❌ Error during food analysis: %s", str(e), exc_info=True)
        return JSONResponse(content={"error": "Failed to analyze food image."}, status_code=500)
