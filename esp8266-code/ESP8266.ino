#include <vector>
#include <algorithm> 
#include <EEPROM.h>
#include <SoftwareSerial.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <WebSocketsClient.h>
#include "HX711.h"
#define EEPROM_SIZE 128
#define WEBSOCKETS_LOGLEVEL 3
#define NUM_READINGS 10

// Hardware identifiers
String MAC = "c3d2aefc2745";
String hardwareID = "5bb95ebb-1b78-440c-b652-7d38079b22f8";

// WiFi credentials (default values for first run)
char* ssid = nullptr;        
char* password = nullptr;    

// Default server configurations
const char* defaultHttpServer = "http://192.168.137.1:3002/api/hardware";
const char* defaultWebSocketServer = "192.168.137.1";
const int webSocketPort = 3002;
String httpServer;
String webSocketServer;

// NTP setup
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", 3600, 60000);

// WebSocket setup
WebSocketsClient webSocket;

// Store parsed and sorted alarm times
std::vector<String> alarmTimes; 
// Index of the next alarm
int nextAlarmIndex = 0; 
unsigned long previousMillis = 0;
unsigned long previousMillis2 = 0;

int portionSize =  0;
int lastCheckedDay = -1;

// HX711 circuit wiring
HX711 scale;
float tareOffset = 0;
float scalingFactor = 229.54; // Adjust this for your scale
float readings[NUM_READINGS]; // Array to store the last N readings
const int LOADCELL_DOUT_PIN = D7;
const int LOADCELL_SCK_PIN = D8;
int readIndex = 0;
float total = 0;
float average = 0;

// Check if its feeding
bool feeding = false;
int feedTimeout = 2000;
int previousFeedMilis = 0;
 
// System function
void sendMessageToMicrobit(String message) {
    while (Serial.availableForWrite() < message.length()) {
        delay(10); // Wait for buffer space
    }
    Serial.print(message );
    Serial.flush(); // Ensure the message is fully transmitted
}

