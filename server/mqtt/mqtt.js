
// Import mqtt
const mqtt = require('mqtt');

const client = mqtt.connect('mqtt://test.mosquitto.org:1883');

//Note: Separate the mqtt connection and connecting to a specific topic
// Handle connection
function connectToTopic(topic){
    client.on('connect', () => {
      console.log('Connected to Mosquitto broker');
  
      // Subscribe to a topic
      client.subscribe(topic , (err) => {
        if (err) {
          console.error('Failed to subscribe:', err);
        } else {
          console.log(`Subscribed to ${topic}`);
        }
      });
  
    });

    client.on('message', (topic, message) => {
      console.log(`Received message: ${message.toString()} on topic: ${topic}`);
      try{
        message = JSON.parse(message.toString());
        console.log("This is the JSON format of the patient list" + message)
        console.log(message);
      }catch(error){
        console.error('Error Parsing the Patient list', error)
      }
    });

    client.on('error', (error) => {
      console.error('MQTT Error:', error);
    });
}

function publishToTopic(topic){ 
    client.on('connect', () => {
    client.publish(topic , 'Hello from mqtt.js!');
  });
}

// Handle errors

module.exports = {
  client,
  connectToTopic,
  publishToTopic,
}
