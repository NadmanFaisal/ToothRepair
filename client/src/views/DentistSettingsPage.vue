<template>
    <div class="screen-container">

      <DentistTopBarComponent />

      <div class="main-content-section">
        <BButton class="logout-button" @click="logout">Log out button</BButton>
      </div>
    </div>
  </template>

<script>
import { publishToTopic, unsubscribeFromTopic, client } from '../mqtt/mqtt.js'
import DentistTopBarComponent from '../components/DentistComponents/DentistTopBarComponent.vue'

export default {
  data() {
    return {
      name: 'DentistSettingsPage'
    }
  },
  components: {
    DentistTopBarComponent
  },
  methods: {
    // Logs out by destroying necessary saved detailes and listeners.
    logout() {
      const PUBLISH_LOGOUT_TOPIC = 'logout'
      const PUBLISH_LOGGED_OUT_USER_ID = 'authenticationService/dentist/logout'
      publishToTopic(PUBLISH_LOGOUT_TOPIC, 'User has logged out of the Teeth Repair System')
      publishToTopic(PUBLISH_LOGGED_OUT_USER_ID, JSON.parse(localStorage.getItem('UserID')))
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
      unsubscribeFromTopic('client/scheduleService/appointmentInfo')
      localStorage.clear()
      client.removeAllListeners()
      this.$router.push('/login')
    }
  }
}

</script>

  <style>
  </style>
