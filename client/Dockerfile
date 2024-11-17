# src: https://v2.vuejs.org/v2/cookbook/dockerize-vuejs-app.html?redirect=true

# build stage
FROM node:lts-alpine
RUN npm install -g http-server
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

EXPOSE 8080
CMD [ "http-server", "dist" ]