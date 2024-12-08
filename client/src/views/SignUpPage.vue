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
              <h1 class="register-title">Register</h1>
              <label class="login-label">Already have an account? <span class="highlighted-text" @click="navigateToLoginPage">Log in!</span></label>
          </div>

          <div class="col-12 patient-dentist-selection-section">
            <button type="button" class="btn patient-button" :class="{ activeButton: !isDentist }" @click="setDentistFalse()">Patient</button>
            <button type="button" class="btn dentist-button" :class="{ activeButton: isDentist }" @click="setDentistTrue()">Dentist</button>
          </div>

          <div class="col-12 username-section">
            <label class="username-label">Name</label>
            <input class="form-control username-input" v-model="username" placeholder="Your name...">
          </div>

          <div class="col-12 middle-middle-section">

            <div class="col-6 middle-left-section">

              <div class="col-12 email-section">
                <label class="email-label">Email</label>
                <input class="form-control email-input" v-model="email" placeholder="example@email.com">
              </div>

              <div class="col-12 password-section">
                <label class="password-label">Password</label>
                <input class="form-control password-input" v-model="password" placeholder="**********">
              </div>

            </div>

            <div class="col-6 middle-right-section">

              <div class="col-12 phone-section">
                <label class="phone-label">Phone</label>
                <input class="form-control phone-input" v-model="phone" placeholder="073*******">
              </div>

              <div class="col-12 confirm-password-section">
                <label class="confirm-password-label">Confirm Password</label>
                <input class="form-control confirm-password-input" v-model="confirmPassword" placeholder="**********">
              </div>

            </div>

          </div>

          <div class="col-12 bottom-section">

            <div class="col-12 dropdown clinic-container" v-if="isDentist">

              <button
                class="btn btn-secondary dropdown-toggle clinic-dropdown-button"
                type="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
                text="Select a clinic"
                >
                {{ selectedClinicName || 'Select a clinic' }}
              </button>

              <ul class="dropdown-menu">
                <li class="dropdown-item" v-for="clinic in clinics" :key="clinic.id" @click="selectAClinic(clinic)">{{ clinic.name }}</li>
              </ul>

            </div>

            <button class="col-8 btn signup-button" @click="submitSignUp">Sign Up</button>
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
      phone: null,
      confirmPassword: '',
      clinics: [],
      selectedClinicId: null,
      selectedClinicName: null,
      message: '',
      isDentist: false
    }
  },

  methods: {
    navigateToLoginPage() {
      this.$router.push('/login')
    },
    selectAClinic(clinic) {
      this.selectedClinicId = clinic.id
      this.selectedClinicName = clinic.name
    },
    // post the email, name and password of the businessOwner and creates a new one in backend
    async submitSignUp() {
      if (this.password !== this.confirmPassword) {
        alert('Passwords do not match. Try again.')
        return
      }

      const PUBLISH_PATIENT_TOPIC = 'patient/authentication/signup'
      const PUBLISH_DENTIST_TOPIC = 'dentist/authentication/signup'
      const SUBCRIBE_AUTHENTICATION_TOPIC = 'authentication/status'
      const emailVerification = /^[^\s@]+@[^\s@]+.[^\s@]+$/
      console.log('This is the clinics ' + this.selectedClinicId)
      try {
        await subscribeToTopic(SUBCRIBE_AUTHENTICATION_TOPIC)
        if (this.isDentist) {
          if (this.username && this.password && emailVerification.test(this.email) && this.selectedClinicId) {
            const newDentist = {
              name: this.username,
              email: this.email,
              password: this.password,
              clinic: this.selectedClinicId
            }

            publishValue(PUBLISH_DENTIST_TOPIC, JSON.stringify(newDentist))

            setTimeout(() => {
              this.$router.push('/login')
            }, 2000)
          } else {
            alert('Error: Input field left empty, please provide values for all input fields')
          }
        } else {
          if (this.username && this.password && emailVerification.test(this.email)) {
            const newPatient = {
              name: this.username,
              email: this.email,
              password: this.password
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
  height: 15%;
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

.patient-dentist-selection-section {
  display: flex;
  flex-direction: row;
  height: 10%;
}

.patient-button {
  width: 50%;
  height: 65%;
  border-radius: 10px 0px 0px 10px;
  border: 1px solid #D2D1D1;
  color: #1FC2C2;
  text-align: center;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.dentist-button {
  width: 50%;
  height: 65%;
  border-radius: 0px 10px 10px 0px;
  border: 1px solid #D2D1D1;
  color: #1FC2C2;
  text-align: center;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.activeButton {
  background: #1FC2C2;
  color: #FFF;
  text-align: center;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.username-section {
  display: flex;
  flex-direction: column;
  height: 15%;
  align-items: start;
}

.username-label {
  padding: 5px;
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.username-input {
  border-radius: 15px;
  border: 1px solid #D2D1D1;
  background: #FFF;

  height: 45%;
  width: 100%;
  color: #BBB9B9;
  text-align: left;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.middle-middle-section {
  display: flex;
  flex-direction: row;
  height: 35%;
  flex-wrap: wrap;
}

.middle-left-section, .middle-right-section {
  display: flex;
  flex-direction: column;
}

.email-section, .password-section, .phone-section, .confirm-password-section {
  padding: 5px;
  display: flex;
  flex-direction: column;
  height: 50%;
  align-items: start;
}

.email-label, .password-label, .phone-label, .confirm-password-label {
  padding: 5px;
  color: #515151;
  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 600;
  line-height: normal;
}

.email-input, .password-input, .phone-input, .confirm-password-input {
  border-radius: 15px;
  border: 1px solid #D2D1D1;
  background: #FFF;

  height: 45%;
  width: 100%;
  color: #BBB9B9;
  text-align: left;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.bottom-section {
  height: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.clinic-dropdown-button {
  border-radius: 15px;
  border: 1px solid #D2D1D1;
  background: #FFF;
  width: 100%;

  color: #BBB9B9;
  text-align: left;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.signup-button {
  margin-top: 20px;
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

@media screen and (max-width: 750px) {
  .right-section {
    display: none;
  }

  .left-section {
    flex: 1;
  }

  .logo-title {
    align-self: center;
    text-align: start;
  }

  .login-button {
    margin-top: 20px;
  }
}
</style>
