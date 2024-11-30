<template>
    <div class="col-12 calender-contaiener">
        <div class="col-12 calender-content-container">
        </div>
    </div>
</template>

<script>
export default {
  name: 'DentistCalendarComponent'
}
</script>

<style scoped>
.calender-contaiener {
    display: flex;
    flex-direction: column;
    justify-items: center;
    align-items: center;
    height: 50%;
    width: 100%;
    padding: 40px;
}

.calender-content-container {
    background-color: white;
    height: 100%;
}
</style>
