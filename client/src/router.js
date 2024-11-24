import { createRouter, createWebHistory } from 'vue-router'
import App from '../src/App.vue'
import LogInPage from './views/LoginPage.vue'
import SignUpPage from './views/SignUpPage.vue'
const routes = [
    {path: '/', name: 'app', component: App},
    {path: '/signUpPage', name: 'signUpPage', component: SignUpPage},
    {path: '/logInPage', name: 'logInPage', component: LogInPage},
]

const router = createRouter({
    history: createWebHistory(),
    routes
})





export default router
