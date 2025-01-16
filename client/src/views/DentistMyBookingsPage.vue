<template>
    <div class="col-12 screen-container">

        <DentistTopBar />

        <div class="col-12 content-section">

            <div class="main-content-section">

                <div class="title-container">

                    <div class="col-8 left-title-container">
                        <label class="title-label">Feeling Ready to be a Dentist?</label>
                        <label class="book-appointment-label">Make Appointments Available Now!</label>
                    </div>

                    <div class="col-4 right-title-container">
                        <button class="col-8 btn book-appointment-button" @click="navigateToHomePage">Make Appointments Available!</button>
                    </div>
                </div>

                <div class="appointment-container">

                  <div class="col-12 appointment-title-container">
                    <label class="appointment-title-label">My Appointments</label>
                  </div>

                  <hr>

                  <div class="col-12 appointment-content-section">

                    <div class="col-12 appointment-slot-container" v-for="appointment in availableAppointments" :key="appointment.id" >
                      
                      <div class="col-1 logo-container">
                        <img src="../assets/appointment-slot-image.png" class="appointment-image">
                        <div class="vl"></div>
                      </div>

                      <div class="col-4 date-container">
                        <label class="appointment-day-label">{{ formatAppointmentDate(appointment.date).day }}</label>
                        <label class="appointment-month-day-label">{{ formatAppointmentDate(appointment.date).monthAndDay }} at <span class="appointment-start-time-span">{{ appointment.startTime }}</span></label>
                      </div>

                      <div class="col-4 clinic-details-container">
                        <!-- <label class="clinic-name-label">{{ appointment.clinic }}</label> -->
                      </div>

                      <div class="col-3 status-container">

                        <div v-if="today <= appointment.date" class="col-12 button-container">
                          <!--<button class="col-10 btn reschedule-button" @click="rescheduleAppointment(appointment.id)">Reschedule</button>-->
                          <button class="col-10 btn cancel-button" @click="cancelAppointment(appointment)">Cancel</button>
                        </div>

                        <div v-else class="col-12 completed-container">
                          <label class="completed-label">COMPLETED!</label>
                        </div>

                      </div>

                    </div>
                   

                  </div>

                </div>

                <div class="maps-container">
                </div>

            </div>

        </div>
    </div>
  </template>

<script>
import DentistTopBar from '../components/DentistComponents/DentistTopBarComponent.vue'
import { subscribeToTopic, messageArrived, unsubscribeFromTopic, publishToTopic, client } from '../mqtt/mqtt'

