import json
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
    st.header("🧠 Test ChatBot with LLM Agent (Text Only)")

    # Persistent session variables for history & summary
    if "chat_history" not in st.session_state:
        st.session_state.chat_history = []
    if "summary" not in st.session_state:
        st.session_state.summary = ""

    user_input = st.text_input("You:", key="test_chat_input")

    if st.button("Send", key="send_test_chat"):
        if user_input.strip():
            # Convert chat history to JSON string
            payload = {
                "user_input": user_input,
                "chat_history": json.dumps(st.session_state.chat_history),
                "summary": st.session_state.summary
            }

            response = requests.post(
                f"{API_URL}/chat",
                data=payload
            )

            if response.status_code == 200:
                data = response.json()
                reply = data.get("response", "No response.")
                st.session_state.chat_history.append({"role": "user", "content": user_input})
                st.session_state.chat_history.append({"role": "assistant", "content": reply})
                st.session_state.summary = data.get("summary", st.session_state.summary)
            else:
                st.error(f"Error: {response.status_code} - {response.text}")

    # Display full chat history
    for msg in st.session_state.chat_history:
        if msg["role"] == "user":
            st.markdown(f"👤 **You**: {msg['content']}")
        else:
            st.markdown(f"🤖 **NutriAI**: {msg['content']}")


