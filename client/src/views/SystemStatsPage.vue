<template>
    <div class="screen-container">
        <p>User count being displayed: {{ userCount }}</p>
        <p>Number of all the available appointments in the system: {{ appointmentCount }}</p>
        <div>Authentication
            <p>Total Messages sent: {{ msgSentAuth }} Total Messages received: {{ msgRecievedAuth }}</p>
        </div>
        <div>Clinic
            <p>Total Messages sent: {{ msgSentClinic }} Total Messages received: {{ msgRecievedClinic }}</p>
        </div>
        <div>Schedule
            <p>Total Messages sent: {{ msgSentSchedule }} Total Messages received: {{ msgRecievedSchedule }}</p>
        </div>
        <div>Notification
            <p>Total Messages sent: {{ msgSentNotification }} Total Messages received: {{ msgRecievedNotification }}</p>
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
      msgSentClinic: 0,
      msgRecievedClinic: 0,
      msgSentSchedule: 0,
      msgRecievedSchedule: 0,
      msgSentNotification: 0,
      msgRecievedNotification: 0

    }
  },

  mounted() {
    client.on('connect', () => {
      this.getUserCount()
      this.getAvailableAppointmentsCount()
      this.getTotalMessagesAuth()
      this.getTotalMessagesClinic()
      this.getTotalMessagesSchedule()
      this.getTotalMessagesNotification()
    })
  },
  unmounted() {
    client.removeAllListeners('message')
    console.log('This page is Unmounted')
  },
  created() {
    this.$watch(
      () => this.$route,
      () => {
        this.getUserCount()
        this.getAvailableAppointmentsCount()
        this.getTotalMessagesAuth()
        this.getTotalMessagesClinic()
        this.getTotalMessagesSchedule()
        this.getTotalMessagesNotification()
      },
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
    async getAvailableAppointmentsCount() {
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
    async getTotalMessagesAuth() {
      await subscribeToTopic('authenticationService/totalMsgSent')
      await subscribeToTopic('authenticationService/totalMsgReceived')
      publishToTopic('authenticationService/totalMsgSentAlert', 'Get messages sent')
      publishToTopic('authenticationService/totalMsgReceivedAlert', 'Get messages received')
      messageArrived((topic, message) => {
        if (topic === 'authenticationService/totalMsgSent') {
          console.log('This is the count of all messages sent to authentication: ', message)
          this.msgSentAuth = message
          unsubscribeFromTopic('authenticationService/totalMsgSent')
        } else if (topic === 'authenticationService/totalMsgReceived') {
          console.log('This is the count of all messages received by authentication : ', message)
          this.msgRecievedAuth = message
          unsubscribeFromTopic('authenticationService/totalMsgReceived')
        }
      })
    },

    async getTotalMessagesSchedule() {
      await subscribeToTopic('scheduleService/totalMsgSent')
      await subscribeToTopic('scheduleService/totalMsgReceived')
      publishToTopic('scheduleService/totalMsgSentAlert', 'Get messages sent')
      publishToTopic('scheduleService/totalMsgReceivedAlert', 'Get messages received')
      messageArrived((topic, message) => {
        if (topic === 'scheduleService/totalMsgSent') {
          console.log('This is the count of all messages sent to schedule : ', message)
          this.msgSentSchedule = message
          unsubscribeFromTopic('scheduleService/totalMsgSent')
        } else if (topic === 'scheduleService/totalMsgReceived') {
          console.log('This is the count of all messages received by schedule : ', message)
          this.msgRecievedSchedule = message
          unsubscribeFromTopic('scheduleService/totalMsgReceived')
        }
      })
    },

    async getTotalMessagesClinic() {
      await subscribeToTopic('clinicService/totalMsgSent')
      await subscribeToTopic('clinicService/totalMsgReceived')
      publishToTopic('clinicService/totalMsgSentAlert', 'Get messages sent')
      publishToTopic('clinicService/totalMsgReceivedAlert', 'Get messages received')
      messageArrived((topic, message) => {
        if (topic === 'clinicService/totalMsgSent') {
          console.log('This is the count of all messages sent to clinic : ', message)
          this.msgSentClinic = message
          unsubscribeFromTopic('clinicService/totalMsgSent')
        } else if (topic === 'clinicService/totalMsgReceived') {
          console.log('This is the count of all messages received by clinic : ', message)
          this.msgRecievedClinic = message
          unsubscribeFromTopic('clinicService/totalMsgReceived')
        }
      })
    },
    async getTotalMessagesNotification() {
      await subscribeToTopic('notificationService/totalMsgSent')
      await subscribeToTopic('notificationService/totalMsgReceived')
      publishToTopic('notificationService/totalMsgSentAlert', 'Get messages sent')
      publishToTopic('notificationService/totalMsgReceivedAlert', 'Get messages received')
      messageArrived((topic, message) => {
        if (topic === 'notificationService/totalMsgSent') {
          console.log('This is the count of all messages sent to notification: ', message)
          this.msgSentNotification = message
          unsubscribeFromTopic('notificationService/totalMsgSent')
        } else if (topic === 'notificationService/totalMsgReceived') {
          console.log('This is the count of all messages received by notification : ', message)
          this.msgRecievedNotification = message
          unsubscribeFromTopic('notificationService/totalMsgReceived')
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
