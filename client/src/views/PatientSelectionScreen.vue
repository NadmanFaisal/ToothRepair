<template>

    <div class="screen-container">

      <TopBar />

      <div class="col-9 content-section">
        <div class="col-7 left-section">

          <div class="col-12 description-contaienr">

            <label class="description-title-label">The Complete Dental Experience</label>

            <label class="description-content-label">At our practice, we believe that every smile tells a story,
              and we're here to help yours shine its brightest.
              With a commitment to gentle care and the latest in dental technology,
              we make your comfort our top priority.
            </label>

          </div>

          <div class="col-6 dental-treatment-container">

          </div>

          <div class="col-6 dental-cosmetic-container">

          </div>

        </div>

        <div class="col-5 right-section">

          <div class="col-12 book-appointment-container">

          </div>

        </div>
      </div>

    </div>

</template>

<script>
import TopBar from '../components/TopBar.vue'
export default {
  name: 'PatientSelectionScreen',
  components: {
    TopBar
  },
  data() {
    return {

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
  flex-direction: row;
  height: 93%;
}

.left-section {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  height: 100%;
}

.description-contaienr {
  display: flex;
  flex-direction: column;
  height: 45%;
  justify-content: flex-end;
}

.description-title-label {
  color: #FFF;
  font-family: Inter;
  font-size: 64px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
  text-align: left
}

.description-content-label {
  color: #FFF;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 300;
  line-height: normal;
  text-align: left
}

.dental-treatment-container {
  display: flex;
  flex-direction: column;
  height: 55%;
}

.dental-cosmetic-container {
  display: flex;
  flex-direction: column;
  height: 55%;
}

.right-section {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.book-appointment-container {
  display: flex;
  flex-direction: column;
}
</style>
