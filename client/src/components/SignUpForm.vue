<template>
    <div>
      <b-container fluid>
        <b-row class="justify-content-center">
          <b-col xs="12" md="10" class="mb-3">
            <!-- User name input -->
            <label class="signup-label" for="username">Username</label>
            <b-form-input
              id="username"
              v-model="localUsername"
              placeholder="Enter your username"
              class="signup-input"
            ></b-form-input>
          </b-col>
          <!-- Email input -->
          <b-col xs="12" md="10" class="mb-3">
            <label class="signup-label" for="email">Email</label>
            <b-form-input
              type="email"
              id="email"
              v-model="localEmail"
              placeholder="Enter your email"
              class="signup-input"
            ></b-form-input>
          </b-col>
          <!-- Password input -->
          <b-col xs="12" md="10" class="mb-3">
            <label class="signup-label" for="password">Password</label>
            <b-form-input
              type="password"
              id="password"
              v-model="localPassword"
              placeholder="Enter your password"
              class="signup-input"
            ></b-form-input>
          </b-col>

          <b-col xs="12" md="10" class="mb-3" v-if="isDentist">
            <label class="signup-label" for="password">Clinic</label>
            <b-dropdown :text=" localClinic.name ||'Select a clinic'">
                <b-dropdown-item v-for="clinic in clinics" :key="clinic.id" @click="localClinic = clinic">{{clinic.name}}, {{clinic.address}}</b-dropdown-item>
            </b-dropdown>
          </b-col>
        </b-row>

        <b-row class="justify-content-center">
          <b-col xs="12" md="10">
            <b-button class="signup-component-box w-100" @click="handleSubmit">
              <span class="signup-component-text">Sign Up</span>
            </b-button>
          </b-col>
        </b-row>
      </b-container>
    </div>
  </template>
<script>
import { subscribeToTopic, messageArrived, publishValue, unsubscribeFromTopic, client } from '../mqtt/mqtt.js'
export default {
  props: {
    username: String,
    email: String,
    password: String,
    clinic: String,
    isDentist: Boolean
  },
  data() {
    return {
      localUsername: this.username,
      localEmail: this.email,
      localPassword: this.password,
      localClinic: this.clinic,
      // clinics: [{ id: 1234, name: 'Gothenburg Teeth Repair' }, { id: 12345, name: 'Dentists in GB' }, { id: 123456, name: 'Healthy Teeth' }, { id: 12, name: 'GB Nice Tooth spot' }],
      clinics: []
    }
  },
  mounted() {
    client.on('connect', () => {
      this.getAllClinics()
    })
  },

  methods: {
    // emit values to parent class, which is the SignUpPage.vue
    handleSubmit() {
      this.$emit('submit', {
        username: this.localUsername,
        email: this.localEmail,
        password: this.localPassword,
        clinic: this.localClinic
      })
    },
    async getAllClinics(){
      const SUBCRIBED_CLINIC_TOPIC = "clinicService/clinicList"
      const PUBLISHED_CLINIC_TOPIC = "dentist/clinicService/alert"
      const publishMessage = "Get Clinics"
      await subscribeToTopic(SUBCRIBED_CLINIC_TOPIC)
      publishValue(PUBLISHED_CLINIC_TOPIC, publishMessage)
      messageArrived((topic, message) => {
          if (topic === SUBCRIBED_CLINIC_TOPIC) {
            console.log('Received clinics list:', message)
            this.clinics = JSON.parse(message)
            unsubscribeFromTopic(SUBCRIBED_CLINIC_TOPIC)
          }
      })
    }


  }

}
</script>

  <style scoped>
  .signup-input {
    border-radius: 20px;
    background: rgba(204, 204, 204, 0.50);
    box-shadow: inset 0px 2px 2px rgba(0, 0, 0, 0.25);
    padding: 8px 12px;
    font-size: 16px;
  }

  .signup-label {
    font-family: 'Istok Web', sans-serif;
    font-size: 20px;
    font-weight: 700;
    color: #000;
  }

  .signup-component-box {
    border-radius: 30px;
    background: #37f;
    padding: 10px 20px;
    font-size: 18px;
    width: 100%;
  }

  .signup-component-text {
    color: #fff;
    font-family: 'Istok Web', sans-serif;
    font-size: 18px;
  }

  @media (max-width: 768px) {
    .signup-input {
      font-size: 14px;
      padding: 6px 10px;
    }

    .signup-component-box {
      font-size: 16px;
    }
  }

  @media (max-width: 576px) {
    .signup-label {
      font-size: 18px;
    }

    .signup-component-box {
      font-size: 14px;
    }
  }
  </style>
