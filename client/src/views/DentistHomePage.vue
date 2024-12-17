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
      appointments: []
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
        console.log('Entered')
        messageArrived((topic, message) => {
          if (topic === 'Client/ScheduleService/AppointmentInfo') {
            // Check if the receiving message is already a JSON string, if not, parse it
            const parsedMessage = typeof message === 'string' ? JSON.parse(message) : message
            // Using shallow copy allows Vue to detect changes in this.appointments and helps reactivity
            this.appointments = [...parsedMessage]
          }
        })
        await subscribeToTopic('Client/ScheduleService/AppointmentInfo')
        publishToTopic('ScheduleService/Appointment/getAppointmentsByClinic', '{"clinic": "' + this.clinicId + '"}')
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    async getClinicId() {
      try {
        const userId = localStorage.getItem('UserID')
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
        console.log('userid:' + userId)
        publishToTopic('AuthenticationService/Dentist/GetClinicId', '{"id": ' + userId + '}')

        await clinicIdPromise
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    logout() {
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
