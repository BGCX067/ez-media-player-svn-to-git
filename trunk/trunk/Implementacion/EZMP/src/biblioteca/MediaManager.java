package biblioteca;

import interfaz.Initializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * Manages all operations which require any kind of access to the library.
 */
public abstract class MediaManager {

    /**
     * Identifiers for the possible type of media elements.
     */
    public static final int AUDIO_TYPE = 1, VIDEO_TYPE = 2;

    /**
     * Provides the names of all elements in the library.
     *
     * @return {@link ArrayList} of {@link String} The names of all elements
     * found in the library, sorted by media type.
     */
    public static ArrayList<String> getAllMediaElements() {
        return XML.getAllMediaElements();
    }

    /**
     * Provides the type of a given element.
     *
     * @param mediaName {@link String} The name of the element.
     * @return An integer, either {@link MediaManager#AUDIO_TYPE} for audio
     * elements or {@link MediaManager#VIDEO_TYPE} for video elements,
     * representing the type of the element.
     */
    public static int getTypeOf(String mediaName) {
        if (XML.getAttribute("audio", "name", mediaName, "name", Paths.get(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI()))) != null) {
            return MediaManager.AUDIO_TYPE;
        } else if (XML.getAttribute("video", "name", mediaName, "name", Paths.get(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI()))) != null) {
            return MediaManager.VIDEO_TYPE;
        }
        throw new IllegalArgumentException(Initializer.getMessage(14));
    }

    /**
     * Provides a certaing attribute of a given media element.
     *
     * @param mediaName {@link String} The name of the media element.
     * @param attributeName {@link String} The name of the requested attribute.
     * @return {@link String} The value of the required attribute.
     */
    private static String getAttributeOf(String mediaName, String attributeName) {
        String mediaTypeAsString;

        switch (MediaManager.getTypeOf(mediaName)) {
            case MediaManager.AUDIO_TYPE:
                mediaTypeAsString = "audio";
                break;
            case MediaManager.VIDEO_TYPE:
                mediaTypeAsString = "video";
                break;
            default:
                throw new IllegalArgumentException(Initializer.getMessage(15));
        }

        return XML.getAttribute(mediaTypeAsString, "name", mediaName, attributeName, Paths.get(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI())));
    }

    /**
     * Provides the author of a given media element.
     *
     * @param mediaName {@link String} The name of the media element.
     * @return {@link String} The requested author.
     */
    public static String getAuthorOf(String mediaName) {
        return MediaManager.getAttributeOf(mediaName, "author");
    }

    /**
     * Provides the year of a given media element.
     *
     * @param mediaName {@link String} The name of the media element.
     * @return An integer representing the requested year.
     */
    public static int getYearOf(String mediaName) {
        return Integer.parseInt(MediaManager.getAttributeOf(mediaName, "year"));
    }

    /**
     * Provides the genre of a given media element.
     *
     * @param mediaName {@link String} The name of the media element.
     * @return {@link String} The requested genre.
     */
    public static String getGenreOf(String mediaName) {
        return MediaManager.getAttributeOf(mediaName, "genre");
    }

    /**
     * Provides the path to the file of a given media element.
     *
     * @param mediaName {@link String} The name of the media element.
     * @return {@link Path} The requested element.
     */
    private static Path getPath(String mediaName) {
        return Paths.get(MediaManager.getAttributeOf(mediaName, "path"));
    }

    /**
     * Deletes a media element.
     *
     * @param mediaName {@link String} The name of the media element.
     * @param fromDisk A boolean used to indicate if the media element file
     * should be deleted from disk or just from the index. If
     * <value>true</value>, it'll be deleted from both. Otherwise it'll be
     * deleted just from the index.
     */
    public static void deleteElement(String mediaName, boolean fromDisk) {
        try {
            if (fromDisk) {
                Files.deleteIfExists(MediaManager.getPath(mediaName));
            }
            XML.removeElementFromIndex(mediaName);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(19), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            //Do nothing.
        }
    }

    /**
     * Checks wether a given name already belongs to an element in the library.
     *
     * @param issueElementName {@link String} The name to check.
     * @return A boolean reporting about the existance of the issue name.
     */
    public static boolean exists(String issueElementName) {
        if ((XML.getAttribute("audio", "name", issueElementName, "name", Paths.get(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI()))) != null) || (XML.getAttribute("video", "name", issueElementName, "name", Paths.get(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI()))) != null)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates the information of a certain media element in the library.
     *
     * @param currentName {@link String} The current name of the element.
     * @param newName {@link String} The new name of the element.
     * @param newAuthor {@link String} The new author of the element.
     * @param newYearString {@link String} The new year of the element.
     * @param newGenre {@link String} The new genre of the element.
     */
    public static void updateElementInfo(String currentName, String newName, String newAuthor, String newYearString, String newGenre) {
        XML.updateElementInfo(currentName, newName, newAuthor, newYearString, newGenre);
    }
}
