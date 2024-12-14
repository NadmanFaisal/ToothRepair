// Import mqtt
import mqtt from 'mqtt'
let currentBrokerIndex = 0
const MAX_RETRIES = 3
// Connect to WebSocket version of MQTT since Web Browsers only do WebSocket for connections

const BROKER_URLS = [
  {
    host: 'test.mosquitto.org',
    port: 8081,
    protocol: 'wss',
  },
  {
    host: 'broker.hivemq.com',
    port: 8884,
    protocol: 'wss',
    path: '/mqtt'
  },
  {
    host: 'broker.emqx.io',
    port: 8084,
    protocol: 'wss',
    path: '/mqtt'

  }
]

export let client = mqtt.connect({
  host: BROKER_URLS[currentBrokerIndex].host,
  port: BROKER_URLS[currentBrokerIndex].port,
  protocol: BROKER_URLS[currentBrokerIndex].protocol,
  path: BROKER_URLS[currentBrokerIndex].path || '',
  reconnectPeriod: 2000,
  connectTimeout: 4000,
  will: {
    topic: 'status',
    payload: 'offline',
    qos: 1,
    retains: true
  }
})

client.on('connect', () => {
  const connectedHost = client.options.host
  const connectedPort = client.options.port

  currentBrokerIndex = BROKER_URLS.findIndex((broker) => {
    return broker.host === connectedHost && broker.port === Number(connectedPort)
  })
  if (currentBrokerIndex >= 0) {
    console.log(`Successfully connected to broker: ${BROKER_URLS[currentBrokerIndex].protocol}://${BROKER_URLS[currentBrokerIndex].host}:${BROKER_URLS[currentBrokerIndex].port}`)
  } else {
    console.warn('Connected to unknown broker!')
  }
})

client.on('close', () => {
  console.log('Lost connection to current broker, trying for reconnection...')
  handleReconnection()
})


function handleReconnection() {
  let retryCount = 0
  console.log('Connection lost, attempting to reconnect...')
  
  const reconnectInterval = setInterval(() => {
    if(client && client.connected) {
      clearInterval(reconnectInterval)
      console.log(`Successfully reconnected to broker: ${BROKER_URLS[currentBrokerIndex].protocol}://${BROKER_URLS[currentBrokerIndex].host}:${BROKER_URLS[currentBrokerIndex].port}`)
      retryCount = 0
      return
    } else if (retryCount < MAX_RETRIES) {
      console.log(`Reconnection attempt ${retryCount + 1} of ${MAX_RETRIES}`)
      retryCount++
      if (client) {
        client.removeAllListeners('connect')
        client.removeAllListeners('error')
        client.removeAllListeners('close')
        console.log('Event handlers cleaned up')
      }

      client.end(true, () => {
        setTimeout(() => {
          console.log(`Reconnecting to broker; ${BROKER_URLS[currentBrokerIndex].protocol}://${BROKER_URLS[currentBrokerIndex].host}:${BROKER_URLS[currentBrokerIndex].port}`)
          client.reconnect()

        }, 1000)
      })
    } else {
      console.log('Max retries reached, switching to another broker...')
      retryCount = 0
      if (client) {
        client.removeAllListeners('connect')
        client.removeAllListeners('error')
        client.removeAllListeners('close')
        console.log('Event handlers cleaned up')
      }
      currentBrokerIndex = (currentBrokerIndex + 1) % BROKER_URLS.length

      console.log(`Switching to another broker: ${BROKER_URLS[currentBrokerIndex]}`)

      client.end(true, () => {
        setTimeout(() => {
          console.log('Disconnected from current broker, reconnecting...')
          client = mqtt.connect(BROKER_URLS[currentBrokerIndex])

        }, 1000) 
      })
    }
  }, 2000)
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
  client.on('message', (topic, message) => {
    try {
      console.log('This is the JSON format of the message' + message)
      callback(topic, message.toString())
    } catch (error) {
      console.error('Error Parsing the message', error)
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

