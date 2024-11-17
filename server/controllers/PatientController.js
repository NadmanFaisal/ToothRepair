const mqtt = require('../mqtt/mqtt')
const PATIENT_SUB_TOPIC = '/AuthencticationService/patients'
const mqtt = require('mqtt');

mqtt.connectToTopic(PATIENT_SUB_TOPIC)

exports.getPatients = async (req, res, next) => {
try{
    const patients = mqtt.handleIncomingMessage(PATIENT_SUB_TOPIC);
    if(!patients){
        return res.status(404).json({error: 'No Patients found'})
    }
    return res.status(200).json(patients);

}catch(error){
    next(error);
}
}