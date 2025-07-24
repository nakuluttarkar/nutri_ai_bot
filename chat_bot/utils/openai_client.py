import base64
from openai import OpenAI
from fastapi import UploadFile

client = OpenAI()

async def analyze_food_image(image: UploadFile, user_query):
    image_data = await image.read()
    base64_image = base64.b64encode(image_data).decode()

    response = client.chat.completions.create(
    model="gpt-4o",
    messages=[
        {
        "role": "system",
        "content": (
            "You are an expert pediatric nutrition assistant.\n\n"
            "Your job is to analyze a cooked baby food image and:\n"
            "1. Estimate total calories and nutritional values (protein, carbs, fat, fiber, sugar).\n"
            "2. Provide a healthiness rating from 1 to 5:\n"
            "   - 1 = Very unhealthy\n"
            "   - 5 = Very healthy\n"
            "3. Explain why this food is rated as such (in 2–3 sentences).\n"
            "4. Suggest 1–3 healthier or more balanced baby food alternatives (e.g., more veggies, less sugar).\n\n"
            "Only return your response in the exact JSON format shown below.\n\n"
            "Strict JSON Format:\n"
            "{\n"
            "  \"rating\": \"<1-5>\",\n"
            "  \"estimated_calories\": \"<calories in kcal>\",\n"
            "  \"comment\": \"<short explanation about the rating>\",\n"
            "  \"nutrition_estimates\": {\n"
            "    \"protein\": \"<value in grams>\",\n"
            "    \"carbohydrates\": \"<value in grams>\",\n"
            "    \"fat\": \"<value in grams>\",\n"
            "    \"fiber\": \"<value in grams>\",\n"
            "    \"sugar\": \"<value in grams>\"\n"
            "  },\n"
            "  \"suggested_alternatives\": [\n"
            "    \"<short suggestion 1>\",\n"
            "    \"<short suggestion 2>\",\n"
            "    ...\n"
            "  ]\n"
            "}\n\n"
            "Only respond with this JSON. Do not include any additional explanation or text."
        )
        },
        {
            "role": "user",
            "content": [
                {"type": "text", "text": user_query or "Analyze this baby food image."},
                {"type": "image_url", "image_url": {"url": f"data:image/png;base64,{base64_image}"}}
            ]
        }
    ],
    max_tokens=1000
    )
    return {"response": response.choices[0].message.content}


async def rate_nutrition_label(image: UploadFile, user_query):
    image_data = await image.read()
    base64_image = base64.b64encode(image_data).decode()
    user_base_prompt = ""
    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {
                "role": "system",
                "content": "You are an expert pediatric nutrition assistant.\n\nYour task is to analyze an image of a nutrition label and ingredients list. Based on the content:\n\n1. Extract important nutrients and their values (e.g., protein, sugar, sodium).\n2. Identify any concerning ingredients or warning signs (e.g., high sugar, preservatives).\n3. Provide a healthiness rating from 1 to 5:\n   - 1 = Very unhealthy\n   - 5 = Very healthy\n4. Explain why you gave this rating, based on the label content.\n5. Always respond strictly in the following JSON format:\n\n{\n  \"rating\": \"<rating from 1 to 5>\",\n  \"comment\": \"<reason for the rating, in 2–3 sentences>\",\n  \"nutrition_values\": {\n    \"protein\": \"<amount>\",\n    \"sugar\": \"<amount>\",\n    \"sodium\": \"<amount>\",\n    \"total_calories\": \"<amount>\"...\n  },\n  \"warnings\": [\"<short warning 1>\", \"<short warning 2>\", ...]\n}\n\nDo not include anything outside of the JSON block."
            },
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": user_query or "Analyze this baby food label."},
                    {"type": "image_url", "image_url": {"url": f"data:image/png;base64,{base64_image}"}}
                ]
            }
        ]
    )
    return {"response": response.choices[0].message.content}


async def get_recipes(age_months: int, allergies: str | None, illness: str | None):
    prompt = f"Suggest nutritious food recipes for a {age_months}-month-old baby."

    if illness:
        prompt += f" The baby is currently experiencing '{illness}', so recipes should be gentle, easy to digest, and support recovery. Include medicinal or symptom-relief ingredients if suitable."

    if allergies:
        prompt += f" Avoid any recipes containing: {allergies}."

    prompt += " Recipes should be easy to prepare at home, and suitable for a baby's digestion and nutritional needs."

    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[{"role": "user", "content": prompt}]
    )
    return {"response": response.choices[0].message.content}
