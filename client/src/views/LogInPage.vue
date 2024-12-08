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

            <div class="col-12 welcome-section">
              <h1 class="welcome-title">Welcome</h1>
              <label class="create-account-label">Don't have an account? <span class="highlighted-text" @click="navigateToSignupPage">Click here to create an account</span></label>
            </div>

            <div class="col-12 email-section">
              <label class="email-label">Email</label>
              <input class="form-control email-input" v-model="email" placeholder="example@email.com">
            </div>

            <div class="col-12 password-section">
              <label class="email-label">Password</label>
              <input class="form-control password-input" v-model="password" placeholder="**********">
            </div>

            <div class="col-12 user-type-section">

              <label class="selec-user-label">Select user type: </label>

              <div class="form-check">
                <input
                  class="form-check-input"
                  type="radio"
                  name="userType"
                  id="patient-radio"
                  @change="setDentistFalse()"
                  :checked="!isDentist">
                <label class="form-check-label patient-radio-label" for="patientRadio">
                  I am a Patient
                </label>
              </div>

              <div class="form-check">

                <input
                  class="form-check-input"
                  type="radio"
                  name="userType"
                  id="dentist-radio"
                  @change="setDentistTrue()"
                  :checked="isDentist">
                <label class="form-check-label dentist-radio-label" for="dentistRadio">
                  I am a Dentist
                </label>

              </div>

            </div>

            <div class="col-12 login-button-section">
              <button class="col-8 btn login-button" @click="gotToAppointmentPage">Log In</button>
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
    navigateToSignupPage() {
      this.$router.push('/signup')
    },
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

.welcome-section {
  display: flex;
  flex-direction: column;
  height: 30%;
  align-items: start;
  justify-content: center;

}

.welcome-title {
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 40px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.create-account-label {
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

.email-section, .password-section {
  display: flex;
  flex-direction: column;
  height: 20%;
  align-items: start;
}

.email-label, .password-label {
  padding: 15px;
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.email-input, .password-input {
  border-radius: 15px;
  border: 1px solid #D2D1D1;
  background: #FFF;

  height: 35%;
  width: 100%;
  color: #BBB9B9;
  text-align: left;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.user-type-section {
  display: flex;
  flex-direction: row;
  height: 5%;
  width: 100%;
}

.selec-user-label {
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 16px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

#patient-radio, #dentist-radio {
  margin-left: 5px;
}

.patient-radio-label, .dentist-radio-label {
  margin-left: 10px;
}

.form-check-label {
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 16px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.form-check-input:checked {
  background-color: #1FC2C2;
  border-color: #1FC2C2;
}

.login-button-section {
  display: flex;
  flex-direction: row;
  height: 25%;
  justify-content: center;
}

.login-button {
  border-radius: 15px;
  background: #1FC2C2;
  box-shadow: 0px 4px 4px 0px rgba(31, 194, 194, 0.70);
  color: #FFF;
  height: 25%;
  width: 30%;

  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
}
</style>
