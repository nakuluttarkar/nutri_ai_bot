# NutriAI Backend (Phase 1)

FastAPI backend for analyzing baby food images, nutrition labels, suggesting recipes, and recommending pediatricians.

## 🚀 Setup

```bash
git clone <repo-url>
cd nutri_ai_backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### Add your OpenAI key

Set environment variable:
```bash
export OPENAI_API_KEY="your-api-key"
```

## 🧪 Run the server

```bash
uvicorn main:app --reload
```

Visit docs at: [http://localhost:8000/docs](http://localhost:8000/docs)
