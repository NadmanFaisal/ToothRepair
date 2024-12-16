#!/bin/bash
apt-get update
cd ~

# Install curl
apt-get install curl -y

# Install Go using curl
curl https://raw.githubusercontent.com/canha/golang-tools-install-script/master/goinstall.sh | bash
export PATH=$PATH:/root/bin
go version

# Install mqtt-benchmark with Go
go install github.com/krylovsk/mqtt-benchmark@main

#     might be useful for CI pipeline
#apt-get update && apt-get install -y curl git
#curl -LO https://go.dev/dl/go1.20.0.linux-amd64.tar.gz
#tar -C /usr/local -xzf go1.20.0.linux-amd64.tar.gz
#export PATH=$PATH:/usr/local/go/bin
#go version
#go install github.com/krylovsk/mqtt-benchmark@main

# Base configuration
base_email="toothrepair356@gmail.com"
broker="wss://193a0f31e34647d9a74f1e130a9238ba.s1.eu.hivemq.cloud:8884/mqtt"
password="1234567890"
clients=100  # Set the amount of clients you wan to run concurrently

# Stress-test authentication service (to signup clients)
for i in $(seq 1 $clients); do
  email="toothrepair356+$i@gmail.com"
  payload="{\"name\":\"Taha\",\"email\":\"$email\",\"password\":\"1234567890\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "authenticationService/patient/signup" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

# Stress-test authentication service (to login clients)
for i in $(seq 1 $clients); do
  email="toothrepair356+$i@gmail.com"
  payload="{\"name\":\"Vaibhav Puram\",\"email\":\"$email\",\"password\":\"1234567890\"}"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "authenticationService/patient/login" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

# Stress-test clinic service (get the map)
for i in $(seq 1 $clients); do
  payload="{\"Get Clinics $i\"}"
  #payload="Get Clinics"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "clinicService/clinic/getClinicAlert" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

# Stress-test schedule service (get appointments)
for i in $(seq 1 $clients); do
  payload="{\"Get Appointments $i\"}"
  #payload="Get Appointments"
  mqtt-benchmark --broker "$broker" --count 1 --clients 1 --insecure --username "Administrator" --password "Vaibhav12Taha" --qos 2 --topic "scheduleService/appointment/getAppointments" --client-prefix "mqtt-client-$i" --payload "$payload" &
done

wait
echo "All benchmarks completed."