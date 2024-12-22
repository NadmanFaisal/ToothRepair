<template>
  <div class="col-12 screen-container">
    <BButton @click="logout"> Log Out button</BButton>

      <TopBarComponent />

      <div class="col-12 content-section">

          <div class="col-3 left-section">

            <CalendarComponent @patientSelectedDate="updateSelectedDate"/>

            <MapComponent :clinics="clinics"></MapComponent>

          </div>

          <div class="col-9 right-section">

                <AppointmentComponent :patientSelectedDate="patientSelectedDate" :appointments="appointments" :triggerGetAppointments="getAppointments"/>

          </div>

      </div>
  </div>
</template>

<script>
import { subscribeToTopic, client, messageArrived, unsubscribeFromTopic, publishToTopic } from '../mqtt/mqtt.js'
import { store } from '../store'

import TopBarComponent from '../components/PatientComponents/PatientTopBarComponent.vue'
import CalendarComponent from '../components/PatientComponents/PatientCalendarComponent.vue'
import MapComponent from '../components/PatientComponents/PatientMapComponent.vue'
import AppointmentComponent from '../components/PatientComponents/PatientAppointmentComponent.vue'

export default {
  name: 'PatientAppointmentPage',
  components: {
    TopBarComponent,
    CalendarComponent,
    MapComponent,
    AppointmentComponent
  },
  data() {
    return {
      clinics: [],
      patientSelectedDate: new Date().toISOString().split('T')[0],
      appointments: [],
      userId: localStorage.getItem('UserID')
    }
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
        await this.getAllClinics()
        await this.getAppointments()
      } catch (error) {
        console.error('Error during data fetch:', error)
      }
    }
    connectAndRun()
  },
  methods: {
    async getAppointments() {
      try {
        console.log('Subscribing to topic...')
        console.log('Publishing request for appointments...')
        await subscribeToTopic('Client/ScheduleService/AppointmentInfo')
        publishToTopic('ScheduleService/Appointment/getAppointmentsByClinic', `{"userID": ${this.userId}, "clinic": "${this.$route.query.clinicId}"}`)
        console.log('Subscribed successfully')

        console.log('Setting up message listener...')
        messageArrived((topic, message) => {
          console.log(topic)
          if (topic === 'Client/ScheduleService/AppointmentInfo') {
            console.log('recieved: ' + message)
            const parsedMessage = JSON.parse(message)
            console.log('What happens when parsing' + parsedMessage)
            console.log('userid = ' + parsedMessage.userID)
            console.log('localstorage = ' + this.userId)
            if (JSON.parse(this.userId) === parsedMessage.userID) {
              this.appointments = [...parsedMessage.appointments]
            } else {
              console.log('Recieved another users request')
            }
          }
        })
        console.log(`This should be working: ${this.appointments}`)
      } catch (error) {
        console.error('Error in getAppointments:', error)
      }
    },
    updateSelectedDate(date) {
      console.log('Parent received selectedDate from child:', date)
      this.patientSelectedDate = date
    },
    async getAllClinics() {
      try {
        await subscribeToTopic('clinicService/clinics/getClinicList')
        await subscribeToTopic('authenticationService/dentist/getDentistNames')
        publishToTopic('clinicService/clinic/getClinicAlert', 'Get Clinics')

        messageArrived((topic, message) => {
          if (topic === 'clinicService/clinics/getClinicList') {
            console.log('Recieved clinic list: ', message)
            this.clinics = JSON.parse(message)
            if (this.clinics) {
              this.clinics.forEach(clinic => {
                clinic.dentists = clinic.dentists.map(dentistId => ({
                  dentistId,
                  dentistName: ''
                }))
              })
              const allDentistIds = this.clinics.flatMap(clinic => clinic.dentists.map(d => d.dentistId))
              console.log('dentistIds: ', allDentistIds)
              publishToTopic('authenticationService/dentist/getDentistNamesAlert', JSON.stringify(allDentistIds))
            }

            unsubscribeFromTopic('clinicService/clinics/getClinicList')
          } else if (topic === 'authenticationService/dentist/getDentistNames') {
            const newMessage = JSON.parse(message)
            console.log('fixed message: ', newMessage)
            if (message) {
              newMessage.forEach(dentistData => {
                this.clinics.forEach(clinic => {
                  clinic.dentists.forEach(dentist => {
                    if (dentist.dentistId === dentistData.id) {
                      dentist.dentistName = dentistData.name
                      console.log('Dentist id: ' + dentist.dentistId + 'Dentist name: ' + dentist.dentistName)
                    }
                  })
                })
                console.log('Here are all the clinics', this.clinics)
              })
              unsubscribeFromTopic('authenticationService/dentist/getDentistNames')
            }
          }
        })
      } catch (error) {
        console.error('Tried to retrieve all clinics: ', error)
      }
      return new Promise((resolve) => {
        setTimeout(() => {
          console.log('Clinic ID fetched')
          resolve()
        }, 500)
      })
    },
    logout() {
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
      this.$router.push('/login')
      store.reset()
      localStorage.clear()
    }
  },
  created() {
    this.$watch(
      () => this.$route,
      this.getAppointments,
      this.getAllClinics
    )
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
