from fastapi import FastAPI
from routers import food_analysis, nutrition_rating, recipes, pediatricians

app = FastAPI(title="NutriAI - Baby Health Backend")

app.include_router(food_analysis.router)
app.include_router(nutrition_rating.router)
app.include_router(recipes.router)
app.include_router(pediatricians.router)
