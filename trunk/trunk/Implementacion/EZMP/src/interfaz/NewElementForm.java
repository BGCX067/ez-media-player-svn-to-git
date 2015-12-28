package interfaz;

import biblioteca.MediaManager;
import biblioteca.XML;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * The form that the user will use to provide the data of a brand new element to
 * add.
 */
public class NewElementForm extends JDialog implements ActionListener {

    /**
     * The introduced data.
     */
    private static ArrayList<String> data = null;
    /**
     * The GUI buttons.
     */
    private static final JButton buttonForAccept = new JButton(), buttonForCancel = new JButton();
    /**
     * The labels for the static data.
     */
    private static final JLabel labelForName = new JLabel(XML.getAttribute("labelData", "name", "name", "text1", Initializer.getDataURI())), labelForAuthor = new JLabel(XML.getAttribute("labelData", "name", "author", "text1", Initializer.getDataURI())), labelForYear = new JLabel(XML.getAttribute("labelData", "name", "year", "text1", Initializer.getDataURI())), labelForGenre = new JLabel(XML.getAttribute("labelData", "name", "genre", "text1", Initializer.getDataURI()));
    /**
     * The fields for getting the data.
     */
    private static final JTextField fieldForName = new JTextField(), fieldForAuthor = new JTextField(), fieldForYear = new JTextField(), fieldForGenre = new JTextField();
    /**
     * Singleton class object.
     */
    private static NewElementForm singleton;

    /**
     * Provides the singleton instance of this class.
     *
     * @return {@link NewElementForm} The single instance of this class.
     */
    public static NewElementForm getInstance() {
        try {
            NewElementForm.singleton.setVisible(true);
        } catch (NullPointerException ex) {
            NewElementForm.singleton = new NewElementForm();
            NewElementForm.singleton.setVisible(true);
        }

        return NewElementForm.singleton;
    }

    /**
     * Checks the information that the user enters to make sure its valid.
     * Provides graphical feedback about it if wrong.
     *
     * @return A boolean representing the succcess of the test. If
     * <value>true</value>, input is valid. Otherwise it isn't.
     */
    private static boolean checkAndFeedBackInput() {
        final String name = NewElementForm.fieldForName.getText(), author = NewElementForm.fieldForAuthor.getText(), year = NewElementForm.fieldForYear.getText(), genre = NewElementForm.fieldForGenre.getText();
        boolean valid = true;

        if (MediaManager.exists(name) || name.matches("UseMeAsDefault")) {
            valid = false;
            NewElementForm.fieldForName.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForName.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForName.setText(Initializer.getMessage(20));
        } else if (name.length() > MainWindow.getMaxFieldLength()) {
            valid = false;
            NewElementForm.fieldForName.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForName.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForName.setText(Initializer.getMessage(22));
        }

        try {
            if (Integer.parseInt(year) < 1) {
                NewElementForm.fieldForYear.setBackground(MainWindow.getWrongFieldBackgroundColor());
                NewElementForm.fieldForYear.setForeground(MainWindow.getWrongFieldForegroundColor());
                NewElementForm.fieldForYear.setText(Initializer.getMessage(21));
                valid = false;
            }
        } catch (NumberFormatException ex) {
            valid = false;
            NewElementForm.fieldForYear.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForYear.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForYear.setText(Initializer.getMessage(21));
        }

        if (name.matches("")) {
            valid = false;
            NewElementForm.fieldForName.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForName.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForName.setText(Initializer.getMessage(25));
        }

        if (author.matches("")) {
            valid = false;
            NewElementForm.fieldForAuthor.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForAuthor.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForAuthor.setText(Initializer.getMessage(25));
        }

        if (year.matches("")) {
            valid = false;
            NewElementForm.fieldForYear.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForYear.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForYear.setText(Initializer.getMessage(25));
        }

        if (genre.matches("")) {
            valid = false;
            NewElementForm.fieldForGenre.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForGenre.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForGenre.setText(Initializer.getMessage(25));
        }

        if (author.length() > MainWindow.getMaxFieldLength()) {
            valid = false;
            NewElementForm.fieldForAuthor.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForAuthor.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForAuthor.setText(Initializer.getMessage(22));
        }

        if (year.length() > MainWindow.getMaxFieldLength()) {
            valid = false;
            NewElementForm.fieldForYear.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForYear.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForYear.setText(Initializer.getMessage(22));
        }

        if (genre.length() > MainWindow.getMaxFieldLength()) {
            valid = false;
            NewElementForm.fieldForGenre.setBackground(MainWindow.getWrongFieldBackgroundColor());
            NewElementForm.fieldForGenre.setForeground(MainWindow.getWrongFieldForegroundColor());
            NewElementForm.fieldForGenre.setText(Initializer.getMessage(22));
        }

        if (!valid) {
            return false;
        }

        NewElementForm.data = new ArrayList<>();
        NewElementForm.data.add(name);
        NewElementForm.data.add(author);
        NewElementForm.data.add(year);
        NewElementForm.data.add(genre);

        return true;
    }

