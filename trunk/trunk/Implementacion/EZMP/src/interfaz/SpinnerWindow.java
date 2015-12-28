package interfaz;

import biblioteca.XML;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * A modal dialog for showing a loading spinner.
 */
public class SpinnerWindow extends JDialog implements ActionListener {

    /**
     * Singleton class object.
     */
    private static SpinnerWindow singleton;
    /**
     * The label for the loading spinner.
     */
    private static final JLabel labelForSpinner = new JLabel();
    private static final JButton buttonForContinue = new JButton();

    /**
     * Provides the singleton instance of this class.
     *
     * @return {@link NewElementForm} The single instance of this class.
     */
    @SuppressWarnings("empty-statement")
    public static SpinnerWindow getInstance() {
        try {
            SpinnerWindow.singleton.setVisible(true);
        } catch (NullPointerException ex) {
            SpinnerWindow.singleton = new SpinnerWindow();
            SpinnerWindow.showSpinner();
            while (true);
        }

        return SpinnerWindow.singleton;
    }

    /**
     * Hides the loading spinner and shows the button for the user to accept.
     */
    public static void showButton() {
        try {
            SpinnerWindow.labelForSpinner.setVisible(false);
            SpinnerWindow.buttonForContinue.setVisible(true);
        } catch (NullPointerException ex) {
            SpinnerWindow.showButton(); //Maybe the calling thread calls before than the constructor may finish with the singleton construction.
        }
    }

    /**
     * Hides the button for the user to accept and shows the loading spinner.
     */
    private static void showSpinner() {
        try {
            SpinnerWindow.labelForSpinner.setVisible(true);
            SpinnerWindow.buttonForContinue.setVisible(false);
        } catch (NullPointerException ex) {
            SpinnerWindow.showSpinner(); //Maybe the calling thread calls before than the constructor may finish with the singleton construction.
        }
    }

    /**
     * Singleton class constructor method.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    private SpinnerWindow() {
        this.setLayout(null);

        int w = (int) Float.parseFloat(XML.getAttribute("spinnerData", "name", "main", "w", Initializer.getDataURI())), h = (int) Float.parseFloat(XML.getAttribute("spinnerData", "name", "main", "h", Initializer.getDataURI())), screenW = Toolkit.getDefaultToolkit().getScreenSize().width, screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
        ImageIcon iconForSpinner = new ImageIcon(Initializer.class.getResource(
                Initializer.getResources() + XML.getAttribute("spinnerData", "name", "main", "spinnerIcon", Initializer.getDataURI()))), iconForButton = new ImageIcon(Initializer.class.getResource(
                Initializer.getResources() + XML.getAttribute("spinnerData", "name", "main", "finishIcon", Initializer.getDataURI())));

        this.setTitle(XML.getAttribute("spinnerData", "name", "main", "title", Initializer.getDataURI()));
        this.setUndecorated(true);
        this.setBounds((screenW - w) / 2, (screenH - h) / 2, iconForSpinner.getIconWidth(), iconForSpinner.getIconHeight());
        this.setModal(true);
        this.setResizable(false);

        SpinnerWindow.labelForSpinner.setBounds(0, 0, w, h);
        SpinnerWindow.labelForSpinner.setIcon(new ImageIcon(iconForSpinner.getImage().getScaledInstance(90, 90, Image.SCALE_FAST)));
        this.add(SpinnerWindow.labelForSpinner);

        SpinnerWindow.buttonForContinue.setBounds(0, 0, w, h);
        SpinnerWindow.buttonForContinue.setVisible(false);
        SpinnerWindow.buttonForContinue.setIcon(iconForButton);
        SpinnerWindow.buttonForContinue.addActionListener(this);
        this.add(SpinnerWindow.buttonForContinue);

        this.setIconImage(new ImageIcon(Initializer.class.getResource(
                Initializer.getResources() + XML.getAttribute("windowData", "name", "main", "icon", Initializer.getDataURI()))).getImage());
        //this.getRootPane().setOpaque(false);
    }

    /**
     * Handles actions to perform when a button is pressed.
     *
     * @param e {@link ActionEvent} The thrown event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        SpinnerWindow.singleton.setVisible(false);
    }

    /**
     * Hides the window without showing a feedback button.
     */
    public static void hideWindow() {
        SpinnerWindow.singleton.setVisible(false);
    }
}
