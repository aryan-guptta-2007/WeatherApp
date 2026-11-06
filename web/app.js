// State variables
let currentUnit = 'C'; // 'C' or 'F'
let currentWeatherData = null;
let particlesEnabled = true;
let favorites = JSON.parse(localStorage.getItem('weather_favorites') || '["Tokyo", "London", "New York"]');

// DOM Elements
const searchInput = document.getElementById('search-input');
const searchBtn = document.getElementById('search-btn');
const locateBtn = document.getElementById('locate-btn');
const favoriteBtn = document.getElementById('favorite-btn');
const particlesToggleBtn = document.getElementById('particles-toggle-btn');
const unitToggleBtn = document.getElementById('unit-toggle-btn');
const autocompleteList = document.getElementById('autocomplete-list');
const favoritesContainer = document.getElementById('favorites-container');
const toastEl = document.getElementById('toast');
const spinner = document.getElementById('loading-spinner');
const copyReportBtn = document.getElementById('copy-report-btn');

// Display Elements
const cityNameEl = document.getElementById('city-name');
const localTimeEl = document.getElementById('local-time');
const weatherIconEl = document.getElementById('weather-icon');
const temperatureValEl = document.getElementById('temperature-val');
const tempUnitEl = document.getElementById('temp-unit');
const weatherDescEl = document.getElementById('weather-desc');
const feelsLikeEl = document.getElementById('feels-like');

// 6 Metrics Elements
const humidityValEl = document.getElementById('humidity-val');
const humidityDescEl = document.getElementById('humidity-desc');
const windspeedValEl = document.getElementById('windspeed-val');
const windDirEl = document.getElementById('wind-dir');
const pressureValEl = document.getElementById('pressure-val');
const pressureDescEl = document.getElementById('pressure-desc');
const uvValEl = document.getElementById('uv-val');
const uvBadgeEl = document.getElementById('uv-badge');
const rainChanceValEl = document.getElementById('rain-chance-val');
const precipValEl = document.getElementById('precip-val');
const sunriseValEl = document.getElementById('sunrise-val');
const sunsetValEl = document.getElementById('sunset-val');

// Containers
const hourlyContainer = document.getElementById('hourly-container');
const forecastContainer = document.getElementById('forecast-container');
const defaultChips = document.querySelectorAll('.default-chips .chip');

// -------------------------------------------------------------
// 1. Interactive Canvas Weather Particles Engine
// -------------------------------------------------------------
const canvas = document.getElementById('weather-canvas');
const ctx = canvas.getContext('2d');
let particles = [];
let animFrameId = null;
let currentConditionCategory = 'Cloudy'; // Clear, Cloudy, Rain, Snow

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}
window.addEventListener('resize', resizeCanvas);
resizeCanvas();

class Particle {
    constructor(category) {
        this.reset(category);
    }

    reset(category) {
        this.category = category;
        this.x = Math.random() * canvas.width;
        this.y = Math.random() * canvas.height;

        if (category === 'Rain') {
            this.length = Math.random() * 18 + 12;
            this.speedY = Math.random() * 9 + 11;
            this.speedX = -1.5;
            this.opacity = Math.random() * 0.4 + 0.3;
        } else if (category === 'Snow') {
            this.radius = Math.random() * 2.5 + 1.2;
            this.speedY = Math.random() * 1.5 + 0.8;
            this.angle = Math.random() * Math.PI * 2;
            this.swaySpeed = Math.random() * 0.02 + 0.01;
            this.opacity = Math.random() * 0.6 + 0.4;
        } else if (category === 'Clear') {
            this.radius = Math.random() * 2 + 1;
            this.speedY = -(Math.random() * 0.6 + 0.2);
            this.speedX = (Math.random() - 0.5) * 0.5;
            this.opacity = Math.random() * 0.5 + 0.2;
        } else {
            // Cloudy / Mist
            this.radius = Math.random() * 40 + 20;
            this.speedX = Math.random() * 0.4 + 0.2;
            this.speedY = (Math.random() - 0.5) * 0.1;
            this.opacity = Math.random() * 0.08 + 0.03;
        }
    }