// clear EEPROM
void clearEEPROM() {
   EEPROM.begin(EEPROM_SIZE);

  int ipStartAddress = 100; // Offset where IP address starts
  int ipLen = EEPROM.read(ipStartAddress); // Length of IP address stored
  int ipEndAddress = ipStartAddress + 1 + ipLen; // Calculate the end address of the IP

  for (int i = 0; i < EEPROM_SIZE; i++) {
    // Skip the IP address range
    if (i >= ipStartAddress && i <= ipEndAddress) {
      continue;
    }
    EEPROM.write(i, 0); // Clear other regions
  }

  EEPROM.commit();
  // Serial.println("[DEBUG] EEPROM cleared except for IP address!");
}
// Save Site IP Adress
void saveCompanyIP(const char* ip) {
  EEPROM.begin(EEPROM_SIZE);
  
  clearEEPROM(); // Clear EEPROM before writing if needed

  int ipLen = strlen(ip);

  // Save IP length and data
  int ipStartAddress = 100; // Example offset, adjust as per your EEPROM usage
  EEPROM.write(ipStartAddress, ipLen); // Save length of IP
  for (int i = 0; i < ipLen; i++) {
    EEPROM.write(ipStartAddress + 1 + i, ip[i]);
  }

  EEPROM.commit();
  // Serial.println("[DEBUG] Company IP saved to EEPROM!");
}
// Get IP Adres Server
String readCompanyIP() {
  EEPROM.begin(EEPROM_SIZE);

  int ipStartAddress = 100; // Same offset used in saveCompanyIP
  int ipLen = EEPROM.read(ipStartAddress);

  if (ipLen == 0) {
    // Serial.println("[DEBUG] No Company IP stored in EEPROM.");
    return String(); // Return an empty string if no IP is stored
  }

  char ip[ipLen + 1];
  for (int i = 0; i < ipLen; i++) {
    ip[i] = EEPROM.read(ipStartAddress + 1 + i);
  }
  ip[ipLen] = '\0'; // Null-terminate the IP string

  // Serial.println("[DEBUG] Company IP read from EEPROM: " + String(ip));
  return String(ip);
}
// Save wifi NAME
void saveSSID(const char* _ssid) {
  EEPROM.begin(EEPROM_SIZE);
  clearEEPROM(); // Clear EEPROM before writing

  int ssidLen = strlen(_ssid);

  // Save SSID length and data
  EEPROM.write(0, ssidLen); // Save length of SSID
  for (int i = 0; i < ssidLen; i++) {
    EEPROM.write(1 + i, _ssid[i]);
  }

  EEPROM.commit();
  // Serial.println("[DEBUG] SSID saved to EEPROM!");
}
// Save wifi passwoord
void savePassword(const char* _password) {
 EEPROM.begin(EEPROM_SIZE);
  // Assuming SSID is already saved, get the length of SSID stored
  int ssidLen = EEPROM.read(0);

  int passLen = strlen(_password);

  // Save Password length and data
  EEPROM.write(1 + ssidLen, passLen); // Save length of Password
  for (int i = 0; i < passLen; i++) {
    EEPROM.write(2 + ssidLen + i, _password[i]);
  }

  EEPROM.commit();
  // Serial.println("[DEBUG] Password saved to EEPROM!");
}
// Function to read WiFi credentials from EEPROM
void readWiFiCredentials(char*& _ssid, char*& _password) {
   // Read SSID length
  int ssidLen = EEPROM.read(0);
  
  // If SSID length is 0, set _ssid to nullptr and return
  if (ssidLen == 0) {
    _ssid = nullptr;
  } else {
    _ssid = new char[ssidLen + 1]; // Allocate memory for SSID
    for (int i = 0; i < ssidLen; i++) {
      _ssid[i] = EEPROM.read(1 + i);
    }
    _ssid[ssidLen] = '\0'; // Null-terminate the SSID
  }

  // Read Password length
  int passLen = EEPROM.read(1 + ssidLen);
  
  // If password length is 0, set _password to nullptr
  if (passLen == 0) {
    _password = nullptr;
  } else {
    _password = new char[passLen + 1]; // Allocate memory for Password
    for (int i = 0; i < passLen; i++) {
      _password[i] = EEPROM.read(2 + ssidLen + i);
    }
    _password[passLen] = '\0'; // Null-terminate the Password
  }

  if (_ssid == nullptr || _password == nullptr) {
    // Serial.println("[DEBUG] WiFi credentials are empty or not stored in EEPROM.");
  } else {
    // Serial.println("[DEBUG] WiFi credentials read from EEPROM:");
    // Serial.println("[DEBUG] SSID: " + String(_ssid));
    // Serial.println("[DEBUG] Password: " + String(_password));
  }
}


