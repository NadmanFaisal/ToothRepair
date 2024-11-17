# src: https://medium.com/@muhammadnaqeeb/dockerizing-a-node-js-and-express-js-app-9cb31cf9139e

FROM node
WORKDIR /app
COPY package.json /app
RUN npm install
COPY . /app
CMD ["node","index.js"]