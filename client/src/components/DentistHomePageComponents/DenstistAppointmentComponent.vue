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
            <div class="col-2 appointment-slot-container"
            v-for="appointment in appointments"
            :key="appointment.id"
            :class="{
              'available-slot': appointment.status === 'available',
              'unavailable-slot': appointment.status !== 'available',
              'selected-slot': appointment.id === selectedAppointmentId
              }
              " @click="selectAppointment(appointment)">
              <div class="col- 4 status-mark-container">
                <img :src="getStatusImage(appointment.status)" class="status-mark-image">
              </div>
              <div class="col-8 appointment-information-container">
                <label class="appointment-information-label" :class="{ 'available-label': appointment.status === 'available', 'available-label': appointment.status === 'booked', 'unavailable-label': appointment.status === 'unavailable' }">{{ appointment.startTime }} PM</label>
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
              <button type="button" @click="makeAvailable" class="btn btn-primary confirm-booking-button">Confirm</button>
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
  name: 'DentistAppointmentComponent',
  data() {
    return {
      appointments: [],
      selectedAppointmentId: null
    }
  },
  created() {
    this.$watch(
      () => this.$route,
      this.getAppointments,
      { immediate: true }
    )
  },
  methods: {
    async createAppointment() {
      try {
        await this.getAppointments()

        // This function is used to increament the time of the slots by 30 minutes
        const incrementTime = (time) => {
          const [hours, minutes] = time.split(':').map(Number)
          const newMinutes = minutes + 30
          const newHours = hours + Math.floor(newMinutes / 60)
          const adjustedMinutes = newMinutes % 60
          return `${String(newHours).padStart(2, '0')}:${String(adjustedMinutes).padStart(2, '0')}`
        }

        // Uses 9:00 as initial time and keeps incrementing it to check if a slot with same time exists.
        let startTime = '09:00'
        const duplicateTimes = this.appointments.map(app => app.startTime)

        // Increments the time if duplicate time exists
        while (duplicateTimes.includes(startTime)) {
          startTime = incrementTime(startTime)
        }

        // Creates new appointment with next available time slot
        await subscribeToTopic('client/scheduleService/appointmentInfo')
        const newAppointment = {
          status: 'available',
          date: '1111-11-11',
          startTime,
          endTime: incrementTime(startTime)
        }
        publishToTopic('scheduleService/appointment/createAppointment', JSON.stringify(newAppointment))
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    async getAppointments() {
      try {
        await subscribeToTopic('client/scheduleService/appointmentInfo')
        publishToTopic('scheduleService/appointment/getAppointments', 'Get Appointments')
        messageArrived((topic, message) => {
          if (topic === 'client/scheduleService/appointmentInfo') {
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
    async makeAvailable() {
      if (!this.selectedAppointmentId) {
        alert('No booking slot has been selected')
        return
      }
      try {
        const userId = localStorage.getItem('UserID')
        await subscribeToTopic('client/scheduleService/appointmentInfo')
        publishToTopic('scheduleService/appointment/changeAppointmentStatus', '{"id": "' + this.selectedAppointmentId + '", "dentist": ' + userId + '}')
        this.selectedAppointmentId = null
        this.getAppointments()
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    getStatusImage(status) {
      return status === 'available' ? checkMark : crossMark
    },
    selectAppointment(appointment) {
      if (appointment.status === 'booked') {
        const confirmation = confirm('This has already been booked by patients. Do you want to delete their bookings and make it unavailable?')
        if (!confirmation) {
          return
        }
      }
      this.selectedAppointmentId = this.selectedAppointmentId === appointment.id ? null : appointment.id
      console.log(appointment.id)
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

.appointment-slot-container:hover {
  transform: translateY(-2px);
  box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
}

.appointment-slot-container:active {
  transform: translateY(1px);
  box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.1);
}

.unavailable-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  border: 1px solid #DEDEDE;
  background-color: #FFF;
  box-shadow: 0px 4px 4px 0px rgba(0, 0, 0, 0.25);
  color:#6B6B6B;
}

.available-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  border: 1px solid #DEDEDE;
  background-color: #FFF;
  box-shadow: 0px 4px 4px 0px rgba(0, 0, 0, 0.25);
  color:#DCDCDC;
}

.selected-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  background-color: #FFF;
  border: 3px solid #007BFF;
  box-shadow: 0px 0px 10px rgba(0, 123, 255, 0.5);
}

.available-label {
  color:#DCDCDC;
}

.unavailable-label {
  color:#6B6B6B;
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

.confirm-booking-button {
  border-radius: 15px;
  background: #06F;
  box-shadow: 0px 4px 4px 0px rgba(92, 64, 255, 0.70);
  color: #FFF;
  text-align: center;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}
</style>
