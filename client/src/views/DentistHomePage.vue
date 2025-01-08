<template>
    <div class="screen-container">

        <DentistTopBarComponent :dentistUsername="dentistUsername" />

        <div class="col-12 content-section">

            <div class="col-3 left-section">

                <DentistCalendarComponent @dentistSelectedDate="updateSelectedDate" />

            </div>

            <div class="col-9 right-section">

                <DenstistAppointmentComponent :dentistSelectedDate="dentistSelectedDate" :appointments="appointments" :clinicId="clinicId"/>

            </div>

            </div>
        </div>
</template>

<script>
import DentistTopBarComponent from '../components/DentistComponents/DentistTopBarComponent.vue'
import DenstistAppointmentComponent from '../components/DentistComponents/DenstistAppointmentComponent.vue'
import DentistCalendarComponent from '../components/DentistComponents/DentistCalendarComponent.vue'
import { subscribeToTopic, messageArrived, publishToTopic, client, unsubscribeFromTopic } from '../mqtt/mqtt.js'

export default {
  name: 'MyBookingsPage',
  data() {
    return {
      dentistSelectedDate: new Date().toISOString().split('T')[0],
      clinicId: null,
      appointments: [],
      dentistUsername: 'Loading...',
      userId: localStorage.getItem('UserID'),
      getAppointmentStatus: null
    }
  },
  components: {
    DenstistAppointmentComponent,
    DentistCalendarComponent,
    DentistTopBarComponent
  },
  mounted() {
    const connectAndRun = async () => {
      if (!client.connected) {
        console.log('Waiting for MQTT connection...')
        setTimeout(connectAndRun, 500)
        return
      }
      console.log('MQTT connected, fetching data...')
      try {
        await this.getAppointments()
        await this.getDentistName()
        publishToTopic('scheduleService/appointment/pendingAppointments', '{"id": "null", "dentist": ' + this.userId + ', "clinic": ' + JSON.stringify(this.clinicId) + '}')
      } catch (error) {
        console.error('Error during data fetch:', error)
      }
    }
    connectAndRun()
  },
  unmounted() {
    client.removeAllListeners('message')
    console.log('This page is Unmounted')
  },
  methods: {
    async getAppointments() {
      try {
        // Clinic ID required for getAppointments to work
        await this.getClinicId()
        await subscribeToTopic('client/scheduleService/getAppointmentStatus')
        // Topic which receives appointments list
        await subscribeToTopic('Client/ScheduleService/AppointmentInfo')
        // Sends the clinicID and userId to receive appointment list for that specific clinic and to filter which user recieves it
        publishToTopic('ScheduleService/Appointment/getAppointmentsByClinic', `{"userID": ${this.userId}, "clinic": "${this.clinicId}"}`)
        messageArrived((topic, message) => {
          console.log(topic)
          if (topic === 'client/scheduleService/getAppointmentStatus') {
            this.getAppointmentStatus = message
          }
          if (topic === 'Client/ScheduleService/AppointmentInfo') {
            console.log('recieved: ' + message)
            const parsedMessage = JSON.parse(message)
            console.log('What happens when parsing' + parsedMessage)
            console.log('userid = ' + parsedMessage.userID)
            console.log('localstorage = ' + this.userId)
            console.log('GET APPOINTMENT STATUS: ' + this.getAppointmentStatus)
            if (JSON.parse(this.userId) === parsedMessage.userID) {
              this.appointments = parsedMessage.appointments
            } else {
              console.log('Received another users request')
              if (this.getAppointmentStatus === null) {
                console.log('Someone refreshed or changed clinic')
              } else {
                console.log('cancel, book, pending or available')
                this.getAppointments()
              }
            }
            this.getAppointmentStatus = null
          }
        })
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    async getDentistName() {
      try {
        // Topic to receive dentist name
        await subscribeToTopic('authenticationService/dentist/dentistName')
        // Sends used ID as message to receive the name of user with that specific user id
        publishToTopic('authenticationService/dentist/getDentistName', JSON.parse(localStorage.getItem('UserID')))

        messageArrived((topic, message) => {
          if (topic === 'authenticationService/dentist/dentistName') {
            console.log('Recieved dentist name: ', message)
            // Sets the variable for top bar to show username
            this.dentistUsername = message
            // Sets the local storage for top bar to show username when there are route changes for TOPBAR
            localStorage.setItem('Username', message)

            unsubscribeFromTopic('authenticationService/dentist/dentistName')
          }
        })
      } catch (error) {
        console.error('Tried to retrieve dentist name: ', error)
      }
    },
    async getClinicId() {
      try {
        // User ID to be used for getting clinicID for that specific dentist
        const userId = localStorage.getItem('UserID')
        // Topic to receive the clinicID
        await subscribeToTopic('Client/AuthenticationService/ClinicId')
        // Promise to wait for setting up messageArrived
        const clinicIdPromise = new Promise((resolve, reject) => {
          messageArrived((topic, message) => {
            if (topic === 'Client/AuthenticationService/ClinicId') {
              console.log('Received Clinic ID:', message)

              // Check if the receiving message is already a JSON string, if not, parse it
              const parsedMessage = typeof message === 'string' ? JSON.parse(message) : message
              this.clinicId = parsedMessage
              console.log('This is the clinic Id: ' + this.clinicId)
              resolve()
            }
          })
        })
        // Sends userID to receive clinicID for that specific dentist
        console.log('localstorage: ' + localStorage.getItem('UserID'))
        console.log('userid:' + this.userId)
        publishToTopic('AuthenticationService/Dentist/GetClinicId', '{"id": ' + this.userId + '}')

        await clinicIdPromise
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    // Variable updated to send information to child component through prop feature
    updateSelectedDate(date) {
      console.log('Parent received selectedDate from child:', date)
      this.dentistSelectedDate = date
    }
  }
}
</script>

<style scoped>
.screen-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.content-section {
  display: flex;
  flex-direction: row;
  height: 93%;
  background-image: linear-gradient(0deg, rgba(31, 194, 194, 0.24) 0%, rgba(31, 194, 194, 0.24) 100%), url('@/assets/patient-home-bg-image.jpeg');
  background-size: contain;
}

@media (max-width: 1260px) {
  .content-section {
    flex-direction: column;
    background: rgba(31, 194, 194, 0.24);
    height: 150vh;
    overflow-y: auto;
  }

  .left-section {
    display: flex;
    flex-direction: row;
    width: 100%;
    height: 50%;
  }

  .right-section {
    width: 100%;
    height: 50%;
  }

}

@media (max-width: 800px) {
  .content-section {
    flex-direction: column;
    background: rgba(31, 194, 194, 0.24);
    height: 200vh;
    overflow-y: auto;
  }

  .left-section {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 50%;
  }

  .right-section {
    width: 100%;
    height: 50%;
  }

}
</style>
