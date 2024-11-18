import { Api } from './Api'

export const patientApi = {

  getTestInfo() {
    return Api.get('/patients')
  }
}
