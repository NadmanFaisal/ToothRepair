
// Import mqtt
import mqtt from 'mqtt';

export const client = mqtt.connect('ws://test.mosquitto.org:8081');

//Note: Separate the mqtt connection and connecting to a specific topic
// Handle connection

client.on('connect', () => {
    console.log('Connected to Mosquitto broker');
});



client.on('error', (error) => {
  console.error('MQTT Error:', error);
});

export function subscribeToTopic(topic){
  return new Promise((resolve, reject) => {
    // Subscribe to a topic
    if(client.connected){

        client.subscribe(topic , (err) => {
        if (err) {
            console.error('Failed to subscribe:', err);
            reject(err);
        } else {
            console.log(`Subscribed to ${topic}`);
            resolve();
        }
        });
    }else{
        console.error('Client not connected. Cannot subscribe.');
        reject(new Error('Client not connected'));
    }
  }
  
  )
}

export function messageArrived (callback){
    client.on('message', (topic, message) => {
        console.log(`Received message: ${message.toString()} on topic: ${topic}`);
        try{
          const parsedMessage = JSON.parse(message.toString());
          console.log("This is the JSON format of the patient list" + message)
          callback(topic, parsedMessage)
        }catch(error){
          console.error('Error Parsing the Patient list', error)
          callback(topic, message.toString());
        }
        return message;
    });
}

export function publishToTopic(topic){
  if(client.connected){
    console.log("Im trying to publish to the broker");
    client.publish(topic , "Get Patients");
    console.log("I have published get patients to the broker");
  }
}



