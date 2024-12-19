<template>
    <div class="col-12 map-container">
      <div class="col-12 map-content-container" ref="map">
      </div>
    </div>
</template>

<script>
import dentistClinicIcon from '../../assets/dentistClinicIcon.png'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import { store } from '../../store'

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
    navigateToClinic(clinic) {
      console.log('Will work')
      console.log(clinic.name)
      store.setSelectedClinic(clinic)
      this.$router.push({
        path: '/patientAppointmentPage',
        query: {
          clinicId: clinic.id
        }
      })
    },
    addMarkers() {
      console.log('Access addMarkers')
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

        const dentistNames = clinic.dentists.map(dentist => dentist.dentistName).join(', ')
        const popUpContent = document.createElement('div')
        popUpContent.innerHTML =
        `
        <div>
          <h3>${clinic.name}</h3>
          <p><strong>Address: </strong>${clinic.address}</p>
          <p><strong>Contact Info:</strong> ${clinic.contactInfo.number}, ${clinic.contactInfo.email}</p>
          <p><strong>Open Hours:</strong> ${clinic.openHours}</p>
          <p><strong>Dentists: </strong> ${dentistNames || 'No dentists registered'} </p>
          <button id="navigate-btn">Navigate</button>
        </div>
          `

        const button = popUpContent.querySelector('#navigate-btn')
        button.addEventListener('click', () => this.navigateToClinic(clinic))

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
      },
      deep: true
    }
  },
  mounted() {
    this.map = L.map(this.$refs.map).setView([57.708870, 11.974560], 4)

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
      console.log('Here in map clinics: ', this.clinics)
      this.addMarkers()
    }
  }
}

</script>

<style>
.map-container {
  display: flex;
  max-width: 100%;
  height: 50%;
  position: relative;
  overflow: hidden;
  padding: 40px;
}

.map-content-container {
  background-color: white;
  height: 100%;
}

.leaflet-tooltip {
  font-weight: bold;
  background-size: small;
}

@media (max-width: 1260px) {
  .map-container {
    height: 100%;
    max-width: 50%;
    padding: 40px;
  }

}

@media (max-width: 800px) {
  .map-container {
    height: 50%;
    max-width: 100%;
    padding: 40px;
  }

}
</style>
