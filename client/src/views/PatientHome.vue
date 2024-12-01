<template>
    <div>
        <img alt="Vue logo" src="../assets/logo.png">
        <HelloWorld msg="Welcome to Your Patient Home Page"/>
    </div>
</template>

<script>
import HelloWorld from '../components/HelloWorld.vue'

export default {
  name: 'PatientHome',
  components: {
    HelloWorld
  }
}

</script>

  <style>
  .btn_message {
    margin-bottom: 1em;
  }
  </style>