// WebSocket event handler
void webSocketEvent(WStype_t type, uint8_t *payload, size_t length) {
 switch (type) {
    case WStype_DISCONNECTED:
      // Serial.println("[DEBUG] WebSocket Disconnected");
      break;
    case WStype_CONNECTED:
      // Serial.println("[DEBUG] WebSocket Connected");
        break;
    case WStype_TEXT:
      // Serial.printf("[DEBUG] WebSocket Message: %s\n", payload);
      
      // Send beep signal to microbit
      sendMessageToMicrobit("BEEP\n");

      // Check if the payload contains "TIME:"
      if (strstr((char *)payload, "TIME:UPDATE") != nullptr) {
        // Serial.println("[DEBUG] Update alarm clock");
        fetchTimestips();
      }
      // Check if the payload contains "PORTION:"
      else if (strstr((char *)payload, "PORTION:") != nullptr) {
        // Extract the data after "PORTION:"
        char *portion = strstr((char *)payload, "PORTION:") + 8; // Move pointer 8 characters ahead to skip "PORTION:"
        
        // Convert the extracted data to an integer
        int portionSize = atoi(portion);

        // Print the extracted portion size for verification
        // Serial.printf("[DEBUG] Extracted Portion Size: %d\n", portionSize);
      }
     
      break;
    case WStype_ERROR:
      // Serial.println("[DEBUG] WebSocket ERROR: Connection failed or lost.");
      break;
    case WStype_PONG:
      // Serial.println("[DEBUG] WebSocket PONG received.");
      break;
    default:
      break;
  }
}
// Connect to websocket
void connectWebSocket() {
 if (WiFi.status() == WL_CONNECTED) {
    // Construct the WebSocket URL with query parameters
    String wsUrl = String("/?type=hardware") + "&macId=" + MAC + "&hardwareId=" + hardwareID;
    // Serial.println("[DEBUG] WebSocket URL: "+ webSocketServer + ":" + webSocketPort + wsUrl);

    // Connect to WebSocket server
    webSocket.begin(webSocketServer, webSocketPort, wsUrl.c_str());

    // webSocket.setReconnectInterval(5000); // Retry every 5 seconds
    // Enable heartbeat (ping) every 30 seconds
    // webSocket.enableHeartbeat(30000, 1000, false);  // (interval, timeout, pong required)
    
   } else {
    // Serial.println("[DEBUG] Skipping WebSocket connection due to WiFi failure.");
  }
}
// Initialize time 
void connectTimeClient() {  
  if(WiFi.status() == WL_CONNECTED) {
    timeClient.begin();
    // Serial.println("[DEBUG] NTP client initialized.");
  } else {
    // Serial.println("[DEBUG] Skipping NTP client initialization due to WiFi failure.");
  }
}
// Connect to wifi
void connectToWiFi(const char* _ssid, const char* _password) {
  if(WiFi.status() == WL_CONNECTED) return;

  if (_ssid == nullptr || _password  == nullptr) {
    // Serial.println("[DEBUG] No WiFi credentials available.");
    return;
  }

  // Debugging
  // Serial.println("[DEBUG] " + String("WIFI:") + _ssid + "+" + _password);

  WiFi.begin(_ssid, _password);
  unsigned long startTime = millis(); // Record the start time
 
  Serial.print("[DEBUG] Connecting to WiFi"); 
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000); // Wait for 1 second
    // Serial.print("."); // Print a dot for animation

    // Check if the timeout has been exceeded
    if (millis() - startTime > 15000) { // 15-second timeout
      // Serial.println("[DEBUG] \nWiFi connection timed out!");
      return; // Exit the function if timeout occurs
    }
  }

  // Serial.println("[DEBUG] " + String("Device IP: ") + WiFi.localIP().toString());
  // Serial.println("[DEBUG] \nConnected to WiFi!");
  
  // Initialize NTP client
  connectTimeClient();

  // Connect to WebSocket server
  webSocket.onEvent(webSocketEvent);
  connectWebSocket();
}


// Function to fetch and parse alarm times
void fetchTimestips() {
 if (WiFi.status() != WL_CONNECTED) {
    // Serial.println("[DEBUG] WiFi Disconnected. Cannot fetch alarm times.");
    return;
  }

  WiFiClient client;  // Create a WiFiClient object
  HTTPClient http;  // Create an HTTPClient object
  // Use the new begin() method with WiFiClient and the URL
  http.begin(client, String(httpServer) + "/get-timestips/" + MAC);  // Pass client and URL to begin()

  int httpCode = http.GET();

  if (httpCode <= 0 ||httpCode == 400 ) {
    // Serial.println("[DEBUG] Failed to fetch alarm times.");
    http.end();
    return;
  }

  // Process response
  String response = http.getString();
  // Serial.println("[DEBUG] Fetched Alarm Times: " + response);

  // Parse and sort alarm times
  alarmTimes.clear();
  int start = 0, end;
  while ((end = response.indexOf(',', start)) != -1) {
    alarmTimes.push_back(response.substring(start, end));
    start = end + 1;
  }
  alarmTimes.push_back(response.substring(start)); // Add the last alarm
  std::sort(alarmTimes.begin(), alarmTimes.end());

  // Serial.println("[DEBUG] Sorted Alarm Times:");
  /*for (const auto& time : alarmTimes) {
    // Serial.println("[DEBUG] " + time);
  }*/

  // Set `nextAlarmIndex` to the next upcoming alarm
  String currentTime = timeClient.getFormattedTime().substring(0, 5); // HH:MM format
  nextAlarmIndex = 0;

  while (nextAlarmIndex < alarmTimes.size() && currentTime >= alarmTimes[nextAlarmIndex]) {
    nextAlarmIndex++; // Skip alarms that have already passed
  }

  // Serial.printf("[DEBUG] Next Alarm Index: %d\n", nextAlarmIndex);
  http.end();

}
// Fetch portionSize
void fetchPortionSize(){
  if (WiFi.status() != WL_CONNECTED) {
    // Serial.println("[DEBUG] WiFi Disconnected. Cannot fetch alarm times.");
    return;
  }

  WiFiClient client;  // Create a WiFiClient object
  HTTPClient http;  // Create an HTTPClient object

  // Use the new begin() method with WiFiClient and the URL
  http.begin(client, String(httpServer) + "/get-portionsize/" + MAC);  // Pass client and URL to begin()

  int httpCode = http.GET();
  if (httpCode <= 0 ||httpCode == 400) {
    // Serial.println("[DEBUG] Failed to fetch portion size.");
    http.end();
    return;
  }

  // Process response
  String response = http.getString();
  delay(100);
  // Serial.println("[DEBUG] Fetched portion size: " + response);

  // Parse and store portion size 
  int parsedSize = response.toInt();
  if(parsedSize >=0 && parsedSize != portionSize ){
    portionSize = parsedSize;
  }

   http.end();
}


