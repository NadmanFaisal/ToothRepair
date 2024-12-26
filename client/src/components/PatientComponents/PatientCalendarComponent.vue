<template>
  <div class="col-12 calender-contaiener">
      <div class="col-12 calender-content-container">
          <vue-cal
              class="vuecal--rounded-theme vuecal--green-theme"
              xsmall
              hide-view-selector
              :time="false"
              active-view="month"
              events-count-on-year-view
              :disable-views="['week']"
              :selected-date="selectedDate"
              :events="events"
              @cell-click="updateSelectedDate"
            >
          </vue-cal>
      </div>
  </div>
</template>

<script>
import VueCal from 'vue-cal'
import 'vue-cal/dist/vuecal.css'

export default {
  data() {
    return {
      selectedDate: new Date().toISOString().split('T')[0],
      events: []
    }
  },
  name: 'CalendarComponent',
  components: {
    VueCal
  },
  props: {
    appointments: {
      type: Array,
      default: () => []
    }
  },
  watch: {
    appointments: {
      immediate: true,
      handler(newAppointments) {
        this.updateEvents(newAppointments)
      }
    }
  },
  methods: {
    updateSelectedDate(date) {
      this.selectedDate = new Date(date).toISOString().split('T')[0]
      this.$emit('patientSelectedDate', this.selectedDate)
      console.log('Emitting selectedDate from child:', this.selectedDate)
    },
    updateEvents(appointments) {
      // Update the events field with new appointments upon change in prop
      this.events = []

      // Iterates over all appointments which are available
      appointments
        .filter(appointment => appointment.status === 'available')
        .forEach(appointment => {
          this.events.push({
            start: appointment.date,
            end: appointment.date,
            title: 'Available'
          })
        })

      console.log('Events added to calendar:', this.events)
    }

  }
}
</script>

<style>
.calender-contaiener {
  display: flex;
  flex-direction: column;
  justify-items: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.calender-content-container {
  background-color: white;
  height: 100%;
}

.vuecal__cell-events-count {
  font-size: 8px;
  width: 6px;
  height: 10px;
  background-color: green;
  color: white;
  text-align: center;
  margin-top: 2px;
}

@media (max-width: 1260px) {
  .calender-contaiener {
    height: 100%;
    width: 50%;
  }

}

@media (max-width: 800px) {
  .calender-contaiener {
    height: 100%;
    width: 100%;
  }

}
</style>
