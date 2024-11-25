// Import mqtt
import mqtt from 'mqtt'

// Connect to WebSocket version of MQTT since Web Browsers only do WebSocket for connections
export const client = mqtt.connect('wss://test.mosquitto.org:8081')

// On connection to client,  print connected
client.on('connect', () => {
  console.log('Connected to Mosquitto broker')
})

// If error occured, print error in console
client.on('error', (error) => {
  console.error('MQTT Error:', error)
})

/**
 * This function subscribes using the topic and throws error if client is not connected.
 *
 * @param {*} topic subscribes using the topic.
 * @returns {resolve, reject} resolves the Promise or rejects it
 */
export function subscribeToTopic(topic) {
  return new Promise((resolve, reject) => {
    // Subscribe to a topic
    if (client.connected) {
      client.subscribe(topic, (err) => {
        if (err) {
          console.error('Failed to subscribe:', err)
          reject(err)
        } else {
          console.log(`Subscribed to ${topic}`)
          resolve()
        }
      })
    } else {
      console.error('Client not connected. Cannot subscribe.')
      reject(new Error('Client not connected'))
    }
  }

  )
}

/**
 * This function handles recieved message by parsing it to JSON.
 *
 * @param {*} callback callback to keep checking for arrived messages.
 * @returns {message} returns the message recieved from the topic
 */
export function messageArrived(callback) {
  client.on('message', (topic, message) => {
    console.log(`Received message: ${message.toString()} on topic: ${topic}`)
    try {
      
      console.log('This is the JSON format of the patient list' + message)
      callback(topic, message)
    } catch (error) {
      console.error('Error Parsing the Patient list', error)
      callback(topic, message.toString())
    }
    return message
  })
}

/**
 * This function publishes an alert to the microservice so that the microservice can publish its values.
 *
 * @param {*} topic requires a topic to publish a value
 */
export function publishToTopic(topic) {
  if (client.connected) {
    console.log('Im trying to publish to the broker')
    client.publish(topic, 'Get Patients') // publishes Get Patients as a message to recieve all patients
    console.log('I have published get patients to the broker')
    }
}
export function publishValue(topic, payload){
    if(client.connected){
        console.log('Publishing '+ payload + "to " + topic);
        client.publish(topic, payload)
        console.log(payload+" Has been published to " + topic);
    }

}