// Read current weight 
void readWeight(){
  /*
  // Subtract the last reading
  total -= readings[readIndex];

  // Read a new weight value and add it to the total
  readings[readIndex] = scale.get_units(1); // Single reading
  total += readings[readIndex];

  // Advance to the next position in the array
  readIndex = (readIndex + 1) % NUM_READINGS;

  // Calculate the average
  average = total / NUM_READINGS;

  if(average < 0 ) average = 0;

  // Send message to websocket
  String message = "WEIGHT:" + String(average);
  webSocket.sendTXT(message);
  */

  // Subtract the oldest reading from the total
  total -= readings[readIndex];

  // Get a new reading from the HX711
  float newReading = 0;

  // Average multiple samples for a stable reading
  for (int i = 0; i < 5; i++) { // Take 5 samples for better stability
    newReading += scale.get_units(1);
    delay(10); // Small delay between samples
  }
  newReading /= 5.0;

  // Ensure the reading is valid (e.g., not an extreme outlier)
  if (newReading < 0) {
    newReading = 0; // Prevent negative weight
  }

  // Store the new reading and update the total
  readings[readIndex] = newReading;
  total += newReading;

  // Advance to the next index in the array
  readIndex = (readIndex + 1) % NUM_READINGS;

  // Calculate the average
  average = total / NUM_READINGS;

  /*  // Send the weight to the WebSocket
  String message = "WEIGHT:" + String(average, 2); // Send with 2 decimal places
  webSocket.sendTXT(message);*/
}
// Send current weight
void sendWeight(float weight, int portion, bool save = false) {
  if (WiFi.status() != WL_CONNECTED) {
    // Serial.println("[DEBUG] WiFi Disconnected. Cannot send weight data.");
    return;
  }

  String timestamp = timeClient.getFormattedTime();

  WiFiClient client;  // Create a WiFiClient object
  HTTPClient http;    // Create an HTTPClient object

  // Construct the URL with query parameters
  String url = String(httpServer) + "/update-weight?id=" + MAC +
               "&value=" + String(weight, 2) +
               "&portionSize=" + String(portion) +
               "&time=" + timestamp +
               "&currentPortion=" + String(nextAlarmIndex - 1 < 0 ?  0 : nextAlarmIndex-1) + 
               "&hhmm=" +  alarmTimes[nextAlarmIndex - 1]; 

  // Serial.println("[DEBUG] Sending GET request to: " + url);

  http.begin(client, url);  // Initialize HTTP client with the URL

  // Send the GET request
  int httpCode = http.GET();

  // Check the response
  if (httpCode > 0) {
    // Serial.printf("[DEBUG] GET request sent. HTTP code: %d\n", httpCode);
    if (httpCode == HTTP_CODE_OK) {
      String response = http.getString();
      // Serial.println("[DEBUG] Response: " + response);
    } else {
      // Serial.println("[DEBUG] Unexpected HTTP response.");
    }
  } else {
    Serial.printf("[DEBUG] Failed to send GET request. Error: %s\n", http.errorToString(httpCode).c_str());
  }

  http.end();  // Close the connection
}
// Check alarm of feeder
void checkAlarms() {
  if (alarmTimes.empty()) return; // No alarms to process

  // Get current time and day
  String currentTime = timeClient.getFormattedTime().substring(0, 5); // HH:MM format
  int currentDay = timeClient.getDay(); // Returns 0-6 for Sunday-Saturday

  // Reset alarms if the day has changed
  if (currentDay != lastCheckedDay) {
    // Serial.println("[DEBUG] New day detected. Resetting alarm index.");
    lastCheckedDay = currentDay; // Update the last checked day
    nextAlarmIndex = 0;          // Reset the alarm index
  }

  // Check if the current time matches the next alarm
  if (nextAlarmIndex < alarmTimes.size() && currentTime == alarmTimes[nextAlarmIndex]) {
    // Trigger the alarm
    String alarmMessage = "Alarm Triggered: " + alarmTimes[nextAlarmIndex];
    // Serial.println("[DEBUG] " + alarmMessage);

    // Send weight before feeding next portion
    sendWeight(average,portionSize);

    // Send alarm notification and animation
    onFeedEvent();

    // Move to the next alarm
    nextAlarmIndex++;

    // Log if all alarms for the day have been processed
    if (nextAlarmIndex >= alarmTimes.size()) {
      // Serial.println("[DEBUG] All alarms for the day have been processed.");
      
      // Send message to websocket
      String message = "NEXTFEED:" + alarmTimes[0];
      webSocket.sendTXT(message);
    } else {
      // Send message to websocket
      String message = "NEXTFEED:" + alarmTimes[nextAlarmIndex];
      webSocket.sendTXT(message);
     }
  }
}
// Feeding Animation
void onFeedEvent() {
    feeding =true;
    sendMessageToMicrobit("FEED:1\n");

    while(feeding){
      unsigned long currentMillis = millis();

      // Update the weight 
       readWeight();

      if (currentMillis - previousMillis2 >= 500){
          previousMillis2 = currentMillis;
          // Send the weight to the WebSocket "WEIGHT:" +

          String nextAlarm = alarmTimes.empty() ? "N/A" : alarmTimes[nextAlarmIndex];
          String message = String(average, 2) + "," + nextAlarm + "," + (nextAlarmIndex < 0 ? 0 : nextAlarmIndex);
          webSocket.sendTXT(message);
      }

      if(average >= portionSize){
        sendMessageToMicrobit("FEED:0\n");
        feeding = false;
      }
    }
}


