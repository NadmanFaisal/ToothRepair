const express = require('express');
const router = express.Router();
const patientController = require('../controllers/PatientController')

router.get('/Patients/', patientController.getPatients);
