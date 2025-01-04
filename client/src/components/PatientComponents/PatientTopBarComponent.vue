<template>
    <div class="col-12 top-bar-container">

        <div class="col-3 logo-container">
            <img src="../../assets/app-logo.png" class="logo">
            <label class="logo-label">TEETH REPAIR</label>
        </div>

        <div class="col-6 router-container">
            <div v-for="item in navItems" :key="item.name" @click="navigateTo(item)">
                <h1 class="router-label">
                    {{ item.name }}
                </h1>
            </div>
        </div>

        <div class="col-3 settings-container" @click="navigateToSettingsPage">
            <img src="../../assets/notifications.png" class="notification-label">
            <img src="../../assets/profile-picture.png" class="profile-picture">
            <label class="patient-name-label"> {{ patientUsername || localstorageUsername }}</label>
        </div>

    </div>
</template>

<script>

export default {
  name: 'PatientTopBarComponent',
  props: {
    selectedClinicId: {
      type: String,
      default: null
    },
    patientUsername: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      navItems: [
        { name: 'My Home', route: '/patientHomePage' },
        { name: 'Book Appointments', route: '/patientAppointmentPage' },
        { name: 'My Bookings', route: '/patientMyBookingsPage' },
        { name: 'System Stats', route: '/systemStats' }
        // Add more navigation items here
      ],
      localstorageUsername: localStorage.getItem('Username')
    }
  },
  methods: {
    navigateTo(item) {
      // Does not go to appointment page without clinicID
      if (item.route === '/patientAppointmentPage' && !localStorage.getItem('ClinicID')) {
        alert('No clinic has been selected. Please select a clinic first')
        return
      } else if ((item.route === '/patientAppointmentPage' && localStorage.getItem('ClinicID'))) {
        // Navigates to clinic by setting Query parameter (required for showing appointments)
        this.$router.push({
          path: '/patientAppointmentPage',
          query: {
            clinicId: localStorage.getItem('ClinicID')
          }
        })
        return
      }
      this.$router.push(item.route)
    },
    navigateToSettingsPage() {
      this.$router.push('/patientSettingsPage')
    }
  }
}
</script>

<style scoped>
.top-bar-container {
  display: flex;
  flex-direction: row;
  background-color: white;
  height:7%;
}

.logo-container {
  padding-left: 20px;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.logo {
  height: 45px;
  width: 30px;
}

.logo-label {
  margin-left: 15px;
  color: #1FC2C2;
  text-align: center;
  font-family: Inter;
  font-size: 24px;
  font-style: normal;
  font-weight: 900;
  line-height: normal;
}

.router-container {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.router-label {
  margin-left: 20px;
  color: #040404;
  text-align: center;
  font-family: Inter;
  font-size: 20px;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
}

.settings-container {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.notification-label {
  margin-left: 150px;
  height: 35px;
  width: 35px;
}

.profile-picture {
  margin-left: 10px;;
  height: 50px;
  width: 50px;
}

.patient-name-label {
  padding-left: 10px;
  color: #000;
  text-align: center;
  font-family: Inter;
  font-size: 16px;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
}

@media (max-width: 1260px) {

  .top-bar-container {
    width: 100%;
  }

  .notification-label {
    margin-left: 15px;
    height: 35px;
    width: 35px;
  }

}

@media (max-width: 700px) {

  .logo-container {
    padding-left: 10px;
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .logo {
    height: 35px;
    width: 30px;
  }

  .logo-label {
    margin-left: 5px;
    font-size: 15px;
  }

  .router-label {
    margin-left: 5px;
    font-size: 15px;
  }

  .profile-picture {
    margin-left: 5px;
    height: 40px;
    width: 40px;
  }

  .patient-name-label {
    padding-left: 5px;
    font-size: 10px;
  }

  .notification-label {
    margin-left: 5px;
    height: 25px;
    width: 25px;
  }

}

@media (max-width: 470px) {

  .router-label {
    margin-left: 5px;
    font-size: 10px;
  }

  .profile-picture {
    display: none;
  }

  .patient-name-label {
    padding-left: 5px;
    font-size: 8px;
  }

  .notification-label {
    margin-left: 5px;
    height: 20px;
    width: 20px;
  }

}
</style>