// Setup HX711 scaler
void initializeHX711(){
  scale.set_scale(scalingFactor);
  scale.set_gain(128);

  if (tareOffset == 0) {
    // If tareOffset is 0, calculate and print it
    scale.tare(); // Tare the scale
    tareOffset = scale.get_offset(); // Get the tare offset
    // Serial.printf("[DEBUG] \nTare offset calculated: %ld\n", tareOffset);
  } else {
    // Use the hardcoded tare offset
    scale.set_offset(tareOffset);
   // Serial.printf("[DEBUG] Using hardcoded tare offset: %ld\n", tareOffset);
  }

    // Initialize the readings array
  for (int i = 0; i < NUM_READINGS; i++) {
    readings[i] = 0;
  }
}
// Setup server configuration
void initializeServername(){
  // Try to read the saved IP address
  String savedIP = readCompanyIP();

  if (savedIP.length() > 0) {
    // If a valid IP is found, configure servers with it
    httpServer = "http://" + savedIP + ":3002/api/hardware";
    webSocketServer = savedIP;
    // Serial.println("[DEBUG] Using saved IP for server configurations:");
  } else {
    // Use default values if no IP is saved
    httpServer = defaultHttpServer;
    webSocketServer = defaultWebSocketServer;
    // Serial.println("[DEBUG] Using default server configurations:");
  }

  // Log the configurations
  // Serial.println("[DEBUG] HTTP Server: " + httpServer);
  // Serial.println("[DEBUG] WebSocket Server: " + webSocketServer);
}

