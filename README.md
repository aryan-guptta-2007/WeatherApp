# Weather App GUI

A modern desktop weather application built in Java Swing that provides real-time weather information for any location worldwide using the Open-Meteo API.

---

## ✨ Features

- **Worldwide Geocoding:** Search for any city, town, or region across the globe.
- **Accurate Real-Time Weather:** Live temperature, real-feel index, humidity, wind speed, and weather condition fetched via the Open-Meteo API.
- **Matched Location Display:** Shows the resolved location and country (e.g., `London, United Kingdom`, `Tokyo, Japan`).
- **Dynamic Weather Illustrations:** Responsive visual representations for Clear, Cloudy/Overcast, Rain, and Snow conditions.
- **Unit Toggle (°C / °F):** Instant toggle between Metric (°C, km/h) and Imperial (°F, mph) units with real-time recalculation.
- **Non-blocking Asynchronous Search:** Powered by `SwingWorker` to keep the UI completely smooth and responsive during API calls.
- **Keyboard Shortcuts:** Press <kbd>Enter</kbd> inside the search input for quick lookups.
- **Robust Error Handling:** Clean feedback alerts for invalid queries or network interruptions without crashes.
- **Native Look & Feel:** Seamless OS window borders, controls, and custom taskbar/window icon.
- **Instant Launch:** Automatically loads live weather on startup.

---

## 🛠️ Architecture & Technologies

- **Language:** Java (JDK 17+)
- **GUI Framework:** Java Swing (`JFrame`, `JLabel`, `JTextField`, `JButton`, `SwingWorker`)
- **JSON Parser:** [json-simple 1.1.1](https://code.google.com/archive/p/json-simple/)
- **Weather API:** [Open-Meteo Weather API](https://open-meteo.com/) (Geocoding & Forecast APIs)
- **Networking:** Built-in Java `HttpURLConnection` with connect and read timeouts

### Project Structure

```
WeatherAppGUI-Java-main/
├── lib/
│   └── json-simple-1.1.1.jar       # JSON parsing library
├── out/
│   └── production/WeatherAppGUI/   # Compiled bytecode and bundled assets
├── src/
│   ├── assets/                     # PNG graphic assets
│   │   ├── clear.png
│   │   ├── cloudy.png
│   │   ├── humidity.png
│   │   ├── rain.png
│   │   ├── search.png
│   │   ├── snow.png
│   │   └── windspeed.png
│   ├── AppLauncher.java            # Main application entry point & Look-and-Feel
│   ├── WeatherApp.java             # Backend API client, geocoding & unit parsing
│   └── WeatherAppGui.java          # Frontend GUI, event listeners & async rendering
├── pom.xml                         # Complete Maven build configuration
├── WeatherApp.jar                  # Standalone executable fat JAR
└── README.md
```

---

## 🚀 How to Run

### Option 1: Run the Standalone JAR (Easiest)
You can directly double-click `WeatherApp.jar` or run it from the terminal:

```bash
java -jar WeatherApp.jar
```

### Option 2: Run via Compiled Classes

```bash
java -cp "out/production/WeatherAppGUI;lib/json-simple-1.1.1.jar" AppLauncher
```

### Option 3: Compile from Source

```bash
javac -cp "lib/json-simple-1.1.1.jar" -d "out/production/WeatherAppGUI" src/*.java
java -cp "out/production/WeatherAppGUI;lib/json-simple-1.1.1.jar" AppLauncher
```
