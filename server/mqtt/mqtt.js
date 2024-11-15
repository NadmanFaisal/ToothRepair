
// Import mqtt
const mqtt = require('mqtt');

// Connect to the Mosquitto broker (localhost in this case)
const client = mqtt.connect('mqtt://localhost:1883');

// Handle connection
client.on('connect', () => {
  console.log('Connected to Mosquitto broker');

  // Subscribe to a topic
  client.subscribe('test/topic', (err) => {
    if (err) {
      console.error('Failed to subscribe:', err);
    } else {
      console.log('Subscribed to test/topic');
    }
  });

  // Publish a message to the topic
  client.publish('test/topic', 'Hello from mqtt.js!');
  client.publish('test/Authentication', 'Trying to reach authentication service');
});

// Handle incoming messages
client.on('message', (topic, message) => {
  console.log(`Received message: ${message.toString()} on topic: ${topic}`);
});

// Handle errors
client.on('error', (error) => {
  console.error('MQTT Error:', error);
});
