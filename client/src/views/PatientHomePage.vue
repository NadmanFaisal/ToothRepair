<template>
    <div class="col-12 screen-container">
      <BButton @click="logout"> Log Out button</BButton>

        <TopBarComponent />

        <div class="col-12 content-section">

            <div class="col-3 left-section">

                <CalendarComponent />

                <div class="mapCompContainer">
                    <p>This is the Leaflet map</p>
                    <MapComponent :clinics="clinics"></MapComponent>
                </div>

            </div>

            <div class="col-9 right-section">

                <AppointmentComponent />

            </div>

        </div>
    </div>
</template>

<script>
import { subscribeToTopic, client, messageArrived, unsubscribeFromTopic, publishMsgToTopic } from '../mqtt/mqtt.js'

import TopBarComponent from '../components/TopBar.vue'
import CalendarComponent from '../components/PatientHomePageComponents/CalendarComponent.vue'
import MapComponent from '../components/MapComponent.vue'
import AppointmentComponent from '../components/PatientHomePageComponents/AppointmentComponent.vue'

export default {
  name: 'PatientHomePage',
  components: {
    TopBarComponent,
    CalendarComponent,
    MapComponent,
    AppointmentComponent
  },
  data() {
    return {
      clinics: []
    }
  },
  mounted() {
    client.on('connect', () => {
      this.getAllClinics()
    })
  },
  methods: {
    async getAllClinics() {
      try {
        await subscribeToTopic('test/clinicList')
        await subscribeToTopic('authentication/dentist/getDentistNames')
        publishMsgToTopic('test/clinicAlert', 'Get Clinics')

        messageArrived((topic, message) => {
          if (topic === 'test/clinicList') {
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
              publishMsgToTopic('authentication/dentist/getDentistNamesAlert', JSON.stringify(allDentistIds))
            }

            unsubscribeFromTopic('test/clinicList')
          } else if (topic === 'authentication/dentist/getDentistNames') {
            const fixedMessage = '[' + message + ']'
            const newMessage = JSON.parse(fixedMessage)
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
            }
          }
        })
      } catch (error) {
        console.error('Tried to retrieve all clinics: ', error)
      }
    },
    logout() {
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
      this.$router.push('/login')
    }
  },
  created() {
    this.$watch(
      () => this.$route,
      this.getAllClinics,
      { immediate: true }
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

</style>
