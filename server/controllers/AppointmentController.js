const mqtt = require('../mqtt/mqtt')
const APPOINTMENT_SUB_TOPIC = "test/ScheduleAlert"
const APPOINTMENT_PUB_TOPIC = "test/ScheduleList"



module.exports.getAppointments = async (req, res, next) => {
    try{
        
        mqtt.publishToTopic(APPOINTMENT_PUB_TOPIC)
        console.log("Please give some kind of sign");
        const appointments = await mqtt.connectToTopic(APPOINTMENT_SUB_TOPIC);
        console.log("something before appointments")
        console.log(appointments)
        if(!appointments){
            console.log("That sucks")
            return res.status(404).json({error: 'No appointments found'})
        }
        return res.status(200).json(appointments);

    }catch(error){
        next(error);
    }

}
