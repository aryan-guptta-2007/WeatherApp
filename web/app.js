// State variables
let currentUnit = 'C'; // 'C' or 'F'
let currentWeatherData = null;

// DOM Elements
const searchInput = document.getElementById('search-input');
const searchBtn = document.getElementById('search-btn');
const locateBtn = document.getElementById('locate-btn');
const unitToggleBtn = document.getElementById('unit-toggle-btn');
const alertMsg = document.getElementById('alert-msg');
const spinner = document.getElementById('loading-spinner');

const cityNameEl = document.getElementById('city-name');
const localTimeEl = document.getElementById('local-time');
const weatherIconEl = document.getElementById('weather-icon');
const temperatureValEl = document.getElementById('temperature-val');
const tempUnitEl = document.getElementById('temp-unit');
const weatherDescEl = document.getElementById('weather-desc');
const feelsLikeEl = document.getElementById('feels-like');
const humidityValEl = document.getElementById('humidity-val');
const windspeedValEl = document.getElementById('windspeed-val');
const hourlyContainer = document.getElementById('hourly-container');
const forecastContainer = document.getElementById('forecast-container');
const cityChips = document.querySelectorAll('.chip');

// Weather code interpreter
function getWeatherCondition(code) {
    if (code === 0) {
        return { desc: 'Clear Sky', icon: 'assets/clear.png' };
    } else if (code >= 1 && code <= 3) {
        return { desc: 'Cloudy', icon: 'assets/cloudy.png' };
    } else if (code === 45 || code === 48) {
        return { desc: 'Foggy', icon: 'assets/cloudy.png' };
    } else if ((code >= 71 && code <= 77) || code === 85 || code === 86) {
        return { desc: 'Snow', icon: 'assets/snow.png' };
    } else if ((code >= 51 && code <= 67) || (code >= 80 && code <= 84) || (code >= 95 && code <= 99)) {
        return { desc: 'Rain', icon: 'assets/rain.png' };
    }
    return { desc: 'Cloudy', icon: 'assets/cloudy.png' };
}

// Temperature conversion helpers
function cToF(c) {
    return (c * 9 / 5) + 32;
}

function kmhToMph(kmh) {
    return kmh * 0.621371;
}

function round1(val) {
    return Math.round(val * 10) / 10;
}

// Show/hide alert notification
function showAlert(message) {
    alertMsg.textContent = message;
    alertMsg.classList.remove('hidden');
    setTimeout(() => {
        alertMsg.classList.add('hidden');
    }, 4500);
}

// Fetch coordinates for city name
async function getCoordinates(city) {
    const url = `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(city.trim())}&count=1&language=en&format=json`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Geocoding request failed');
    const data = await res.json();
    if (!data.results || data.results.length === 0) {
        return null;
    }
    const loc = data.results[0];
    const name = loc.name + (loc.country ? `, ${loc.country}` : '');
    return {
        lat: loc.latitude,
        lon: loc.longitude,
        name: name,
        timezone: loc.timezone
    };
}

