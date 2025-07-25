import os
from openai import OpenAI

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

def classify_image(base64_image: str) -> str:
    classification_prompt = (
        "Is this image a picture of cooked baby food or a nutrition label? "
        "Reply with only one word: 'food' or 'label'."
    )
    print("Classifying Image")
    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": classification_prompt},
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": f"data:image/jpeg;base64,{base64_image}"
                        }
                    }
                ]
            }
        ],
        max_tokens=10,
    )
    print("Classified Image = ", response.choices[0].message.content.strip().lower())
    return response.choices[0].message.content.strip().lower()
