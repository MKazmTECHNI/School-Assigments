import asyncio
import json
import logging
import datetime
import os

from gmqtt import Client as MQTTClient
from gmqtt.mqtt.constants import MQTTv311

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [SIMULATOR] %(levelname)s %(message)s",
)
logger = logging.getLogger(__name__)

BROKER_HOST = os.environ.get("MQTT_BROKER_HOST", "broker.emqx.io")
BROKER_PORT = int(os.environ.get("MQTT_BROKER_PORT", "1883"))
CLIENT_ID = "light-simulator"


class LightSimulator:
    def __init__(self):
        self._client = MQTTClient(CLIENT_ID)
        self._client.on_connect = self._on_connect
        self._client.on_message = self._on_message
        self._devices: dict[str, dict] = {}

    async def start(self):
        await self._client.connect(BROKER_HOST, BROKER_PORT)
        logger.info("Simulator connected to broker")

        self._client.subscribe("switch/register")
        self._client.subscribe("switch/+/command")
        logger.info("Subscribed to topics")

        await asyncio.Event().wait()

    def _on_connect(self, client, flags, rc, properties):
        logger.info("Connected to MQTT broker")

    def _on_message(self, client, topic, payload, qos, properties):
        try:
            data = json.loads(payload.decode())
        except (json.JSONDecodeError, UnicodeDecodeError):
            logger.warning("Invalid message on %s", topic)
            return

        parts = topic.split("/")

        if topic == "switch/register":
            asyncio.create_task(self._handle_register(data))
            return

        if len(parts) == 3 and parts[0] == "switch" and parts[2] == "command":
            switch_id = parts[1]
            asyncio.create_task(self._handle_command(switch_id, data))
            return

    async def _handle_register(self, data: dict):
        switch_id = data.get("id")
        name = data.get("name", "Unknown")
        if not switch_id:
            logger.warning("Registration without id: %s", data)
            return

        self._devices[switch_id] = {"name": name, "state": "OFF"}
        logger.info("Registered new switch: %s (%s)", name, switch_id)

        self._client.publish(
            f"switch/register/confirm/{switch_id}",
            json.dumps({"id": switch_id, "status": "ok", "name": name}).encode(),
        )
        logger.info("Sent confirmation for switch %s", switch_id)

    async def _handle_command(self, switch_id: str, data: dict):
        state = data.get("state", "").upper()
        if state not in ("ON", "OFF"):
            logger.warning("Invalid state command: %s", state)
            return

        if switch_id not in self._devices:
            logger.warning("Command for unknown switch %s", switch_id)
            self._devices[switch_id] = {"name": "unknown", "state": "OFF"}

        self._devices[switch_id]["state"] = state
        timestamp = datetime.datetime.utcnow().isoformat()
        logger.info(
            "Switch %s turned %s at %s",
            switch_id,
            state,
            timestamp,
        )

        self._client.publish(
            f"switch/{switch_id}/status",
            json.dumps({
                "id": switch_id,
                "state": state,
                "timestamp": timestamp,
            }).encode(),
        )
        logger.info("Published status for switch %s: %s", switch_id, state)


async def main():
    simulator = LightSimulator()
    await simulator.start()


if __name__ == "__main__":
    asyncio.run(main())