// Fetch weather data from Open-Meteo
async function fetchWeather(lat, lon, locationName) {
    spinner.classList.remove('hidden');

    try {
        const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto`;
        const res = await fetch(url);
        if (!res.ok) throw new Error('Weather API request failed');

        const data = await res.json();
        currentWeatherData = {
            locationName: locationName,
            time: data.current.time,
            tempC: data.current.temperature_2m,
            feelsLikeC: data.current.apparent_temperature,
            weatherCode: data.current.weather_code,
            humidity: data.current.relative_humidity_2m,
            windSpeedKmh: data.current.wind_speed_10m,
            hourly: data.hourly,
            daily: data.daily
        };

        renderWeather();
    } catch (err) {
        console.error(err);
        showAlert('Unable to retrieve weather data. Please try again.');
    } finally {
        spinner.classList.add('hidden');
    }
}

// Render data to DOM
function renderWeather() {
    if (!currentWeatherData) return;

    const condition = getWeatherCondition(currentWeatherData.weatherCode);

    cityNameEl.textContent = currentWeatherData.locationName;
    weatherIconEl.src = condition.icon;
    weatherDescEl.textContent = condition.desc;
    humidityValEl.textContent = `${currentWeatherData.humidity}%`;

    // Local time formatting
    try {
        const timePart = currentWeatherData.time.split('T')[1];
        localTimeEl.textContent = `Local Time: ${timePart}`;
    } catch (e) {
        localTimeEl.textContent = '';
    }

    updateUnits();
}

// Update unit display
function updateUnits() {
    if (!currentWeatherData) return;

    if (currentUnit === 'C') {
        temperatureValEl.textContent = round1(currentWeatherData.tempC);
        tempUnitEl.textContent = '°C';
        feelsLikeEl.textContent = `Feels like: ${round1(currentWeatherData.feelsLikeC)} °C`;
        windspeedValEl.textContent = `${round1(currentWeatherData.windSpeedKmh)} km/h`;
        unitToggleBtn.textContent = '°F';
    } else {
        const tempF = cToF(currentWeatherData.tempC);
        const feelsLikeF = cToF(currentWeatherData.feelsLikeC);
        const windMph = kmhToMph(currentWeatherData.windSpeedKmh);

        temperatureValEl.textContent = round1(tempF);
        tempUnitEl.textContent = '°F';
        feelsLikeEl.textContent = `Feels like: ${round1(feelsLikeF)} °F`;
        windspeedValEl.textContent = `${round1(windMph)} mph`;
        unitToggleBtn.textContent = '°C';
    }

    renderHourly();
    renderForecast();
}

// Render next 24 hours
function renderHourly() {
    if (!currentWeatherData || !currentWeatherData.hourly) return;
    hourlyContainer.innerHTML = '';

    const hourly = currentWeatherData.hourly;
    const currentTimeStr = currentWeatherData.time;

    // Find starting index matching current hour
    let startIndex = 0;
    const currentHourPrefix = currentTimeStr.substring(0, 13); // "YYYY-MM-DDTHH"
    for (let i = 0; i < hourly.time.length; i++) {
        if (hourly.time[i].startsWith(currentHourPrefix)) {
            startIndex = i;
            break;
        }
    }

    const hoursToShow = 24;
    const endIndex = Math.min(hourly.time.length, startIndex + hoursToShow);

    for (let i = startIndex; i < endIndex; i++) {
        const timeStr = hourly.time[i];
        const hourPart = timeStr.split('T')[1]; // "HH:00"
        const isNow = i === startIndex;

        const code = hourly.weather_code[i];
        const cond = getWeatherCondition(code);

        let temp = hourly.temperature_2m[i];
        if (currentUnit === 'F') {
            temp = cToF(temp);
        }

        const hourCard = document.createElement('div');
        hourCard.className = 'hourly-card';
        hourCard.innerHTML = `
            <span class="hour-time">${isNow ? 'Now' : hourPart}</span>
            <img src="${cond.icon}" alt="${cond.desc}">
            <span class="hour-temp">${round1(temp)}°</span>
        `;
        hourlyContainer.appendChild(hourCard);
    }
}

// Render 5-day forecast
function renderForecast() {
    if (!currentWeatherData || !currentWeatherData.daily) return;
    forecastContainer.innerHTML = '';

    const daily = currentWeatherData.daily;
    const daysToShow = Math.min(5, daily.time.length);

    for (let i = 0; i < daysToShow; i++) {
        const dateStr = daily.time[i];
        const date = new Date(dateStr + 'T00:00:00');
        const dayName = i === 0 ? 'Today' : date.toLocaleDateString('en-US', { weekday: 'short' });

        const code = daily.weather_code[i];
        const cond = getWeatherCondition(code);

        let max = daily.temperature_2m_max[i];
        let min = daily.temperature_2m_min[i];

        if (currentUnit === 'F') {
            max = cToF(max);
            min = cToF(min);
        }

        const dayCard = document.createElement('div');
        dayCard.className = 'forecast-day-card';
        dayCard.innerHTML = `
            <span class="day-name">${dayName}</span>
            <img src="${cond.icon}" alt="${cond.desc}">
            <span class="temp-range">${round1(max)}° / ${round1(min)}°</span>
        `;
        forecastContainer.appendChild(dayCard);
    }
}

// Execute search
async function handleSearch(cityName) {
    const query = (cityName || searchInput.value).trim();
    if (!query) return;

    searchInput.value = query;
    spinner.classList.remove('hidden');

    try {
        const loc = await getCoordinates(query);
        if (!loc) {
            showAlert(`Location "${query}" not found. Please check spelling.`);
            spinner.classList.add('hidden');
            return;
        }
        await fetchWeather(loc.lat, loc.lon, loc.name);
    } catch (err) {
        console.error(err);
        showAlert('Search error. Please verify your connection.');
        spinner.classList.add('hidden');
    }
}

// Event Listeners
searchBtn.addEventListener('click', () => handleSearch());
searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        handleSearch();
    }
});

cityChips.forEach(chip => {
    chip.addEventListener('click', () => {
        const city = chip.getAttribute('data-city');
        handleSearch(city);
    });
});

unitToggleBtn.addEventListener('click', () => {
    currentUnit = currentUnit === 'C' ? 'F' : 'C';
    updateUnits();
});

locateBtn.addEventListener('click', () => {
    if (!navigator.geolocation) {
        showAlert('Geolocation is not supported by your browser.');
        return;
    }

    spinner.classList.remove('hidden');
    navigator.geolocation.getCurrentPosition(
        async (position) => {
            const { latitude, longitude } = position.coords;
            await fetchWeather(latitude, longitude, 'My Location');
        },
        (error) => {
            spinner.classList.add('hidden');
            showAlert('Unable to retrieve your location.');
        }
    );
});

// Load default city on startup
window.addEventListener('DOMContentLoaded', () => {
    handleSearch('Tokyo');
});
