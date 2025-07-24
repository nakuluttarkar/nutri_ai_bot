from openai import OpenAI
from agents.agent_tools import *
import json

client = OpenAI()

def run_agent(user_input: str) -> str:
    tools = [
        get_recipes_tool(),
        analyze_food_tool(),
        rate_label_tool(),
        get_pediatricians_tool(),
    ]

    messages = [
                {
                    "role": "system",
                    "content": (
                        "You are NutriAI — a helpful, knowledgeable, and empathetic AI assistant specializing in baby nutrition, health, and wellness.\n\n"
                        "Your primary job is to assist parents of children aged 0–5 years. You have access to a set of tools that help answer specific queries more accurately.\n\n"
                        "🔧 Tool Usage Instructions:\n"
                        "- Use `analyze_food_image` when the user uploads a food image or asks to estimate calories, nutrients, or healthiness of cooked baby food.\n"
                        "- Use `rate_nutrition_label` when the user uploads a nutrition label or asks to rate how healthy a packaged food item is.\n"
                        "- Use `get_baby_recipes` when the user asks for baby food suggestions, especially considering age, allergies, or illnesses like cold, fever, etc.\n"
                        "- Use `get_pediatricians` when the user mentions their baby is unwell, asks for a consultation, or seeks professional medical help.\n\n"
                        "📌 Multi-Tool Scenarios:\n"
                        "- If the baby is sick (e.g., cold, fever, stomach issues), you can suggest `get_baby_recipes` for soothing or medicinal food, and also `get_pediatricians` to recommend a doctor for consultation.\n"
                        "- If the user asks about vaccination schedules, provide the schedule from general knowledge, then use `get_pediatricians` to suggest doctors nearby for follow-up.\n"
                        "- If both an image and a health-related question are provided, use the appropriate image analysis tool **and** follow up with recipes or pediatrician info if relevant.\n\n"
                        "💬 Response Guidelines:\n"
                        "- Be empathetic and friendly in your tone.\n"
                        "- Clearly explain what you are doing (e.g., 'Based on your baby’s age and illness, here are some suggested recipes...')\n"
                        "- Do not call tools unnecessarily. Only use tools when you need accurate, dynamic, or personalized information.\n"
                        "- When calling a tool, always explain to the user why you are using it.\n\n"
                        "Stay concise, medically responsible, and helpful. Always prioritize the baby’s health and safety in your answers."
                    )
                },
                {"role": "user", "content": user_input}
            ]

    while True:
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=messages,
            tools=tools,
            tool_choice="auto",
            temperature=0.2
        )

        message = response.choices[0].message

        # Handle tool call
        if message.tool_calls:
            messages.append(message.model_dump())  # Append assistant's message

            for tool_call in message.tool_calls:
                name = tool_call.function.name
                arguments = json.loads(tool_call.function.arguments or "{}")

                match name:
                    case "get_baby_recipes":
                        result = call_get_baby_recipes(**arguments)
                    case "analyze_food_image":
                        result = call_analyze_food_image(**arguments)
                    case "rate_nutrition_label":
                        result = call_rate_nutrition_label(**arguments)
                    case "get_pediatricians":
                        result = call_get_pediatricians()
                    case _:
                        result = f"❌ Unknown tool: {name}"

                messages.append({
                    "role": "tool",
                    "tool_call_id": tool_call.id,
                    "name": name,
                    "content": str(result)
                })
            final_response = client.chat.completions.create(
                model="gpt-4o",
                messages=messages,
                temperature=0.2
            )

            return final_response.choices[0].message.content
        else:
            return message.content  # Final response from assistant
