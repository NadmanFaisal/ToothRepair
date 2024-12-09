// Import mqtt
import mqtt from 'mqtt'
const BROKER_URLS = [
  'wss://test.mosquitto.org:8081',
  'wss://broker.hivemq.com:8000/mqtt',
  'wss://broker.emqx.io:8084/mqtt'
]
let currentBrokerIndex = 0
const MAX_RETRIES = 3
// Connect to WebSocket version of MQTT since Web Browsers only do WebSocket for connections
export let client = await initializeClient()



// On connection to client,  print connected
client.on('connect', () => {
  console.log('Connected to broker: ', BROKER_URLS[currentBrokerIndex])
})

// If error occured, print error in console
client.on('error', (error) => {
  console.error('MQTT Error:', error)
})

client.on('close', () => {
  console.log('Connection to broker has been lost, triggering reconnection')
  client = handleReconnection()
})

function initializeClient()  {
  console.log("Trying to initialize client");
  
  return new Promise((resolve, reject) => {
  for (let i = 0; i < BROKER_URLS.length; i++) {
    console.log('Connecting to this broker: ' + BROKER_URLS[i])
    let middleware = mqtt.connect(BROKER_URLS[i])
    
    try {
        middleware.on('connect', () => {
          console.log('Successfully connected to broker: ' + BROKER_URLS[i])
          currentBrokerIndex = i
          resolve(middleware)
        })
    
        middleware.on('error', (error) => {
          reject(error)
        })
        
      } catch (error) {
        console.log('Error while connecting to broker: ', BROKER_URLS[currentBrokerIndex], error)
      }
      
    }
  })
}

function handleReconnection() {
  let retryCount = 0
  console.log('Connection lost, attempting to reconnect...')
  
  const reconnectInterval = setInterval(() => {
    if(client.connected) {
      clearInterval(reconnectInterval)
      console.log('Successfully reconnected to the broker!')
      retryCount = 0
    } else if (retryCount < MAX_RETRIES) {
      console.log(`Reconnection attempt ${retryCount} of ${MAX_RETRIES}`)
      client.end(true, () => {
        client.connect()
        retryCount++
      })
    } else {
      console.log('Max retries reached, switching to another broker...')
      retryCount = 0
      currentBrokerIndex = (currentBrokerIndex + 1) % BROKER_URLS.length

      console.log(`Switching to another broker: ${BROKER_URLS[currentBrokerIndex]}`)

      client.end(true, () => {
        client = mqtt.connect(BROKER_URLS[currentBrokerIndex])
      })
    }
  }, 2000)

  client.on('connect', () => {
    clearInterval(reconnectInterval)
    console.log('Successfully reconnected to broker: ', BROKER_URLS[currentBrokerIndex])
    retryCount = 0
  })

  client.on('error', (error) => {
    console.error('Error during reconnection', error)
  })
}

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
    client.publish(topic, message, { qos: 2, retain: false })
    console.log('Published the message: ' + message + 'to topic: ' + topic)
  }
}

