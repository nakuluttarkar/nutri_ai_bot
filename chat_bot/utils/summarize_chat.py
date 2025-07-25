from openai import AsyncOpenAI
client = AsyncOpenAI()

async def summarize_chat_history(chat_history: list) -> str:
    chat_text = "\n".join([f"{m['role']}: {m['content']}" for m in chat_history[-6:]])

    response = await client.chat.completions.create(
        model="gpt-4",
        messages=[
            {"role": "system", "content": "Summarize the following chat in 1-2 sentences for quick context."},
            {"role": "user", "content": chat_text}
        ],
        temperature=0.5,
        max_tokens=100,
    )
    return response.choices[0].message.content.strip()