void setup() {
  // Initialize SoftwareSerial with Micro:bit
  Serial.begin(57600); 
 
  // Setup storage
  EEPROM.begin(EEPROM_SIZE);

  // HX711 Setup
  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
  initializeHX711();

  Serial.println("[DEBUG] Device Started");

  // Initialize server name
  initializeServername();

  // Read WiFi credentials from EEPROM
  readWiFiCredentials(ssid, password);

  // Connect to wifi
  connectToWiFi(ssid, password);    

  // Fetch feeder details
  fetchTimestips();
  delay(1000);
  fetchPortionSize();

  // Send command to Micro:bit
  delay(100);
  sendMessageToMicrobit("UART:1\n\n\n\n");
}
void loop() {
  unsigned long currentMillis = millis();

  // Handle WebSocket communication
  webSocket.loop();

  // Update NTP time
  timeClient.update();

  // Update the weight 
  readWeight();

  // Serial.printf("[DEBUG] Weight: %.2f grams\n", average);

  if (currentMillis - previousMillis2 >= 500){
    previousMillis2 = currentMillis;
    // Send the weight to the WebSocket "WEIGHT:" +

    String nextAlarm = alarmTimes.empty() ? "N/A" : alarmTimes[nextAlarmIndex];
    String message = String(average, 2) + "," + nextAlarm + "," + (nextAlarmIndex < 0 ? 0 : nextAlarmIndex);
    webSocket.sendTXT(message);
  }

  // Non blocking delay 1 minute
  if (currentMillis - previousMillis >= 10000) {
    // Save the last time we ran the code
    previousMillis = currentMillis;

    // Serial.println("[DEBUG] Time: " + timeClient.getFormattedTime());

    // Check alarm
    checkAlarms();
  }
  
  // from SoftwareSerial (Micro:bit)
  if (Serial.available()) {
    String incomingData = ""; 

    // Read data byte-by-byte from SoftwareSerial
    while (Serial.available()) {
      char incomingChar = Serial.read();
      incomingData += incomingChar; // Store data
    }

    // Process the incoming data from Micro:bit (for example, print it)
    // // Serial.println("[DEBUG] Received from Micro:bit: " + incomingData);

    if(incomingData.startsWith("UART:GET")) {
      sendMessageToMicrobit("UART:1\n");
    }
    else if(incomingData.startsWith("WIFI:GET")){
      if(WiFi.status() == WL_CONNECTED){
        sendMessageToMicrobit("WIFI:1\n");
      }
      else{
          sendMessageToMicrobit("WIFI:0\n");
      }
    }
    else if(incomingData.startsWith("SD:")){
      // Extract and trim SSID 
      String trimmedSSID = incomingData.substring(3); 
      trimmedSSID.trim();

      ssid = new char[incomingData.length() - 3 + 1]; // Allocate memory
      strcpy(ssid, trimmedSSID.c_str());
      // // Serial.println("[DEBUG] SSID to: " + String(ssid) + "#");
      
      // Save SSID only 
      saveSSID(ssid);
    }
    else if(incomingData.startsWith("PS:")){
      // Extract and trim Password
      String trimmedPassword = incomingData.substring(3); 
      trimmedPassword.trim(); 
      
      password = new char[incomingData.length() - 3 + 1]; // Allocate memory
      strcpy(password, trimmedPassword.c_str());
      // // Serial.println("[DEBUG] Password to: " + String(password)+ "#");
      
      // Save WiFi credentials (without SSID for now)
      savePassword(password);
    }
    else if(incomingData.startsWith("WIFI:CONNECT")){
      // End time 
      timeClient.end();

      // Attempt to connect to WiFi
      connectToWiFi(ssid, password);   

      // Fetch feeder details
      fetchTimestips();
      fetchPortionSize(); 
    }
    else if(incomingData.startsWith("IP:")){
      // Extract and trim IP
      String ip = incomingData.substring(3); // Extract IP after "IP:"
      ip.trim(); // Remove any leading or trailing whitespace

      // Validate IP address format
      if (ip.length() > 0 && ip.indexOf('.') != -1) { // Basic check for non-empty and contains dots
          // Print for debugging
          // Serial.println("[DEBUG] " + String("New IP: ") + ip);

          // Save the IP address to EEPROM
          saveCompanyIP(ip.c_str()); // Directly pass the c_str() of the String to saveCompanyIP
      } else {
          // Serial.println("[DEBUG] Invalid IP address received!");
      }

      initializeServername();
    }
    else if(incomingData.startsWith("RESET")){
      clearEEPROM();
    }
    else if(incomingData.startsWith("FACTORY")){
      for (int i = 0; i < EEPROM_SIZE; i++) {
        EEPROM.write(i, 0); // Write 0 to each byte in EEPROM
      }
      EEPROM.commit(); // Commit the changes
      // Serial.println("[DEBUG] EEPROM cleared!");
    }
    else if(incomingData.startsWith("DOOR")){
      onFeedEvent();
    }
  }
}
  

