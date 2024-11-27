<template>
    <div class="row">
        <div class="col-12 top-bar-container">

            <div class="col-3 logo-container">
                <img src="../assets/app-logo.png" class="logo">
                <label class="logo-label">TEETH REPAIR</label>
            </div>

            <div class="col-6 router-container">
            </div>

            <div class="col-3 settings-container">
                <img src="../assets/notifications.png" class="notification-label">
                <img src="../assets/profile-picture.png" class="profile-picture">
                <label class="patient-name-label"> Name of Patient</label>
            </div>

        </div>

        <div class="col-12 content-section">
        </div>
    </div>
</template>

<script>
export default {
  name: 'PatientHomePage'
}
</script>

<style scoped>

.row {
    display: flex;
    flex-direction: column;
    height: 100vh;
}

.top-bar-container {
    display: flex;
    flex-direction: row;
    background-color: white;
    height:7%;
}

.logo-container {
    margin-left: 15px;
    display: flex;
    flex-direction: row;
    align-items: center;
}

.logo {
    height: 45px;
    width: 30px;
}

.logo-label {
    margin-left: 5px;
    color: #1FC2C2;
    text-align: center;
    font-family: Inter;
    font-size: 24px;
    font-style: normal;
    font-weight: 900;
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
    margin-left: 10px;
    color: #000;
    text-align: center;
    font-family: Inter;
    font-size: 16px;
    font-style: normal;
    font-weight: 700;
    line-height: normal;
}

.content-section {
    display: flex;
    flex-direction: row;
    height: 93%;
    background-image: linear-gradient(rgba(62, 180, 137, 0.5), rgba(62, 180, 137, 0.5)), url('@/assets/patient-home-bg-image.jpeg');
    background-size: cover;
}
</style>
