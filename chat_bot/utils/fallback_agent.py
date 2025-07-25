def handle_symptom_query(user_input: str) -> str:
    return (
        "🩺 It seems your query is related to symptoms or illness.\n"
        "I recommend consulting a pediatrician. You can book a consultation here: [Consult Pediatrician](https://www.halodoc.com/)\n\n"
        "Meanwhile, here are some gentle recipes for sick babies:\n"
        "- Mashed banana\n"
        "- Rice porridge\n"
        "- Steamed carrot puree"
    )

def run_agent_fallback(user_input, chat_history, summary):
    from agents.nutri_ai_agent import run_agent
    return run_agent(user_input, chat_history, summary)
