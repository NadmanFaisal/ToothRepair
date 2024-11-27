<template>
    <div class="col-12 screen-container">

        <TopBarComponent />

        <div class="col-12 content-section">

            <div class="col-3 left-section">

                <CalendarComponent />

                <MapComponent />

            </div>

            <div class="col-9 right-section">

                <AppointmentComponent />

            </div>

        </div>
    </div>
</template>

<script>

import TopBarComponent from '../components/TopBar.vue'
import CalendarComponent from '../components/PatientHomePageComponents/CalendarComponent.vue'
import MapComponent from '../components/PatientHomePageComponents/MapComponent.vue'
import AppointmentComponent from '../components/PatientHomePageComponents/AppointmentComponent.vue'

export default {
  name: 'PatientHomePage',
  components: {
    TopBarComponent,
    CalendarComponent,
    MapComponent,
    AppointmentComponent
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
