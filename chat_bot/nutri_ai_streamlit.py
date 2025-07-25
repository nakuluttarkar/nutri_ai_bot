import streamlit as st
import requests
import os
import base64
from agents.nutri_ai_agent import run_agent
import base64
import os
from openai import OpenAI
import logging

logging.basicConfig(
    level=logging.DEBUG,  # Change to INFO to reduce noise
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)

API_URL = "http://localhost:8000"

st.set_page_config(page_title="NutriAI - Baby Health Assistant", page_icon="👶")
st.title("👶 NutriAI - Baby Health Assistant")

tab1, tab2, tab3, tab4, tab5, tab6 = st.tabs(["📷 Analyze Food", "📦 Rate Label", "🍲 Baby Recipes", "👩‍⚕️ Pediatricians", "ChatBot", "testChatBot"])

with tab1:
    st.header("📷 Upload Food Image")
    food_img = st.file_uploader("Upload food image", type=["jpg", "jpeg", "png"])
    if st.button("Analyze Food") and food_img:
        res = requests.post(f"{API_URL}/analyze-food/", files={"image": food_img.getvalue()})
        st.json(res.json())

with tab2:
    st.header("📦 Upload Nutrition Label")
    label_img = st.file_uploader("Upload label image", type=["jpg", "jpeg", "png"])
    if st.button("Rate Label") and label_img:
        res = requests.post(f"{API_URL}/rate-label/", files={"image": label_img.getvalue()})
        st.json(res.json())

with tab3:
    st.header("🍲 Suggest Recipes")
    age = st.slider("Baby's Age (Months)", 0, 36, 12)
    allergies = st.text_input("Allergies (comma separated)", "")
    if st.button("Get Recipes"):
        payload = {"age_months": age, "allergies": allergies or None}
        res = requests.post(f"{API_URL}/recipes/", json=payload)
        st.json(res.json())

with tab4:
    st.header("👩‍⚕️ Find Pediatricians")
    location = st.text_input("Enter your location", "Bangalore")
    if st.button("Find Doctors"):
        res = requests.get(f"{API_URL}/pediatricians/", params={"location": location})
        st.json(res.json())


with tab5:
    st.header("💬 NutriAI Chatbot")

    if "chat_history" not in st.session_state:
        st.session_state.chat_history = []

    user_query = st.text_input("Type your question here:", key="chat_input")

    uploaded_image = st.file_uploader("Upload image (food or label)", type=["jpg", "jpeg", "png"])

    if st.button("Send"):
        client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

        # Handle image upload
        if uploaded_image:
            image_bytes = uploaded_image.read()
            base64_image = base64.b64encode(image_bytes).decode("utf-8")

            classification_prompt = (
                "Is this image a picture of cooked baby food or a nutrition label? "
                "Reply with only one word: 'food' or 'label'."
            )

            vision_response = client.chat.completions.create(
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

            image_type = vision_response.choices[0].message.content.strip().lower()

            # Append user message to history
            st.session_state.chat_history.append({"role": "user", "content": user_query or "[Uploaded Image]"})

            try:
                # Route to appropriate backend based on classification
                if image_type == "food":
                    res = requests.post(
                        f"{API_URL}/analyze-food/",
                        files={"image": image_bytes},
                        data={"user_query": user_query or ""}
                    )
                    data = res.json()
                    st.markdown("**🤖 NutriAI:**")
                    # st.markdown("📊 This looks like cooked baby food. Analyzing nutrients...")
                    st.json(data)

                elif image_type == "label":
                    res = requests.post(
                        f"{API_URL}/rate-label/",
                        files={"image": image_bytes},
                        data={"user_query": user_query or ""}
                    )
                    data = res.json()
                    st.markdown("**🤖 NutriAI:**")
                    # st.markdown("📊 This looks like cooked baby food. Analyzing nutrients...")
                    st.json(data)

                else:
                    st.session_state.chat_history.append({
                        "role": "assistant",
                        "content": f"🤔 Couldn't confidently classify the image. LLM said: `{image_type}`"
                    })

            except Exception as e:
                st.session_state.chat_history.append({
                    "role": "assistant",
                    "content": f"❌ Error during analysis: {e}"
                })

        elif user_query.strip():
            # Text-only query
            try:
                run_agent(user_query)
            except Exception as e:
                st.session_state.chat_history.append({
                    "role": "assistant",
                    "content": f"❌ Error calling agent: {e}"
                })

    # Render the conversation
    for msg in st.session_state.chat_history:
        role = msg.get("role")
        content = msg.get("content")

        if role == "user":
            st.markdown(f"**🧑 You:** {content}")
        elif role == "assistant":
            st.markdown("**🤖 NutriAI:**")
            if isinstance(content, dict):
                if message := content.get("message"):
                    st.markdown(message)
                if result := content.get("result"):
                    st.json(result)
            else:
                st.markdown(content)



