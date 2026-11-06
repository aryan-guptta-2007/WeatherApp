# 🌤️ Weather App — Commercial Edition

A commercial-grade weather application featuring a **Java Swing Desktop Application** with dynamic weather theme gradients and a **Modern Web Application** powered by interactive physics particles, live search autocomplete, and extended forecasts.

---

## 🌟 Feature Matrix

### 💻 1. Java Desktop Application (`src/`)

| Feature | Description |
|---|---|
| **Dynamic Weather Theme Gradients** | Background transitions dynamically based on conditions: *Daylight Sapphire* (Clear), *Cool Slate Graphite* (Cloudy), *Midnight Navy Indigo* (Rain), and *Frost Crystal Teal* (Snow). |
| **3-Day Forecast Cards Deck** | Dedicated forecast cards for Today, Tomorrow, and Day 3 displaying condition icons and min/max temperature ranges. |
| **4 Atmospheric Metrics (2x2 Glass Grid)** | Displays Humidity (%), Wind Speed, Surface Pressure (hPa), and Precipitation in translucent glassmorphic cards. |
| **1-Click 📋 Copy Weather Report** | Copies a formatted emoji weather report to the system clipboard with an animated `✓ Copied!` confirmation badge. |
| **Quick City Chips Bar** | 1-click popular city chips (*Tokyo, London, New York, Paris, Sydney*) directly beneath search input. |
| **15-Minute Auto-Refresh Timer** | Background `javax.swing.Timer` silently keeps weather conditions up to date without manual interaction. |
| **Keyboard Shortcuts** | <kbd>Enter</kbd> to search, <kbd>F5</kbd> to refresh, and auto-select all text on search field focus. |
| **Quiet Offline Startup** | Graceful fallback when launching offline or disconnected without blocking modal popups. |

---

### 🌐 2. Web Application (`web/`)

| Feature | Description |
|---|---|
| **Live Search Autocomplete** | Instant, debounced (250ms) geocoding dropdown as you type, showing cities, administrative regions, and countries with full keyboard arrow navigation (<kbd>↑</kbd> / <kbd>↓</kbd> / <kbd>Enter</kbd> / <kbd>Esc</kbd>). |
| **Interactive Canvas Weather Particles** | Real-time HTML5 physics particles: falling raindrops, swaying snowflakes, shimmering sun rays, and drifting mist (toggleable via the ✨ button). |
| **⭐ Pinned Favorite Cities** | 1-click star icon (⭐) saves cities into `localStorage` with a persistent quick-access favorites bar. |
| **Comprehensive 6-Metric Highlights Grid** | Displays Humidity (%), Wind Speed & Direction, Surface Pressure (hPa), UV Index (Value + Category), Rain Chance (%), and Sun Cycle (Sunrise & Sunset times). |
| **1-Click 📋 Share / Copy** | Formats and copies a neat weather summary to the clipboard with an animated floating toast notification. |
| **GPS Geolocation with Reverse Geocoding** | Clicking 📍 detects coordinates and queries reverse geocoding to resolve the real city and country name. |
| **PWA Ready (`manifest.json`)** | Web app manifest enabling 1-click installation as a standalone app on Windows, macOS, Android, and iOS. |

---

## ⚡ 3. One-Click Universal Launchers

* **`RunWeatherApp.bat`:** Automatically detects `JAVA_HOME`, installed JDK directories in `Program Files\Java\jdk*`, and system PATH to launch the standalone `WeatherApp.jar`.
* **`RunWebApp.bat`:** Multi-runtime detection that works automatically whether the machine has Node.js (`server.js`), Python 3 (`server.py`), Java `jwebserver`, or standard browser fallback.
* **`web/server.py`:** Built-in Python 3 HTTP server for environments without Node.js.

---

## 🛠️ Architecture & Technologies

- **Desktop Framework:** Java 17+ Swing (`JFrame`, custom `Graphics2D` gradients, `SwingWorker`, `json-simple 1.1.1`)
- **Web Frontend:** HTML5, CSS3 Glassmorphism, JavaScript (ES6+), Canvas 2D Particle Engine
- **Data Source:** [Open-Meteo Weather API](https://open-meteo.com/) (Free, no API key required)
- **Reverse Geocoding:** [BigDataCloud Client-side Geocoding](https://api.bigdatacloud.net/)

---

## 🚀 How to Run

### Run the Web Version (Port 3000)
Double-click **`RunWebApp.bat`** (or `Open_Web_App.bat` in the root folder), or run:
```bash
cd web
node server.js
# OR
python server.py
```
Open **`http://localhost:3000`** in any browser.

### Run the Desktop Version
Double-click **`RunWeatherApp.bat`** (or `Open_Desktop_App.bat` in the root folder), or run:
```bash
java -jar WeatherApp.jar
```
