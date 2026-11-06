import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// Backend service logic to fetch real-time weather and forecast data from Open-Meteo API
public class WeatherApp {

    // Fetch comprehensive weather data for given location
    public static JSONObject getWeatherData(String locationName){
        // get location coordinates using the geolocation API
        JSONArray locationData = getLocationData(locationName);

        // check if location was found
        if(locationData == null || locationData.isEmpty()){
            System.out.println("Error: Location data not found for " + locationName);
            return null;
        }

        // extract latitude, longitude, and friendly location name
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = ((Number) location.get("latitude")).doubleValue();
        double longitude = ((Number) location.get("longitude")).doubleValue();

        String cityName = (String) location.get("name");
        String country = (String) location.get("country");
        String displayName = (country != null && !country.trim().isEmpty()) ? cityName + ", " + country : cityName;

        // build API request URL with location coordinates, surface pressure, precipitation, and daily forecast
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature,surface_pressure,precipitation" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto";

        HttpURLConnection conn = null;
        try{
            // call api and get response
            conn = fetchApiResponse(urlString);

            if(conn == null || conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to Weather API");
                return null;
            }

            // store resulting json data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                resultJson.append(scanner.nextLine());
            }
            scanner.close();

            // parse through our data
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // Extract current weather data
            JSONObject current = (JSONObject) resultJsonObj.get("current");
            if(current != null){
                double temperature = ((Number) current.get("temperature_2m")).doubleValue();
                double feelsLike = current.get("apparent_temperature") != null ?
                        ((Number) current.get("apparent_temperature")).doubleValue() : temperature;
                long weathercode = ((Number) current.get("weather_code")).longValue();
                String weatherCondition = convertWeatherCode(weathercode);
                long humidity = ((Number) current.get("relative_humidity_2m")).longValue();
                double windspeed = ((Number) current.get("wind_speed_10m")).doubleValue();
                double pressure = current.get("surface_pressure") != null ?
                        ((Number) current.get("surface_pressure")).doubleValue() : 1013.25;
                double precipitation = current.get("precipitation") != null ?
                        ((Number) current.get("precipitation")).doubleValue() : 0.0;

                // Build frontend payload
                JSONObject weatherData = new JSONObject();
                weatherData.put("location_name", displayName);
                weatherData.put("temperature", temperature);
                weatherData.put("feels_like", feelsLike);
                weatherData.put("weather_condition", weatherCondition);
                weatherData.put("weather_code", weathercode);
                weatherData.put("humidity", humidity);
                weatherData.put("windspeed", windspeed);
                weatherData.put("pressure", pressure);
                weatherData.put("precipitation", precipitation);

                // Extract 3-day forecast
                JSONObject daily = (JSONObject) resultJsonObj.get("daily");
                if(daily != null){
                    JSONArray dailyTimes = (JSONArray) daily.get("time");
                    JSONArray dailyCodes = (JSONArray) daily.get("weather_code");
                    JSONArray dailyMaxTemps = (JSONArray) daily.get("temperature_2m_max");
                    JSONArray dailyMinTemps = (JSONArray) daily.get("temperature_2m_min");

                    JSONArray forecastList = new JSONArray();
                    int count = Math.min(3, dailyTimes.size());
                    for(int i = 0; i < count; i++){
                        JSONObject dayObj = new JSONObject();
                        dayObj.put("date", dailyTimes.get(i));
                        dayObj.put("weather_code", dailyCodes.get(i));
                        dayObj.put("condition", convertWeatherCode(((Number) dailyCodes.get(i)).longValue()));
                        dayObj.put("max_temp", ((Number) dailyMaxTemps.get(i)).doubleValue());
                        dayObj.put("min_temp", ((Number) dailyMinTemps.get(i)).doubleValue());
                        forecastList.add(dayObj);
                    }
                    weatherData.put("forecast", forecastList);
                }

                return weatherData;
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(conn != null){
                conn.disconnect();
            }
        }

        return null;
    }

    // retrieves geographic coordinates for given location name
    public static JSONArray getLocationData(String locationName){
        // sanitize and url-encode location name
        try{
            locationName = URLEncoder.encode(locationName.trim(), StandardCharsets.UTF_8);
        }catch(Exception e){
            locationName = locationName.replaceAll(" ", "+");
        }

        // build API url with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        HttpURLConnection conn = null;
        try{
            conn = fetchApiResponse(urlString);

            if(conn == null || conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to Geocoding API");
                return null;
            }else{
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }
                scanner.close();

                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                if(resultsJsonObj == null || !resultsJsonObj.containsKey("results")){
                    return null;
                }

                return (JSONArray) resultsJsonObj.get("results");
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(conn != null){
                conn.disconnect();
            }
        }

        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    // convert the weather code to standard readable condition
    public static String convertWeatherCode(long weathercode){
        String weatherCondition = "Cloudy";
        if(weathercode == 0L){
            weatherCondition = "Clear";
        }else if(weathercode >= 1L && weathercode <= 3L){
            weatherCondition = "Cloudy";
        }else if(weathercode == 45L || weathercode == 48L){
            weatherCondition = "Cloudy";
        }else if((weathercode >= 71L && weathercode <= 77L) || weathercode == 85L || weathercode == 86L){
            weatherCondition = "Snow";
        }else if((weathercode >= 51L && weathercode <= 67L)
                    || (weathercode >= 80L && weathercode <= 84L)
                    || (weathercode >= 95L && weathercode <= 99L)){
            weatherCondition = "Rain";
        }

        return weatherCondition;
    }
}