    update() {
        if (this.category === 'Rain') {
            this.y += this.speedY;
            this.x += this.speedX;
            if (this.y > canvas.height) {
                this.y = -20;
                this.x = Math.random() * canvas.width;
            }
        } else if (this.category === 'Snow') {
            this.angle += this.swaySpeed;
            this.x += Math.sin(this.angle) * 1.2;
            this.y += this.speedY;
            if (this.y > canvas.height) {
                this.y = -10;
                this.x = Math.random() * canvas.width;
            }
        } else if (this.category === 'Clear') {
            this.y += this.speedY;
            this.x += this.speedX;
            if (this.y < -10) {
                this.y = canvas.height + 10;
                this.x = Math.random() * canvas.width;
            }
        } else {
            this.x += this.speedX;
            this.y += this.speedY;
            if (this.x - this.radius > canvas.width) {
                this.x = -this.radius;
                this.y = Math.random() * canvas.height;
            }
        }
    }

    draw() {
        if (this.category === 'Rain') {
            ctx.strokeStyle = `rgba(180, 215, 255, ${this.opacity})`;
            ctx.lineWidth = 1.2;
            ctx.beginPath();
            ctx.moveTo(this.x, this.y);
            ctx.lineTo(this.x + this.speedX * 2, this.y + this.length);
            ctx.stroke();
        } else if (this.category === 'Snow') {
            ctx.fillStyle = `rgba(255, 255, 255, ${this.opacity})`;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
            ctx.fill();
        } else if (this.category === 'Clear') {
            ctx.fillStyle = `rgba(255, 235, 170, ${this.opacity})`;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
            ctx.fill();
        } else {
            ctx.fillStyle = `rgba(255, 255, 255, ${this.opacity})`;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
            ctx.fill();
        }
    }
}

function initParticles(category) {
    currentConditionCategory = category;
    particles = [];
    const count = category === 'Rain' ? 90 : (category === 'Snow' ? 70 : (category === 'Clear' ? 40 : 20));
    for (let i = 0; i < count; i++) {
        particles.push(new Particle(category));
    }
}

function animateParticles() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (particlesEnabled) {
        particles.forEach(p => {
            p.update();
            p.draw();
        });
    }
    animFrameId = requestAnimationFrame(animateParticles);
}
initParticles('Cloudy');
animateParticles();

particlesToggleBtn.addEventListener('click', () => {
    particlesEnabled = !particlesEnabled;
    particlesToggleBtn.style.opacity = particlesEnabled ? '1' : '0.5';
    showToast(particlesEnabled ? '✨ Weather particles enabled' : 'Weather particles disabled');
});

// -------------------------------------------------------------
// 2. Weather Code Interpreter
// -------------------------------------------------------------
function getWeatherCondition(code) {
    if (code === 0) {
        return { desc: 'Clear Sky', category: 'Clear', icon: 'assets/clear.png' };
    } else if (code >= 1 && code <= 3) {
        return { desc: 'Cloudy', category: 'Cloudy', icon: 'assets/cloudy.png' };
    } else if (code === 45 || code === 48) {
        return { desc: 'Foggy', category: 'Cloudy', icon: 'assets/cloudy.png' };
    } else if ((code >= 71 && code <= 77) || code === 85 || code === 86) {
        return { desc: 'Snow', category: 'Snow', icon: 'assets/snow.png' };
    } else if ((code >= 51 && code <= 67) || (code >= 80 && code <= 84) || (code >= 95 && code <= 99)) {
        return { desc: 'Rain', category: 'Rain', icon: 'assets/rain.png' };
    }
    return { desc: 'Cloudy', category: 'Cloudy', icon: 'assets/cloudy.png' };
}

// Temperature & metric conversion helpers
function cToF(c) { return (c * 9 / 5) + 32; }
function kmhToMph(kmh) { return kmh * 0.621371; }
function mmToIn(mm) { return mm * 0.0393701; }
function round1(val) { return Math.round(val * 10) / 10; }

