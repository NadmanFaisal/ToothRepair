<template>
    <div class="col-12 appointment-container">

      <div class="col-12 appointment-content-container">

        <div class="col-12 morning-container">

          <div class="col-12 top-section">

            <div class="col-1 sunrise-container">
              <img src="../../assets/sunrise.png" class="sunrise-image">
            </div>

            <div class="col-11 title-container">
              <h1 class="title-label">Morning</h1>
              <label class="time-label">9:00 AM to 12:00 PM</label>
              <!--Buttons for testing purposes-->
              <button @click="getAppointments">Get appointments</button>
              <button @click="createAppointment">create appointments</button>
            </div>

          </div>

          <div class="col-10 slot-section">

            <!-- Dynamically sets the color of the slots according to the status -->
            <div class="col-2 appointment-slot-container" v-for="appointment in appointments" :key="appointment.id" :class="{ 'available-slot': appointment.status === 'available', 'unavailable-slot': appointment.status !== 'available' }">
              <div class="col- 4 status-mark-container">
                <img :src="getStatusImage(appointment.status)" class="status-mark-image">
              </div>
              <div class="col-8 appointment-information-container">
                <label class="appointment-information-label">{{ appointment.status }}</label>
              </div>
            </div>

          </div>

        </div>

        <div class="col-12 section-divider-container">
          <hr class="col-10 section-divider">
        </div>

        <div class="col-12 evening-container">

          <div class="col-12 top-section">

            <div class="col-1 sunrise-container">
              <img src="../../assets/sunrise.png" class="sunrise-image">
            </div>

            <div class="col-11 title-container">
              <h1 class="title-label">Evening</h1>
              <label class="time-label">12:00 PM to 17:00 PM</label>
            </div>

          </div>

        </div>

      </div>

    </div>
</template>

<script>

import { subscribeToTopic, messageArrived, publishToTopic } from '../../mqtt/mqtt.js'
import checkMark from '../../assets/check-mark.png'
import crossMark from '../../assets/cross-mark.png'

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
          if (topic === 'test/appointmentList') {
            console.log('Received Appointment list:', message)

            // Check if the receiving message is already a JSON string, if not, parse it
            const parsedMessage = typeof message === 'string' ? JSON.parse(message) : message
            this.appointments = parsedMessage
          }
        })
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    getStatusImage(status) {
      return status === 'available' ? checkMark : crossMark
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
  display: flex;
  flex-direction: column;
  background-color: white;
  height: 100%;
  border-radius: 5px;
}

.morning-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 47.5%;
}

.top-section {
  display: flex;
  flex-direction: row;
  height: 30%;
}

.sunrise-container {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.sunrise-image {
  height: 70px;
}

.title-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
}

.title-label {
  color: #828282;
  text-align: center;
  font-family: Inter;
  font-size: 32px;
  font-style: normal;
  font-weight: 500;
  line-height: normal;
}

.time-label {
  color: #A1A1A1;
  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 300;
  line-height: normal;
}

.slot-section {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  height: 70%;
}

.available-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  background-color: #009C15;
  box-shadow: 0px 4px 4px 0px rgba(0, 156, 31, 0.25);
  color:white;
}

.unavailable-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  background-color: #E70505;
  box-shadow: 0px 4px 4px 0px rgba(231, 5, 5, 0.25);
  color:white;
}

.status-mark-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.status-mark-image {
  margin-left: 5px;
  height: 30px;
}

.appointment-information-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: start;
  margin-left: 5px;
}

.appointment-information-label {
  color: #FFF;
  text-align: center;
  font-family: Inter;
  font-size: 23px;
  font-style: normal;
  font-weight: 500;
  line-height: normal;
}

.section-divider-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 5%;
}

.evening-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 47.5%;
}
</style>
