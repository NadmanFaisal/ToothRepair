<template>
    <div class="mapContainer" ref="map"></div>
</template>

<script>
import dentistClinicIcon from '../assets/dentistClinicIcon.png'
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

      this.clinics.forEach(clinic => {
        const marker = L.marker([clinic.coordinate.latitude, clinic.coordinate.longitude],
          {
            icon: L.icon({
              iconUrl: dentistClinicIcon,
              iconSize: [28, 28],
              iconAnchor: [14, 10]
            })
          })

        marker.bindTooltip(clinic.name, {
          permanent: true,
          direction: 'top'
        })

        const popUpContent =
        `
        <div>
          <h3>${clinic.name}</h3>
          <p><strong>Address: </strong>${clinic.address}</p>
          <p><strong>Contact Info:</strong> ${clinic.contactInfo.number}, ${clinic.contactInfo.email}</p>
          <p><strong>Open Hours:</strong> ${clinic.openHours}</p>
          <p><strong>Dentists: </strong> ${clinic.dentists}</p>
        </div>
        `
        const popUp = L.popup().setContent(popUpContent)
        marker.bindPopup(popUp)
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
    this.map = L.map(this.$refs.map).setView([57.708870, 11.974560], 10)

    const streetView = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      noWrap: true
    })

    const satelliteView = L.tileLayer('http://{s}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}', {
      maxZoom: 20,
      subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
    })

    streetView.addTo(this.map)

    L.control.layers({
      Street: streetView,
      Satellite: satelliteView
    }).addTo(this.map)

    if (this.clinics && this.clinics.length > 0) {
      this.addMarkers()
    }
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

    .leaflet-tooltip {
      font-weight: bold;
      background-size: small;
    }

</style>
