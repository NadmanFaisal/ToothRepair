<template>
  <div class="col-12 calender-contaiener">
      <div class="col-12 calender-content-container">
          <vue-cal
              class="vuecal--rounded-theme vuecal--green-theme"
              xsmall
              hide-view-selector
              :time="false"
              active-view="month"
              :disable-views="['week']"
              :selected-date="selectedDate"
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
      selectedDate: new Date().toISOString().split('T')[0]
    }
  },
  name: 'CalendarComponent',
  components: {
    VueCal
  },
  methods: {
    updateSelectedDate(date) {
      this.selectedDate = new Date(date).toISOString().split('T')[0]
      this.$emit('patientSelectedDate', this.selectedDate)
      console.log('Emitting selectedDate from child:', this.selectedDate)
    }
  }
}
</script>

<style scoped>
.calender-contaiener {
  display: flex;
  flex-direction: column;
  justify-items: center;
  align-items: center;
  width: 100%;
  height: 50%;
  padding: 40px;
}

.calender-content-container {
  background-color: white;
  height: 100%;
}

@media (max-width: 1260px) {
  .calender-contaiener {
    height: 100%;
    width: 50%;
  }

}

@media (max-width: 800px) {
  .calender-contaiener {
    height: 50%;
    width: 100%;
  }

}
</style>
