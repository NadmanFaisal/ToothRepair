export const store = {
  state: {
    selectedClinicId: null,
    selectedClinicName: null
  },
  setSelectedClinic(clinic) {
    this.state.selectedClinicId = clinic.id
    this.state.selectedClinicName = clinic.name
  },
  getSelectedClinicId() {
    return this.state.selectedClinicId
  },
  getSelectedClinicName() {
    return this.state.selectedClinicName
  },
  reset() {
    this.state.selectedClinicId = null
    this.state.selectedClinicName = null
  }
}
