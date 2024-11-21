
// Import mqtt
const mqtt = require('mqtt');

const client = mqtt.connect('mqtt://test.mosquitto.org:1883');

//Note: Separate the mqtt connection and connecting to a specific topic
// Handle connection
client.on('connect', () => {
  console.log('Connected to Mosquitto broker');
});

client.on('error', (error) => {
  console.error('MQTT Error:', error);
});

function connectToTopic(topic){
  return new Promise((resolve, reject) => {
    // Subscribe to a topic
    client.subscribe(topic , (err) => {
      if (err) {
        console.error('Failed to subscribe:', err);
        return reject(err)
      } else {
        console.log(`Subscribed to ${topic}`);
      }
    });
    
    client.on('message', (topic, message) => {
      console.log(`Received message: ${message.toString()} on topic: ${topic}`);
      try{
        
        if(topic === "test/patientList"){
        message = JSON.parse(message.toString());
        console.log("This is the JSON format of the patient list" + message)
        resolve(message)
        }else{
        resolve(message.toString());
        }
      }catch(error){
        console.error('Error Parsing the Patient list', error)
        reject(error)
      }
    });
  
  })
}

function publishToTopic(topic){
  console.log("Im trying to publish to the broker");
  client.publish(topic , "Get Patients");
  console.log("I have published get patients to the broker");
}

// Handle errors

module.exports = {
  client,
  connectToTopic,
  publishToTopic,
}
