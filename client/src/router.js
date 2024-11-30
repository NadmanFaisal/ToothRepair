import { createRouter, createWebHistory } from 'vue-router'
import Home from './views/Home.vue'
import LogInPage from './views/LogInPage.vue'
import SignUpPage from './views/SignUpPage.vue'
import SignUpPage2 from './views/SignUpPage2.vue'

const routes = [
  { path: '/', name: 'app', component: Home },
  { path: '/signup', name: 'SignUpPage', component: SignUpPage },
  { path: '/login', name: 'LogInPage', component: LogInPage },
  { path: '/signup2', name: 'SignUpPage2', component: SignUpPage2 }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
