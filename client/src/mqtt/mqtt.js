// Import mqtt
import mqtt from 'mqtt'

// Connect to WebSocket version of MQTT since Web Browsers only do WebSocket for connections
export const client = mqtt.connect('wss://broker.hivemq.com:8884/mqtt')

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

export function unsubscribeFromTopic(topic) {
  try {
    client.unsubscribe(topic, (err) => {
      if (err) {
        console.error(`Failed to unsubscribe from topic ${topic}:`, err)
      } else {
        console.log(`Successfully unsubscribed from topic: ${topic}`)
      }
    })
  } catch (error) {
    console.error(`Error while unsubscribing from topic ${topic}:`, error)
  }
}

/**
 * This function handles recieved message by parsing it to JSON.
 *
 * @param {*} callback callback to keep checking for arrived messages.
 * @returns {message} returns the message recieved from the topic
 */
export function messageArrived(callback) {
  client.removeAllListeners('message')
  client.on('message', (topic, message) => {
    try {
      console.log('This is the JSON format of the patient list' + message)
      callback(topic, message.toString())
    } catch (error) {
      console.error('Error Parsing the Patient list', error)
      callback(topic, message.toString())
    }
    return message
  })
}

/**
 * This function sends a message to the topic which is provided
 *
 * @param {*} topic specifies the topic that the message is being sent to
 * @param {*} message the message that is sent through the topic
 */
export function publishToTopic(topic, message) {
  if (client.connected) {
    console.log('Im trying to publish to the broker')
    client.publish(topic, message, { qos: 2, retain: false }) // publishes Get Appointments as a message to recieve all patients
    console.log(`I have published to ${message} to ${topic}`)
  } else {
    console.log('Client did not connect')
  }
  /* this topic is used pls change it
  } else if (client.connected && topic === 'test/clinicAlert') {
    console.log('Im trying to publish to the "test/clinicAlert" topic')
    client.publish(topic, 'Get Clinics') // publishes Get Patients as a message to recieve all patients
    console.log('I have published "Get Clinics" to the "test/clinicAlert" topic')

    Nadman this is only used in the hello world once we get rid of it we should also get rid of this one
  } else if (client.connected && topic === 'test/patientAlert') {
    console.log('Im trying to publish to the broker')
    client.publish(topic, 'Get Patients') // publishes Get Patients as a message to recieve all patients
    console.log('I have published get patients to the broker')
  }
  Files to update topics: MapView, PatientAppointmentPage, LogInPage, PatientHomePage, SignUpPage
  */
}
