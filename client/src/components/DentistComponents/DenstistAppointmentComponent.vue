<template>
    <div class="col-12 content-container">

      <div class="col-12 appointment-content-container">

        <div class="col-12 morning-container">

          <div class="col-12 top-section">

            <div class="col-1 sunrise-container">
              <img src="../../assets/sunrise.png" class="sunrise-image">
            </div>

            <div class="col-11 title-container">
              <h1 class="title-label">Morning</h1>
              <label class="time-label">9:00 AM to 12:00 PM</label>
            </div>

          </div>

          <div class="col-10 slot-section">

            <!-- Dynamically sets the color of the slots according to the status -->
            <div v-if="this.dentistSelectedDate >= today"
            class="col-2 appointment-slot-container"
            v-for="appointment in morningFilteredAppointments"
            :key="`future-${appointment.id}`"
            :class="{
              'available-slot': appointment.status === 'available',
              'unavailable-slot': appointment.status !== 'available',
              'pending-slot': appointment.status === 'pending',
              'selected-slot': appointment.id === selectedAppointmentId
              }
              " @click="selectAppointment(appointment)">
              <div class="col- 4 status-mark-container">
                <img :src="getStatusImage(appointment.status)" class="status-mark-image">
              </div>
              <div class="col-8 appointment-information-container">
                <label class="appointment-information-label"
                :class="{
                  'available-label': appointment.status === 'available',
                  'available-label': appointment.status === 'booked',
                  'unavailable-label': appointment.status === 'unavailable'
                  }"
                  >
                    {{ getTypeOfTime(appointment.startTime) }}
                  </label>
              </div>
            </div>

            <div v-else class="col-2 appointment-slot-container"
            v-for="appointment in morningFilteredAppointments"
            :key="`past-${appointment.id}`"
            id="old-slot"
              @click="showOldAppointmentAlert()">
              <div class="col- 4 status-mark-container">
                <img :src="getStatusImage(appointment.status)" class="status-mark-image">
              </div>
              <div class="col-8 appointment-information-container">
                <label class="appointment-information-label">
                  {{ getTypeOfTime(appointment.startTime) }}
                </label>
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

          <div class="col-10 slot-section">

            <!-- Dynamically sets the color of the slots according to the status -->
            <div v-if="this.dentistSelectedDate >= today"
            class="col-2 appointment-slot-container"
            v-for="appointment in eveningFilteredAppointments"
            :key="`future-${appointment.id}`"
            :class="{
              'available-slot': appointment.status === 'available',
              'unavailable-slot': appointment.status !== 'available',
              'pending-slot': appointment.status === 'pending',
              'selected-slot': appointment.id === selectedAppointmentId
              }
              " @click="selectAppointment(appointment)">
              <div class="col- 4 status-mark-container">
                <img :src="getStatusImage(appointment.status)" class="status-mark-image">
              </div>
              <div class="col-8 appointment-information-container">
                <label class="appointment-information-label"
                :class="{
                  'available-label': appointment.status === 'available',
                  'available-label': appointment.status === 'booked',
                  'unavailable-label': appointment.status === 'unavailable'
                  }"
                  >
                    {{ getTypeOfTime(appointment.startTime) }}
                </label>
              </div>
            </div>

            <div v-else class="col-2 appointment-slot-container"
            v-for="appointment in eveningFilteredAppointments"
            :key="`past-${appointment.id}`"
            id="old-slot"
              @click="showOldAppointmentAlert()">
              <div class="col- 4 status-mark-container">
                <img :src="getStatusImage(appointment.status)" class="status-mark-image">
              </div>
              <div class="col-8 appointment-information-container">
                <label class="appointment-information-label">
                  {{ getTypeOfTime(appointment.startTime) }}
                </label>
              </div>
            </div>

          </div>

        </div>

      </div>

    </div>
</template>

<script>

import { subscribeToTopic, publishToTopic } from '../../mqtt/mqtt.js'
import checkMark from '../../assets/check-mark.png'
import crossMark from '../../assets/cross-mark.png'

