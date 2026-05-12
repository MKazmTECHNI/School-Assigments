import datetime
import uuid

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Switch
from app.mqtt_client import MQTTManager

router = APIRouter(prefix="/switches", tags=["switches"])


class SwitchCreate(BaseModel):
    name: str


class SwitchTurn(BaseModel):
    state: str  # "on" or "off"


class SwitchResponse(BaseModel):
    id: str
    name: str
    state: bool
    total_on_time: float
    created_at: datetime.datetime

    model_config = {"from_attributes": True}


class StatsResponse(BaseModel):
    id: str
    name: str
    total_on_time: float


def get_mqtt() -> MQTTManager:
    from app.main import mqtt_manager
    return mqtt_manager


@router.post("", response_model=SwitchResponse, status_code=201)
async def create_switch(body: SwitchCreate, db: Session = Depends(get_db), mqtt: MQTTManager = Depends(get_mqtt)):
    switch_id = str(uuid.uuid4())

    mqtt.subscribe(f"switch/register/confirm/{switch_id}")

    mqtt.publish("switch/register", {"id": switch_id, "name": body.name})

    try:
        await mqtt.wait_for_confirm(switch_id)
    except TimeoutError:
        raise HTTPException(
            status_code=504,
            detail="No confirmation from device within timeout"
        )

    switch = Switch(id=switch_id, name=body.name)
    db.add(switch)
    db.commit()
    db.refresh(switch)
    return switch


@router.get("", response_model=list[SwitchResponse])
def list_switches(db: Session = Depends(get_db)):
    return db.query(Switch).all()


@router.get("/{switch_id}", response_model=SwitchResponse)
def get_switch(switch_id: str, db: Session = Depends(get_db)):
    switch = db.query(Switch).filter(Switch.id == switch_id).first()
    if not switch:
        raise HTTPException(status_code=404, detail="Switch not found")
    return switch


@router.put("/{switch_id}/turn", response_model=SwitchResponse)
async def turn_switch(switch_id: str, body: SwitchTurn, db: Session = Depends(get_db), mqtt: MQTTManager = Depends(get_mqtt)):
    switch = db.query(Switch).filter(Switch.id == switch_id).first()
    if not switch:
        raise HTTPException(status_code=404, detail="Switch not found")

    target_state = body.state.lower()
    if target_state not in ("on", "off"):
        raise HTTPException(status_code=422, detail="State must be 'on' or 'off'")

    mqtt.subscribe(f"switch/{switch_id}/status")

    mqtt.publish(f"switch/{switch_id}/command", {"state": target_state.upper()})

    try:
        status_data = await mqtt.wait_for_status(switch_id)
    except TimeoutError:
        raise HTTPException(
            status_code=504,
            detail="No status confirmation from device within timeout"
        )

    now = datetime.datetime.utcnow()

    if target_state == "on":
        if not switch.state:
            switch.last_turned_on = now
        switch.state = True
    else:
        if switch.state and switch.last_turned_on:
            elapsed = (now - switch.last_turned_on).total_seconds()
            switch.total_on_time += elapsed
        switch.state = False
        switch.last_turned_on = None

    db.commit()
    db.refresh(switch)
    return switch


@router.get("/{switch_id}/stats", response_model=StatsResponse)
def get_switch_stats(switch_id: str, db: Session = Depends(get_db)):
    switch = db.query(Switch).filter(Switch.id == switch_id).first()
    if not switch:
        raise HTTPException(status_code=404, detail="Switch not found")

    current_on_time = switch.total_on_time
    if switch.state and switch.last_turned_on:
        current_on_time += (datetime.datetime.utcnow() - switch.last_turned_on).total_seconds()

    return StatsResponse(
        id=switch.id,
        name=switch.name,
        total_on_time=current_on_time,
    )
