from fastapi import FastAPI, Header, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import uuid
import random
import time

app = FastAPI()

tasks = {}

class TaskRequest(BaseModel):
    email: str
    count: int


@app.post("/task")
def create_task(data: TaskRequest):
    task_id = str(uuid.uuid4())

    tasks[task_id] = {
        "email": data.email,
        "count": data.count,
        "status": "pending",
    }

    return JSONResponse(
        content={
            "task_id": task_id,
            "status": "pending"
        },
        status_code=202,
        headers={"Location": f"/task/{task_id}"}
    )


@app.get("/task/{task_id}")
def get_task(task_id: str):
    if task_id not in tasks:
        raise HTTPException(status_code=404, detail="Not found")

    task = tasks[task_id]

    if task["status"] == "pending":
        task["status"] = random.choice(["pending", "done", "error"])

    headers = {}

    if task["status"] == "done":
        headers["Location"] = f"/task_result/{task_id}"

    return JSONResponse(
        content={
            "email": task["email"],
            "status": task["status"]
        },
        headers=headers
    )


@app.get("/task_result/{task_id}")
def get_result(task_id: str):
    if task_id not in tasks:
        raise HTTPException(status_code=404)

    task = tasks[task_id]

    if task["status"] != "done":
        raise HTTPException(status_code=400, detail="Not ready")

    return {
        "email": task["email"],
        "emails": [f"mail-{i}@example.com" for i in range(task["count"])]
    }