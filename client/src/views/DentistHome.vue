<template>
    <div>
        <img alt="Vue logo" src="../assets/logo.png">
        <div>This is the Dentist Page</div>
        <BButton @click="logout"> Log Out button</BButton>
    </div>
</template>

<script>
// import HelloWorld from '../components/HelloWorld.vue'

export default {
  name: 'DentistHome',
  components: {},
  methods:{
    logout() {
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
      this.$router.push('/login')
    }
}

}



</script>

  <style>
  .btn_message {
    margin-bottom: 1em;
  }
  </style>
