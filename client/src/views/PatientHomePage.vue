<template>
    <div class="row">

        <TopBarComponent />

        <div class="col-12 content-section">

            <div class="col-6 left-section">

                <div class="col-6 calender-contaiener">
                    container 1
                </div>

                <div class="col-6 map-container">
                    container 2
                </div>

            </div>

            <div class="col-6 right-section">

                <div class="col-6 appointment-container">
                    container 3
                </div>

            </div>

        </div>
    </div>
</template>

<script>

import TopBarComponent from '../components/TopBar.vue'

export default {
  name: 'PatientHomePage',
  components: {
    TopBarComponent
  }
}
</script>

<style scoped>

.row {
    display: flex;
    flex-direction: column;
    height: 100vh;
}

.content-section {
    display: flex;
    flex-direction: row;
    height: 93%;
    background-image: linear-gradient(rgba(62, 180, 137, 0.5), rgba(62, 180, 137, 0.5)), url('@/assets/patient-home-bg-image.jpeg');
    background-size: cover;
}

.calender-contaiener {
    display: flex;
    flex-direction: column;
    height: 50%;
    width: 100%;
}

.map-container {
    display: flex;
    flex-direction: column;
    height: 50%;
    width: 100%
}

.appointment-container {
    display: flex;
    flex-direction: column;
    height: 50%;
    width: 100%
}
</style>
