import { createRouter, createWebHistory } from 'vue-router'

import HelloWorld from './components/HelloWorld.vue'
import PatientHomePage from './views/PatientHomePage.vue'

const routes = [
  { path: '/', name: 'helloWorld', component: HelloWorld },
  { path: '/homePage', name: 'patientHome', component: PatientHomePage }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
