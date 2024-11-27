<template>
  <div class="hello">
    <img alt="Vue logo" src="../assets/logo.png">
    <h1>{{ msg }}</h1>
    <button @click="getTestValue">TEST MQTT Button</button>
    <p>{{ this.patients }}</p>
    <button @click="getAppointments">Test appointments</button>
    <p>{{ this.appointments }}</p>
    <button @click="pushPatientHomePage">Move to homepage</button>
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
import { subscribeToTopic, messageArrived, publishToTopic } from '../mqtt/mqtt.js'
export default {
  name: 'HelloWorld',
  props: {
    msg: String
  },
  data() {
    return {
      patients: [],
      appointments: []
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
    async getAppointments() {
      try {
        await subscribeToTopic('test/appointmentList')
        publishToTopic('test/appointmentAlert')
        messageArrived((topic, message) => {
          this.appointments.push(`${message}`)
          if (topic === 'test/appointmentList') {
            console.log('Received Appointment list:', message)
            this.appointments.push(message)
          }
        })
      } catch (error) {
        console.error('This bombaclaat wont work' + error)
      }
    },
    pushPatientHomePage() {
      this.$router.push('/homePage')
    }
  }

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
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
