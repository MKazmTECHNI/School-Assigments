from flask import Flask, jsonify, request
from datetime import datetime
import time
from collections import deque

app = Flask(__name__)

# Message queue for long polling
message_queue = deque()

@app.route('/poll', methods=['GET'])
def long_poll():
    """
    Long polling endpoint - waits for new messages
    """
    timeout = 30  # Wait up to 30 seconds
    start_time = time.time()
    
    while time.time() - start_time < timeout:
        if message_queue:
            message = message_queue.popleft()
            return jsonify({
                'status': 'success',
                'message': message,
                'timestamp': datetime.now().isoformat()
            })
        time.sleep(0.5)  # Check every 0.5 seconds
    
    # Timeout - no new messages
    return jsonify({
        'status': 'timeout',
        'message': None,
        'timestamp': datetime.now().isoformat()
    })

@app.route('/send', methods=['POST'])
def send_message():
    """
    Endpoint to send a message to the queue
    """
    data = request.get_json()
    message = data.get('message', '')
    
    if message:
        message_queue.append(message)
        return jsonify({
            'status': 'success',
            'message': 'Message queued'
        })
    
    return jsonify({
        'status': 'error',
        'message': 'No message provided'
    }), 400

@app.route('/')
def index():
    return '''
    <h1>Long Polling Example</h1>
    <p>Use /poll to wait for messages</p>
    <p>Use /send to post messages</p>
    '''

if __name__ == '__main__':
    app.run(debug=True, threaded=True)