<template>
  <div class="screen-container">

    <PatientTopBarComponent />

    <div class="main-content-section">
      <BButton class="logout-button" @click="logout">Log out button</BButton>
    </div>
  </div>
</template>

<script>
import { store } from '../store'
import { publishToTopic, unsubscribeFromTopic } from '../mqtt/mqtt.js'
import PatientTopBarComponent from '../components/PatientComponents/PatientTopBarComponent.vue'

export default {
  data() {
    return {
      name: 'PatientSettingsPage'
    }
  },
  components: {
    PatientTopBarComponent
  },
  methods: {
    logout() {
      const PUBLISH_LOGOUT_TOPIC = 'logout'
      const PUBLISH_LOGGED_OUT_USER_ID = 'authenticationService/patient/logout'
      publishToTopic(PUBLISH_LOGOUT_TOPIC, 'User has logged out of the Teeth Repair System')
      publishToTopic(PUBLISH_LOGGED_OUT_USER_ID, JSON.parse(localStorage.getItem('UserID')))
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
      unsubscribeFromTopic('Client/ScheduleService/AppointmentInfo')
      store.reset()
      localStorage.clear()
      this.$router.push('/login')
    }
  }
}

</script>

<style>
</style>
