// server/index.js
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const corsOptions = {
  origin: function (origin, callback) {
    if (origin === 'http://localhost:5001' || origin === 'http://192.168.10.238:8080') {
      callback(null, true);
    } else {
      callback(new Error('Not allowed by CORS'));
    }
  }, 
  credentials: true,               
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'], 
  allowedHeaders: ['Content-Type', 'Authorization'],             
};
const app = express();
const PORT = 5001;

// Middleware
app.use(express.json());

app.use(cors(corsOptions));

// Connect to MongoDB
mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log("MongoDB connected"))
  .catch(err => console.log(err));

// Simple Route
app.get('/', (req, res) => {
  res.send('Hello from the MEVN backend!');
});

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
