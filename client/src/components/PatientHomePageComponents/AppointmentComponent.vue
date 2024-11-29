<template>
    <div class="col-12 appointment-container">
        <div class="col-12 appointment-content-container">
            <button @click="getAppointments">Get appointments</button>
            <p>{{ this.appointments }}</p>
            <button @click="createAppointment">Create an appointment</button>
        </div>
    </div>
</template>

<script>

import { subscribeToTopic, messageArrived, publishToTopic } from '../../mqtt/mqtt.js'

export default {
  name: 'AppointmentComponent',
  data() {
    return {
      appointments: []
    }
  },
  methods: {
    async createAppointment() {
      try {
        await subscribeToTopic('test/appointmentList')
        publishToTopic('test/createAppointment')
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    async getAppointments() {
      try {
        await subscribeToTopic('test/appointmentList')
        publishToTopic('test/appointmentAlert')
        messageArrived((topic, message) => {
          this.appointments.push(`${message}`)
          if (topic === 'test/appointmentList') {
            console.log('Received Appointment list:', message)
            this.appointments.push(message)
          }
        })
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    }
  }
}
</script>

<style scoped>
.appointment-container {
    display: flex;
    flex-direction: column;
    justify-items: center;
    align-items: center;
    height: 100%;
    width: 100%;
    padding: 40px;
}

.appointment-content-container {
    background-color: white;
    height: 100%;
}
</style>
