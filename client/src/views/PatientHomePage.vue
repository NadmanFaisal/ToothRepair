<template>

    <div class="screen-container">

      <PatientTopBar />

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

            <div class="col-12 email-continer">
              <label class="email-label">Email</label>
              <input class="form-control email-input">
            </div>

            <div class="col-12 mobile-container">
              <label class="mobile-label">Mobile</label>
              <input class="form-control mobile-input">
            </div>

            <div class="dropdown clinic-container">

              <label class="clinic-label">Clinic</label>
              <button
                class="btn btn-secondary dropdown-toggle clinic-dropdown-button"
                type="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
                text="Select a clinic"
                >
                {{ selectedClinicName || 'Select a clinic' }}
              </button>

              <ul class="dropdown-menu">
                <li class="dropdown-item" v-for="clinic in clinics" :key="clinic.id" @click="selectAClinic(clinic)">{{ clinic.name }}</li>
              </ul>

            </div>

            <div class="col-12 button-container">
              <button class="col-8 btn show-slot-button" @click="gotToAppointmentPage">Show Slots</button>
            </div>

          </div>

        </div>
      </div>

    </div>

</template>

<script>
import PatientTopBar from '../components/PatientHomePageComponents/PatientTopBarComponent.vue'
import { subscribeToTopic, publishValue, messageArrived, unsubscribeFromTopic, client } from '../mqtt/mqtt.js'
import { store } from '../store'

export default {
  name: 'PatientHomePage',
  components: {
    PatientTopBar
  },
  data() {
    return {
      clinics: [],
      selectedClinicName: null
    }
  },
  mounted() {
    client.on('connect', () => {
      this.getAllClinics()
    })
  },
  methods: {
    selectAClinic(clinic) {
      store.setSelectedClinic(clinic)
      this.selectedClinicName = store.getSelectedClinicName()
    },
    gotToAppointmentPage() {
      if (!store.getSelectedClinicId()) {
        alert('No clinic has been selected. Please select a clinic')
        return
      }
      this.$router.push({
        path: '/patientAppointmentPage',
        query: {
          clinicId: store.getSelectedClinicId()
        }
      })
    },
    async getAllClinics() {
      const SUBCRIBED_CLINIC_TOPIC = 'clinicService/clinicList'
      const PUBLISHED_CLINIC_TOPIC = 'dentist/clinicService/alert'
      const publishMessage = 'Get Clinics'
      await subscribeToTopic(SUBCRIBED_CLINIC_TOPIC)
      publishValue(PUBLISHED_CLINIC_TOPIC, publishMessage)
      messageArrived((topic, message) => {
        if (topic === SUBCRIBED_CLINIC_TOPIC) {
          console.log('Received clinics list:', message)
          this.clinics = JSON.parse(message)
          unsubscribeFromTopic(SUBCRIBED_CLINIC_TOPIC)
        }
      })
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
  padding: 4.5%;
  justify-content: end;
}

.book-appointment-container {
  padding: 8%;
  display: flex;
  flex-direction: column;
  height: 85%;
  background-color: #FFF;
}

.email-continer, .mobile-container, .clinic-container {
  height: 20%;
  display: flex;
  flex-direction: column;
}

.book-appointment-label {
  color: #515151;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.email-label, .mobile-label, .clinic-label {
  color: #515151;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
  text-align: start;
}

.clinic-dropdown-button {
  border-radius: 15px;
  border: 1px solid #D2D1D1;
  background: #FFF;
  width: 100%;

  color: #BBB9B9;
  text-align: left;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.dropdown-item {
  text-align: left;
  font-family: Inter;
  font-size: 15px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.button-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 20%;
}

.show-slot-button {
  border-radius: 15px;
  background: #1FC2C2 !important;
  box-shadow: 0px 4px 4px 0px rgba(31, 194, 194, 0.70);

  color: #FFF !important;
  text-align: center;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}
</style>
