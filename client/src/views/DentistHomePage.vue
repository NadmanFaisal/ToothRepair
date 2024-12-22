<template>
    <div class="screen-container">
        <BButton @click="logout"> Log Out button</BButton>

        <DentistTopBarComponent />

        <div class="col-12 content-section">

            <div class="col-3 left-section">

                <DentistCalendarComponent @dentistSelectedDate="updateSelectedDate" />

            </div>

            <div class="col-9 right-section">

                <DenstistAppointmentComponent :dentistSelectedDate="dentistSelectedDate" :appointments="appointments" :triggerGetAppointments="getAppointments"/>

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
      } catch (error) {
        console.error('Error during data fetch:', error)
      }
    }
    connectAndRun()
  },
  created() {
    this.$watch(
      () => this.$route,
      this.getAppointments
    )
  },
  methods: {
    async getAppointments() {
      try {
        await this.getClinicId()
        await subscribeToTopic('Client/ScheduleService/AppointmentInfo')
        publishToTopic('ScheduleService/Appointment/getAppointmentsByClinic', `{"userID": ${this.userId}, "clinic": "${this.clinicId}"}`)
        console.log('Entered')
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
            if (this.getAppointmentStatus) {
              this.appointments = [...parsedMessage.appointments]
            } else {
              if (JSON.parse(this.userId) === parsedMessage.userID) {
                this.appointments = [...parsedMessage.appointments]
              } else {
                console.log('Recieved another users request')
              }
            }
          }
        })
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    async getClinicId() {
      try {
        await subscribeToTopic('Client/AuthenticationService/ClinicId')
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
        console.log('localstorage: ' + localStorage.getItem('UserID'))
        console.log('userid:' + this.userId)
        publishToTopic('AuthenticationService/Dentist/GetClinicId', '{"id": ' + this.userId + '}')

        await clinicIdPromise
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    logout() {
      const PUBLISH_LOGOUT_TOPIC = "logout"
      const PUBLISH_LOGGED_OUT_USER_ID = "authenticationService/dentist/logout"
      publishToTopic(PUBLISH_LOGOUT_TOPIC, "User has logged out of the Teeth Repair System")
      publishToTopic(PUBLISH_LOGGED_OUT_USER_ID, JSON.parse(localStorage.getItem('UserID')))
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
      unsubscribeFromTopic('client/scheduleService/appointmentInfo')
      localStorage.clear()
      this.$router.push('/login')
    },
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
