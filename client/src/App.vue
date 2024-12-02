<template>
  <div id="app">

      <router-link to = "/login"> LogIn</router-link>
      <br>
      <router-link to = "/signup"> SignUp</router-link>
      <br>
      <router-link to = "/patientHomePage"> Patient Home</router-link>
      <br>
      <router-link to = "/dentistHomePage"> Dentist Home</router-link>
      <br>
      <router-link to = "/mapView">Map View Page</router-link>
      <BButton @click="logout"> Log Out button</BButton>

      <!--<router-link to = "/signup2"> NewSignUp</router-link>-->

      <router-view/>
  </div>
</template>

<script>

export default {

  name: 'App',
  components: {},
  methods: {
    logout() {
      document.cookie = 'userInfo=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;'
      this.$router.push('/login')
    }
  }

}

</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  padding-left: 0px !important;
}

@media (max-width: 757px){
  #nav {
  display: flex;
  flex-direction: column;
  }
}
</style>
