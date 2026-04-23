from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import time
import random
import uuid

app = FastAPI()

tasks = {}

@app.post("/task")
def create_task(data: dict):
    task_id = str(uuid.uuid4())

    tasks[task_id] = {
        "email": data["email"],
        "count": data["count"],
        "created_at": time.time()
    }

    return {"task_id": task_id}


@app.get("/task/{task_id}")
def long_poll(task_id: str):
    if task_id not in tasks:
        raise HTTPException(404)

    task = tasks[task_id]

    start = time.time()

    # ⏳ LONG POLLING LOOP (max 3 min)
    while True:
        elapsed = time.time() - start

        # 1. po 3 minutach kończymy
        if elapsed > 180:
            status = "done"
            break

        # 2. losowo możemy skończyć wcześniej
        if random.random() < 0.1:
            status = "done"
            break

        time.sleep(2)

    return JSONResponse({
        "email": task["email"],
        "status": status
    }, headers={
        "Location": f"/task_result/{task_id}"
    })


@app.get("/task_result/{task_id}")
def result(task_id: str):
    task = tasks.get(task_id)
    if not task:
        raise HTTPException(404)

    return {
        "email": task["email"],
        "emails": [f"mail-{i}" for i in range(task["count"])]
    }