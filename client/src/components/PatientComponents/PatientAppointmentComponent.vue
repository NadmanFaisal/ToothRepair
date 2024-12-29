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
          </div>

        </div>

        <div class="col-10 slot-section">

          <!-- Dynamically sets the color of the slots according to the status -->
          <div
          class="col-2 appointment-slot-container"
          v-for="appointment in morningFilteredAppointments"
          :key="appointment.id"
          :class="{
            'available-slot': appointment.status === 'available',
            'booked-slot': appointment.status === 'booked',
            'unavailable-slot': appointment.status === 'unavailable',
            'selected-slot': appointment.id === selectedAppointmentId
            } "
            @click="selectAppointment(appointment)"
            >
            <div class="col- 4 status-mark-container">
              <img :src="getStatusImage(appointment.status)" class="status-mark-image">
            </div>
            <div class="col-8 appointment-information-container">
              <label class="appointment-information-label" :class="{ 'unavailable-label': appointment.status === 'unavailable' }">{{ getTypeOfTime(appointment.startTime) }}</label>
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
            <button type="button" @click="bookAppointment" class="btn btn-primary confirm-booking-button">Confirm</button>
          </div>

        </div>

        <div class="col-10 slot-section">

          <!-- Dynamically sets the color of the slots according to the status -->
          <div
          class="col-2 appointment-slot-container"
          v-for="appointment in eveningFilteredAppointments"
          :key="appointment.id"
          :class="{
            'available-slot': appointment.status === 'available',
            'booked-slot': appointment.status === 'booked',
            'unavailable-slot': appointment.status === 'unavailable',
            'selected-slot': appointment.id === selectedAppointmentId
            } "
            @click="selectAppointment(appointment)"
            >
            <div class="col- 4 status-mark-container">
              <img :src="getStatusImage(appointment.status)" class="status-mark-image">
            </div>
            <div class="col-8 appointment-information-container">
              <label class="appointment-information-label" :class="{ 'unavailable-label': appointment.status === 'unavailable' }">{{ getTypeOfTime(appointment.startTime) }}</label>
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
  name: 'AppointmentComponent',
  data() {
    return {
      selectedAppointmentId: null,
      selectedAppointmentStartTime: null
    }
  },
  props: {
    patientSelectedDate: {
      type: String
    },
    triggerGetAppointments: {
      type: Function,
      required: true
    },
    appointments: {
      type: Array,
      default: () => []
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
          appointment.date === this.patientSelectedDate &&
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
          appointment.date === this.patientSelectedDate &&
          startTimeInMinutes > eveningStartTime &&
          startTimeInMinutes <= eveningEndTime
        )
      })
    }
  },
  methods: {
    getTypeOfTime(time) {
      const [hour, minute] = time.split(':').map(Number) // Split the time into hour and minute
      const period = hour >= 12 ? 'PM' : 'AM' // Determine if it’s AM or PM
      const adjustedHour = hour % 12 || 12 // Convert 0 hour to 12 for AM and handle 12-hour format
      return `${adjustedHour}:${minute.toString().padStart(2, '0')} ${period}` // Format the time with leading zeros
    },
    getStatusImage(status) {
      return status === 'available' ? checkMark : crossMark
    },
    async bookAppointment() {
      if (!this.selectedAppointmentId) {
        alert('No booking slot has been selected')
        return
      }
      try {
        const userId = localStorage.getItem('UserID')
        await subscribeToTopic('Client/ScheduleService/AppointmentInfo')
        publishToTopic('ScheduleService/Appointment/bookAppointment', '{"id": "' + this.selectedAppointmentId + '", "patient": ' + userId + '}')
        alert(`Successfully booked appointment at ${this.selectedAppointmentStartTime}`)
        this.selectedAppointmentId = null
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    selectAppointment(appointment) {
      if (appointment.status === 'unavailable') {
        alert('This slot is unavailable')
        return
      }
      if (appointment.status === 'booked') {
        alert('Slot has already been booked. Please select a different slot')
        return
      }
      this.selectedAppointmentStartTime = this.selectedAppointmentStartTime === appointment.startTime ? null : appointment.startTime
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

.available-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  border: 1px solid #009C1F;
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
  border: 1px solid #DEDEDE;
  background-color: #FFF;
  box-shadow: 0px 4px 4px 0px rgba(0, 0, 0, 0.25);
  color:#DCDCDC;
}

.unavailable-label {
  color:#DCDCDC;
}

.selected-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  background-color: #009C15;
  box-shadow: 0px 4px 4px 0px rgba(0, 156, 31, 0.25);
  color:white;
  border: 3px solid #007BFF;
  box-shadow: 0px 0px 10px rgba(0, 123, 255, 0.5);
}

.booked-slot {
  margin: 20px;
  display: flex;
  flex-direction: row;
  height: 75px;
  border-radius: 5px;
  border: 1px solid #E70505;
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

  .booked-slot {
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

  .booked-slot {
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

  .booked-slot {
    margin: 5px;
    height: 50px;
  }

  .status-mark-image {
    margin-left: 0px;
    height: 12px;
  }

  .appointment-information-label {
    font-size: 10px;
  }

}
</style>
