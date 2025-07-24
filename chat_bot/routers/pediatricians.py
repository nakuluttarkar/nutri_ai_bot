# routers/pediatricians.py
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from db.session import get_db
from models.doctor import Doctor

router = APIRouter(prefix="/pediatricians", tags=["Pediatricians"])

@router.get("/")
def get_pediatricians(db: Session = Depends(get_db)):
    results = db.query(Doctor).filter(
        Doctor.post_salutation == "Sp.A",
        Doctor.status == "active",
        Doctor.display == True
    ).order_by(Doctor.display_order).limit(20).all()
    print("Calling get pediatrician...")
    return [
        {
            "id": doc.id,
            "name": f"{doc.first_name} {doc.last_name or ''}".strip(),
            "gender": doc.gender,
            "image_url": doc.image_url,
            "phone_numbers": doc.phone_numbers,
            "email_addresses": doc.email_addresses,
        }
        for doc in results
    ]
