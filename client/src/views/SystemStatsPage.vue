<template>
    <div class="screen-container">
        <p>User count being displayed: {{ userCount }}</p>
        <p>Number of all the available appointments in the system: {{ appointmentCount }}</p>
        <div>Authentication
            <p>Total Messages sent: {{ msgSentAuth }} Total Messages received: {{ msgRecievedAuth }}</p>
        </div>
        <div>Schedule
            <p>Total Messages sent: {{  }} Total Messages received: {{  }}</p>
        </div>
        <div>Clinic
            <p>Total Messages sent: {{  }} Total Messages received: {{  }}</p>
        </div>
        <div>Notification
            <p>Total Messages sent: {{  }} Total Messages received: {{  }}</p>
        </div>
    
    </div>
</template>
<script>
import { messageArrived, subscribeToTopic, client, publishToTopic, unsubscribeFromTopic } from '@/mqtt/mqtt'

export default {
    name: 'SystemStats',
    data() {
        return {
            userCount: 0,
            appointmentCount: 0,
            msgRecievedAuth: 0,
            msgSentAuth: 0,

        }
    },
    
    mounted() {
        client.on('connect', () => {
            this.getUserCount()
            this.getAvailableAppointmentsCount()
            this.getTotalMessagesAuth()
        })
    },
    created(){
        this.$watch(
      () => this.$route,
        this.getUserCount,
        this.getAvailableAppointmentsCount,
        this.getTotalMessagesAuth,
      { immediate: true }
    )
    },
    methods: {
        async getUserCount() {
            await subscribeToTopic('authenticationService/dentist&patient/userCount')
            publishToTopic('authenticationService/users/getActiveUsersAlert', 'Send Active Users')
            messageArrived((topic, message) => {
                if (topic === 'authenticationService/dentist&patient/userCount') {
                    console.log('This is the user count: ', message)
                    this.userCount = message
                    unsubscribeFromTopic('authenticationService/dentist&patient/userCount')
                }
            })
        },
        async getAvailableAppointmentsCount(){
            await subscribeToTopic('scheduleService/availableAppointmentCount')
            publishToTopic('scheduleService/appointment/getAvailableAppointmentsAlert', 'Get Available Appointments')
            messageArrived((topic, message) => {
                if (topic === 'scheduleService/availableAppointmentCount') {
                    console.log('This is the count of all available appointments: ', message)
                    this.appointmentCount = message
                    unsubscribeFromTopic('scheduleService/availableAppointmentCount')
                }
            })
        },
        async getTotalMessagesAuth(){
            await subscribeToTopic('authenticationService/totalMsgSent')
            await subscribeToTopic('authenticationService/totalMsgReceived')
            publishToTopic('authenticationService/totalMsgSentAlert', 'Get messages sent')
            publishToTopic('authenticationService/totalMsgReceivedAlert', 'Get messages received')
            messageArrived((topic, message) => {
                if (topic === 'authenticationService/totalMsgSent') {
                    console.log('This is the count of all messages sent to authentication: ', message)
                    this.msgSentAuth = message;
                    unsubscribeFromTopic('authenticationService/totalMsgSent')
                }else if('authenticationService/totalMsgReceived'){
                    console.log('This is the count of all messages received by authentication : ', message)
                    this.msgRecievedAuth = message;
                    unsubscribeFromTopic('authenticationService/totalMsgReceived')
                }
            })
            
        },
        
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