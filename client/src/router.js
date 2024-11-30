import { createRouter, createWebHistory } from 'vue-router'
import Home from './views/Home.vue'
import LogInPage from './views/LogInPage.vue'
import SignUpPage from './views/SignUpPage.vue'

const routes = [
  { path: '/', name: 'app', component: Home },
  { path: '/signup', name: 'SignUpPage', component: SignUpPage },
  { path: '/login', name: 'LogInPage', component: LogInPage }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})



export default router