export default {
  name: 'DentistAppointmentComponent',
  data() {
    return {
      selectedAppointmentId: null,
      selectedAppointmentStartTime: null,
      userId: localStorage.getItem('UserID'),
      pendingTimer: null,
      today: new Date().toISOString().split('T')[0]
    }
  },
  props: {
    dentistSelectedDate: {
      type: String,
      required: true
    },
    appointments: {
      type: Array,
      default: () => []
    },
    clinicId: {
      type: String,
      required: true
    }
  },
  watch: {
    dentistSelectedDate: {
      handler(newDate) {
        console.log('New selected date in DentistAppointmentComponent:', newDate)
      }
    }
  },
  computed: {
    // computed because the changes are cached only if selectedDate changes
    morningFilteredAppointments() {
      // Filters the appointments according to its time
      return this.appointments.filter(appointment => {
        // Breaks the appointment hour and minutes
        const [hour, minute] = appointment.startTime.split(':').map(Number)
        // Converts the hours and the minutes to total minute
        const startTimeInMinutes = hour * 60 + minute
        // Morning start time threshold
        const morningStartTime = 5 * 60
        // Morning end time threshold
        const morningEndTime = 12 * 60
        return (
          appointment.date === this.dentistSelectedDate &&
          startTimeInMinutes >= morningStartTime &&
          startTimeInMinutes <= morningEndTime
        )
      })
    },
    eveningFilteredAppointments() {
      return this.appointments.filter(appointment => {
        // Breaks the appointment hour and minutes
        const [hour, minute] = appointment.startTime.split(':').map(Number)
        // Converts the hours and the minutes to total minute
        const startTimeInMinutes = hour * 60 + minute
        // Evening start time threshold
        const eveningStartTime = 12 * 60
        // Evening end time threshold
        const eveningEndTime = 20 * 60
        return (
          appointment.date === this.dentistSelectedDate &&
          startTimeInMinutes > eveningStartTime &&
          startTimeInMinutes <= eveningEndTime
        )
      })
    }
  },
  methods: {
    // Makes selected appointment available
    async makeAvailable() {
      // If no appointments selected, does not proceed
      if (!this.selectedAppointmentId) {
        alert('No booking slot has been selected')
        return
      }
      try {
        await subscribeToTopic('Client/ScheduleService/AppointmentInfo')
        // Sends the selected appointment ID and dentist ID for making slot available
        publishToTopic('ScheduleService/Appointment/makeAppointmentAvailable', '{"id": "' + this.selectedAppointmentId + '", "dentist": ' + this.userId + ', "clinic": ' + JSON.stringify(this.clinicId) + '}')
        // Alert for confirmation of slot booking showing necessary details
        alert('Successfully made a ' + this.selectedAppointmentStartTime + ' AM appointment slot available on ' + this.dentistSelectedDate)
        // Sets the selectedAppointmentId to null to prevent making same appointment available again
        this.selectedAppointmentId = null
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    getTypeOfTime(time) {
      const [hour, minute] = time.split(':').map(Number) // Split the time into hour and minute
      const period = hour >= 12 ? 'PM' : 'AM' // Determine if it’s AM or PM
      const adjustedHour = hour % 12 || 12 // Convert 0 hour to 12 for AM and handle 12-hour format
      return `${adjustedHour}:${minute.toString().padStart(2, '0')} ${period}` // Format the time with leading zeros
    },
    showOldAppointmentAlert() {
      alert('This appointment is older than the current date, please try to make another appointment available.')
    },
    getStatusImage(status) {
      return status === 'available' ? checkMark : crossMark
    },
    async selectAppointment(appointment) {
      console.log(appointment.id)
      console.log(appointment.status)
      console.log(appointment.dentist)
      console.log(this.userId)
      if (appointment.status === 'available') {
        alert('This slot is already available, please make another slot available')
        return
      }
      if (appointment.status === 'booked') {
        alert('Slot has already been booked. Please select a different slot')
        return
      }
      if (appointment.status === 'pending' && !(appointment.dentist === JSON.parse(this.userId))) {
        if (appointment.patient === null) {
          alert('This slot is pending, it might soon become available so check other appointments')
        } else {
          alert('This slot is pending, it might soon be booked so check other appointments')
        }
        return
      }
      if (appointment.status === 'pending' && appointment.dentist === JSON.parse(this.userId) && !(appointment.patient === null)) {
        alert('This slot is pending, it might soon be booked so check other appointments')
        return
      }
      if (this.selectedAppointmentId !== null && !(appointment.dentist === JSON.parse(this.userId))) {
        alert('Please unselect your pending appointment')
        return
      }
      console.log("Before assigning value to selectedAppointmentStartTime: ", this.selectedAppointmentStartTime)
      this.selectedAppointmentStartTime = this.selectedAppointmentStartTime === appointment.startTime ? null : appointment.startTime
      console.log("After assigning value to selectedAppointmentStartTime: ", this.selectedAppointmentStartTime)
      this.selectedAppointmentId = this.selectedAppointmentId === appointment.id ? null : appointment.id
      publishToTopic('scheduleService/appointment/pendingAppointments', '{"id": "' + this.selectedAppointmentId + '", "dentist": ' + this.userId + ', "clinic": ' + JSON.stringify(this.clinicId) + '}')
      if (this.pendingTimer) {
        clearTimeout(this.pendingTimer)
      }
      this.pendingTimer = setTimeout(() => {
        console.log('Clinic ID fetched')
        publishToTopic('scheduleService/appointment/pendingAppointments', '{"id": "null", "dentist": ' + this.userId + ', "clinic": ' + JSON.stringify(this.clinicId) + '}')
        this.selectedAppointmentId = null
      }, 10000)
    }
  }

}
</script>

<style scoped>
.content-container {
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
}

#old-slot {
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

.pending-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  background-color: yellow;
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

@media (max-width: 1260px) {

.sunrise-image {
  height: 55px;
}

.title-label {
  font-size: 25px;
}

.time-label {
  font-size: 17px;
}

.available-slot {
  margin: 20px;
  height: 50px;
}

.unavailable-slot {
  margin: 20px;
  height: 50px;
}

.selected-slot {
  margin: 20px;
  height: 50px;
}

.status-mark-image {
  margin-left: 5px;
  height: 20px;
}

.appointment-information-container {
  margin-left: 5px;
}

.appointment-information-label {
  font-size: 18px;
}

}

@media (max-width: 750px) {

.sunrise-image {
  height: 45px;
}

.title-label {
  font-size: 20px;
}

.time-label {
  font-size: 15px;
}

.slot-section {
  width: 100%;
}

}

@media (max-width: 700px) {

.available-slot {
  margin: 10px;
  height: 50px;
}

.unavailable-slot {
  margin: 10px;
  height: 50px;
}

.selected-slot {
  margin: 10px;
  height: 50px;
}

.appointment-information-label {
  font-size: 13px;
}

}

@media (max-width: 660px) {

.sunrise-container {
  display: none;
}

.title-label {
  padding-left: 10px;
}

.time-label {
  padding-left: 10px;
}

}

@media (max-width: 500px) {

.available-slot {
  margin: 5px;
  height: 50px;
}

.unavailable-slot {
  margin: 5px;
  height: 50px;
}

.selected-slot {
  margin: 5px;
  height: 50px;
}

.appointment-information-label {
  font-size: 10px;
}

.status-mark-image {
  margin-left: 0px;
  height: 12px;
}

}
</style>
