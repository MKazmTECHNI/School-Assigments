import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.database import engine, Base
from app.mqtt_client import MQTTManager
from app.routers import switches

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MQTT_HOST = os.environ.get("MQTT_BROKER_HOST", "broker.emqx.io")
MQTT_PORT = int(os.environ.get("MQTT_BROKER_PORT", "1883"))

mqtt_manager = MQTTManager(broker_host=MQTT_HOST, broker_port=MQTT_PORT)


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)

    await mqtt_manager.connect()
    logger.info("Application started")

    yield

    await mqtt_manager.disconnect()
    logger.info("Application stopped")


app = FastAPI(title="Light Switch Controller", lifespan=lifespan)
app.include_router(switches.router)