export default {
  data() {
    return {
      today: new Date().toISOString().split('T')[0],
      userId: localStorage.getItem('UserID'),
      appointments: [],
      clinics: [],
      subscribedTopics: []
    }
  },
  components: {
    DentistTopBar
  },

  created() {
    this.$watch(
      () => this.$route,
      () => {
        this.getAppointments()
      },
      { immediate: true }
    )
  },
  mounted() {
    client.on('connect', () => {
      this.getAppointments()
      console.log(this.appointments)
    })
  },
  unmounted() {
    this.cleanupSubscriptions()
  },
  computed: {
    availableAppointments() {
      return this.appointments.filter((appointment) => appointment.status === 'available')
    }
  },
  methods: {
    async getAppointments() {
      try {
        if (!this.userId) {
          throw new Error('User ID not found in localStorage')
        }

        const topic = 'Client/ScheduleService/AppointmentInfo'
         // If a same topic has been subscribed, does not subscribe any further
        if (!this.subscribedTopics.includes(topic)) {
          await subscribeToTopic(topic)
          this.subscribedTopics.push(topic)
          console.log('Subscribed successfully')
        }
        subscribeToTopic('client/scheduleService/getAppointmentStatus')
        console.log('Publishing request for appointments...')
        // Publishes userID to receive appointments for that specific user
        publishToTopic('ScheduleService/Appointment/getAppointmentsByDentist', `{"dentist": ${this.userId}}`)

        messageArrived((topic, message) => {
          console.log(topic)
          if (topic === 'Client/ScheduleService/AppointmentInfo') {
            const parsedMessage = JSON.parse(message)
            console.log('Received appointments for specific dentist:', parsedMessage)
            console.log('recieved: ' + message)
            console.log('What happens when parsing' + parsedMessage)
            console.log('userid = ' + parsedMessage.dentist)
            console.log('localstorage = ' + this.userId)
            if (JSON.parse(this.userId) === parsedMessage.dentist) {
              this.appointments = parsedMessage.appointments.sort((a, b) => {
              const ascendingDate = new Date(a.date) - new Date(b.date)
              if (ascendingDate !== 0) {
                // If dates are not same, returns ascendingDate
                return ascendingDate
              }
              // If dates are same, sorts according to startTime and returns ascendingDate
              return a.startTime.localeCompare(b.startTime)
            })
            } else {
              console.log('Recieved another users request')
            }
          }
        })
      } catch (error) {
        console.error('Error in getAppointments:', error)
      }
    },
    async getClinic(clinicId) {
      try {
        console.log('Setting up message listener for clinic info...')

        messageArrived((topic, message) => {
          if (topic === 'Client/ClinicService/ClinicInfo') {
            const parsedMessage = typeof message === 'string' ? JSON.parse(message) : message
            console.log('Received clinic info:', parsedMessage)

            if (Object.keys(parsedMessage).length === 0) {
              console.warn('No clinic found for the given ID.')
            } else {
              this.clinics.push(parsedMessage)
            }
          }
        })

        console.log('Subscribing to topic...')
        await subscribeToTopic('Client/ClinicService/ClinicInfo')
        console.log('Subscribed successfully')

        console.log('Publishing request for clinic info...')
        publishToTopic('ClinicService/Clinic/getClinicById', clinicId)
      } catch (error) {
        console.error('Error in getClinic:', error)
      }
    },
    async cleanupSubscriptions() {
      try {
        for (const topic of this.subscribedTopics) {
          console.log(`Unsubscribing from topic: ${topic}`)
          unsubscribeFromTopic(topic)
        }
        this.subscribedTopics = []
        console.log('Unsubscribed from all topics.')
      } catch (error) {
        console.error('Error unsubscribing from topics:', error)
      }
    },
    async cancelAppointment(appointment) {
      const confirmation = confirm('Are you sure you want to cancel it?')
      if (!confirmation) {
        return
      }

      try {
        console.log('Attempting to cancel appointment' + appointment.id)
        await subscribeToTopic('Client/ScheduleService/AppointmentInfo')
        // Publishes the appointmentID and userID for cancelling appointments
        publishToTopic('ScheduleService/Appointment/dentistCancelAppointments', '{"id": "' + appointment.id + '", "dentist": ' + this.userId + '}')
        messageArrived((topic, message) => {
          console.log(topic)
          if (topic === 'Client/ScheduleService/AppointmentInfo') {
            console.log('recieved: ' + message)
            const parsedMessage = JSON.parse(message)
            console.log('What happens when parsing' + parsedMessage)
            console.log('userid = ' + parsedMessage.userID)
            console.log('localstorage = ' + this.userId)
            if (JSON.parse(this.userId) === parsedMessage.userID) {
              this.appointments = parsedMessage.appointments.sort((a, b) => {
              const ascendingDate = new Date(a.date) - new Date(b.date)
              if (ascendingDate !== 0) {
                // If dates are not same, returns ascendingDate
                return ascendingDate
              }
              // If dates are same, sorts according to startTime and returns ascendingDate
              return a.startTime.localeCompare(b.startTime)
              })
            } else {
              console.log('Recieved another users request')
            }
          }
        })
        alert('Cancelled an Appointment at ' + appointment.startTime + ' on ' + appointment.date)
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    navigateToHomePage() {
      this.$router.push('/dentistHomePage')
    },
    // Date is taken and the corresponding month and day is shown in the bookings page
    formatAppointmentDate(dateString) {
      const date = new Date(dateString)
      const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
      const months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
      ]

      const day = days[date.getDay()]
      const month = months[date.getMonth()]
      const dayOfMonth = date.getDate()
      const ordinal = this.getOrdinal(dayOfMonth)

      // Returns the value of day and its ordinal, as well as month
      return {
        day: `${dayOfMonth}${ordinal}`,
        monthAndDay: `${month}, ${day}`
      }
    },
    getOrdinal(day) {
      if (day > 10 && day < 20) return 'th'
      const lastDigit = day % 10
      switch (lastDigit) {
        case 1: return 'st'
        case 2: return 'nd'
        case 3: return 'rd'
        default: return 'th'
      }
    }
  }
}

</script>

  <style scoped>
  .screen-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  align-items: center;
  background-image: linear-gradient(0deg, rgba(31, 194, 194, 0.24) 0%, rgba(31, 194, 194, 0.24) 100%), url('@/assets/patient-home-bg-image.jpeg');
  background-size: contain;
  }

  .content-section {
  display: flex;
  flex-direction: column;
  height: 93%;
  background-image: linear-gradient(0deg, rgba(31, 194, 194, 0.24) 0%, rgba(31, 194, 194, 0.24) 100%), url('@/assets/patient-home-bg-image.jpeg');
  background-size: contain;
  justify-content: center;
  align-items: center;
  }

  .main-content-section {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  height: 95%;
  width: 97%;
  background: rgba(255, 255, 255, 0.70);
  padding: 15px;
  }

  .title-container {
  display: flex;
  flex-direction: column;
  flex-wrap: wrap;
  height: 18%;
  width: 100%;
  background-image: linear-gradient(0deg, rgba(31, 194, 194, 0.24) 0%, rgba(31, 194, 194, 0.24) 100%), url('@/assets/patient-home-bg-image.jpeg');
  background-size: 100%;
  background-position: top;
  background-repeat: no-repeat;
  }

  .left-title-container {
  display: flex;
  flex-direction: column;
  align-content: flex-start;
  justify-content: center;
  height: 100%;
  padding: 10px;
  }

  .title-label {
  color: #FFF;
  text-align: center;
  font-family: Inter;
  font-size: 50px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
  align-self: flex-start;
  }
  .book-appointment-label {
  color: #FFF;
  text-align: center;
  font-family: Inter;
  font-size: 30px;
  font-style: normal;
  font-weight: 300;
  line-height: normal;
  align-self: flex-start;
  }

  .right-title-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 10px;
  }

  .book-appointment-button {
  border-radius: 15px;
  background: #1FC2C2;
  box-shadow: 0px 4px 4px 0px rgba(31, 194, 194, 0.70);
  color: #FFF;
  height: 50%;
  width: 50%;

  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
  }

  .appointment-container {
  display: flex;
  flex-direction: column;
  height: 80%;
  width: 60%;
  padding: 10px;
  background: #FBFBFB;
  margin-top: 15px;
  }

  .appointment-title-container {
    display: flex;
    flex-direction: column;
    align-items: start;
  }

  .appointment-title-label {
  color: #818181;
  text-align: center;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 500;
  line-height: normal;
  }

  .appointment-content-section {
    display: flex;
    flex-direction: column;
    height: 88%;
    overflow-y: auto;
  }

  .appointment-slot-container {
  display: flex;
  flex-direction: row;
  height: 120px;
  border-radius: 10px;
  background: #FFF;
  box-shadow: 0px 2px 4px 0px rgba(0, 0, 0, 0.25);
  margin-bottom: 10px;
  }

  .logo-container {
  display: flex;
  flex-direction: row;
  height: 100%;
  justify-content: center;
  align-items: center;
  }

  .vl {
  border-left: 2px solid #c5c5c5;
  height: 70%;
  margin: 8px;
  }

  .appointment-image {
  height: 60px;
  width: 60px;
  }

  .date-container, .clinic-details-container, .status-container , .button-container, .completed-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: center;
  align-items: flex-start;
  }

  .appointment-day-label {
  color: #1FC2C2;
  text-align: center;
  font-family: Inter;
  font-size: 64px;
  font-style: normal;
  font-weight: 500;
  line-height: normal;
  }

  .appointment-month-day-label {
  color: #818181;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 200;
  line-height: normal;
  }

  .appointment-start-time-span {
  color: #1FC2C2;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 200;
  line-height: normal;
  }

  .clinic-name-label {
  color: #999;
  text-align: center;
  font-family: Inter;
  font-size: 32px;
  font-style: normal;
  font-weight: 500;
  line-height: normal;
  }

  .completed-label {
  color: #07A41F;
  text-align: center;
  font-family: Inter;
  font-size: 25px;
  font-style: normal;
  line-height: normal;
  font-weight: 700;
  }

  .maps-container {
  display: flex;
  flex-direction: column;
  height: 80%;
  width: 38%;
  background: #FBFBFB;
  margin-top: 15px;
  margin-left: 1%;
  }

  .reschedule-button {
  margin-bottom: 10px;
  border-radius: 10px;
  background: #06F;
  box-shadow: 0px 2px 4px 0px rgba(0, 102, 255, 0.70);

  color: #FFF;
  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
  }

  .cancel-button {
  border-radius: 10px;
  background: #FC494C;
  box-shadow: 0px 2px 4px 0px rgba(252, 73, 76, 0.70);

  color: #FFF;
  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
  }

  @media (max-width: 1450px) {
  .book-appointment-button {
    font-size: 15px;
  }

  .appointment-image {
    height: 40px;
    width: 40px;
  }

  .appointment-day-label {
    font-size: 55px;
  }

  .appointment-month-day-label, .appointment-start-time-span {
    font-size: 16px;
  }
}

