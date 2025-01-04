<template>

    <div class="screen-container">

      <PatientTopBar :patientUsername="patientUsername"/>

      <div class="col-9 content-section">
        <div class="col-7 left-section">

          <div class="col-12 description-contaienr">

            <label class="description-title-label">The Complete Dental Experience</label>

            <label class="description-content-label">At our practice, we believe that every smile tells a story,
              and we're here to help yours shine its brightest.
              With a commitment to gentle care and the latest in dental technology,
              we make your comfort our top priority.
            </label>

          </div>

          <div class="col-6 dental-treatment-container">

            <div class="col-12 dental-treatment-content">
              <img src="../assets/hospital.png" class="hospital-image">
              <label class="dental-treatment-label-header">Dental Treatment</label>
              <label class="dental-treatment-label-paragraph">Dental treatment encompasses a variety of procedures aimed at maintaining oral health</label>
            </div>

          </div>

          <div class="col-6 dental-cosmetic-container">

            <div class="col-12 dental-cosmetic-content">
              <img src="../assets/teeth.png" class="teeth-image">
              <label class="dental-cosmetic-label-header">Dentistry Cosmetic</label>
              <label class="dental-cosmetic-label-paragraph">Cosmetic dentistry focuses on improving the appearance of teeth, gums, and smiles.</label>
            </div>

          </div>

        </div>

        <div class="col-5 right-section">

          <div class="col-12 book-appointment-container">
            <label class="book-appointment-label">Book Appointment</label>
            <hr>

            <div class="col-12 map-description-container">
              <label class="map-description-header">Select Your Clinic</label>
              <label class="map-description-label">From hundreds of clinics all around Sweden with world class dentists, select your prefered clinic from the map</label>
            </div>

            <div class="col-12 select-map-container">
              <PatientMapComponent :clinics="clinics"/>
            </div>

          </div>

        </div>
      </div>

    </div>

</template>

<script>
import PatientTopBar from '../components/PatientComponents/PatientTopBarComponent.vue'
import { subscribeToTopic, publishToTopic, messageArrived, unsubscribeFromTopic, client } from '../mqtt/mqtt.js'
import PatientMapComponent from '../components/PatientComponents/PatientMapComponent.vue'

export default {
  name: 'PatientHomePage',
  components: {
    PatientTopBar,
    PatientMapComponent
  },
  data() {
    return {
      clinics: [],
      selectedClinicName: null,
      patientUsername: 'Loading...'
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
        await this.getPatientName()
      } catch (error) {
        console.error('Error during data fetch:', error)
      }
    }
    connectAndRun()
  },
  unmounted() {
    client.removeAllListeners('message')
    client.removeAllListeners('connect')
    console.log('This page is Unmounted')
  },
  methods: {
    selectAClinic(clinic) {
      this.selectedClinicName = clinic.name
      this.selectedClinicId = clinic.id
      localStorage.setItem('ClinicID', this.selectedClinicId)
      console.log(this.selectedClinicId)
    },
    // Navigation to appointmentPage not allowed without selecting clinic
    gotToAppointmentPage() {
      if (!localStorage.getItem('ClinicID')) {
        alert('No clinic has been selected. Please select a clinic')
        return
      }
      this.$router.push({
        path: '/patientAppointmentPage',
        query: {
          clinicId: this.selectedClinicId
        }
      })
    },
    // Patient name fetched to send to TOPBAR
    async getPatientName() {
      try {
        await subscribeToTopic('authenticationService/patient/patientName')
        publishToTopic('authenticationService/patient/getPatientName', JSON.parse(localStorage.getItem('UserID')))

        messageArrived((topic, message) => {
          if (topic === 'authenticationService/patient/patientName') {
            console.log('Recieved patient name: ', message)
            // Variable set if the message arrives
            this.patientUsername = message
            // Local storage set if the message arrives
            localStorage.setItem('Username', message)

            unsubscribeFromTopic('authenticationService/patient/patientName')
          }
        })
      } catch (error) {
        console.error('Tried to retrieve patient name: ', error)
      }
    },
    // All clinics fetched for displaying clinic in the map
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
            // Dentist names fetched to show dentists in the individual clinics inside the map
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
              })
              unsubscribeFromTopic('authenticationService/dentist/getDentistNames')
            }
          }
        })
      } catch (error) {
        console.error('Tried to retrieve all clinics: ', error)
      }
    }
  },
  created() {
    this.$watch(
      () => this.$route,
      () => {
        this.getAllClinics()
        this.getPatientName()
      },
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
  align-items: center;
  background-image: linear-gradient(0deg, rgba(31, 194, 194, 0.24) 0%, rgba(31, 194, 194, 0.24) 100%), url('@/assets/patient-home-bg-image.jpeg');
  background-size: contain;
}

.content-section {
  display: flex;
  flex-direction: row;
  height: 93%;
}

.left-section {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  height: 100%;
}

.description-contaienr {
  display: flex;
  flex-direction: column;
  height: 50%;
  justify-content: flex-end;
}

.description-title-label {
  color: #FFF;
  font-family: Inter;
  font-size: 64px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
  text-align: left
}

.description-content-label {
  color: #FFF;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 300;
  line-height: normal;
  text-align: left
}

.dental-treatment-container, .dental-cosmetic-container {
  padding: 8%;
  display: flex;
  flex-direction: column;
  height: 50%;
}

.dental-treatment-content, .dental-cosmetic-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #FFF;
  padding: 10px;
  box-shadow: 0px 4px 4px 0px rgba(0, 0, 0, 0.25);
}

