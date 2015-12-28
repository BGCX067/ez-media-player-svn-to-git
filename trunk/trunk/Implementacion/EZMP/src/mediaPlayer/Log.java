package mediaPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import biblioteca.XML;
import interfaz.Initializer;

/**
 * Abstract class Log, made for managing the logs of unusual behavior into a
 * files separated by date.
 */
public abstract class Log {

    /**
     * Writes message with timestamp into the log file, named by date
     *
     * @param ex {@link  Exception} The exception to log
     * @throws IOException If the writing into log file fails, the error message
     * pops up
     */
    public static void log(Exception ex) {
        String folderString = XML.getAttribute("systemInfo", "name", "main", "logPath", Initializer.getDataURI());
        Path folderPath = Paths.get(folderString);
        Path path = Paths.get(folderString, getDate() + ".log");
        String content = "[" + Log.getDateTime().trim() + "] - " + ex.toString() + "\n";
        byte[] contentBytes = content.getBytes();

        try {
            if (!Files.exists(folderPath)) {
                Files.createDirectory(folderPath);
            }

            if (!Files.exists(path)) {
                path = Files.createFile(path);
            }
            Files.write(path, contentBytes, StandardOpenOption.APPEND);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Provides the current date
     *
     * @return {@link  String} The date of today. For example, for October the
     * first, 2012, 2012-10-01
     */
    private static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Provides the current date and time
     *
     * @return {@link  String} The current date and hour. For example, for
     * October the first, 2012, 12:50:59, 2012-10-01 12:50:59
     */
    private static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss\t");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
