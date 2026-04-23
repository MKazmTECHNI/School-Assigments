import requests
import time

BASE = "http://127.0.0.1:8000"

# 1. create task
r = requests.post(f"{BASE}/task", json={
    "email": "nEY9R@example.com",
    "count": 5
})

task_id = r.json()["task_id"]
print("TASK:", task_id)

# 2. polling loop
while True:
    res = requests.get(f"{BASE}/task/{task_id}")
    data = res.json()

    print("STATUS:", data["status"])

    if data["status"] == "done":
        result_url = res.headers.get("Location")
        print("DONE →", result_url)

        final = requests.get(BASE + result_url)
        print("RESULT:", final.json())
        break

    time.sleep(1)