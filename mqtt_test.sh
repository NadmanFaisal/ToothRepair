# Base configuration
base_email="toothrepair356@gmail.com"
broker="wss://193a0f31e34647d9a74f1e130a9238ba.s1.eu.hivemq.cloud:8884/mqtt"
clinic_id="674a01ce4d3aa16b1e5f5f4a"
patient_id="67683551693b7b734d000cb3"
dentist_id="67683482693b7b734d000cb1"
password="1234567890"
clients=10  # Set the amount of clients you wan to run concurrently (left at 10 because this is very demanding to the local broker, only run 1000s for manual testing)

# Stress-test authentication service (to signup clients)
for i in $(seq 1 $clients); do
  email="toothrepair356+$i@gmail.com"
  payload="{\"name\":\"Taha\",\"email\":\"$email\",\"password\":\"1234567890\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "authenticationService/patient/signup" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

wait

# Stress-test authentication service (to login clients)
for i in $(seq 1 $clients); do
  email="toothrepair356+$i@gmail.com"
  payload="{\"name\":\"Vaibhav Puram\",\"email\":\"$email\",\"password\":\"1234567890\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "authenticationService/patient/login" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

wait

# Stress-test clinic service (get the map)
for i in $(seq 1 $clients); do
  payload="{\"Get Clinics $i\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "clinicService/clinic/getClinicAlert" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

wait

# Stress-test clinic service (get clinic by clinic id)
for i in $(seq 1 $clients); do
  payload=$clinic_id
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "ClinicService/Clinic/getClinicById" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

wait

# Stress-test schedule service (get appointments)
for i in $(seq 1 $clients); do
  payload="{\"Get Appointments $i\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "ScheduleService/Appointment/getAppointments" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

wait

# Stress-test schedule service (get available appointments)
for i in $(seq 1 $clients); do
  payload="{\"Get Available Appointments $i\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "scheduleService/appointment/getAvailableAppointmentsAlert" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

wait

# Stress-test schedule service (get appointments by clinic) 
for i in $(seq 1 $clients); do
  payload="{\"clinic\": \"$clinic_id\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "ScheduleService/Appointment/getAppointmentsByClinic" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

#wait

# Stress-test schedule service (get appointments by patient)
for i in $(seq 1 $clients); do
  payload="{\"patient\": \"$patient_id\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "ScheduleService/Appointment/getAppointmentsByPatient" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

#wait

# Stress-test schedule service (get appointments by dentist)
for i in $(seq 1 $clients); do
  payload="{\"dentist\": \"$dentist_id\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "ScheduleService/Appointment/getAppointmentsByDentist" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

wait
echo "All benchmarks completed."