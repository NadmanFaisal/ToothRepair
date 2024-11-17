
// Import mqtt
const mqtt = require('mqtt');

// Connect to the Mosquitto broker (localhost in this case)

const client = mqtt.connect('mqtt://localhost:1883');

// Handle connection
function connectToTopic(topic){

client.on('connect', () => {
  console.log('Connected to Mosquitto broker');

  // Subscribe to a topic
  client.subscribe(topic , (err) => {
    if (err) {
      console.error('Failed to subscribe:', err);
    } else {
      console.log('Subscribed to test/topic');
    }
  });

})
}

function publishToTopic(topic){ 
    client.on('connect', () => {
    client.publish(topic , 'Hello from mqtt.js!');
  });
}

// Handle incoming messages
function handleIncomingMessage(topic){
  client.on('message', (topic, message) => {
    console.log(`Received message: ${message.toString()} on topic: ${topic}`);
    message = JSON.parse(message);
    return message;
  });
}

// Handle errors
client.on('error', (error) => {
  console.error('MQTT Error:', error);
});

module.exports = {
  client,
  connectToTopic,
  publishToTopic,
  handleIncomingMessage
}
