from fastapi import APIRouter

router = APIRouter(prefix="/pediatricians", tags=["Pediatricians"])

@router.get("/")
async def get_pediatricians(location: str = "Bangalore"):
    return {
        "recommendations": [
            {"name": "Dr. A", "location": location, "rating": 4.8},
            {"name": "Dr. B", "location": location, "rating": 4.5}
        ]
    }
