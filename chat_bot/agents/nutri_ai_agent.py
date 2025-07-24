from openai import OpenAI
from agents.agent_tools import (
    get_recipes_tool, call_get_baby_recipes,
    analyze_food_tool, call_analyze_food_image,
    rate_label_tool, call_rate_nutrition_label,
    
)

client = OpenAI()

def run_agent(user_input: str) -> str:
    tools = [
        get_recipes_tool(),
        analyze_food_tool(),
        rate_label_tool(),
       
    ]
    
    messages = [{"role": "user", "content": user_input}]

    response = client.chat.completions.create(
        model="gpt-4o",
        messages=messages,
        tools=tools,
        tool_choice="auto"
    )

    message = response.choices[0].message

    if message.tool_calls:
        tool_call = message.tool_calls[0]
        name = tool_call.function.name
        arguments = eval(tool_call.function.arguments)

        match name:
            case "get_baby_recipes":
                result = call_get_baby_recipes(**arguments)
            case "analyze_food_image":
                result = call_analyze_food_image(**arguments)
            case "rate_nutrition_label":
                result = call_rate_nutrition_label(**arguments)
            case _:
                return "Sorry, I don't recognize that tool."

        messages.append(message.model_dump())
        messages.append({
            "role": "tool",
            "tool_call_id": tool_call.id,
            "name": name,
            "content": str(result)
        })

        final = client.chat.completions.create(
            model="gpt-4o",
            messages=messages
        )
        return final.choices[0].message.content

    return message.content
