<template>
  <div class="col-12 screen-container">

    <div class="col-7 left-section">

      <div class="col-12 logo-section">
        <img src="../assets/app-logo.png" class="logo-image">
        <h1 class="logo-title">TEETH REPAIR</h1>
      </div>

      <div class="col-12 login-content-section">

        <div class="col-2 left-empty-section">
        </div>

        <div class="col-8 middle-login-section">
          <div class="col-12 register-section">
              <h1 class="register-title">Welcome</h1>
              <label class="login-label">Already have an account? <span class="highlighted-text" @click="navigateToLoginPage">Log in!</span></label>
          </div>
        </div>

        <div class="col-2 right-empty-section">
        </div>

      </div>

    </div>

    <div class="col-5 right-section">
    </div>

  </div>
</template>

<script>
import { subscribeToTopic, publishValue, messageArrived, unsubscribeFromTopic } from '../mqtt/mqtt.js'

export default {
  name: 'SignUpPage',

  data() {
    return {
      username: '',
      email: '',
      password: '',
      clinics: [],
      message: '',
      isDentist: false
    }
  },

  methods: {
    navigateToLoginPage() {
      this.$router.push('/login')
    },
    // post the email, name and password of the businessOwner and creates a new one in backend
    async submitSignUp({ username, email, password, clinic }) {
      const PUBLISH_PATIENT_TOPIC = 'patient/authentication/signup'
      const PUBLISH_DENTIST_TOPIC = 'dentist/authentication/signup'
      const SUBCRIBE_AUTHENTICATION_TOPIC = 'authentication/status'
      const emailVerification = /^[^\s@]+@[^\s@]+.[^\s@]+$/
      console.log('This is the clinics ' + clinic?.id)
      try {
        await subscribeToTopic(SUBCRIBE_AUTHENTICATION_TOPIC)
        if (this.isDentist) {
          if (username && password && emailVerification.test(email) && clinic?.id) {
            const newDentist = {
              name: username,
              email,
              password,
              clinic: clinic?.id
            }

            publishValue(PUBLISH_DENTIST_TOPIC, JSON.stringify(newDentist))

            setTimeout(() => {
              this.$router.push('/login')
            }, 2000)
          } else {
            alert('Error: Input field left empty, please provide values for all input fields')
          }
        } else {
          if (username && password && emailVerification.test(email)) {
            const newPatient = {
              name: username,
              email,
              password
            }
            publishValue(PUBLISH_PATIENT_TOPIC, JSON.stringify(newPatient))

            setTimeout(() => {
              this.$router.push('/login')
            }, 2000)
          } else {
            alert('Error: Input field left empty, please provide values for all input fields')
          }
        }
        messageArrived((topic, message) => {
          if (topic === SUBCRIBE_AUTHENTICATION_TOPIC) {
            console.log(message)
            alert(message)
            unsubscribeFromTopic('authentication/status')
          }
        })

        // waits a while to display the Sign Up Successful message to user until we move him to login

        /*

          */
      } catch (error) {
        this.message = 'Sign Up Failed: ' + (error.response?.data?.error || error.message)
      }
    },

    async getAllClinics() {
      const SUBCRIBED_CLINIC_TOPIC = 'clinicService/clinicList'
      const PUBLISHED_CLINIC_TOPIC = 'dentist/clinicService/alert'
      const publishMessage = 'Get Clinics'
      await subscribeToTopic(SUBCRIBED_CLINIC_TOPIC)
      publishValue(PUBLISHED_CLINIC_TOPIC, publishMessage)
      messageArrived((topic, message) => {
        if (topic === SUBCRIBED_CLINIC_TOPIC) {
          console.log('Received clinics list:', message)
          this.clinics = JSON.parse(message)
          unsubscribeFromTopic(SUBCRIBED_CLINIC_TOPIC)
        }
      })
    },

    // goes to login page
    goToLoginPage() {
      this.$router.push('/login')
    },
    goToSignUpPage() {
      this.$router.push('/signup')
    },
    setDentistTrue() {
      this.isDentist = true
      this.getAllClinics()
    },
    setDentistFalse() {
      this.isDentist = false
    }
  }
}
</script>

<style scoped>
.screen-container {
  display: flex;
  flex-direction: row;
  height: 100vh;
}

.left-section {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.right-section {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-image: linear-gradient(0deg, rgba(31, 194, 194, 0.24) 0%, rgba(31, 194, 194, 0.24) 100%), url('@/assets/login-image.jpeg');
  background-size: contain;

}

.logo-section {
  display: flex;
  flex-direction: row;
  height: 15%;
  align-items: center;
}

.logo-image {
  margin-left: 40px;
  height: 55%;
}

.logo-title {
  margin-left: 20px;
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 40px;
  font-style: normal;
  font-weight: 900;
  line-height: normal;
}

.login-content-section {
  display: flex;
  flex-direction: row;
  height: 85%;
  flex-wrap: wrap;
}

.register-section {
  display: flex;
  flex-direction: column;
  height: 30%;
  align-items: start;
  justify-content: center;

}

.register-title {
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 40px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.login-label {
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 16px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.highlighted-text {
  color: #1FC2C2;
  font-family: Inter;
  font-size: 16px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
  text-decoration: underline;
}
</style>