    /**
     * Singleton class constructor method.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    private NewElementForm() {
        this.setModal(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);

        this.setTitle(XML.getAttribute("newElementFormData", "name", "main", "title", Initializer.getDataURI()));

        this.setLayout(null);
        this.setIconImage(new ImageIcon(Initializer.class.getResource(
                Initializer.getResources() + XML.getAttribute("windowData", "name", "main", "icon", Initializer.getDataURI()))).getImage());

        int width = (int) Float.parseFloat(XML.getAttribute("newElementFormData", "name", "main", "w", Initializer.getDataURI())), height = (int) Float.parseFloat(XML.getAttribute("newElementFormData", "name", "main", "h", Initializer.getDataURI()));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        this.setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);

        Font newElementFont;
        String fontName = XML.getAttribute("newElementFormData", "name", "main", "fontName", Initializer.getDataURI());
        int fontStyle = 0, fontSize = Integer.parseInt(XML.getAttribute("addModeData", "name", "main", "fontSize", Initializer.getDataURI()));

        Field field;

        try {
            field = Font.class.getField(XML.getAttribute("newElementFormData", "name", "main", "fontStyle", Initializer.getDataURI()));
            fontStyle = (int) field.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            Initializer.logAndForceExit(ex);
        }

        newElementFont = new Font(fontName, fontStyle, fontSize);
        int separation = (int) Float.parseFloat(XML.getAttribute("newElementFormData", "name", "main", "separation", Initializer.getDataURI()));

        NewElementForm.labelForName.setBounds(separation, separation, Integer.parseInt(XML.getAttribute("labelData", "name", "name", "w", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", "name", "h", Initializer.getDataURI())));
        NewElementForm.labelForName.setFont(newElementFont);
        this.add(NewElementForm.labelForName);

        NewElementForm.labelForAuthor.setBounds(separation, 2 * separation + Integer.parseInt(XML.getAttribute("labelData", "name", "name", "h", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", "author", "w", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", "author", "h", Initializer.getDataURI())));
        NewElementForm.labelForAuthor.setFont(newElementFont);
        this.add(NewElementForm.labelForAuthor);

        NewElementForm.labelForYear.setBounds(separation, 3 * separation + Integer.parseInt(XML.getAttribute("labelData", "name", "name", "h", Initializer.getDataURI())) + Integer.parseInt(XML.getAttribute("labelData", "name", "author", "h", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", "year", "w", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", "year", "h", Initializer.getDataURI())));
        NewElementForm.labelForYear.setFont(newElementFont);
        this.add(NewElementForm.labelForYear);

        NewElementForm.labelForGenre.setBounds(separation, 4 * separation + Integer.parseInt(XML.getAttribute("labelData", "name", "name", "h", Initializer.getDataURI())) + Integer.parseInt(XML.getAttribute("labelData", "name", "author", "h", Initializer.getDataURI())) + Integer.parseInt(XML.getAttribute("labelData", "name", "year", "h", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", "genre", "w", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", "genre", "h", Initializer.getDataURI())));
        NewElementForm.labelForGenre.setFont(newElementFont);
        this.add(NewElementForm.labelForGenre);
        int fieldWidth = (int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "name", "w", Initializer.getDataURI()));

        NewElementForm.fieldForName.setBounds(separation * 2 + NewElementForm.labelForName.getSize().width, 2 * separation + NewElementForm.fieldForName.getY(), fieldWidth, (int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "name", "h", Initializer.getDataURI())));
        NewElementForm.fieldForName.setFont(newElementFont);
        this.add(NewElementForm.fieldForName);

        NewElementForm.fieldForAuthor.setBounds(separation * 2 + NewElementForm.labelForAuthor.getSize().width, separation + NewElementForm.labelForAuthor.getY(), fieldWidth, (int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "author", "h", Initializer.getDataURI())));
        NewElementForm.fieldForAuthor.setFont(newElementFont);
        this.add(NewElementForm.fieldForAuthor);

        NewElementForm.fieldForYear.setBounds(separation * 2 + NewElementForm.labelForYear.getSize().width, separation + NewElementForm.labelForYear.getY(), fieldWidth, (int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "year", "h", Initializer.getDataURI())));
        NewElementForm.fieldForYear.setFont(newElementFont);
        this.add(NewElementForm.fieldForYear);

        NewElementForm.fieldForGenre.setBounds(separation * 2 + NewElementForm.labelForGenre.getSize().width, separation + NewElementForm.labelForGenre.getY(), fieldWidth, (int) Float.parseFloat(XML.getAttribute("textFieldData", "name", "genre", "h", Initializer.getDataURI())));
        NewElementForm.fieldForGenre.setFont(newElementFont);
        this.add(NewElementForm.fieldForGenre);

        int x, y, w, h;
        String imageName;

        x = (int) Float.parseFloat(XML.getAttribute("buttonData", "name", "accept", "x", Initializer.getDataURI()));
        y = (int) Float.parseFloat(XML.getAttribute("buttonData", "name", "accept", "y", Initializer.getDataURI()));
        w = (int) Float.parseFloat(XML.getAttribute("buttonData", "name", "accept", "w", Initializer.getDataURI()));
        h = (int) Float.parseFloat(XML.getAttribute("buttonData", "name", "accept", "h", Initializer.getDataURI()));
        imageName = XML.getAttribute("buttonData", "name", "accept", "icon1", Initializer.getDataURI());

        NewElementForm.buttonForAccept.setBounds(x, y, w, h);
        NewElementForm.buttonForAccept.addActionListener(this);
        NewElementForm.buttonForAccept.setIcon(new ImageIcon(MainWindow.class
                .getResource(Initializer.getResources() + imageName)));
        this.add(NewElementForm.buttonForAccept);

        x = (int) Float.parseFloat(XML.getAttribute("buttonData", "name", "stop", "x", Initializer.getDataURI()));
        imageName = XML.getAttribute("buttonData", "name", "stop", "icon1", Initializer.getDataURI());

        NewElementForm.buttonForCancel.setBounds(x, y, w, h);
        NewElementForm.buttonForCancel.addActionListener(this);
        NewElementForm.buttonForCancel.setIcon(new ImageIcon(new ImageIcon(MainWindow.class
                .getResource(Initializer.getResources() + imageName)).getImage()));
        this.add(NewElementForm.buttonForCancel);
    }

    /**
     * Handles actions to perform when a button is pressed.
     *
     * @param e {@link ActionEvent} The thrown event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        boolean end;

        if (e.getSource() == NewElementForm.buttonForAccept) {
            end = NewElementForm.checkAndFeedBackInput();
        } else {
            end = true;
        }
        if (end) {
            NewElementForm.singleton.setVisible(false);
            NewElementForm.fieldForName.setText("");
            NewElementForm.fieldForAuthor.setText("");
            NewElementForm.fieldForYear.setText("");
            NewElementForm.fieldForGenre.setText("");
            NewElementForm.fieldForName.setBackground(MainWindow.getDefaultFieldBackgroundColor());
            NewElementForm.fieldForName.setForeground(MainWindow.getDefaultFieldForegroundColor());
            NewElementForm.fieldForAuthor.setBackground(MainWindow.getDefaultFieldBackgroundColor());
            NewElementForm.fieldForAuthor.setForeground(MainWindow.getDefaultFieldForegroundColor());
            NewElementForm.fieldForYear.setBackground(MainWindow.getDefaultFieldBackgroundColor());
            NewElementForm.fieldForYear.setForeground(MainWindow.getDefaultFieldForegroundColor());
            NewElementForm.fieldForGenre.setBackground(MainWindow.getDefaultFieldBackgroundColor());
            NewElementForm.fieldForGenre.setForeground(MainWindow.getDefaultFieldForegroundColor());
        }
    }

    /**
     * Provides the selected option.
     *
     * @return {@link ArrayList} of {@link String} representing the data to
     * associate to the element. It is 100% valid. If <value>null</value>, it
     * means the user cancelled the process.
     */
    public ArrayList<String> getInfo() {
        ArrayList<String> ret = NewElementForm.data;
        NewElementForm.data = null;
        return ret;
    }
}
