<template>
    <div class="screen-container">
        <BButton @click="logout"> Log Out button</BButton>

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
import { subscribeToTopic, client, messageArrived, unsubscribeFromTopic, publishMsgToTopic, publishValue } from '../mqtt/mqtt.js'

export default {
  name: 'MyBookingsPage',
  components: {
    DenstistAppointmentComponent,
    DentistCalendarComponent,
    TopBarComponent
  },
  methods: {
    logout() {
      const PUBLISH_LOGOUT_TOPIC = "logout"
      publishMsgToTopic(PUBLISH_LOGOUT_TOPIC, "User has logged out of the Teeth Repair System");
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
      this.$router.push('/login')
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
