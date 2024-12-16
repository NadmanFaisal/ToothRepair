<template>
    <div class="screen-container">
        <p>User count being displayed: {{ userCount || 'potato'}}</p>
        <p>Number of messages in the whole system</p>
        <p>CPU Usage maybe???</p>
    </div>
</template>
<script>
import { messageArrived, subscribeToTopic, client, publishToTopic } from '@/mqtt/mqtt'

export default {
    name: 'SystemStats',
    data() {
        return {
            userCount: 0
        }
    },
    mounted() {
    client.on('connect', () => {
        this.getUserCount()
    })
    },
    methods: {
        async getUserCount() {
            await subscribeToTopic('authenticationService/dentist&patient/userCount')
            publishToTopic('authenticationService/users/getActiveUsersAlert', 'Send Active Users')
            messageArrived((topic, message) => {
                if (topic === 'authenticationService/dentist&patient/userCount') {
                    console.log('This is the user count: ', message)
                    this.userCount = message
                }
            })
        }
    }
}
</script>

<style>
    .screen-container {
        display: flex;
        flex-direction: column;
        height: 100vh;
    }
</style>