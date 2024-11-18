// server/index.js
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = 5001;

const corsOptions = {
  origin: 'http://localhost:5173',
  credentials: true
}

var patientRoute = require('./routes/PatientRoutes')
// Middleware
app.use(cors(corsOptions));


// Connect to MongoDB
mongoose.connect(process.env.MONGO_URI)
.then(() => console.log("MongoDB connected"))
.catch(err => console.log(err));


// Simple Route
app.get('/', (req, res) => {
  res.send('Hello from the MEVN backend!');
});


app.use(patientRoute)

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
