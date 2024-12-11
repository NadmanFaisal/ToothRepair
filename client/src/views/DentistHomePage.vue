<template>
    <div class="screen-container">
        <BButton @click="logout"> Log Out button</BButton>

        <DentistTopBarComponent />

        <div class="col-12 content-section">

            <div class="col-3 left-section">

                <DentistCalendarComponent @dentistSelectedDate="updateSelectedDate" />

            </div>

            <div class="col-9 right-section">

                <DenstistAppointmentComponent :dentistSelectedDate="dentistSelectedDate"/>

            </div>

            </div>
        </div>
</template>

<script>
import DentistTopBarComponent from '../components/DentistComponents/DentistTopBarComponent.vue'
import DenstistAppointmentComponent from '../components/DentistComponents/DenstistAppointmentComponent.vue'
import DentistCalendarComponent from '../components/DentistComponents/DentistCalendarComponent.vue'

export default {
  name: 'MyBookingsPage',
  data() {
    return {
      dentistSelectedDate: new Date().toISOString().split('T')[0]
    }
  },
  components: {
    DenstistAppointmentComponent,
    DentistCalendarComponent,
    DentistTopBarComponent
  },
  methods: {
    logout() {
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
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
</style>
