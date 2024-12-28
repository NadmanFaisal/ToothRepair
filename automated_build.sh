#!/bin/bash

# Project service directory names
SERVICES=("AuthenticationService" "ClinicService" "NotificationService" "ScheduleService")

# Docker image services names in GitLab Container Registry
IMAGES=("authentication-service" "clinic-service" "notification-service" "schedule-service")

REGISTRY="registry.git.chalmers.se/courses/dit355/2024/student_teams/dit356_2024_20/dit-356-project-group-20"

for i in "${!SERVICES[@]}"
do
  SERVICE="${SERVICES[$i]}"
  IMAGE="${IMAGES[$i]}"

  echo "Building $SERVICE -> $IMAGE..."

  cd "services/$SERVICE" || exit

  mvn clean package || { echo "Build failed for $SERVICE"; exit 1; }

  docker build -t "$REGISTRY/$IMAGE" .

  docker push "$REGISTRY/$IMAGE"

  cd - || exit
done

# Deploy the services as stack to Docker Swarm
docker stack deploy -c docker-compose.yml microservices_stack
