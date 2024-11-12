import axios from 'axios'

axios.defaults.withCredentials = true

export const Api = axios.create({
    baseURL: import.meta.env.VITE_API_ENDPOINT || 'http://localhost:3000/v1/api',
    withCredentials: true // for the axios to push cookies, incase the defaulter didnt work
  })

  axios.interceptors.response.use(
    response => {
      return response
    },
    error => {
      if (error.response.status === 401) {
        console.error('Unauthorized - redirecting to login')
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }
  )