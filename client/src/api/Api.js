import axios from 'axios'

axios.defaults.withCredentials = true

export const Api = axios.create({
  baseURL: 'http://localhost:5001',
  withCredentials: true // for the axios to push cookies, incase the defaulter didnt work
})
