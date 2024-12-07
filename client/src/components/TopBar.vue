<template>
    <div class="col-12 top-bar-container">

        <div class="col-3 logo-container">
            <img src="../assets/app-logo.png" class="logo">
            <label class="logo-label">TEETH REPAIR</label>
        </div>

        <div class="col-6 router-container">
            <div v-for="item in navItems" :key="item.name">
                <h1 class="router-label">
                    <router-link :to="item.route" class="nav-link">
                        {{ item.name }}
                    </router-link>
                </h1>
            </div>
        </div>

        <div class="col-3 settings-container">
            <img src="../assets/notifications.png" class="notification-label">
            <img src="../assets/profile-picture.png" class="profile-picture">
            <label class="patient-name-label"> Name of Patient</label>
        </div>

    </div>
</template>

<script>
export default {
  name: 'TopBarComponent',
  data() {
    return {
      navItems: [
        { name: 'My Home', route: '/patientHomePage' },
        { name: 'My Bookings', route: '/patientAppointmentPage' }
        // Add more navigation items here
      ]
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
</style>
