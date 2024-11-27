<template>
    <div class="col-12 map-container">
        <div class="col-12 map-content-container">
        </div>
    </div>
</template>

<script>
export default {
  name: 'MapComponent'
}
</script>

<style scoped>
.map-container {
    display: flex;
    flex-direction: column;
    justify-items: center;
    align-items: center;
    height: 50%;
    width: 100%;
    padding: 40px;
}

.map-content-container {
    background-color: white;
    height: 100%;
}
</style>