function showToast(msg) {
    toastEl.textContent = msg;
    toastEl.classList.remove('hidden');
    setTimeout(() => {
        toastEl.classList.add('hidden');
    }, 3200);
}

// -------------------------------------------------------------
// 3. Live Search Autocomplete with Arrow Navigation
// -------------------------------------------------------------
let debounceTimer = null;
let activeSuggestIdx = -1;
let currentSuggestions = [];

searchInput.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    const query = searchInput.value.trim();
    if (query.length < 2) {
        autocompleteList.classList.add('hidden');
        autocompleteList.innerHTML = '';
        currentSuggestions = [];
        return;
    }

    debounceTimer = setTimeout(async () => {
        try {
            const url = `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(query)}&count=5&language=en&format=json`;
            const res = await fetch(url);
            if (!res.ok) return;
            const data = await res.json();
            currentSuggestions = data.results || [];
            renderAutocomplete();
        } catch (e) {
            console.error(e);
        }
    }, 250);
});

function renderAutocomplete() {
    autocompleteList.innerHTML = '';
    activeSuggestIdx = -1;

    if (currentSuggestions.length === 0) {
        autocompleteList.classList.add('hidden');
        return;
    }

    currentSuggestions.forEach((loc, idx) => {
        const li = document.createElement('li');
        const region = [loc.admin1, loc.country].filter(Boolean).join(', ');
        li.innerHTML = `
            <span class="loc-name">${loc.name}</span>
            <span class="loc-region">${region}</span>
        `;
        li.addEventListener('click', () => {
            selectLocation(loc);
        });
        autocompleteList.appendChild(li);
    });

    autocompleteList.classList.remove('hidden');
}

searchInput.addEventListener('keydown', (e) => {
    const items = autocompleteList.querySelectorAll('li');
    if (!autocompleteList.classList.contains('hidden') && items.length > 0) {
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            activeSuggestIdx = (activeSuggestIdx + 1) % items.length;
            updateSuggestHighlight(items);
            return;
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            activeSuggestIdx = (activeSuggestIdx - 1 + items.length) % items.length;
            updateSuggestHighlight(items);
            return;
        } else if (e.key === 'Enter' && activeSuggestIdx >= 0) {
            e.preventDefault();
            selectLocation(currentSuggestions[activeSuggestIdx]);
            return;
        } else if (e.key === 'Escape') {
            autocompleteList.classList.add('hidden');
            return;
        }
    }

    if (e.key === 'Enter') {
        autocompleteList.classList.add('hidden');
        handleSearch();
    }
});

function updateSuggestHighlight(items) {
    items.forEach((item, idx) => {
        if (idx === activeSuggestIdx) {
            item.classList.add('active');
            item.scrollIntoView({ block: 'nearest' });
        } else {
            item.classList.remove('active');
        }
    });
}

function selectLocation(loc) {
    const displayName = loc.name + (loc.country ? `, ${loc.country}` : '');
    searchInput.value = displayName;
    autocompleteList.classList.add('hidden');
    fetchWeather(loc.latitude, loc.longitude, displayName);
}

document.addEventListener('click', (e) => {
    if (!searchInput.contains(e.target) && !autocompleteList.contains(e.target)) {
        autocompleteList.classList.add('hidden');
    }
});

