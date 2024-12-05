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

            <div class="col-12 dental-treatment-content">
              <img src="../assets/hospital.png" class="hospital-image">
              <label class="dental-treatment-label-header">Dental Treatment</label>
              <label class="dental-treatment-label-paragraph">Dental treatment encompasses a variety of procedures aimed at maintaining oral health</label>
            </div>

          </div>

          <div class="col-6 dental-cosmetic-container">

            <div class="col-12 dental-cosmetic-content">
              <img src="../assets/teeth.png" class="teeth-image">
              <label class="dental-cosmetic-label-header">Dentistry Cosmetic</label>
              <label class="dental-cosmetic-label-paragraph">Cosmetic dentistry focuses on improving the appearance of teeth, gums, and smiles.</label>
            </div>

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
  height: 50%;
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

.dental-treatment-container, .dental-cosmetic-container {
  padding: 8%;
  display: flex;
  flex-direction: column;
  height: 50%;
}

.dental-treatment-content, .dental-cosmetic-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #FFF;
  padding: 10px;
}

.hospital-image, .teeth-image {
  margin-top: 10%;
  align-self: center;
  height: 35%;
  width: 35%;
}

.dental-cosmetic-label-header, .dental-treatment-label-header {
  color: #015C5C;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
  text-align: start;
}

.dental-treatment-label-paragraph, .dental-cosmetic-label-paragraph {
  color: #015C5C;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 200;
  line-height: normal;
  text-align: start;
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
