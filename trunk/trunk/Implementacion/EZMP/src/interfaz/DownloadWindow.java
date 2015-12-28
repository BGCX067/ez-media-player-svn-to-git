package interfaz;

import biblioteca.MediaManager;
import biblioteca.XML;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import mediaPlayer.Client;
import mediaPlayer.Log;

/**
 * Handles the interactions required to the user to download.
 */
public class DownloadWindow extends JDialog implements ActionListener {

    /**
     * Field for user input.
     */
    private static final JTextField fieldForElement = new JTextField("");
    /**
     * Button to start the element fetching/download.
     */
    private static final JButton buttonForDownload = new JButton();
    /**
     * Singleton class object.
     */
    private static DownloadWindow singleton;
    /*
     * Object used to interact with the server.
     */
    private static Client client;
    /**
     * The port to connect to the server.
     */
    private static final int PORT = 5999;
    /**
     * The address of the server.
     */
    private static final String SERVER_ADDR = "84.121.226.115";

    /**
     * Provides the singleton instance of this class.
     *
     * @return {@link DownloadWindow} The single instance of this class.
     */
    public static DownloadWindow getInstance() {
        try {
            DownloadWindow.singleton.setVisible(true);
        } catch (NullPointerException ex) {
            DownloadWindow.singleton = new DownloadWindow();
            DownloadWindow.singleton.setVisible(true);
        }
        DownloadWindow.fieldForElement.setText("");
        return DownloadWindow.singleton;
    }

    /**
     * Constructor method.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    private DownloadWindow() {
        this.setModal(true);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setLayout(null);
        this.setResizable(false);

        this.setTitle(XML.getAttribute("downloadData", "name", "main", "title", Initializer.getDataURI()));

        int width = (int) Float.parseFloat(XML.getAttribute("downloadData", "name", "main", "w", Initializer.getDataURI())), height = (int) Float.parseFloat(XML.getAttribute("downloadData", "name", "main", "h", Initializer.getDataURI()));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);

        DownloadWindow.fieldForElement.setBounds((int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "download", "x", Initializer.getDataURI())), (int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "download", "y", Initializer.getDataURI())), (int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "download", "w", Initializer.getDataURI())), (int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "download", "h", Initializer.getDataURI())));
        this.add(DownloadWindow.fieldForElement);

        DownloadWindow.buttonForDownload.setBounds((int) Float.parseFloat(XML.getAttribute("buttonData", "name", "downloadInDialog", "x", Initializer.getDataURI())), (int) Float.parseFloat(XML.getAttribute("buttonData", "name", "downloadInDialog", "y", Initializer.getDataURI())), (int) Float.parseFloat(XML.getAttribute("buttonData", "name", "downloadInDialog", "w", Initializer.getDataURI())), (int) Float.parseFloat(XML.getAttribute("buttonData", "name", "downloadInDialog", "h", Initializer.getDataURI())));
        DownloadWindow.buttonForDownload.addActionListener(this);
        String imageName = XML.getAttribute("buttonData", "name", "downloadInDialog", "icon1", Initializer.getDataURI());
        DownloadWindow.buttonForDownload.setIcon(new ImageIcon(new ImageIcon(MainWindow.class.getResource(Initializer.getResources() + imageName)).getImage().getScaledInstance(50, 50, Image.SCALE_FAST)));
        DownloadWindow.buttonForDownload.setContentAreaFilled(false);
        this.add(DownloadWindow.buttonForDownload);

        this.setIconImage(new ImageIcon(Initializer.class.getResource(
                Initializer.getResources() + XML.getAttribute("windowData", "name", "main", "icon", Initializer.getDataURI()))).getImage());
        DownloadWindow.client = new Client();
    }

    /**
     * Handles actions to perform when a button is pressed.
     *
     * @param e {@link ActionEvent} The thrown event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == DownloadWindow.buttonForDownload) {
            DownloadWindow.chat();
            DownloadWindow.singleton.dispose();
        }
    }

    /**
     * Handles interaction with the server.
     */
    private static void chat() {
        final String desiredElementName = DownloadWindow.fieldForElement.getText();

        if (MediaManager.exists(desiredElementName)) {
            JOptionPane.showMessageDialog(DownloadWindow.singleton, "Ese elemento ya existe", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DownloadWindow.client.connect(DownloadWindow.SERVER_ADDR, DownloadWindow.PORT);
            DownloadWindow.client.sendString("fetch" + desiredElementName);
            String reply = DownloadWindow.client.receiveString();
            switch (reply) {
                case "yes":
                    DownloadWindow.client.sendString("retrieveType" + desiredElementName);
                    String type = DownloadWindow.client.receiveString();
                    String extension = type.matches("audio") ? ".mp3" : ".mp4";
                    DownloadWindow.client.sendString("retrieveFile" + desiredElementName);
                    Path toFile = Paths.get("library", type, desiredElementName + extension);
                    DownloadWindow.client.receiveFile(toFile);
                    DownloadWindow.client.sendString("retrieveData" + desiredElementName);
                    StringTokenizer tokenizer = new StringTokenizer(DownloadWindow.client.receiveString());
                    String name = tokenizer.nextToken(),
                     author = tokenizer.nextToken(),
                     genre = tokenizer.nextToken(),
                     year = tokenizer.nextToken();
                    XML.addMediaElement(name, author, Integer.parseInt(year), genre, toFile);
                    JOptionPane.showMessageDialog(DownloadWindow.singleton, "Elemento agregado", "Error", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "no":
                    JOptionPane.showMessageDialog(DownloadWindow.singleton, "No se ha encontrado ningún elemento", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    Initializer.logAndForceExit(new Exception("Recibida una respuesta inválida: -" + reply + "-"));
            }
            DownloadWindow.client.disconnect();
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(Paths.get("library", "video", desiredElementName + ".mp4"));
                Files.deleteIfExists(Paths.get("library", "audio", desiredElementName + ".mp3"));
                MediaManager.deleteElement(desiredElementName, true);
            } catch (IOException ex1) {
                Initializer.logAndForceExit(ex1);
            }
            JOptionPane.showMessageDialog(null, "Ocurrió un error. Por favor compruebe su conexión a internet. Para más información consulte el archivo de log.", "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }
        DownloadWindow.client = new Client();
    }
}
