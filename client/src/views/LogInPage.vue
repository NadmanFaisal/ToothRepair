<template>
    <div class="col-12 screen-container">

      <div class="col-7 left-section">

        <div class="col-12 logo-section">
        </div>

        <div class="col-12 welcome-section">
        </div>

        <div class="col-12 email-continer">
          <label class="email-label">Email</label>
          <input class="form-control email-input" v-model="email">
        </div>

        <div class="col-12 password-continer">
          <label class="email-label">Password</label>
          <input class="form-control email-input" v-model="password">
        </div>

        <div class="col-12 user-type-section">
          <BButton type="button" class="login-button" @click="setDentistFalse()">I am a Patient</BButton>
          <BButton type="button" class="login-button" @click="setDentistTrue()">I am a Dentist</BButton>
        </div>

        <div class="col-12 login-button-section">
          <Bbutton type="button" @click="loginUser">Login</Bbutton>
        </div>

      </div>

      <div class="col-5 right-section">
      </div>

    </div>

</template>

<script>
import { subscribeToTopic, publishValue, messageArrived, unsubscribeFromTopic } from '../mqtt/mqtt.js'
export default {
  name: 'LogInPage',
  data() {
    return {
      email: '',
      password: '',
      isDentist: null,
      error: null
    }
  },
  methods: {
    async loginUser() {
      if (this.isDentist === null) {
        alert('Please specify your role.')
        return
      }
      this.error = null
      const PUBLISH_PATIENT_LOGIN_ALERT = 'patient/authentication/login'
      const PUBLISH_DENTIST_LOGIN_ALERT = 'dentist/authetication/login'
      const SUBCRIBE_AUTHENTICATION_ALERT = 'authentication/alert/login'
      const SUBSCRIBE_USER_ID = 'authentication/userID'

      await subscribeToTopic(SUBCRIBE_AUTHENTICATION_ALERT)
      await subscribeToTopic(SUBSCRIBE_USER_ID)
      try {
        const logInData = {
          email: this.email,
          password: this.password
        }

        if (this.isDentist) {
          publishValue(PUBLISH_DENTIST_LOGIN_ALERT, JSON.stringify(logInData))
        } else {
          publishValue(PUBLISH_PATIENT_LOGIN_ALERT, JSON.stringify(logInData))
        }

        messageArrived((topic, message) => {
          if (topic === SUBCRIBE_AUTHENTICATION_ALERT) {
            console.log(message)

            if (message === 'User is sucessfully logged in!') {
              const userInfo = {
                email: this.email,
                role: this.isDentist ? 'dentist' : 'patient'
              }
              console.log(userInfo)
              const encodedUserInfo = btoa(JSON.stringify(userInfo))
              console.log(encodedUserInfo)
              document.cookie = `userInfo=${encodedUserInfo}; path=/; max-age=3600`
              console.log(document.cookie)

              if (this.isDentist) {
                this.$router.push('/dentistHomePage')
              } else {
                this.$router.push('/patientHomePage')
              }
            }
            setTimeout(function () {
              alert(message)
            }, 500)
            unsubscribeFromTopic(SUBCRIBE_AUTHENTICATION_ALERT)
          } else if (topic === SUBSCRIBE_USER_ID) {
            localStorage.setItem('UserID', JSON.stringify(message))
            console.log('This is the stored User ID: ' + JSON.stringify(message))
            unsubscribeFromTopic(SUBSCRIBE_USER_ID)
          }
        })
      } catch (err) {
        console.error(err)
        this.error = err.response?.data?.message || 'An error occurred during login'
      }
    },

    goToSignupPage() {
      this.$router.push('/signup')
    },
    setDentistTrue() {
      this.isDentist = true
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
  height: 100vh;
}

.right-section {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-image: linear-gradient(0deg, rgba(31, 194, 194, 0.24) 0%, rgba(31, 194, 194, 0.24) 100%), url('@/assets/login-image.jpeg');
  background-size: contain;

}

</style>
