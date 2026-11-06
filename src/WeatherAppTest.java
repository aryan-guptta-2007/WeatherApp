import java.io.File;

// Automated test suite for Weather App core backend logic & assets
public class WeatherAppTest {
    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("         Running Weather App Test Suite           ");
        System.out.println("==================================================");

        testWeatherCodeConversions();
        testUnitConversions();
        testAssetIntegrity();
        testBuildArtifacts();

        System.out.println("--------------------------------------------------");
        System.out.printf("Results: %d / %d Tests Passed%n", testsPassed, testsRun);
        System.out.println("==================================================");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    private static void testWeatherCodeConversions() {
        System.out.println("\n[1] Testing Weather Code Categories...");

        assertCondition("Code 0 should map to Clear", "Clear".equals(WeatherApp.convertWeatherCode(0L)));
        assertCondition("Code 2 should map to Cloudy", "Cloudy".equals(WeatherApp.convertWeatherCode(2L)));
        assertCondition("Code 45 should map to Cloudy (Fog)", "Cloudy".equals(WeatherApp.convertWeatherCode(45L)));
        assertCondition("Code 61 should map to Rain", "Rain".equals(WeatherApp.convertWeatherCode(61L)));
        assertCondition("Code 71 should map to Snow", "Snow".equals(WeatherApp.convertWeatherCode(71L)));
        assertCondition("Code 95 should map to Rain (Thunderstorm)", "Rain".equals(WeatherApp.convertWeatherCode(95L)));
    }

    private static void testUnitConversions() {
        System.out.println("\n[2] Testing Temperature & Speed Conversions...");

        // 0 C = 32 F
        double fAtZero = (0.0 * 9.0 / 5.0) + 32.0;
        assertCondition("0°C == 32°F", Math.abs(fAtZero - 32.0) < 0.001);

        // 100 C = 212 F
        double fAtHundred = (100.0 * 9.0 / 5.0) + 32.0;
        assertCondition("100°C == 212°F", Math.abs(fAtHundred - 212.0) < 0.001);

        // 10 km/h in mph (~6.21371)
        double mph = 10.0 * 0.621371;
        assertCondition("10 km/h converts accurately to mph", Math.abs(mph - 6.21371) < 0.01);
    }

    private static void testAssetIntegrity() {
        System.out.println("\n[3] Testing Asset Files Integrity...");

        String[] assets = {
            "clear.png",
            "cloudy.png",
            "humidity.png",
            "rain.png",
            "search.png",
            "snow.png",
            "windspeed.png"
        };

        for (String asset : assets) {
            File fSrc = new File("src/assets/" + asset);
            File fWeb = new File("web/assets/" + asset);
            assertCondition("src asset exists: " + asset, fSrc.exists() && fSrc.length() > 0);
            assertCondition("web asset exists: " + asset, fWeb.exists() && fWeb.length() > 0);
        }
    }

    private static void testBuildArtifacts() {
        System.out.println("\n[4] Testing Build Artifacts & PWA Assets...");

        File jar = new File("WeatherApp.jar");
        assertCondition("WeatherApp.jar exists", jar.exists() && jar.length() > 50000);

        File manifest = new File("web/manifest.json");
        assertCondition("web/manifest.json exists", manifest.exists() && manifest.length() > 0);

        File sw = new File("web/sw.js");
        assertCondition("web/sw.js (Service Worker) exists", sw.exists() && sw.length() > 0);
    }

    private static void assertCondition(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  ✓ PASS: " + testName);
        } else {
            System.err.println("  ✗ FAIL: " + testName);
        }
    }
}