// -------------------------------------------------------------
// 4. Weather API Fetching & Reverse Geocoding
// -------------------------------------------------------------
async function fetchWeather(lat, lon, locationName) {
    spinner.classList.remove('hidden');

    try {
        const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}` +
            `&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature,surface_pressure,precipitation` +
            `&hourly=temperature_2m,weather_code` +
            `&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max` +
            `&timezone=auto`;

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
            pressureHpa: data.current.surface_pressure || 1013,
            precipMm: data.current.precipitation || 0,
            uvIndex: data.daily?.uv_index_max?.[0] || 0,
            rainChance: data.daily?.precipitation_probability_max?.[0] || 0,
            sunrise: data.daily?.sunrise?.[0] || '--:--',
            sunset: data.daily?.sunset?.[0] || '--:--',
            hourly: data.hourly,
            daily: data.daily
        };

        renderWeather();
    } catch (err) {
        console.error(err);
        showToast('Unable to retrieve weather. Please check connection.');
    } finally {
        spinner.classList.add('hidden');
    }
}

// Render active weather state
function renderWeather() {
    if (!currentWeatherData) return;

    const condition = getWeatherCondition(currentWeatherData.weatherCode);

    cityNameEl.textContent = currentWeatherData.locationName;
    weatherIconEl.src = condition.icon;
    weatherDescEl.textContent = condition.desc;
    humidityValEl.textContent = `${currentWeatherData.humidity}%`;
    humidityDescEl.textContent = currentWeatherData.humidity > 70 ? 'High' : (currentWeatherData.humidity < 40 ? 'Dry' : 'Comfortable');

    // Local time formatting
    try {
        const timePart = currentWeatherData.time.split('T')[1];
        localTimeEl.textContent = `Local Time: ${timePart}`;
    } catch (e) {
        localTimeEl.textContent = '';
    }

    // Update Favorite Star button state
    updateFavoriteBtnState();

    // Trigger Canvas Particle theme
    initParticles(condition.category);

    updateUnits();
}

function updateUnits() {
    if (!currentWeatherData) return;

    if (currentUnit === 'C') {
        temperatureValEl.textContent = round1(currentWeatherData.tempC);
        tempUnitEl.textContent = '°C';
        feelsLikeEl.textContent = `Feels like: ${round1(currentWeatherData.feelsLikeC)} °C`;
        windspeedValEl.textContent = `${round1(currentWeatherData.windSpeedKmh)} km/h`;
        precipValEl.textContent = `${round1(currentWeatherData.precipMm)} mm`;
        unitToggleBtn.textContent = '°F';
    } else {
        const tempF = cToF(currentWeatherData.tempC);
        const feelsLikeF = cToF(currentWeatherData.feelsLikeC);
        const windMph = kmhToMph(currentWeatherData.windSpeedKmh);
        const precipIn = mmToIn(currentWeatherData.precipMm);

        temperatureValEl.textContent = round1(tempF);
        tempUnitEl.textContent = '°F';
        feelsLikeEl.textContent = `Feels like: ${round1(feelsLikeF)} °F`;
        windspeedValEl.textContent = `${round1(windMph)} mph`;
        precipValEl.textContent = `${round1(precipIn)} in`;
        unitToggleBtn.textContent = '°C';
    }

    // Wind details
    windDirEl.textContent = currentWeatherData.windSpeedKmh > 20 ? 'Breezy' : 'Gentle';

    // Pressure
    pressureValEl.textContent = `${Math.round(currentWeatherData.pressureHpa)} hPa`;
    pressureDescEl.textContent = currentWeatherData.pressureHpa > 1015 ? 'High' : (currentWeatherData.pressureHpa < 1005 ? 'Low' : 'Normal');

    // UV Index
    const uv = round1(currentWeatherData.uvIndex);
    uvValEl.textContent = uv;
    if (uv <= 2) {
        uvBadgeEl.textContent = 'Low';
        uvBadgeEl.style.background = 'rgba(46, 204, 113, 0.4)';
    } else if (uv <= 5) {
        uvBadgeEl.textContent = 'Moderate';
        uvBadgeEl.style.background = 'rgba(241, 196, 15, 0.4)';
    } else if (uv <= 7) {
        uvBadgeEl.textContent = 'High';
        uvBadgeEl.style.background = 'rgba(230, 126, 34, 0.4)';
    } else {
        uvBadgeEl.textContent = 'Very High';
        uvBadgeEl.style.background = 'rgba(231, 76, 60, 0.4)';
    }

    // Rain chance
    rainChanceValEl.textContent = `${currentWeatherData.rainChance}%`;

    // Sun cycle (Sunrise / Sunset times)
    try {
        const sRise = currentWeatherData.sunrise.split('T')[1];
        const sSet = currentWeatherData.sunset.split('T')[1];
        sunriseValEl.textContent = `↑ ${sRise}`;
        sunsetValEl.textContent = `↓ ${sSet}`;
    } catch (e) {
        sunriseValEl.textContent = '↑ --:--';
        sunsetValEl.textContent = '↓ --:--';
    }

    renderHourly();
    renderForecast();
}

function renderHourly() {
    if (!currentWeatherData || !currentWeatherData.hourly) return;
    hourlyContainer.innerHTML = '';

    const hourly = currentWeatherData.hourly;
    const currentTimeStr = currentWeatherData.time;

    let startIndex = 0;
    const currentHourPrefix = currentTimeStr.substring(0, 13);
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
        const hourPart = timeStr.split('T')[1];
        const isNow = i === startIndex;

        const code = hourly.weather_code[i];
        const cond = getWeatherCondition(code);

        let temp = hourly.temperature_2m[i];
        if (currentUnit === 'F') temp = cToF(temp);

        const card = document.createElement('div');
        card.className = 'hourly-card';
        card.innerHTML = `
            <span class="hour-time">${isNow ? 'Now' : hourPart}</span>
            <img src="${cond.icon}" alt="${cond.desc}">
            <span class="hour-temp">${Math.round(temp)}°</span>
        `;
        hourlyContainer.appendChild(card);
    }
}

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
            <span class="temp-range">${Math.round(max)}° / ${Math.round(min)}°</span>
        `;
        forecastContainer.appendChild(dayCard);
    }
}

