import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    private JTextField searchTextField;
    private JButton searchButton;
    private JLabel locationLabel;
    private JLabel weatherConditionImage;
    private JLabel temperatureText;
    private JLabel weatherConditionDesc;
    private JLabel feelsLikeText;
    private JButton unitToggleButton;
    private JLabel humidityText;
    private JLabel windspeedText;

    // Unit toggle state
    private boolean isCelsius = true;
    private double currentTempC = 10.0;
    private double currentFeelsLikeC = 10.0;
    private double currentWindSpeedKmh = 15.0;

    public WeatherAppGui(){
        // setup our gui and add a title
        super("Weather App");

        // configure gui to end the program's process once it has been closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set the size of our gui (in pixels)
        setSize(450, 650);

        // load our gui at the center of the screen
        setLocationRelativeTo(null);

        // make our layout manager null to manually position our components within the gui
        setLayout(null);

        // prevent any resize of our gui
        setResizable(false);

        // set application window and taskbar icon
        ImageIcon appIcon = loadImage("src/assets/cloudy.png");
        if(appIcon != null){
            setIconImage(appIcon.getImage());
        }

        addGuiComponents();

        // auto-load default city on startup so app is immediately live
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                searchTextField.setText("Tokyo");
                performSearch();
            }
        });
    }

    private void addGuiComponents(){
        // search field
        searchTextField = new JTextField();
        searchTextField.setBounds(15, 15, 351, 45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 22));
        searchTextField.setToolTipText("Enter city name and press Enter");

        // allow pressing Enter key to trigger search
        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        add(searchTextField);

        // search button
        searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.setToolTipText("Search weather");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        add(searchButton);

        // location name display
        locationLabel = new JLabel("Loading location...");
        locationLabel.setBounds(0, 70, 450, 30);
        locationLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        locationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(locationLabel);

        // weather image
        weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 105, 450, 215);
        add(weatherConditionImage);

        // temperature text
        temperatureText = new JLabel("10 °C");
        temperatureText.setBounds(0, 325, 450, 50);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 46));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // weather condition description
        weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 375, 450, 32);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 26));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // feels like temperature text
        feelsLikeText = new JLabel("Feels like: 10 °C");
        feelsLikeText.setBounds(0, 410, 450, 24);
        feelsLikeText.setFont(new Font("Dialog", Font.PLAIN, 16));
        feelsLikeText.setForeground(new Color(80, 80, 80));
        feelsLikeText.setHorizontalAlignment(SwingConstants.CENTER);
        add(feelsLikeText);

        // Celsius / Fahrenheit unit toggle button
        unitToggleButton = new JButton("Switch to °F");
        unitToggleButton.setBounds(155, 442, 140, 30);
        unitToggleButton.setFont(new Font("Dialog", Font.BOLD, 12));
        unitToggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        unitToggleButton.setFocusPainted(false);
        unitToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCelsius = !isCelsius;
                updateDisplayUnits();
            }
        });
        add(unitToggleButton);

        // humidity image
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // humidity text
        humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // windspeed image
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // windspeed text
        windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);
    }

    private void performSearch(){
        // get location from user
        String userInput = searchTextField.getText();

        // validate input - remove whitespace to ensure non-empty text
        if(userInput.replaceAll("\\s", "").length() <= 0){
            return;
        }

        // show loading indicator state
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        searchButton.setEnabled(false);

        // fetch weather data asynchronously to prevent GUI freezing
        SwingWorker<JSONObject, Void> worker = new SwingWorker<JSONObject, Void>() {
            @Override
            protected JSONObject doInBackground() {
                return WeatherApp.getWeatherData(userInput);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                searchButton.setEnabled(true);

                try {
                    weatherData = get();

                    // validate if weather data was retrieved
                    if(weatherData == null){
                        JOptionPane.showMessageDialog(
                                WeatherAppGui.this,
                                "Could not find weather data for \"" + userInput + "\".\nPlease verify the location name and try again.",
                                "Location Not Found",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    // update location name
                    String locationName = (String) weatherData.get("location_name");
                    if(locationName != null){
                        locationLabel.setText(locationName);
                        locationLabel.setToolTipText(locationName);
                    }

                    // update weather image
                    String weatherCondition = (String) weatherData.get("weather_condition");
                    if(weatherCondition == null){
                        weatherCondition = "Cloudy";
                    }

                    // update weather icon
                    switch(weatherCondition){
                        case "Clear":
                            weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                            break;
                        case "Cloudy":
                            weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                            break;
                        case "Rain":
                            weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                            break;
                        case "Snow":
                            weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                            break;
                        default:
                            weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                            break;
                    }

                    // cache metric values for unit toggling
                    currentTempC = ((Number) weatherData.get("temperature")).doubleValue();
                    currentFeelsLikeC = weatherData.get("feels_like") != null ?
                            ((Number) weatherData.get("feels_like")).doubleValue() : currentTempC;
                    currentWindSpeedKmh = ((Number) weatherData.get("windspeed")).doubleValue();

                    // update weather condition description text
                    weatherConditionDesc.setText(weatherCondition);

                    // update humidity text
                    long humidity = ((Number) weatherData.get("humidity")).longValue();
                    humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                    // update temperatures and wind speed based on selected unit
                    updateDisplayUnits();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            WeatherAppGui.this,
                            "An unexpected error occurred while fetching weather data.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    // updates temperature, feels-like, and wind speed units based on isCelsius state
    private void updateDisplayUnits(){
        if(isCelsius){
            temperatureText.setText(roundToOneDecimal(currentTempC) + " °C");
            feelsLikeText.setText("Feels like: " + roundToOneDecimal(currentFeelsLikeC) + " °C");
            windspeedText.setText("<html><b>Windspeed</b> " + roundToOneDecimal(currentWindSpeedKmh) + "km/h</html>");
            unitToggleButton.setText("Switch to °F");
        }else{
            double tempF = (currentTempC * 9.0 / 5.0) + 32.0;
            double feelsLikeF = (currentFeelsLikeC * 9.0 / 5.0) + 32.0;
            double windspeedMph = currentWindSpeedKmh * 0.621371;

            temperatureText.setText(roundToOneDecimal(tempF) + " °F");
            feelsLikeText.setText("Feels like: " + roundToOneDecimal(feelsLikeF) + " °F");
            windspeedText.setText("<html><b>Windspeed</b> " + roundToOneDecimal(windspeedMph) + "mph</html>");
            unitToggleButton.setText("Switch to °C");
        }
    }

    private double roundToOneDecimal(double val){
        return Math.round(val * 10.0) / 10.0;
    }

    // used to create images in our gui components
    private ImageIcon loadImage(String resourcePath){
        try{
            // 1. Try loading as classpath resource (works inside JAR and standard classpath)
            String pathInClasspath = resourcePath.startsWith("src/") ? resourcePath.substring(3) : resourcePath;
            if(!pathInClasspath.startsWith("/")){
                pathInClasspath = "/" + pathInClasspath;
            }
            URL resourceUrl = getClass().getResource(pathInClasspath);
            if(resourceUrl != null){
                BufferedImage image = ImageIO.read(resourceUrl);
                if(image != null){
                    return new ImageIcon(image);
                }
            }

            // 2. Try direct file path
            File file = new File(resourcePath);
            if(file.exists()){
                BufferedImage image = ImageIO.read(file);
                if(image != null){
                    return new ImageIcon(image);
                }
            }

            // 3. Try relative to project directory (WeatherAppGUI-Java-main)
            File altFile = new File("WeatherAppGUI-Java-main", resourcePath);
            if(altFile.exists()){
                BufferedImage image = ImageIO.read(altFile);
                if(image != null){
                    return new ImageIcon(image);
                }
            }

            // 4. Try stripping "src/" if running from root where src/ isn't in working directory
            if(resourcePath.startsWith("src/")){
                File strippedFile = new File(resourcePath.substring(4));
                if(strippedFile.exists()){
                    BufferedImage image = ImageIO.read(strippedFile);
                    if(image != null){
                        return new ImageIcon(image);
                    }
                }
                File strippedAltFile = new File("WeatherAppGUI-Java-main", resourcePath.substring(4));
                if(strippedAltFile.exists()){
                    BufferedImage image = ImageIO.read(strippedAltFile);
                    if(image != null){
                        return new ImageIcon(image);
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Could not find resource: " + resourcePath);
        return null;
    }
}
