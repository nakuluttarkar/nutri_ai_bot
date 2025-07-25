from openai import OpenAI
from agents.agent_tools import *
import json
import logging

client = OpenAI()
logger = logging.getLogger(__name__)

DEFAULT_SYSTEM_PROMPT = (
                    "You are NutriAI — a helpful, knowledgeable, and empathetic AI assistant specializing in baby nutrition, health, and wellness.\n\n"
                    "Your primary job is to assist parents of children aged 0–5 years. You have access to a set of tools that help answer specific queries more accurately.\n\n"
                    "🔧 Tool Usage Instructions:\n"
                    "- Use `analyze_food_image` when the user uploads a food image or asks to estimate calories, nutrients, or healthiness of cooked baby food.\n"
                    "- Use `rate_nutrition_label` when the user uploads a nutrition label or asks to rate how healthy a packaged food item is.\n"
                    "- Use `get_baby_recipes` when the user asks for baby food suggestions, especially considering age, allergies, or illnesses like cold, fever, etc.\n"
                    "- Use `get_pediatricians` when the user mentions their baby is unwell, asks for a consultation, or seeks professional medical help.\n\n"
                    "- Additionally, if the user mentions that the baby is unwell, use `get_pediatricians` and `get_baby_recipes` tools to give appropriate answers"
                    "📌 Multi-Tool Scenarios:\n"
                    "- If the baby is sick (e.g., cold, fever, stomach issues), you can suggest `get_baby_recipes` for soothing or medicinal food, and also `get_pediatricians` to recommend a doctor for consultation.\n"
                    "- If the user asks about vaccination schedules, provide the schedule from general knowledge, then use `get_pediatricians` to suggest doctors nearby for follow-up.\n"
                    "- If both an image and a health-related question are provided, use the appropriate image analysis tool **and** follow up with recipes or pediatrician info if relevant.\n\n"
                    "💬 Response Guidelines:\n"
                    "- Be empathetic and friendly in your tone.\n"
                    "- Clearly explain what you are doing (e.g., 'Based on your baby’s age and illness, here are some suggested recipes...')\n"
                    "- Do not call tools unnecessarily. Only use tools when you need accurate, dynamic, or personalized information.\n"
                    "- When calling a tool, always explain to the user why you are using it.\n\n"
                    "Stay concise, medically responsible, and helpful. Always prioritize the baby’s health and safety in your answers. Preserve context (e.g., baby age, allergies, illness) from earlier messages."
                    )

def run_agent(user_input: str, chat_history: list, summary: str = "") -> tuple[str, list, str]:
    tools = [
        get_recipes_tool(),
        analyze_food_tool(),
        rate_label_tool(),
        get_pediatricians_tool(),
    ]

    # Server owns system
    messages: list[dict] = []
    if summary:
        messages.append({"role": "system", "content": f"Summary of previous conversation:\n{summary}"})
    else:
        messages.append({"role": "system", "content": DEFAULT_SYSTEM_PROMPT})

    # Only re-use safe history (no unfinished tool calls, no tool messages)
    safe_history = strip_unsafe_history(chat_history)
    messages.extend(safe_history)

    # Current user turn
    messages.append({"role": "user", "content": user_input})

    while True:
        logger.debug("Sending to OpenAI: %s", messages)
        assert_tool_invariants(messages)  # sanity check before the call

        response = client.chat.completions.create(
            model="gpt-4o",
            messages=messages,
            tools=tools,
            tool_choice="auto",
            temperature=0.3
        )

        message = response.choices[0].message

        if message.tool_calls:
            # 1) append the assistant message WITH tool_calls
            messages.append(message.model_dump())

            # 2) run tools and append tool results
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

            # loop again — model will now read the tool outputs
            continue

        # No tool_calls => final answer
        messages.append(message.model_dump())

        if message.content:
            # Build a *clean* history for the client to send back next time
            clean_history = strip_unsafe_history(messages)
            # keep just the last N messages (tune N to what you want)
            recent_history = clean_history[-8:]

            updated_summary = summarize_history(messages)
            return message.content, recent_history, updated_summary

            
def summarize_history(messages: list) -> str:
    # If the last assistant message had tool_calls, skip
    last_assistant = next((m for m in reversed(messages) if m["role"] == "assistant"), None)
    if last_assistant and last_assistant.get("tool_calls"):
        return ""

    filtered = [m for m in messages if m["role"] in ("user", "assistant") and not m.get("tool_calls")]

    try:
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": "Summarize this baby health conversation in 2–3 short sentences."},
                *filtered
            ],
            temperature=0.3,
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        logger.error(f"Failed to summarize: {e}")
        return ""

    
def strip_unsafe_history(history: list[dict]) -> list[dict]:
    """
    Keep only user/assistant messages that DO NOT contain tool_calls.
    This is what you can safely store/send back next time as chat_history.
    """
    safe = []
    for m in history:
        if m["role"] in ("user", "assistant"):
            # skip assistant messages that still have tool_calls
            if m["role"] == "assistant" and m.get("tool_calls"):
                continue
            safe.append({"role": m["role"], "content": m.get("content", "")})
    return safe

def assert_tool_invariants(messages: list[dict]):
    """
    Raise if any assistant tool_call id is not closed by a tool message.
    Call this right before you send messages to OpenAI.
    """
    pending = set()
    for m in messages:
        if m["role"] == "assistant" and m.get("tool_calls"):
            for tc in m["tool_calls"]:
                pending.add(tc["id"])
        elif m["role"] == "tool":
            pending.discard(m.get("tool_call_id"))
    if pending:
        raise RuntimeError(f"Unmatched tool_call_ids in history: {pending}")

