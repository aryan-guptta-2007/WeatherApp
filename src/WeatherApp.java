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

// retreive weather data from API - this backend logic will fetch the latest weather
// data from the external API and return it. The GUI will
// display this data to the user
public class WeatherApp {
    // fetch weather data for given location
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

        // build API request URL with location coordinates
        // Using current weather endpoint and timezone=auto for accurate real-time data across all regions
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature" +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=auto";

        HttpURLConnection conn = null;
        try{
            // call api and get response
            conn = fetchApiResponse(urlString);

            // check for response status
            // 200 - means that the connection was a success
            if(conn == null || conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // store resulting json data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                // read and store into the string builder
                resultJson.append(scanner.nextLine());
            }

            // close scanner
            scanner.close();

            // parse through our data
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // 1. Try retrieving current real-time data directly
            JSONObject current = (JSONObject) resultJsonObj.get("current");
            if(current != null){
                double temperature = ((Number) current.get("temperature_2m")).doubleValue();
                double feelsLike = current.get("apparent_temperature") != null ?
                        ((Number) current.get("apparent_temperature")).doubleValue() : temperature;
                long weathercode = ((Number) current.get("weather_code")).longValue();
                String weatherCondition = convertWeatherCode(weathercode);
                long humidity = ((Number) current.get("relative_humidity_2m")).longValue();
                double windspeed = ((Number) current.get("wind_speed_10m")).doubleValue();

                // build the weather json data object that we are going to access in our frontend
                JSONObject weatherData = new JSONObject();
                weatherData.put("location_name", displayName);
                weatherData.put("temperature", temperature);
                weatherData.put("feels_like", feelsLike);
                weatherData.put("weather_condition", weatherCondition);
                weatherData.put("humidity", humidity);
                weatherData.put("windspeed", windspeed);

                return weatherData;
            }

            // 2. Fallback to hourly data if current is not available
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            if(hourly != null){
                JSONArray time = (JSONArray) hourly.get("time");
                int index = findIndexOfCurrentTime(time);

                JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
                double temperature = ((Number) temperatureData.get(index)).doubleValue();

                JSONArray weathercodeData = (JSONArray) hourly.get("weathercode");
                String weatherCondition = convertWeatherCode(((Number) weathercodeData.get(index)).longValue());

                JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
                long humidity = ((Number) relativeHumidity.get(index)).longValue();

                JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
                double windspeed = ((Number) windspeedData.get(index)).doubleValue();

                JSONObject weatherData = new JSONObject();
                weatherData.put("location_name", displayName);
                weatherData.put("temperature", temperature);
                weatherData.put("feels_like", temperature);
                weatherData.put("weather_condition", weatherCondition);
                weatherData.put("humidity", humidity);
                weatherData.put("windspeed", windspeed);

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
            // call api and get a response
            conn = fetchApiResponse(urlString);

            // check response status
            // 200 means successful connection
            if(conn == null || conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }else{
                // store the API results
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // read and store the resulting json data into our string builder
                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                // close scanner
                scanner.close();

                // parse the JSON string into a JSON obj
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                if(resultsJsonObj == null || !resultsJsonObj.containsKey("results")){
                    return null;
                }

                // get the list of location data the API generated from the location name
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(conn != null){
                conn.disconnect();
            }
        }

        // couldn't find location
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            // attempt to create connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set request method to get
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            // connect to our API
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }

        // could not make connection
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();

        // iterate through the time list and see which one matches our current time
        for(int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                // return the index
                return i;
            }
        }

        return 0;
    }

    public static String getCurrentTime(){
        // get current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // format date to be 2023-09-02T00:00 (this is how is is read in the API)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // format and print the current date and time
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    // convert the weather code to something more readable
    public static String convertWeatherCode(long weathercode){
        String weatherCondition = "Cloudy";
        if(weathercode == 0L){
            // clear
            weatherCondition = "Clear";
        }else if(weathercode >= 1L && weathercode <= 3L){
            // cloudy
            weatherCondition = "Cloudy";
        }else if(weathercode == 45L || weathercode == 48L){
            // fog
            weatherCondition = "Cloudy";
        }else if((weathercode >= 71L && weathercode <= 77L) || weathercode == 85L || weathercode == 86L){
            // snow & snow showers
            weatherCondition = "Snow";
        }else if((weathercode >= 51L && weathercode <= 67L)
                    || (weathercode >= 80L && weathercode <= 84L)
                    || (weathercode >= 95L && weathercode <= 99L)){
            // rain & drizzle & rain showers & thunderstorm
            weatherCondition = "Rain";
        }

        return weatherCondition;
    }
}
