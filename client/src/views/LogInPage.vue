<template>
    <div class="split-screen">
      <div class="left-section">
        <b-container fluid>
          <b-row>
            <b-col cols="12" class="company-info">
              <div class="company-name"></div>
              <div class="company-motto"></div>
              <div class="company-description"></div>
            </b-col>
          </b-row>
        </b-container>
      </div>

      <div class="right-section">
        <b-container fluid>
          <b-row>
            <b-col cols="12">
              <div class="login-signup-button-container">
                <BButton type="button" class="login-button" @click="setDentistFalse()">I am a Patient</BButton>
                <BButton type="button" class="login-button" @click="setDentistTrue()">I am a Dentist</BButton>
                <BButton type="button" class="login-button">Log In</BButton>
                <BButton type="button" class="signup-button" @click="goToSignupPage()">Sign up</BButton>
              </div>
            </b-col>
            <b-col cols="12">
              <div class="login-container">
                <h2>Login</h2>
                <form @submit.prevent="loginUser()">
                  <div class="detail-input-group">
                    <label for="email">Email</label>
                    <BFormInput
                      type="email"
                      v-model="email"
                      id="email"
                      placeholder="Enter your email"
                      required
                    />
                  </div>

                  <div class="detail-input-group">
                    <label for="password">Password</label>
                    <BFormInput
                      type="password"
                      v-model="password"
                      id="password"
                      placeholder="Enter your password"
                      required
                    />
                  </div>

                  <div class="check-box-group">
                    <span>Don't have an account?&nbsp;</span>
                    <router-link to="/signup">Create one</router-link>
                  </div>

                  <BButton type="submit" class="login-submit-button">Log In</BButton>
                </form>
                <div v-if="error" class="error-message">
                  {{ error }}
                </div>
              </div>
            </b-col>
          </b-row>
        </b-container>
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
      const PUBLISH_DENTIST_LOGIN_ALERT = 'dentist/authentication/login'
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
            console.log("This is the stored User ID: " +JSON.stringify(message))
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

  .split-screen {
    flex-direction: row;
    display: flex;
    height: 100vh;
  }

  .left-section {
    flex: 3;
    background-color: #FFFFFF;
    display: flex;
    height: 100%;
    align-items: flex-start;
    justify-content: flex-start;
    position: relative;
  }

  .right-section {
    flex: 2;
    background-color: #3377FF;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  @media (max-width: 757px) {
    .split-screen {
      flex-direction: column;
      height: auto;
    }

    .left-section {
      display: none;
    }

    .right-section {
      flex: 1;
      min-height: 100vh;
      display: flex;
      justify-content: center;
      align-items: center;
      position: relative;
    }

    .login-container {
      width: 80%;
    }

  }

  .login-signup-button-container {
    position: absolute;
    top: 50px;
    right: 50px;
    display: flex;
    gap: 20px;
  }

  button.login-button {
    width: 130px;
    height: 50px;
    border: none;
    border-radius: 50px;
    background: #FFF;
    color: #37F;
    font-family: "Istok Web";
    font-size: 16px;
    font-weight: 400;
    box-shadow: inset 0px 4px 4px rgba(0, 0, 0, 0.25);
  }

  button.signup-button {
    width: 130px;
    height: 50px;
    border: none;
    border-radius: 50px;
    background: #37F;
    color: #FFF;
    font-family: "Istok Web";
    font-size: 16px;
    font-weight: 400;
    line-height: normal;
    box-shadow: inset 0px 4px 4px rgba(0, 0, 0, 0.25);
  }

  h2 {
    display: flex;
    justify-content: center;
    align-items: center;

    color: #000;
    text-align: center;
    font-family: "Istok Web", sans-serif;
    font-size: 40px;
    font-style: normal;
    font-weight: 700;
    line-height: normal;
  }

  .login-container {
    max-width: 400px;
    height: 500px;
    padding: 40px;
    border: 1px solid #ccc;
    border-radius: 50px;
    background: #FFF;
    box-shadow: -15px 15px 4px 0px rgba(0, 0, 0, 0.25);
    margin: auto;
  }

  label[for="email"], label[for="password"] {
    display: flex;
    width: 163px;
    height: 42px;
    flex-direction: column;
    justify-content: center;
    flex-shrink: 0;

    color: #000;
    text-align: left;
    font-family: "Istok Web", sans-serif;
    font-size: 20px;
    font-style: normal;
    font-weight: 700;
    line-height: normal;
    padding-left: 10px;
  }

  .detail-input-group {
    margin-bottom: 25px;
  }

  .check-box-group {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 25px;
  }

  input {
    width: 100%;
    height: 60px;
    padding: 15px;
    border-radius: 50px;
    background: rgba(204, 204, 204, 0.50);
    border: none;
    box-shadow: inset 0px 4px 4px rgba(0, 0, 0, 0.25);
    font-size: 16px;
    outline: none;
  }

  button.login-submit-button {
    width: 80%;
    height: 59px;
    flex-shrink: 0;
    border: none;
    border-radius: 50px;
    background: #37F;
    text-align: center;
    font-family: "Istok Web";
    color: #FFF;
    font-size: 24px;
    font-style: normal;
    font-weight: 400;
    line-height: normal;
  }

  button.login-submit-button:hover {
    background-color: rgb(0, 85, 255);
  }

  button.login-button:hover {
    background-color: rgb(235, 235, 235);
  }

  button.signup-button:hover {
    background-color: rgb(0, 85, 255);
  }

  .left-texture-image {
    width: 458px;
    height: 456px;
    position: absolute;
    margin: 85px;
    opacity: 0.4;
  }

  .right-texture-image {
    width: 458px;
    height: 456px;
    position: absolute;
    bottom: 0;
    right: 0;
    transform: rotate(180deg);
    margin: 85px;
    opacity: 0.4;
  }

  .company-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%); /* Adjusts the position to center of left section */
  }

  .company-name {
    color: #787676;
    font-family: "Istok Web";
    font-size: 96px;
    font-weight: 700;
  }

  .company-motto {
    display: flex;
    width: 586px;
    height: 152px;
    flex-direction: column;
    justify-content: center;
    flex-shrink: 0;
    color: #37F;
    font-family: "Istok Web";
    font-size: 40px;
    font-weight: 700;
  }

  .company-description {
    display: flex;
    width: 633px;
    height: 191px;
    flex-direction: column;
    justify-content: center;
    flex-shrink: 0;
    color: #606060;
    font-family: "Istok Web";
    font-size: 20px;
    font-weight: 400;
  }

  .error-message {
    margin-top: 10px;
    color: red;
  }
  </style>
