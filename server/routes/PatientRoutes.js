const express = require('express');
const router = express.Router();
const patientController = require('../controllers/PatientController')
const mqtt = require('mqtt')

router.get('/patients', patientController.getPatients);


module.exports = router;