// -------------------------------------------------------------
// 5. Pinned Favorite Cities (⭐)
// -------------------------------------------------------------
function renderFavorites() {
    favoritesContainer.innerHTML = '';
    favorites.forEach(city => {
        const chip = document.createElement('div');
        chip.className = 'favorite-chip';
        chip.innerHTML = `
            <span>⭐ ${city}</span>
            <span class="remove-fav" title="Remove favorite">&times;</span>
        `;
        chip.querySelector('span:first-child').addEventListener('click', () => {
            handleSearch(city);
        });
        chip.querySelector('.remove-fav').addEventListener('click', (e) => {
            e.stopPropagation();
            removeFavorite(city);
        });
        favoritesContainer.appendChild(chip);
    });
}

function updateFavoriteBtnState() {
    if (!currentWeatherData) return;
    const shortCity = currentWeatherData.locationName.split(',')[0].trim();
    const isFav = favorites.includes(shortCity);
    favoriteBtn.textContent = isFav ? '★' : '☆';
    favoriteBtn.style.color = isFav ? '#ffd700' : '#ffffff';
}

function toggleFavorite() {
    if (!currentWeatherData) return;
    const shortCity = currentWeatherData.locationName.split(',')[0].trim();
    if (favorites.includes(shortCity)) {
        removeFavorite(shortCity);
        showToast(`Removed "${shortCity}" from favorites.`);
    } else {
        favorites.push(shortCity);
        localStorage.setItem('weather_favorites', JSON.stringify(favorites));
        renderFavorites();
        updateFavoriteBtnState();
        showToast(`⭐ Pinned "${shortCity}" to favorites!`);
    }
}

function removeFavorite(city) {
    favorites = favorites.filter(f => f !== city);
    localStorage.setItem('weather_favorites', JSON.stringify(favorites));
    renderFavorites();
    updateFavoriteBtnState();
}

favoriteBtn.addEventListener('click', toggleFavorite);
renderFavorites();

