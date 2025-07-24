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
                    "allergies": {"type": "string", "description": "Comma-separated list of allergies", "nullable": True}
                },
                "required": ["age_months"]
            }
        }
    }

def call_get_baby_recipes(age_months, allergies=None):
    res = requests.post("http://localhost:8000/recipes/", json={"age_months": age_months, "allergies": allergies})
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
