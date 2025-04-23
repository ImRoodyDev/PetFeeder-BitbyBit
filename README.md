# 🐾 Automated Pet Feeder System | School Challenge Project

[![Status](https://img.shields.io/badge/status-completed-success)](https://github.com/imroodydev)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Challenge Project](https://img.shields.io/badge/Challenge-Project-orange)](https://github.com/imroodydev)

<p align="center">
  <img src="https://via.placeholder.com/800x400?text=Pet+Feeder+Project" alt="Pet Feeder Project" width="600">
</p>

## 🌟 Project Overview

This automated pet feeder system ensures your furry friends are fed on time, every time! The system combines hardware components (load cell, servo motor, ESP8266, and Micro:bit) with a custom Java application to schedule and monitor feeding times.

The system precisely measures food weight using a load cell, dispenses food with a servo motor, and connects to a centralized server to store feeding data and allow remote management.

## 📁 Repository Structure

## 🔍 Components

<div style="background-color: #f8f9fa; padding: 20px; border-radius: 10px; margin-bottom: 20px;">

### <img src="https://img.shields.io/badge/3D-Model-FF6A00?style=flat-square&logo=blender&logoColor=white" alt="3D Model" /> 3D Model

The `/3DModel` folder contains all the STL files and design documents for the 3D printed components of the pet feeder. These parts were designed to fit the electronic components perfectly while maintaining an aesthetically pleasing look.

### <img src="https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=java&logoColor=white" alt="Java" /> <img src="https://img.shields.io/badge/JavaFX-007396?style=flat-square&logo=java&logoColor=white" alt="JavaFX" /> Application

The `/Application` folder houses the Java application that provides a user interface for setting up feeding schedules, monitoring food levels, and viewing feeding history. Built with JavaFX for a modern, responsive UI that communicates with the server to send commands to the feeder.

### <img src="https://img.shields.io/badge/ESP8266-E7352C?style=flat-square&logo=espressif&logoColor=white" alt="ESP8266" /> ESP8266

The `/ESP8266` folder contains Arduino code that runs on the ESP8266 WiFi module. This code handles communication between the physical feeder and the server, as well as controlling the servo motor based on received commands.

### <img src="https://img.shields.io/badge/Micro:bit-00ED00?style=flat-square&logo=microbit&logoColor=white" alt="Micro:bit" /> Micro:bit

The `/MicroBit` folder includes the code that runs on the BBC Micro:bit, which serves as a secondary controller and provides additional functionality to the system.

### <img src="https://img.shields.io/badge/Node.js-43853D?style=flat-square&logo=node.js&logoColor=white" alt="Node.js" /> <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL" /> Server

The `/Server` folder contains the Node.js backend that acts as the middleware between the Java application and the feeder hardware. It uses Express for API endpoints and Sequelize ORM with MySQL to store feeding schedules, history, and configuration data securely and efficiently.

</div>

## 💻 Technologies Used

<div style="display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; margin-bottom: 20px;">
    <img src="https://img.shields.io/badge/Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white" alt="Arduino">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java">
    <img src="https://img.shields.io/badge/JavaFX-007396?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX">
    <img src="https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white" alt="Node.js">
    <img src="https://img.shields.io/badge/Express-000000?style=for-the-badge&logo=express&logoColor=white" alt="Express">
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
    <img src="https://img.shields.io/badge/Sequelize-52B0E7?style=for-the-badge&logo=Sequelize&logoColor=white" alt="Sequelize">
    <img src="https://img.shields.io/badge/ESP8266-E7352C?style=for-the-badge&logo=espressif&logoColor=white" alt="ESP8266">
    <img src="https://img.shields.io/badge/Micro:bit-00ED00?style=for-the-badge&logo=microbit&logoColor=white" alt="Micro:bit">
</div>

## ⚙️ Setup Instructions

<div style="background-color: #f0fff4; padding: 15px; border-left: 5px solid #48BB78; margin-bottom: 20px;">

1. Print the 3D model components
2. Assemble the hardware according to the schematics in the documentation
3. Flash the ESP8266 and Micro:bit with their respective code
4. Set up the server:
   ```bash
   cd Server
   npm install
   # Configure .env with your MySQL credentials
   npm start
   ```
5. Launch the Java application and connect to the server
6. Configure your feeding schedule and enjoy!

</div>

## 🏆 Challenge Project Story

<div style="background-color: #fff5f5; padding: 20px; border-radius: 8px; border-left: 5px solid #e53e3e; margin-bottom: 20px;">

I didn't win the trophy for this school challenge, but I felt it needed to be posted because I worked incredibly hard on this project. It represents days without sleeping (a lot of sleepless nights), troubleshooting, and determination to create something functional.

Every line of code, every 3D print, every circuit connection has a story of persistence behind it. This project taught me that success isn't just about winning trophies—it's about what you learn along the way.

I'm a warrior made by <a href="https://github.com/imroodydev">@imroodydev</a>, ready to tackle any coding challenge! Hopefully, this project showcases my skills and dedication as I search for employment opportunities in the tech industry.

</div>

## 📬 Contact

<div style="background-color: #ebf8ff; padding: 20px; border-radius: 8px; margin-bottom: 20px;">

<p align="center">
  <a href="https://github.com/imroodydev">
    <img src="https://img.shields.io/badge/GitHub-imroodydev-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub">
  </a>
  <a href="mailto:your-email@example.com">
    <img src="https://img.shields.io/badge/Email-Contact_Me-D14836?style=for-the-badge&logo=gmail&logoColor=white" alt="Email">
  </a>
  <a href="https://linkedin.com/in/imroodydev">
    <img src="https://img.shields.io/badge/LinkedIn-Connect-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn">
  </a>
</p>

</div>

<p align="center">
  <img src="https://img.shields.io/badge/Made%20with-❤️%20and%20☕-red.svg" alt="Made with love">
  <img src="https://img.shields.io/badge/By-imroodydev-blue.svg" alt="By imroodydev">
</p>
