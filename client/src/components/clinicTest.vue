<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <button @click="getTestValue">TEST MQTT Button</button>
    <button @click="getAllClinics">TEST GET CLINICS</button>
    <div class="createClinicContainer">
      <label for="">Name of Clinic:</label>
      <input type="text" v-model="clinicName" placeholder="Enter clinic name...">
      <label for="">Coordinates:</label>
      <input type="number" v-model="latitude" placeholder="Enter clinic latitude here...">
      <input type="number" v-model="longitude" placeholder="Enter clinic longitude here...">
      <label for="">Address:</label>
      <input type="text" v-model="address" placeholder="Enter clinic address...">
      <label for="">Open Hours:</label>
      <input type="text" v-model="openHours" placeholder="Enter clinic openhours...">
      <label for="">ContactInfo:</label>
      <input type="text" v-model="number" placeholder="Enter clinic phone number...">
      <input type="text" v-model="email" placeholder="Enter clinic email...">
      <button @click="createClinic">Add new Clinic</button>

      <button @click="addDentist">Add dentist</button>
    </div>
    <div class="mapCompContainer">
      <p>This is the Leaflet map</p>
      <MapComponent :clinics="clinics"></MapComponent>
    </div>
    <p>
      For a guide and recipes on how to configure / customize this project,<br>
      check out the
      <a href="https://cli.vuejs.org" target="_blank" rel="noopener">vue-cli documentation</a>.
    </p>
    <h3>Installed CLI Plugins</h3>
    <ul>
      <li><a href="https://github.com/vuejs/vue-cli/tree/dev/packages/%40vue/cli-plugin-babel" target="_blank" rel="noopener">babel</a></li>
      <li><a href="https://github.com/vuejs/vue-cli/tree/dev/packages/%40vue/cli-plugin-eslint" target="_blank" rel="noopener">eslint</a></li>
    </ul>
    <h3>Essential Links</h3>
    <ul>
      <li><a href="https://vuejs.org" target="_blank" rel="noopener">Core Docs</a></li>
      <li><a href="https://forum.vuejs.org" target="_blank" rel="noopener">Forum</a></li>
      <li><a href="https://chat.vuejs.org" target="_blank" rel="noopener">Community Chat</a></li>
      <li><a href="https://twitter.com/vuejs" target="_blank" rel="noopener">Twitter</a></li>
      <li><a href="https://news.vuejs.org" target="_blank" rel="noopener">News</a></li>
    </ul>
    <h3>Ecosystem</h3>
    <ul>
      <li><a href="https://router.vuejs.org" target="_blank" rel="noopener">vue-router</a></li>
      <li><a href="https://vuex.vuejs.org" target="_blank" rel="noopener">vuex</a></li>
      <li><a href="https://github.com/vuejs/vue-devtools#vue-devtools" target="_blank" rel="noopener">vue-devtools</a></li>
      <li><a href="https://vue-loader.vuejs.org" target="_blank" rel="noopener">vue-loader</a></li>
      <li><a href="https://github.com/vuejs/awesome-vue" target="_blank" rel="noopener">awesome-vue</a></li>
    </ul>
  </div>
</template>

<script>
import { subscribeToTopic, messageArrived, publishToTopic, publishMsgToTopic, unSubscribeFromTopic } from '../mqtt/mqtt.js'
import MapComponent from './Map.vue'
export default {
  name: 'HelloWorld',
  props: {
    msg: String
  },
  components: {
    MapComponent
  },
  data() {
    return {
      patients: [],
      clinics: [],
      clinicName: '',
      coordinates: { latitude: null, longitude: null },
      address: '',
      openHours: '',
      contactInfo: { number: '', email: '' },
      dentists: [],
      dentistId: null
    }
  },
  methods: {
    async getTestValue() {
      try {
        await subscribeToTopic('test/patientList')
        publishToTopic('test/patientAlert')
        messageArrived((topic, message) => {
          this.patients.push(`${message}`)
          if (topic === 'test/patientList') {
            console.log('Received patient list:', message)
            this.patients.push(message)
          }
        })
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    async getAllClinics() {
      try {
        await subscribeToTopic('test/clinicList')
        publishMsgToTopic('test/clinicAlert', 'Get Clinics')
        messageArrived((topic, message) => {
          if (topic === 'test/clinicList') {
            console.log('Recieved clinic list: ', message)
            this.clinics = message
            unSubscribeFromTopic('test/clinicList')
          }
        })
      } catch (error) {
        console.error('Tried to retrieve all clinics: ', error)
      }
    },
    async createClinic() {
      const newClinic = {
        name: this.clinicName,
        coordinate: { latitude: this.latitude, longitude: this.longitude },
        address: this.address,
        open_hours: this.openHours,
        contact_info: { number: this.number, email: this.email },
        dentists: this.dentists

      }
      try {
        console.log('Publishing clinic information to test/createClinic')
        publishMsgToTopic('test/createClinic', JSON.stringify(newClinic))
      } catch (error) {
        console.error('Tried to create a clinic: ', error)
      }
    },
    async addDentist() {
      publishMsgToTopic('dentist/clinicService/addDentist', '{ "clinicId": "6747869d8bb1b95b44941a56", "dentistId": "757873994" }')
    }
  }

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
.mapCompContainer {
  display: flex;
  align-items: center;
  height: 50vh;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
