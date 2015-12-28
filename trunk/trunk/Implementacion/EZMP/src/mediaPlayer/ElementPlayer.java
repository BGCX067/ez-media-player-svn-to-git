package mediaPlayer;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import biblioteca.MediaManager;
import biblioteca.XML;
import interfaz.Initializer;
import interfaz.MainWindow;

/**
 * Manages the ways to play any kind of media elements.
 */
public abstract class ElementPlayer {

    /**
     * The path to the player
     */
    public final static String playerPath = "player.html", playerTemplate = ElementPlayer.readFile(ElementPlayer.playerPath);
    /**
     * The default content of the playlist file. VIDEONAME is to be replaced by
     * the video name and extension.
     */
    private final static String command = XML.getAttribute("data", "name", "browser", "path", Paths.get(
            XML.getAttribute("systemInfo", "name", "main", "bridgeFile", Initializer.getDataURI())));
    /**
     * The object in charge of managing the playing of the file.
     */
    private static MediaPlayer mediaPlayer = null;

    /**
     * Plays a media element.
     *
     * @param elementName {@link String} The name of the element to play.
     */
    public static synchronized void play(String elementName) {
        switch (MediaManager.getTypeOf(elementName)) {
            case MediaManager.AUDIO_TYPE:
                ElementPlayer.playAudio(elementName);
                break;
            case MediaManager.VIDEO_TYPE:
                ElementPlayer.playVideo(elementName);
                break;
            default:
                Initializer.logAndForceExit(new IllegalArgumentException(Initializer.getMessage(14)));
                break;
        }
    }

    /**
     * Plays an audio element.
     *
     * @param elementName {@link String} The name of the element to play.
     */
    public static synchronized void playAudio(String elementName) {
        if (MediaManager.getTypeOf(elementName) != MediaManager.AUDIO_TYPE) {
            throw new IllegalArgumentException(Initializer.getMessage(29));
        }

        if (ElementPlayer.mediaPlayer != null) {
            ElementPlayer.mediaPlayer.stop();
        }

        if (!MediaManager.exists(elementName)) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(32), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String mediaPlayerConstructorParam = XML.getAttribute("audio", "name", elementName, "path", Paths.get(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI())));
        URI uri = new File(mediaPlayerConstructorParam).toURI();

        if (!Files.exists(Paths.get(uri))) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(33), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ElementPlayer.mediaPlayer = new MediaPlayer(new Media(uri.toString()));
        ElementPlayer.mediaPlayer.setOnEndOfMedia(new Thread() {
            @Override
            public void run() {
                if (ElementPlayer.mediaPlayer.cycleCountProperty().intValue() == 1) {
                    MainWindow.disablePauseButton();
                    String imageName = XML.getAttribute("buttonData", "name", "pause", "icon3", Initializer.getDataURI());
                    MainWindow.singleton.buttonForPause.setIcon(new ImageIcon(MainWindow.class
                            .getResource(Initializer.getResources() + imageName)));
                }
            }
        });
        ElementPlayer.mediaPlayer.play();
    }

    /**
     * Pauses the current playing element.
     */
    private static synchronized void pauseAudio() {
        if (ElementPlayer.mediaPlayer != null) {
            ElementPlayer.mediaPlayer.pause();
        }
    }

    /**
     * Resumes the current playing element.
     */
    private static synchronized void resumeAudio() {
        if (ElementPlayer.mediaPlayer != null) {
            ElementPlayer.mediaPlayer.play();
        }
    }

    /**
     * Plays a video element.
     *
     * @param elementName {@link String} The name of the element to play.
     */
    private static synchronized void playVideo(String elementName) {
        if (MediaManager.getTypeOf(elementName) != MediaManager.VIDEO_TYPE) {
            throw new IllegalArgumentException(Initializer.getMessage(30));
        }

        try {
            Files.deleteIfExists(Paths.get(ElementPlayer.playerPath)); //Cannot be overwritten because of permissions issues
            Files.write(Paths.get(ElementPlayer.playerPath), ElementPlayer.playerTemplate.replaceAll("VIDEONAME", elementName).getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Initializer.logAndForceExit(ex);
        }

        try {
            Runtime.getRuntime().exec(ElementPlayer.command);
        } catch (IOException ex) {
            Initializer.logAndForceExit(ex);
        }
    }

    /**
     * Toggles the current playing status of the player.
     */
    public static synchronized void toggleAudioElementPlayingStatus() {
        if (ElementPlayer.mediaPlayer == null) {
            return;
        }

        if (ElementPlayer.mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            ElementPlayer.pauseAudio();
        } else {
            ElementPlayer.resumeAudio();
        }
    }

    /**
     * Toggles the current loop status of the player.
     */
    public static synchronized void toggleAudioElementLoopStatus() {
        if (ElementPlayer.mediaPlayer == null) {
            return;
        }

        if (ElementPlayer.mediaPlayer.cycleCountProperty().intValue() != 1) {
            ElementPlayer.mediaPlayer.setCycleCount(1);
        } else {
            ElementPlayer.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }

    /**
     * Provides the status of the loop mode.
     *
     * @return A boolean telling the status of the loop mode. If
     * <value>true</value>, it means loop mode is enabled. Otherwise, it isn't.
     */
    public static synchronized boolean isLoopEnabled() {
        return ElementPlayer.mediaPlayer.cycleCountProperty().intValue() != 1;
    }

    /**
     * Provides the status of the player.
     *
     * @return A boolean telling the player. If <value>true</value>, it means
     * player is playing audio. Otherwise, it isn't.
     */
    public static synchronized boolean isPlayingAudio() {
        try {
            return ElementPlayer.mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
        } catch (NullPointerException ex) {
            return false;
        }
    }

    /**
     * Iteratively reads a whole (inner) file into a string.
     *
     * @param pathToFile {@link String} The source file.
     *
     * @return {@link String} The contents of the file. If any error happens,
     * <value>null</value> will be returned.
     */
    public static String readFile(String pathToFile) {
        String line = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ElementPlayer.class.getResourceAsStream("/res/" + pathToFile)))) {
            while (true) {
                try {
                    line = line.concat(br.readLine());
                } catch (IOException ex) {
                    Initializer.logAndForceExit(ex);
                    return null;
                } catch (NullPointerException ex) {
                    break;
                }
                line = line.concat("\n");
            }
        } catch (IOException ex) {
            Initializer.logAndForceExit(ex);
            return null;
        }

        return line.substring(0, line.length() - 1);
    }
}
