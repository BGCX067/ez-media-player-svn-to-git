package interfaz;

import biblioteca.XML;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javafx.embed.swing.JFXPanel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import mediaPlayer.ElementPlayer;
import mediaPlayer.Log;

/**
 * Main class.
 */
public abstract class Initializer {

    /**
     * All messages to use during the application execution are read just once
     * and stored in this arraylist.
     */
    private static final ArrayList<String> messages = new ArrayList<>();
    /**
     * The main frame.
     */
    private static final JFrame frame = new JFrame();
    /**
     * The main window.
     */
    private static final MainWindow mainWindow = MainWindow.getInstance();
    /**
     * The hardcoded URI of the file containing the system data.
     */
    private static final String dataURI = "/res/config.xml", resources = "/res/";

    /**
     * Main method.
     *
     * @param args Array of {@link String} The command line arguments.
     */
    public static void main(String[] args) {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            @SuppressWarnings("ResultOfObjectAllocationIgnored")
            public void run() {
                new JFXPanel(); // Initializes JavaFX environment and toolkit (mandatory)
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Initializer.logAndForceExit(ex);
        }
        Initializer.loadMessages();
        new Thread() {
            @Override
            public void run() {
                Initializer.initializeSplashScreen();
                Initializer.updateLoadProgress();
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                Initializer.initInterface();
                Initializer.showGUI();
            }
        }.start();
    }

    /**
     * Parses the configuration file for setting up the messages to display
     * during this execution.
     */
    private static void loadMessages() {
        String currentMessage = XML.getAttribute("message", "id", "msg0", "msg", Initializer.dataURI);
        int currentId = 1;

        Initializer.messages.add(currentMessage);
        while (currentMessage != null) {
            currentMessage = XML.getAttribute("message", "id", "msg" + currentId, "msg", Initializer.dataURI);
            if (currentMessage != null) {
                Initializer.messages.add(currentMessage);
                currentId++;
            } else {
                break;
            }
        }
    }

    /**
     * Provides the URI to the internal file which stores all system info.
     *
     * @return {@link String} The {@link Initializer#dataURI} field.
     */
    public static String getDataURI() {
        return Initializer.dataURI;
    }

    /**
     * Provides the link to the resources package.
     *
     * @return {@link String} The {@link Initializer#resources} field.
     */
    public static String getResources() {
        return Initializer.resources;
    }

    /**
     * Initializes some properties for later splash screen customization.
     */
    private static void initializeSplashScreen() {
        SplashScreen splashScreen = SplashScreen.getSplashScreen();

        if (splashScreen != null) {
            Dimension size = splashScreen.getSize();
            int height = size.height;
            int width = size.width;
            Rectangle2D.Double splashTextArea = Initializer.initializeArea("text", height, width);

            Graphics2D splashGraphics = splashScreen.createGraphics();
            String fontName = XML.getAttribute("splashData", "name", "textFormat", "fontName", Initializer.dataURI);
            int fontStyle = Font.PLAIN, fontSize = Integer.parseInt(XML.getAttribute("splashData", "name", "textFormat", "fontSize", Initializer.dataURI));

            Field field;
            try {
                field = Font.class.getField(XML.getAttribute("splashData", "name", "textFormat", "fontStyle", Initializer.dataURI));
                fontStyle = (int) field.get(null);
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
                JOptionPane.showMessageDialog(null, Initializer.messages.get(8), "Error", JOptionPane.ERROR_MESSAGE);
                Log.log(ex);
            }

            Font splashTextFont = new Font(fontName, fontStyle, fontSize);
            splashGraphics.setFont(splashTextFont);

            Color chosenColor = Color.WHITE;
            try {
                field = Color.class.getField(XML.getAttribute("splashData", "name", "textFormat", "fontColor", Initializer.dataURI));
                chosenColor = (Color) field.get(null);
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
                JOptionPane.showMessageDialog(null, Initializer.messages.get(8), "Error", JOptionPane.ERROR_MESSAGE);
                Log.log(ex);
            }

            if (splashScreen.isVisible()) {
                splashGraphics.setPaint(chosenColor);
                splashGraphics.drawString(Initializer.messages.get(7), (int) (splashTextArea.getX() + Integer.parseInt(XML.getAttribute("splashData", "name", "textFormat", "xAddition", Initializer.dataURI))), (int) (splashTextArea.getY() - height + Integer.parseInt(XML.getAttribute("splashData", "name", "textFormat", "yAddition", Initializer.dataURI))));
                splashScreen.update();
            }
        }
    }

