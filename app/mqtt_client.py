import asyncio
import json
import logging

from gmqtt import Client as MQTTClient
from gmqtt.mqtt.constants import MQTTv311

logger = logging.getLogger(__name__)

CONFIRM_TIMEOUT = 5.0


class MQTTManager:
    def __init__(self, broker_host="localhost", broker_port=1883):
        self.broker_host = broker_host
        self.broker_port = broker_port
        self._client: MQTTClient | None = None
        self._pending_confirms: dict[str, asyncio.Future] = {}
        self._pending_status: dict[str, asyncio.Future] = {}
        self._connected = asyncio.Event()

    async def connect(self, client_id: str = "webapp"):
        self._client = MQTTClient(client_id)
        self._client.on_connect = self._on_connect
        self._client.on_message = self._on_message

        await self._client.connect(self.broker_host, self.broker_port)
        logger.info("MQTT client connected")

    async def disconnect(self):
        if self._client:
            await self._client.disconnect()
            logger.info("MQTT client disconnected")

    def _on_connect(self, client, flags, rc, properties):
        self._connected.set()

    def _on_message(self, client, topic, payload, qos, properties):
        try:
            data = json.loads(payload.decode())
        except (json.JSONDecodeError, UnicodeDecodeError):
            logger.warning("Failed to decode MQTT message on %s", topic)
            return

        parts = topic.split("/")

        if len(parts) == 4 and parts[0] == "switch" and parts[1] == "register" and parts[2] == "confirm":
            switch_id = parts[3]
            future = self._pending_confirms.pop(switch_id, None)
            if future and not future.done():
                future.set_result(data)
            return

        if len(parts) == 3 and parts[0] == "switch" and parts[2] == "status":
            switch_id = parts[1]
            future = self._pending_status.pop(switch_id, None)
            if future and not future.done():
                future.set_result(data)

    def publish(self, topic: str, payload: dict):
        if not self._client:
            raise RuntimeError("MQTT client not connected")
        self._client.publish(topic, json.dumps(payload).encode())

    def subscribe(self, topic: str):
        if not self._client:
            raise RuntimeError("MQTT client not connected")
        self._client.subscribe(topic)

    async def wait_for_confirm(self, switch_id: str, timeout: float = CONFIRM_TIMEOUT) -> dict:
        loop = asyncio.get_running_loop()
        future = loop.create_future()
        self._pending_confirms[switch_id] = future
        try:
            return await asyncio.wait_for(future, timeout=timeout)
        except asyncio.TimeoutError:
            self._pending_confirms.pop(switch_id, None)
            raise

    async def wait_for_status(self, switch_id: str, timeout: float = CONFIRM_TIMEOUT) -> dict:
        loop = asyncio.get_running_loop()
        future = loop.create_future()
        self._pending_status[switch_id] = future
        try:
            return await asyncio.wait_for(future, timeout=timeout)
        except asyncio.TimeoutError:
            self._pending_status.pop(switch_id, None)
            raise
