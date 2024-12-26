# Add other services if created in the future
SERVICES=("AuthenticationService" "ClinicService" "NotificationService" "ScheduleService")

# Script to build each services
for SERVICE in "${SERVICES[@]}"
do
  echo "Building $SERVICE..."
  cd "services/$SERVICE" || exit
  mvn clean package || { echo "Build failed for $SERVICE"; exit 1; }
  cd - || exit
done

# Build the docker compose file
docker-compose up --build