    /**
     * Draws and periodically updates a progress bar.
     */
    @SuppressWarnings("SleepWhileInLoop")
    private static void updateLoadProgress() {
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen == null) {
            return;
        }
        float fillPerDelay = Float.parseFloat(XML.getAttribute("splashData", "name", "progressBar", "fillPerDelay", Initializer.dataURI)), floatLoops = (100 / fillPerDelay), refreshDelay = Float.parseFloat(XML.getAttribute("splashData", "name", "progressBar", "refreshDelaySeconds", Initializer.dataURI));
        int loops = (int) floatLoops;

        if (loops < floatLoops) {
            loops++;
        }

        Dimension size = splashScreen.getSize();
        int height = size.height;
        int width = size.width;
        Rectangle2D.Double splashProgressBarArea = Initializer.initializeArea("progressBar", height, width);
        Graphics2D splashGraphics = splashScreen.createGraphics();
        while (true) {
            for (int i = 0; i < loops; i++) {
                if (splashScreen.isVisible()) {

                    int x = (int) splashProgressBarArea.getMinX();
                    int y = (int) splashProgressBarArea.getMinY();
                    int wid = (int) splashProgressBarArea.getWidth();
                    int hgt = (int) splashProgressBarArea.getHeight();

                    splashGraphics.setPaint(Color.WHITE);
                    splashGraphics.fill(splashProgressBarArea);

                    splashGraphics.setPaint(Color.BLUE);
                    splashGraphics.fillRect(x + 1 + (int) fillPerDelay * wid * i / 100, y + 1, (int) fillPerDelay * wid / 100, hgt - 1);

                    splashGraphics.setPaint(Color.BLACK);
                    splashGraphics.draw(splashProgressBarArea);

                    try {
                        Thread.sleep((int) refreshDelay * 1000);
                    } catch (InterruptedException ex) {
                        JOptionPane.showMessageDialog(null, Initializer.messages.get(9), "Error", JOptionPane.ERROR_MESSAGE);
                        Log.log(ex);
                    }
                    if (splashScreen.isVisible()) {
                        splashScreen.update();
                    }
                }
            }
        }
    }

    /**
     * Used to initialize the areas for the text and the progress bar in the
     * splash screen. The method should be called once and only once per area.
     *
     * @param areaName {@link String} The name of the area which data must be
     * retrieved. It must be equal to the name of one of the two areas.
     * @param height An integer representing the height of the splash screen.
     * @param width An integer representing the width of the splash screen.
     * @return {@link Rectangle2D.Double} The initialized area.
     * @throws IllegalArgumentException If the <code>areaName</code> parameter
     * is not valid.
     */
    private static Rectangle2D.Double initializeArea(String areaName, int height, int width) {
        String stringX, stringY, stringW, stringH;
        float x, y, w, h;

        switch (areaName.toLowerCase()) {
            case "text":
                stringX = XML.getAttribute("splashData", "name", "text", "x", Initializer.dataURI);
                stringY = XML.getAttribute("splashData", "name", "text", "y", Initializer.dataURI);
                stringW = XML.getAttribute("splashData", "name", "text", "w", Initializer.dataURI);
                stringH = XML.getAttribute("splashData", "name", "text", "h", Initializer.dataURI);
                break;
            case "progressbar":
                stringX = XML.getAttribute("splashData", "name", "progressBar", "x", Initializer.dataURI);
                stringY = XML.getAttribute("splashData", "name", "progressBar", "y", Initializer.dataURI);
                stringW = XML.getAttribute("splashData", "name", "progressBar", "w", Initializer.dataURI);
                stringH = XML.getAttribute("splashData", "name", "progressBar", "h", Initializer.dataURI);
                break;
            default:
                throw new IllegalArgumentException(Initializer.messages.get(0));
        }
        try {
            x = Float.parseFloat(stringX);
        } catch (NumberFormatException ex) {
            x = Float.parseFloat(stringX.replaceAll("[^\\d.]", ""));
            if (stringX.contains("h")) {
                x *= height;
            }
            if (stringX.contains("w")) {
                x *= width;
            }
        }
        try {
            y = Float.parseFloat(stringY);
        } catch (NumberFormatException ex) {
            y = Float.parseFloat(stringY.replaceAll("[^\\d.]", ""));
            if (stringY.contains("h")) {
                y *= height;
            }
            if (stringY.contains("w")) {
                y *= width;
            }
        }
        try {
            w = Float.parseFloat(stringW);
        } catch (NumberFormatException ex) {
            w = Float.parseFloat(stringW.replaceAll("[^\\d.]", ""));
            if (stringW.contains("h")) {
                w *= height;
            }
            if (stringW.contains("w")) {
                w *= width;
            }
        }
        try {
            h = Float.parseFloat(stringH);
        } catch (NumberFormatException ex) {
            h = Float.parseFloat(stringH.replaceAll("[^\\d.]", ""));
            if (stringH.contains("h")) {
                h *= height;
            }
            if (stringH.contains("w")) {
                h *= width;
            }
        }
        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * Retrieves a message from the messages library.
     *
     * @param index The id of the requested message.
     * @return {@link String} The message corresponding to the given id.
     */
    public static String getMessage(int index) {

        if (index < Initializer.messages.size()) {
            return Initializer.messages.get(index);
        }
        throw new IllegalArgumentException(Initializer.messages.get(2));
    }

    /**
     * Initializies the GUI.
     */
    private static void initInterface() {
        Initializer.mainWindow.setOpaque(false);
        Initializer.frame.add(Initializer.mainWindow);
        Initializer.frame.setResizable(false);
        Initializer.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Initializer.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Files.deleteIfExists(Paths.get(ElementPlayer.playerPath)); //Cannot be overwritten because of permissions issues
                    Files.write(Paths.get(ElementPlayer.playerPath), ElementPlayer.playerTemplate.getBytes(), StandardOpenOption.CREATE);
                } catch (IOException ex) {
                    Initializer.logAndForceExit(ex);
                }
                System.exit(0);
            }
        });
        Initializer.frame.setTitle(XML.getAttribute("windowData", "name", "main", "title", Initializer.dataURI));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Integer.parseInt(XML.getAttribute("windowData", "name", "main", "w", Initializer.dataURI)), height = Integer.parseInt(XML.getAttribute("windowData", "name", "main", "h", Initializer.dataURI)), x = (screenSize.width - width) / 2, y = (screenSize.height - height) / 2;
        Initializer.frame.setBounds(x, y, width, height);
    }

    /**
     * Hides the splash screen (if yet being shown) and shows the GUI.
     */
    private static void showGUI() {
        SplashScreen splashScreen;

        Initializer.frame.setIconImage(new ImageIcon(Initializer.class.getResource(
                Initializer.resources + XML.getAttribute("windowData", "name", "main", "icon", Initializer.dataURI))).getImage());

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (XML.getAttribute("windowData", "name", "main", "lookAndFeelName", Initializer.dataURI).equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(null, Initializer.messages.get(10), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }

        Initializer.frame.setVisible(true);

        if ((splashScreen = SplashScreen.getSplashScreen()) != null) {
            splashScreen.close();
        }
    }

    /**
     * Reports a critical error and forces the closing of the application.
     *
     * @param ex {@link Exception} The exception that caused the error.
     */
    public static void logAndForceExit(Exception ex) {
        Log.log(ex);
        JOptionPane.showMessageDialog(null, Initializer.getMessage(3), "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(-1);
    }
}