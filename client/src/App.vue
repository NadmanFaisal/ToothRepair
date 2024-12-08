<template>
  <div id="app">

      <!--<router-link to = "/signup2"> NewSignUp</router-link>-->

      <router-view/>
  </div>
</template>

<script>

export default {

  name: 'App',
  components: {},
  methods: {

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
