<template>
    <div class="mapContainer" ref="map"></div>
</template>

<script>
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'

export default {
  name: 'MapComponent',
  data() {
    return {
      map: null
    }
  },
  mounted() {
    this.map = L.map(this.$refs.map, L.CRS.Simple).setView([57.708870, 11.974560], 7)

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      noWrap: true
    }).addTo(this.map)
  }
}

</script>
<style>
    .mapContainer {
        width: 400px;
        height: 250px;
        position: relative;
        overflow: hidden;
        border-style: solid;
    }
</style>
