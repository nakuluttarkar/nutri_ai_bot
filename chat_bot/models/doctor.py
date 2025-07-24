# models/doctor.py
from sqlalchemy import Column, String, BigInteger, Boolean
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class Doctor(Base):
    __tablename__ = "doctors"

    id = Column(BigInteger, primary_key=True)
    first_name = Column(String)
    last_name = Column(String)
    gender = Column(String)
    phone_numbers = Column(String)
    email_addresses = Column(String)
    image_url = Column(String)
    post_salutation = Column(String)
    status = Column(String)
    display = Column(Boolean)
    display_order = Column(BigInteger)
