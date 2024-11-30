import { createRouter, createWebHistory } from 'vue-router'

import HelloWorld from './components/HelloWorld.vue'
import PatientHomePage from './views/PatientHomePage.vue'
import DentistHomePage from './views/DentistHomePage.vue'

const routes = [
  { path: '/', name: 'helloWorld', component: HelloWorld },
  { path: '/patientHomePage', name: 'patientHome', component: PatientHomePage },
  { path: '/dentistHomePage', name: 'dentistHome', component: DentistHomePage }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
