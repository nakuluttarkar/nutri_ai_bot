# agent_tools.py
import requests

def get_recipes_tool():
    return {
        "type": "function",
        "function": {
            "name": "get_baby_recipes",
            "description": "Get baby food recipes based on age and allergies.",
            "parameters": {
                "type": "object",
                "properties": {
                    "age_months": {"type": "integer", "description": "Baby's age in months"},
                    "allergies": {"type": "string", "description": "Comma-separated list of allergies", "nullable": True},
                    "illness": {"type": "string", "description": "Optional illness like 'cold', 'fever', 'diarrhea' etc."}
                },
                "required": ["age_months"]
            }
        }
    }

def call_get_baby_recipes(age_months, allergies=None, illness: str = ""):
    payload = {
        "age_months": age_months,
        "allergies": allergies,
        "illness": illness
    }
    payload = {k: v for k, v in payload.items() if v is not None}
    res = requests.post("http://localhost:8000/recipes/", json=payload)
    return res.json()

# --- Food Analysis Tool ---
def analyze_food_tool():
    return {
        "type": "function",
        "function": {
            "name": "analyze_food_image",
            "description": "Analyze a food image and return what it contains.",
            "parameters": {
                "type": "object",
                "properties": {
                    "image_url": {"type": "string", "description": "Public URL to the food image"}
                },
                "required": ["image_url"]
            }
        }
    }

def call_analyze_food_image(image_url: str):
    res = requests.post("http://localhost:8000/analyze-food/", files={"image": requests.get(image_url).content})
    return res.json()

# --- Nutrition Label Rating Tool ---
def rate_label_tool():
    return {
        "type": "function",
        "function": {
            "name": "rate_nutrition_label",
            "description": "Upload a nutrition label and get a health score.",
            "parameters": {
                "type": "object",
                "properties": {
                    "image_url": {"type": "string", "description": "Public URL of the nutrition label image"}
                },
                "required": ["image_url"]
            }
        }
    }

def call_rate_nutrition_label(image_url: str):
    res = requests.post("http://localhost:8000/rate-label/", files={"image": requests.get(image_url).content})
    return res.json()

# --- Get Pediatrician Tool ---
def get_pediatricians_tool():
    return {
        "type": "function",
        "function": {
            "name": "get_pediatricians",
            "description": "Get a list of pediatricians for consulation if any symptoms are reported",
            "parameters": {
                "type": "object",
                "properties": {
                   
                },
                "required": []
            }
        }
    }

def call_get_pediatricians():
    res = requests.get("http://localhost:8000/pediatricians/")
    return res.json()