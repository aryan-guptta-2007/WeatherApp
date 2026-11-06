import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {
        // apply system look and feel for modern native OS controls
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                // display our weather app gui
                new WeatherAppGui().setVisible(true);
            }
        });
    }
}