// -------------------------------------------------------------
// 6. 1-Click 📋 Share / Copy Weather Report
// -------------------------------------------------------------
copyReportBtn.addEventListener('click', () => {
    if (!currentWeatherData) return;

    const unit = currentUnit === 'C' ? '°C' : '°F';
    const windU = currentUnit === 'C' ? 'km/h' : 'mph';
    const temp = currentUnit === 'C' ? round1(currentWeatherData.tempC) : round1(cToF(currentWeatherData.tempC));
    const feels = currentUnit === 'C' ? round1(currentWeatherData.feelsLikeC) : round1(cToF(currentWeatherData.feelsLikeC));
    const wind = currentUnit === 'C' ? round1(currentWeatherData.windSpeedKmh) : round1(kmhToMph(currentWeatherData.windSpeedKmh));
    const condition = getWeatherCondition(currentWeatherData.weatherCode);

    const report = 
`🌤️ Weather Report for ${currentWeatherData.locationName}
🌡️ Temperature: ${temp} ${unit} (Feels like: ${feels} ${unit}) - ${condition.desc}
💧 Humidity: ${currentWeatherData.humidity}% | 💨 Wind: ${wind} ${windU}
📉 Pressure: ${Math.round(currentWeatherData.pressureHpa)} hPa | 🌧️ Rain Chance: ${currentWeatherData.rainChance}%
☀️ UV Index: ${round1(currentWeatherData.uvIndex)} | 🌅 Sunrise: ${currentWeatherData.sunrise.split('T')[1] || '--'} | 🌇 Sunset: ${currentWeatherData.sunset.split('T')[1] || '--'}`;

    navigator.clipboard.writeText(report).then(() => {
        showToast('✓ Weather report copied to clipboard!');
    }).catch(() => {
        showToast('Unable to copy report to clipboard.');
    });
});

// -------------------------------------------------------------
// 7. Geolocation with Reverse Geocoding
// -------------------------------------------------------------
locateBtn.addEventListener('click', () => {
    if (!navigator.geolocation) {
        showToast('Geolocation is not supported by your browser.');
        return;
    }

    spinner.classList.remove('hidden');
    navigator.geolocation.getCurrentPosition(
        async (position) => {
            const { latitude, longitude } = position.coords;
            let locationName = 'My Location';

            // Query free BigDataCloud reverse geocoding client
            try {
                const geoUrl = `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${latitude}&longitude=${longitude}&localityLanguage=en`;
                const geoRes = await fetch(geoUrl);
                if (geoRes.ok) {
                    const geoData = await geoRes.json();
                    const city = geoData.city || geoData.locality || geoData.principalSubdivision;
                    const country = geoData.countryName;
                    if (city) {
                        locationName = `${city}, ${country || ''}`.replace(/,\s*$/, '');
                    }
                }
            } catch (e) {
                console.warn('Reverse geocoding fallback used.');
            }

            await fetchWeather(latitude, longitude, locationName);
        },
        (error) => {
            spinner.classList.add('hidden');
            showToast('Unable to detect location. Please search manually.');
        }
    );
});

// -------------------------------------------------------------
// 8. General Search & Initial Launch
// -------------------------------------------------------------
async function handleSearch(cityName) {
    const query = (cityName || searchInput.value).trim();
    if (!query) return;

    searchInput.value = query;
    autocompleteList.classList.add('hidden');
    spinner.classList.remove('hidden');

    try {
        const url = `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(query)}&count=1&language=en&format=json`;
        const res = await fetch(url);
        if (!res.ok) throw new Error();
        const data = await res.json();
        if (!data.results || data.results.length === 0) {
            showToast(`Location "${query}" not found.`);
            spinner.classList.add('hidden');
            return;
        }

        const loc = data.results[0];
        const displayName = loc.name + (loc.country ? `, ${loc.country}` : '');
        await fetchWeather(loc.latitude, loc.longitude, displayName);
    } catch (err) {
        showToast('Search error. Check your connection.');
        spinner.classList.add('hidden');
    }
}

searchBtn.addEventListener('click', () => handleSearch());

defaultChips.forEach(chip => {
    chip.addEventListener('click', () => {
        handleSearch(chip.getAttribute('data-city'));
    });
});

unitToggleBtn.addEventListener('click', () => {
    currentUnit = currentUnit === 'C' ? 'F' : 'C';
    updateUnits();
});

// Startup launch
window.addEventListener('DOMContentLoaded', () => {
    handleSearch('Tokyo');
});