.hospital-image, .teeth-image {
  margin-top: 10%;
  align-self: center;
  height: 35%;
  width: 35%;
}

.dental-cosmetic-label-header, .dental-treatment-label-header {
  color: #015C5C;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
  text-align: start;
}

.dental-treatment-label-paragraph, .dental-cosmetic-label-paragraph {
  color: #015C5C;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 200;
  line-height: normal;
  text-align: start;
}

.right-section {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 5%;
  justify-content: end;
}

.book-appointment-container {
  padding: 8%;
  display: flex;
  flex-direction: column;
  height: 85%;
  background-color: #FFF;
}

.map-description-container {
  display: flex;
  flex-direction: column;
  text-align: start;
  height: 25%;
}

.select-map-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60%;
}

.book-appointment-label {
  color: #515151;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.map-description-header {
  color: #015C5C;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
  text-align: start;
}

.map-description-label {
  padding-top: 5px;
  color: #015C5C;
  font-family: Inter;
  font-size: 19px;
  font-style: normal;
  font-weight: 200;
  line-height: normal;
  text-align: start;
}

@media (max-width: 1380px) {
  .map-description-label {
    padding-top: 5px;
    font-size: 17px;
  }
}

@media (max-width: 1200px) {
  .map-description-header {
    font-size: 22px;
  }

  .map-description-label {
    font-size: 16px;
  }
}

@media (max-width: 1000px) {
  .description-title-label {
    font-size: 54px;
  }

  .description-content-label {
    font-size: 20px;
  }

  .map-description-header {
    font-size: 20px;
  }

  .map-description-label {
    font-size: 15px;
  }
}

@media (max-width: 930px) {
  .map-description-header {
    font-size: 18px;
  }
}

@media (max-width: 842px) {
  .description-title-label {
    font-size: 45px;
  }

  .dental-cosmetic-label-header, .dental-treatment-label-header {
    font-size: 20px;
  }

  .hospital-image, .teeth-image {
    height: 20%;
    width: 50%;
  }

  .dental-treatment-label-paragraph, .dental-cosmetic-label-paragraph {
    font-size: 17px;
  }
}

@media (max-width: 780px) {
  .left-section {
    display: none;
  }

  .right-section {
    width: 100%;
  }

  .map-description-header {
    font-size: 24px;
  }

  .map-description-label {
    font-size: 18px;
  }
}

@media (max-width: 380px) {
  .map-description-header {
    font-size: 18px;
  }

  .map-description-label {
    font-size: 15px;
  }

}
</style>
