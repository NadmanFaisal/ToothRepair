<template>
    <div class="col-12 appointment-container">
        <div class="col-12 appointment-content-container">
        </div>
    </div>
</template>

<script>
export default {
  name: 'AppointmentComponent'
}
</script>

<style scoped>
.appointment-container {
    display: flex;
    flex-direction: column;
    justify-items: center;
    align-items: center;
    height: 100%;
    width: 100%;
    padding: 40px;
}

.appointment-content-container {
    background-color: white;
    height: 100%;
}
</style>
