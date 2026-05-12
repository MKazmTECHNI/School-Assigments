import datetime
import uuid

from sqlalchemy import Column, String, Boolean, Float, DateTime
from app.database import Base


class Switch(Base):
    __tablename__ = "switches"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String, nullable=False)
    state = Column(Boolean, default=False)
    total_on_time = Column(Float, default=0.0)
    last_turned_on = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