@media (max-width: 1220px) {
  .appointment-image {
    height: 30px;
    width: 30px;
  }

  .appointment-day-label {
    font-size: 45px;
  }

  .completed-label {
    font-size: 22px;
  }

}

@media (max-width: 1154px) {
  .book-appointment-button {
    font-size: 12px;
  }
}

@media (max-width: 1100px) {
  .title-label {
    font-size: 40px;
  }

  .book-appointment-label {
    font-size: 25px;
  }

  .completed-label {
    font-size: 18px;
  }
}

@media (max-width: 850px) {
  .content-section {
    height: 200vh;
  }

  .main-content-section {
    flex-direction: column;
    padding: 15px;
  }

  .title-container {
    height: 10%;
  }

  .appointment-container {
    height: 40%;
    width: 98%;
  }

  .completed-label {
    font-size: 20px;
  }

  .maps-container {
    height: 40%;
    width: 98%;
  }
}

@media (max-width: 680px) {
  .book-appointment-button {
    width: 70%;
    font-size: 8px;
  }

  .appointment-image {
    height: 25px;
    width: 25px;
  }

  .appointment-day-label {
    font-size: 30;
  }

  .appointment-month-day-label, .appointment-start-time-span {
    font-size: 12px;
  }

  .completed-label {
    font-size: 15px;
  }
}

@media (max-width: 560px) {
  .logo-container {
    display: none;
  }
}

@media (max-width: 510px) {
  .title-label {
    font-size: 30px;
  }

  .book-appointment-label {
    font-size: 20px;
  }

  .cancel-button {
    font-size: 15px;
  }
}

@media (max-width: 380px) {
  .title-label {
    font-size: 25px;
  }

  .completed-label {
    font-size: 12px;
  }

  .book-appointment-label {
    font-size: 15px;
  }
}
</style>
