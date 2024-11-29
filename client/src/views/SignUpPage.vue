<template>
    <b-col class="white-background">
      <b-container fluid class="vh-100 d-flex align-items-center justify-content-center">
        <b-row class="justify-content-center align-items-center w-100">
          <b-col md="8" class="signup-blue-container d-flex justify-content-center align-items-center">

            <!-- Navigable Login/SignUp page-->
            <b-col class="login-signup-button-container">
                <BButton type="button" class="login-button" @click="setDentistFalse()">I am a Patient</BButton>
                <BButton type="button" class="login-button" @click="setDentistTrue()">I am a Dentist</BButton>
              <BButton type="button" class="login-button" @click="goToLoginPage()">Log In</BButton>
              <BButton type="button" class="signup-button" @click="goToSignupPage()">Sign up</BButton>
            </b-col>

            <b-col class="white-container-signup p-4">
              <h1 class="signup-title mb-4">Sign Up</h1>

              <!-- the whole SignUpForm -->
              <SignUpForm
                :username="username"
                :email="email"
                :password="password"
                :clinic="clinic"
                :isDentist="isDentist"
                @submit="submitSignUp"
              />

              <!-- checks for error message response -->
              <p
                v-if="message"
                :class="{
                  'text-success': message === 'Sign Up Successful!',
                  'text-danger': message !== 'Sign Up Successful!'
                }"
                class="mt-3"
              >
                {{ message }}
              </p>
            </b-col>
          </b-col>
        </b-row>
      </b-container>
    </b-col>
  </template>

<script>
import SignUpForm from '@/components/SignUpForm.vue'
import { subscribeToTopic, publishValue, messageArrived, unsubscribeFromTopic } from '../mqtt/mqtt.js'

export default {
  name: 'SignUpPage',
  components: {
    SignUpForm
  },

  data() {
    return {
      username: '',
      email: '',
      password: '',
      clinic: '',
      message: '',
      isDentist: false
    }
  },

  methods: {
    // post the email, name and password of the businessOwner and creates a new one in backend
    async submitSignUp({ username, email, password, clinic }) {
      const PUBLISH_PATIENT_TOPIC = 'patient/authentication/signup'
      const PUBLISH_DENTIST_TOPIC = 'dentist/authentication/signup'
      const SUBCRIBE_AUTHENTICATION_TOPIC = 'authentication/status'

      await subscribeToTopic(SUBCRIBE_AUTHENTICATION_TOPIC)
      try {
        if (!clinic) {
          const newPatient = {
            name: username,
            email,
            password
          }
          publishValue(PUBLISH_PATIENT_TOPIC, JSON.stringify(newPatient))
        } else {
          clinic = clinic.id
          const newDentist = {
            name: username,
            email,
            password,
            clinic
          }

          publishValue(PUBLISH_DENTIST_TOPIC, JSON.stringify(newDentist))
        }

        messageArrived((topic, message) => {
          if (topic === SUBCRIBE_AUTHENTICATION_TOPIC) {
            console.log(message)
            alert(message)
            unsubscribeFromTopic('authentication/status')
          }
        })

        // waits a while to display the Sign Up Successful message to user until we move him to login
        setTimeout(() => {
          this.$router.push('/login')
        }, 2000)
        this.message = 'Sign Up Successful!'
      } catch (error) {
        this.message = 'Sign Up Failed: ' + (error.response?.data?.error || error.message)
      }
    },

    // goes to login page
    goToLoginPage() {
      this.$router.push('/login')
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
  .white-background {
    background: #FFF;
    min-height: 100vh;
  }

  .signup-blue-container {
    background: #37F;
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 40px;
    width: 100%;
    max-width: 800px;
    position: relative;
    border-radius: 0;
  }

  .white-container-signup {
    width: 100%;
    max-width: 404px;
    border-radius: 50px;
    background: #FFF;
    box-shadow: -15px 15px 4px 0px rgba(0, 0, 0, 0.25);
    padding: 40px 60px;
  }

  .signup-title {
    font-family: 'Istok Web', sans-serif;
    font-size: 36px;
    font-weight: 700;
    color: #000;
    text-align: center;
  }

  @media (max-width: 768px) {
    .signup-blue-container {
      max-width: 100%;
      padding: 30px;
    }

    .white-container-signup {
      padding: 30px 40px;
    }

    .signup-title {
      font-size: 28px;
    }
  }

  @media (max-width: 576px) {
    .signup-title {
      font-size: 24px;
    }

    .white-container-signup {
      padding: 20px 30px;
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

  button.login-button:hover {
    background-color: rgb(235, 235, 235);
  }

  button.signup-button:hover {
    background-color: rgb(0, 85, 255);
  }

</style>
