<template>
    <div class="screen-container">

        <TopBarComponent />

        <div class="col-12 content-section">

            <div class="col-3 left-section">

                <DentistCalendarComponent />

            </div>

            <div class="col-9 right-section">

                <DenstistAppointmentComponent />

            </div>

            </div>
        </div>
</template>

<script>
import TopBarComponent from '../components/TopBar.vue'
import DenstistAppointmentComponent from '../components/DentistHomePageComponents/DenstistAppointmentComponent.vue'
import DentistCalendarComponent from '../components/DentistHomePageComponents/DentistCalendarComponent.vue'

export default {
  name: 'MyBookingsPage',
  components: {
    DenstistAppointmentComponent,
    DentistCalendarComponent,
    TopBarComponent
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
