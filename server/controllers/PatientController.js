//const mqtt = require('mqtt');
const mqtt = require('../mqtt/mqtt')
const PATIENT_SUB_TOPIC = "test/patientList"



module.exports.getPatients = async (req, res, next) => {
    try{
        
        //mqtt.publishToTopic("test/Authentication")
        console.log("Please give some kind of sign");
        const patients = await mqtt.connectToTopic(PATIENT_SUB_TOPIC);
        console.log("something before patients")
        console.log(patients)
        if(!patients){
            console.log("That sucks")
            return res.status(404).json({error: 'No Patients found'})
        }
        return res.status(200).json(patients);

    }catch(error){
        next(error);
    }

}