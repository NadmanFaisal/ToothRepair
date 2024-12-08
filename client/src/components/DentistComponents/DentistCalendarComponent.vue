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
  name: 'DentistCalendarComponent',
  data() {
    return {
      selectedDate: new Date().toISOString().split('T')[0]
    }
  },
  components: {
    VueCal
  },
  methods: {
    updateSelectedDate(date) {
      this.selectedDate = new Date(date).toISOString().split('T')[0]
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
    height: 50%;
    width: 100%;
    padding: 40px;
}

.calender-content-container {
    background-color: white;
    height: 100%;
}
</style>
