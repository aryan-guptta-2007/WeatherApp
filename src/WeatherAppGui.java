import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;
    private String currentCity = "Tokyo";

    // Custom gradient panel
    private GradientBackgroundPanel mainPanel;

    // GUI Components
    private JTextField searchTextField;
    private JButton searchButton;
    private JLabel locationLabel;
    private JLabel weatherConditionImage;
    private JLabel temperatureText;
    private JLabel weatherConditionDesc;
    private JLabel feelsLikeText;

    // 4 Atmospheric Metrics Components
    private JLabel humidityValLabel;
    private JLabel windspeedValLabel;
    private JLabel pressureValLabel;
    private JLabel precipValLabel;

    // 3-Day Forecast Components
    private JPanel[] forecastCards = new JPanel[3];
    private JLabel[] forecastDayLabels = new JLabel[3];
    private JLabel[] forecastIconLabels = new JLabel[3];
    private JLabel[] forecastTempLabels = new JLabel[3];

    // Bottom Action Buttons
    private JButton unitToggleButton;
    private JButton copyReportButton;
    private JButton refreshButton;

    // Cached metric values for instantaneous unit toggles
    private boolean isCelsius = true;
    private double currentTempC = 16.0;
    private double currentFeelsLikeC = 14.5;
    private double currentWindSpeedKmh = 6.3;
    private double currentPressureHpa = 1013.0;
    private double currentPrecipMm = 0.0;
    private String currentConditionStr = "Cloudy";
    private JSONArray currentForecastArray = null;

    // Auto-refresh timer (15 minutes = 900,000 ms)
    private Timer autoRefreshTimer;

    public WeatherAppGui(){
        super("Weather App - Commercial Edition");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(490, 755);
        setLocationRelativeTo(null);
        setResizable(false);

        // Set application window & taskbar icon
        ImageIcon appIcon = loadImage("src/assets/cloudy.png");
        if(appIcon != null){
            setIconImage(appIcon.getImage());
        }

        // Initialize dynamic gradient container
        mainPanel = new GradientBackgroundPanel();
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        addGuiComponents();
        setupKeyBindings();
        setupAutoRefresh();

        // Quiet startup: fetch initial weather without blocking modal popups
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                searchTextField.setText(currentCity);
                performSearch(false);
            }
        });
    }

    private void addGuiComponents(){
        // 1. Search Bar Field
        searchTextField = new JTextField();
        searchTextField.setBounds(15, 15, 395, 42);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 18));
        searchTextField.setToolTipText("Enter city name and press Enter");
        searchTextField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 10)
        ));
        searchTextField.setBackground(new Color(255, 255, 255, 210));
        searchTextField.setForeground(new Color(25, 25, 25));

        // Auto-select text on click
        searchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                searchTextField.selectAll();
            }
        });

        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch(true);
            }
        });
        mainPanel.add(searchTextField);

        // 2. Search Button
        searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setBounds(418, 15, 42, 42);
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setToolTipText("Search weather");
        searchButton.setBackground(new Color(255, 255, 255, 200));
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 140), 1, true));
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch(true);
            }
        });
        mainPanel.add(searchButton);

        // 3. Quick City Chips Bar
        String[] popularCities = {"Tokyo", "London", "New York", "Paris", "Sydney"};
        int chipX = 15;
        int chipW = 86;
        for (String city : popularCities) {
            JButton chipBtn = createGlassChipButton(city);
            chipBtn.setBounds(chipX, 66, chipW, 26);
            chipBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchTextField.setText(city);
                    performSearch(true);
                }
            });
            mainPanel.add(chipBtn);
            chipX += chipW + 5;
        }

        // 4. Location Name Label
        locationLabel = new JLabel("Tokyo, Japan");
        locationLabel.setBounds(15, 100, 450, 28);
        locationLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        locationLabel.setForeground(Color.WHITE);
        locationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(locationLabel);

        // 5. Weather Condition Illustration
        weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(175, 130, 140, 125);
        weatherConditionImage.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(weatherConditionImage);

        // 6. Temperature Reading
        temperatureText = new JLabel("16.0 °C");
        temperatureText.setBounds(15, 258, 450, 46);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 46));
        temperatureText.setForeground(Color.WHITE);
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(temperatureText);

        // 7. Weather Condition Description
        weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(15, 306, 450, 26);
        weatherConditionDesc.setFont(new Font("Dialog", Font.BOLD, 20));
        weatherConditionDesc.setForeground(Color.WHITE);
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(weatherConditionDesc);

        // 8. Feels Like Reading
        feelsLikeText = new JLabel("Feels like: 14.5 °C");
        feelsLikeText.setBounds(15, 332, 450, 20);
        feelsLikeText.setFont(new Font("Dialog", Font.PLAIN, 14));
        feelsLikeText.setForeground(new Color(230, 240, 255));
        feelsLikeText.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(feelsLikeText);

        // 9. 4 Atmospheric Metrics (2x2 Glass Grid)
        // Card 1: Humidity
        JPanel humCard = createMetricCard("src/assets/humidity.png", "HUMIDITY", "60%");
        humCard.setBounds(15, 360, 218, 62);
        humidityValLabel = (JLabel) humCard.getClientProperty("valueLabel");
        mainPanel.add(humCard);

        // Card 2: Wind Speed
        JPanel windCard = createMetricCard("src/assets/windspeed.png", "WIND SPEED", "6.3 km/h");
        windCard.setBounds(247, 360, 218, 62);
        windspeedValLabel = (JLabel) windCard.getClientProperty("valueLabel");
        mainPanel.add(windCard);

        // Card 3: Atmospheric Pressure
        JPanel pressCard = createMetricCard(null, "PRESSURE", "1013 hPa");
        pressCard.setBounds(15, 430, 218, 62);
        pressureValLabel = (JLabel) pressCard.getClientProperty("valueLabel");
        mainPanel.add(pressCard);

        // Card 4: Precipitation
        JPanel precipCard = createMetricCard(null, "PRECIPITATION", "0.0 mm");
        precipCard.setBounds(247, 430, 218, 62);
        precipValLabel = (JLabel) precipCard.getClientProperty("valueLabel");
        mainPanel.add(precipCard);

        // 10. 3-Day Forecast Section Header
        JLabel forecastTitle = new JLabel("3-DAY FORECAST");
        forecastTitle.setBounds(15, 502, 450, 18);
        forecastTitle.setFont(new Font("Dialog", Font.BOLD, 12));
        forecastTitle.setForeground(new Color(220, 235, 255));
        mainPanel.add(forecastTitle);

        // 11. 3-Day Forecast Cards Deck
        int fX = 15;
        int fW = 143;
        String[] defaultDays = {"Today", "Tomorrow", "Day 3"};
        for(int i = 0; i < 3; i++){
            forecastCards[i] = new RoundedGlassPanel(14);
            forecastCards[i].setLayout(null);
            forecastCards[i].setBounds(fX, 524, fW, 88);

            forecastDayLabels[i] = new JLabel(defaultDays[i]);
            forecastDayLabels[i].setBounds(0, 8, fW, 16);
            forecastDayLabels[i].setFont(new Font("Dialog", Font.BOLD, 12));
            forecastDayLabels[i].setForeground(Color.WHITE);
            forecastDayLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            forecastCards[i].add(forecastDayLabels[i]);

            forecastIconLabels[i] = new JLabel(loadScaledImage("src/assets/cloudy.png", 30, 30));
            forecastIconLabels[i].setBounds(0, 26, fW, 34);
            forecastIconLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            forecastCards[i].add(forecastIconLabels[i]);

            forecastTempLabels[i] = new JLabel("--° / --°");
            forecastTempLabels[i].setBounds(0, 62, fW, 18);
            forecastTempLabels[i].setFont(new Font("Dialog", Font.BOLD, 12));
            forecastTempLabels[i].setForeground(new Color(230, 245, 255));
            forecastTempLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            forecastCards[i].add(forecastTempLabels[i]);

            mainPanel.add(forecastCards[i]);
            fX += fW + 10;
        }

        // 12. Bottom Action Controls
        // Unit Toggle Button
        unitToggleButton = createActionButton("Switch to °F");
        unitToggleButton.setBounds(15, 628, 140, 34);
        unitToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCelsius = !isCelsius;
                updateDisplayUnits();
            }
        });
        mainPanel.add(unitToggleButton);

        // Copy Weather Report Button
        copyReportButton = createActionButton("📋 Copy Report");
        copyReportButton.setBounds(165, 628, 150, 34);
        copyReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyWeatherReportToClipboard();
            }
        });
        mainPanel.add(copyReportButton);

        // Refresh Button
        refreshButton = createActionButton("🔄 Refresh (F5)");
        refreshButton.setBounds(325, 628, 140, 34);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch(true);
            }
        });
        mainPanel.add(refreshButton);
    }

    private void setupKeyBindings(){
        // Register F5 key to refresh weather
        JRootPane rootPane = getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refreshWeather"
        );
        rootPane.getActionMap().put("refreshWeather", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch(true);
            }
        });
    }

    private void setupAutoRefresh(){
        // Silently refresh current location every 15 minutes
        autoRefreshTimer = new Timer(900000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch(false);
            }
        });
        autoRefreshTimer.start();
    }

    private void performSearch(final boolean showUserFeedback){
        final String userInput = searchTextField.getText().trim();
        if(userInput.isEmpty()){
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        searchButton.setEnabled(false);
        refreshButton.setEnabled(false);

        SwingWorker<JSONObject, Void> worker = new SwingWorker<JSONObject, Void>() {
            @Override
            protected JSONObject doInBackground() {
                return WeatherApp.getWeatherData(userInput);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                searchButton.setEnabled(true);
                refreshButton.setEnabled(true);

                try {
                    weatherData = get();

                    if(weatherData == null){
                        if(showUserFeedback){
                            JOptionPane.showMessageDialog(
                                    WeatherAppGui.this,
                                    "Could not find weather data for \"" + userInput + "\".\nPlease check spelling and connection.",
                                    "Location Not Found",
                                    JOptionPane.WARNING_MESSAGE
                            );
                        } else {
                            locationLabel.setText("Offline / Reconnecting...");
                        }
                        return;
                    }

                    // Update active location
                    currentCity = (String) weatherData.get("location_name");
                    locationLabel.setText(currentCity);
                    locationLabel.setToolTipText(currentCity);

                    // Extract metrics
                    currentConditionStr = (String) weatherData.get("weather_condition");
                    if(currentConditionStr == null) currentConditionStr = "Cloudy";

                    currentTempC = ((Number) weatherData.get("temperature")).doubleValue();
                    currentFeelsLikeC = ((Number) weatherData.get("feels_like")).doubleValue();
                    currentWindSpeedKmh = ((Number) weatherData.get("windspeed")).doubleValue();
                    currentPressureHpa = ((Number) weatherData.get("pressure")).doubleValue();
                    currentPrecipMm = ((Number) weatherData.get("precipitation")).doubleValue();
                    currentForecastArray = (JSONArray) weatherData.get("forecast");

                    // Update UI condition illustration
                    updateConditionGraphic(currentConditionStr);

                    // Update Dynamic Theme Gradient
                    mainPanel.setConditionTheme(currentConditionStr);

                    // Update text and numbers according to unit
                    updateDisplayUnits();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    if(showUserFeedback){
                        JOptionPane.showMessageDialog(
                                WeatherAppGui.this,
                                "Error retrieving weather data.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        };

        worker.execute();
    }

    private void updateConditionGraphic(String condition){
        switch(condition){
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
    }

    private void updateDisplayUnits(){
        weatherConditionDesc.setText(currentConditionStr);

        if(isCelsius){
            temperatureText.setText(roundToOneDecimal(currentTempC) + " °C");
            feelsLikeText.setText("Feels like: " + roundToOneDecimal(currentFeelsLikeC) + " °C");
            windspeedValLabel.setText(roundToOneDecimal(currentWindSpeedKmh) + " km/h");
            precipValLabel.setText(roundToOneDecimal(currentPrecipMm) + " mm");
            unitToggleButton.setText("Switch to °F");
        } else {
            double tempF = (currentTempC * 9.0 / 5.0) + 32.0;
            double feelsLikeF = (currentFeelsLikeC * 9.0 / 5.0) + 32.0;
            double windMph = currentWindSpeedKmh * 0.621371;
            double precipIn = currentPrecipMm * 0.0393701;

            temperatureText.setText(roundToOneDecimal(tempF) + " °F");
            feelsLikeText.setText("Feels like: " + roundToOneDecimal(feelsLikeF) + " °F");
            windspeedValLabel.setText(roundToOneDecimal(windMph) + " mph");
            precipValLabel.setText(roundToOneDecimal(precipIn) + " in");
            unitToggleButton.setText("Switch to °C");
        }

        humidityValLabel.setText(((Number) weatherData.get("humidity")).longValue() + "%");
        pressureValLabel.setText(Math.round(currentPressureHpa) + " hPa");

        // Update 3-Day Forecast cards
        if(currentForecastArray != null && currentForecastArray.size() >= 3){
            String[] dayTitles = {"Today", "Tomorrow", "Day 3"};
            for(int i = 0; i < 3; i++){
                JSONObject day = (JSONObject) currentForecastArray.get(i);
                forecastDayLabels[i].setText(dayTitles[i]);

                String cond = (String) day.get("condition");
                forecastIconLabels[i].setIcon(loadScaledImage(getIconPathForCondition(cond), 30, 30));

                double max = ((Number) day.get("max_temp")).doubleValue();
                double min = ((Number) day.get("min_temp")).doubleValue();

                if(!isCelsius){
                    max = (max * 9.0 / 5.0) + 32.0;
                    min = (min * 9.0 / 5.0) + 32.0;
                }
                forecastTempLabels[i].setText(Math.round(max) + "° / " + Math.round(min) + "°");
            }
        }
    }

    private void copyWeatherReportToClipboard(){
        String unitSign = isCelsius ? "°C" : "°F";
        String windUnit = isCelsius ? "km/h" : "mph";
        double temp = isCelsius ? currentTempC : (currentTempC * 9.0 / 5.0) + 32.0;
        double feels = isCelsius ? currentFeelsLikeC : (currentFeelsLikeC * 9.0 / 5.0) + 32.0;
        double wind = isCelsius ? currentWindSpeedKmh : currentWindSpeedKmh * 0.621371;

        StringBuilder sb = new StringBuilder();
        sb.append("🌤️ Weather Report for ").append(currentCity).append("\n");
        sb.append("🌡️ Temperature: ").append(roundToOneDecimal(temp)).append(" ").append(unitSign)
          .append(" (Feels like: ").append(roundToOneDecimal(feels)).append(" ").append(unitSign).append(") - ").append(currentConditionStr).append("\n");
        sb.append("💧 Humidity: ").append(humidityValLabel.getText())
          .append(" | 💨 Wind: ").append(roundToOneDecimal(wind)).append(" ").append(windUnit).append("\n");
        sb.append("📉 Pressure: ").append(pressureValLabel.getText())
          .append(" | 🌧️ Precipitation: ").append(precipValLabel.getText()).append("\n");

        if(currentForecastArray != null && currentForecastArray.size() >= 3){
            sb.append("📅 3-Day Outlook:\n");
            String[] titles = {"Today", "Tomorrow", "Day 3"};
            for(int i = 0; i < 3; i++){
                JSONObject d = (JSONObject) currentForecastArray.get(i);
                double max = ((Number) d.get("max_temp")).doubleValue();
                double min = ((Number) d.get("min_temp")).doubleValue();
                if(!isCelsius){
                    max = (max * 9.0 / 5.0) + 32.0;
                    min = (min * 9.0 / 5.0) + 32.0;
                }
                sb.append("   • ").append(titles[i]).append(": ")
                  .append(d.get("condition")).append(" (")
                  .append(Math.round(max)).append("° / ").append(Math.round(min)).append("°)\n");
            }
        }

        StringSelection selection = new StringSelection(sb.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        // Visual confirmation badge
        copyReportButton.setText("✓ Copied!");
        copyReportButton.setBackground(new Color(46, 204, 113));
        copyReportButton.setForeground(Color.WHITE);

        Timer resetTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyReportButton.setText("📋 Copy Report");
                copyReportButton.setBackground(new Color(255, 255, 255, 180));
                copyReportButton.setForeground(new Color(20, 20, 20));
            }
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }

    private String getIconPathForCondition(String condition){
        if(condition == null) return "src/assets/cloudy.png";
        switch(condition){
            case "Clear": return "src/assets/clear.png";
            case "Cloudy": return "src/assets/cloudy.png";
            case "Rain": return "src/assets/rain.png";
            case "Snow": return "src/assets/snow.png";
            default: return "src/assets/cloudy.png";
        }
    }

    private JPanel createMetricCard(String iconPath, String title, String initialVal){
        RoundedGlassPanel panel = new RoundedGlassPanel(16);
        panel.setLayout(null);

        if(iconPath != null){
            JLabel iconLabel = new JLabel(loadScaledImage(iconPath, 28, 28));
            iconLabel.setBounds(10, 16, 30, 30);
            panel.add(iconLabel);
        } else {
            // Text symbol for Pressure or Precipitation
            String symbol = title.equals("PRESSURE") ? "📉" : "🌧️";
            JLabel symLabel = new JLabel(symbol);
            symLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            symLabel.setBounds(10, 15, 30, 30);
            panel.add(symLabel);
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setBounds(46, 12, 165, 14);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 10));
        titleLabel.setForeground(new Color(220, 235, 255));
        panel.add(titleLabel);

        JLabel valLabel = new JLabel(initialVal);
        valLabel.setBounds(46, 28, 165, 24);
        valLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        valLabel.setForeground(Color.WHITE);
        panel.add(valLabel);

        panel.putClientProperty("valueLabel", valLabel);
        return panel;
    }

    private JButton createGlassChipButton(String text){
        JButton btn = new JButton(text);
        btn.setFont(new Font("Dialog", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(255, 255, 255, 50));
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1, true));
        return btn;
    }

    private JButton createActionButton(String text){
        JButton btn = new JButton(text);
        btn.setFont(new Font("Dialog", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(255, 255, 255, 180));
        btn.setForeground(new Color(20, 20, 20));
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 210), 1, true));
        return btn;
    }

    private double roundToOneDecimal(double val){
        return Math.round(val * 10.0) / 10.0;
    }

    private ImageIcon loadScaledImage(String path, int w, int h){
        ImageIcon icon = loadImage(path);
        if(icon != null){
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }

    private ImageIcon loadImage(String resourcePath){
        try{
            String pathInClasspath = resourcePath.startsWith("src/") ? resourcePath.substring(3) : resourcePath;
            if(!pathInClasspath.startsWith("/")){
                pathInClasspath = "/" + pathInClasspath;
            }
            URL resourceUrl = getClass().getResource(pathInClasspath);
            if(resourceUrl != null){
                BufferedImage image = ImageIO.read(resourceUrl);
                if(image != null) return new ImageIcon(image);
            }

            File file = new File(resourcePath);
            if(file.exists()){
                BufferedImage image = ImageIO.read(file);
                if(image != null) return new ImageIcon(image);
            }

            File altFile = new File("WeatherAppGUI-Java-main", resourcePath);
            if(altFile.exists()){
                BufferedImage image = ImageIO.read(altFile);
                if(image != null) return new ImageIcon(image);
            }

            if(resourcePath.startsWith("src/")){
                File stripped = new File(resourcePath.substring(4));
                if(stripped.exists()){
                    BufferedImage image = ImageIO.read(stripped);
                    if(image != null) return new ImageIcon(image);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    // Dynamic Anti-Aliased Gradient Container Panel
    private static class GradientBackgroundPanel extends JPanel {
        private Color topColor = new Color(43, 48, 53);      // Default Cool Slate
        private Color bottomColor = new Color(73, 80, 87);

        public void setConditionTheme(String condition){
            if(condition == null) condition = "Cloudy";
            switch(condition){
                case "Clear":
                    // Daylight Sapphire
                    topColor = new Color(24, 100, 171);
                    bottomColor = new Color(77, 171, 247);
                    break;
                case "Cloudy":
                    // Cool Slate Graphite
                    topColor = new Color(43, 48, 53);
                    bottomColor = new Color(73, 80, 87);
                    break;
                case "Rain":
                    // Midnight Navy Indigo
                    topColor = new Color(11, 29, 58);
                    bottomColor = new Color(24, 59, 107);
                    break;
                case "Snow":
                    // Frost Crystal Teal
                    topColor = new Color(14, 61, 79);
                    bottomColor = new Color(46, 134, 193);
                    break;
                default:
                    topColor = new Color(43, 48, 53);
                    bottomColor = new Color(73, 80, 87);
                    break;
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }

    // Custom Rounded Glass Panel for cards
    private static class RoundedGlassPanel extends JPanel {
        private final int radius;

        public RoundedGlassPanel(int radius){
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(255, 255, 255, 38));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2d.setColor(new Color(255, 255, 255, 75));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2d.dispose();
        }
    }
}
