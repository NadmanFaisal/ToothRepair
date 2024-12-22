import { createRouter, createWebHistory } from 'vue-router'
import PatientAppointmentPage from './views/PatientAppoinementPage.vue'
import DentistHomePage from './views/DentistHomePage.vue'
import LogInPage from './views/LogInPage.vue'
import SignUpPage from './views/SignUpPage.vue'
import MapView from './views/MapView.vue'
import PatientHomePage from './views/PatientHomePage.vue'
import PatientMyBookingsPage from './views/PatientMyBookingsPage.vue'
import DentistMyBookingsPage from './views/DentistMyBookingsPage.vue'
import SystemStatsPage from './views/SystemStatsPage.vue'
import PatientSettingsPage from './views/PatientSettingsPage.vue'
import DentistSettingsPage from './views/DentistSettingsPage.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/patientAppointmentPage', name: 'patientAppointmentPage', component: PatientAppointmentPage, meta: { requiresRole: 'patient' } },
  { path: '/dentistHomePage', name: 'dentistHome', component: DentistHomePage, meta: { requiresRole: 'dentist' } },
  { path: '/signup', name: 'SignUpPage', component: SignUpPage, meta: { guestOnly: true } },
  { path: '/login', name: 'LogInPage', component: LogInPage, meta: { guestOnly: true } },
  { path: '/mapView', name: 'MapView', component: MapView, meta: { requiresRole: 'patient' } },
  { path: '/systemStats', name: 'SystemStats', component: SystemStatsPage },
  { path: '/patientHomePage', name: 'PatientSelectionScreen', component: PatientHomePage, meta: { requiredRole: 'patient' } },
  { path: '/patientMyBookingsPage', name: 'PatientMyBookingsPage', component: PatientMyBookingsPage, meta: { requiredRole: 'patient' } },
  { path: '/dentistMyBookingsPage', name: 'DentistMyBookingsPage', component: DentistMyBookingsPage, meta: { requiredRole: 'dentist' } },
  { path: '/patientSettingsPage', name: 'PatientSettingsPage', component: PatientSettingsPage, meta: { requiredRole: 'patient' } },
  { path: '/dentistSettingsPage', name: 'DentistSettingsPage', component: DentistSettingsPage, meta: { requiredRole: 'dentist' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function getUserInfoCookie() {
  const cookies = document.cookie.split('; ')
  const userInfoCookie = cookies.find(cookie => cookie.startsWith('userInfo='))

  if (userInfoCookie) {
    const encodedUserInfo = userInfoCookie.split('=')[1]
    try {
      const parsedCookie = JSON.parse(atob(encodedUserInfo))
      console.log(parsedCookie)
      return parsedCookie
    } catch (error) {
      console.error('Error decoding userInfo cookie:', error)
    }
  }
  return null
}

router.beforeEach((to, from, next) => {
  const userInfo = getUserInfoCookie()
  if (to.meta.requiresRole) {
    if (!userInfo || userInfo.role !== to.meta.requiresRole) {
      next(userInfo ? `/${userInfo.role}HomePage` : '/login')
      return
    }
  }
  if (to.meta.guestOnly && userInfo) {
    next(`/${userInfo.role}HomePage`)
    return
  }

  next()
})

export default router
