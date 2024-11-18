const mqtt = require('../mqtt/mqtt')
const PATIENT_SUB_TOPIC = 'test/patientList'
const mqtt = require('mqtt');




exports.getPatients = async (req, res, next) => {
    try{
        mqtt.connectToTopic(PATIENT_SUB_TOPIC)
        mqtt.publishToTopic("test/Authentication")
        const patients =  await mqtt.handleIncomingMessage(PATIENT_SUB_TOPIC);
        if(!patients){
            return res.status(404).json({error: 'No Patients found'})
        }
        return res.status(200).json(patients);

    }catch(error){
        next(error);
    }

}