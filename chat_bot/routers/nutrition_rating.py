import logging
from fastapi import APIRouter, Form
from fastapi.responses import JSONResponse
from utils.openai_client import rate_nutrition_label

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/rate-label", tags=["Nutrition Rating"])

@router.post("/")
async def rate_label(
    image_base64: str = Form(...)
):
    logger.info("🔍 Analyzing nutrition label for query: %s")

    try:
        
        response = await rate_nutrition_label(image_base64)
        raw = response.get("response", "")
        if raw.startswith("```json"):
            raw = raw.removeprefix("```json").strip()
        if raw.endswith("```"):
            raw = raw.removesuffix("```").strip()
        logger.info("✅ Analysis complete.")
        return JSONResponse(content=response)
    except Exception as e:
        logger.error("❌ Error during nutrition label analysis: %s", str(e), exc_info=True)
        return JSONResponse(content={"error": "Failed to analyze label."}, status_code=500)
