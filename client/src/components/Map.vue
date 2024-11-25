<template>
    <div class="mapContainer" ref="map"></div>
</template>

<script>
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'

export default {
  name: 'MapComponent',
  props: {
    clinics: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      map: null,
      markerGroup: null
    }
  },
  methods: {
    addMarkers() {
      if (this.markerGroup) {
        this.markerGroup.clearLayers()
      } else {
        this.markerGroup = L.layerGroup().addTo(this.map)
      }
      console.log('Inside addmarker, check clinics: ' + this.clinics)
      this.clinics.forEach(clinic => {
        const marker = L.marker([clinic.coordinate.latitude, clinic.coordinate.longitude]).addTo(this.map)
        marker.bindPopup(clinic.name)
        this.markerGroup.addLayer(marker)
      })
    }
  },
  watch: {
    clinics: {
      handler(newClinics) {
        console.log('Clinics updated, adding markers: ', newClinics)
        this.addMarkers()
      }
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
