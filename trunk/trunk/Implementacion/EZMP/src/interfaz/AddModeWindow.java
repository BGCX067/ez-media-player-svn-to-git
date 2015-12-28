package interfaz;

import biblioteca.XML;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * This window will be used to decide how to add an element.
 */
public class AddModeWindow extends JDialog implements ActionListener {

    /**
     * The constants to represent the possible selected values.
     */
    public static final int AS_COPY = 1, AS_LINK = 2, CANCEL = 3;
    /**
     * The chosen option.
     */
    private static int choice = 0;
    /**
     * The buttons for the different options.
     */
    private static final JButton buttonForAsCopy = new JButton(), buttonForAsLink = new JButton(), buttonForCancel = new JButton();
    /**
     * The labels for the feedback icon and for the text;
     */
    private static final JLabel labelForIcon = new JLabel(new ImageIcon(new ImageIcon(Initializer.class.getResource(Initializer.getResources() + XML.getAttribute("addModeData", "name", "main", "icon", Initializer.getDataURI()))).getImage().getScaledInstance(84, 84, Image.SCALE_SMOOTH))), labelForText = new JLabel(XML.getAttribute("addModeData", "name", "main", "content", Initializer.getDataURI()));
    /**
     * Singleton class object.
     */
    private static AddModeWindow singleton;

    /**
     * Provides the singleton instance of this class.
     *
     * @return {@link AddModeWindow} The single instance of this class.
     */
    public static AddModeWindow getInstance() {
        try {
            AddModeWindow.singleton.setVisible(true);
        } catch (NullPointerException ex) {
            AddModeWindow.singleton = new AddModeWindow();
            AddModeWindow.singleton.setVisible(true);
        }

        return AddModeWindow.singleton;
    }

    /**
     * Singleton class constructor method.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    private AddModeWindow() {
        this.setModal(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setTitle(XML.getAttribute("addModeData", "name", "main", "title", Initializer.getDataURI()));
        this.setResizable(false);

        this.setLayout(null);
        this.setIconImage(new ImageIcon(Initializer.class.getResource(
                Initializer.getResources() + XML.getAttribute("windowData", "name", "main", "icon", Initializer.getDataURI()))).getImage());

        int width = (int) Float.parseFloat(XML.getAttribute("addModeData", "name", "main", "w", Initializer.getDataURI())), height = (int) Float.parseFloat(XML.getAttribute("addModeData", "name", "main", "h", Initializer.getDataURI()));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Icon labelForIconIcon = AddModeWindow.labelForIcon.getIcon();

        this.setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);

        int buttonW = Integer.parseInt(XML.getAttribute("addModeData", "name", "main", "buttonW", Initializer.getDataURI())), buttonH = Integer.parseInt(XML.getAttribute("addModeData", "name", "main", "buttonH", Initializer.getDataURI())), separation = (int) (width - buttonW * 3 - labelForIconIcon.getIconWidth()) / 4;

        if (4 * separation + 3 * buttonW > width || separation < Integer.parseInt(XML.getAttribute("addModeData", "name", "main", "minimumButtonSeparation", Initializer.getDataURI()))) {
            throw new IllegalArgumentException(Initializer.getMessage(24));
        }

        Font addModeFont;
        String fontName = XML.getAttribute("addModeData", "name", "main", "fontName", Initializer.getDataURI());
        int fontStyle = 0, fontSize = Integer.parseInt(XML.getAttribute("addModeData", "name", "main", "fontSize", Initializer.getDataURI()));

        Field field;

        try {
            field = Font.class.getField(XML.getAttribute("addModeData", "name", "main", "fontStyle", Initializer.getDataURI()));
            fontStyle = (int) field.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            Initializer.logAndForceExit(ex);
        }

        addModeFont = new Font(fontName, fontStyle, fontSize);

        AddModeWindow.buttonForAsCopy.setBounds(width - 3 * (separation + buttonW) - separation, height - 2 * buttonH, buttonW, buttonH);
        AddModeWindow.buttonForAsCopy.addActionListener(this);
        AddModeWindow.buttonForAsCopy.setText(XML.getAttribute("buttonData", "name", "copy", "text", Initializer.getDataURI()));
        AddModeWindow.buttonForAsCopy.setFont(addModeFont);
        this.add(AddModeWindow.buttonForAsCopy);

        AddModeWindow.buttonForAsLink.setBounds(width - 2 * (separation + buttonW) - separation, height - 2 * buttonH, buttonW, buttonH);
        AddModeWindow.buttonForAsLink.addActionListener(this);
        AddModeWindow.buttonForAsLink.setText(XML.getAttribute("buttonData", "name", "link", "text", Initializer.getDataURI()));
        AddModeWindow.buttonForAsLink.setFont(addModeFont);
        this.add(AddModeWindow.buttonForAsLink);

        AddModeWindow.buttonForCancel.setBounds(width - separation - buttonW - separation, height - 2 * buttonH, buttonW, buttonH);
        AddModeWindow.buttonForCancel.addActionListener(this);
        AddModeWindow.buttonForCancel.setText(XML.getAttribute("buttonData", "name", "cancel", "text", Initializer.getDataURI()));
        AddModeWindow.buttonForCancel.setFont(addModeFont);
        this.add(AddModeWindow.buttonForCancel);

        int iconSeparationFromCorner = Integer.parseInt(XML.getAttribute("addModeData", "name", "main", "iconSeparationFromCorner", Initializer.getDataURI()));

        AddModeWindow.labelForIcon.setBounds(iconSeparationFromCorner, iconSeparationFromCorner, labelForIconIcon.getIconWidth(), labelForIconIcon.getIconHeight());
        this.add(AddModeWindow.labelForIcon);

        AddModeWindow.labelForText.setBounds(iconSeparationFromCorner + labelForIconIcon.getIconWidth(), iconSeparationFromCorner, width - 3 * iconSeparationFromCorner - (iconSeparationFromCorner * 2 + labelForIconIcon.getIconWidth()), 85);
        AddModeWindow.labelForText.setHorizontalAlignment(JLabel.CENTER);
        AddModeWindow.labelForText.setFont(addModeFont);
        this.add(AddModeWindow.labelForText);
    }

    /**
     * Handles actions to perform when a button is pressed.
     *
     * @param e {@link ActionEvent} The thrown event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == AddModeWindow.buttonForAsCopy) {
            AddModeWindow.choice = AddModeWindow.AS_COPY;
        } else if (e.getSource() == AddModeWindow.buttonForAsLink) {
            AddModeWindow.choice = AddModeWindow.AS_LINK;
        } else {
            AddModeWindow.choice = AddModeWindow.CANCEL;
        }
        AddModeWindow.singleton.setVisible(false);
    }

    /**
     * Provides the selected option.
     *
     * @return An integer representing the chosen option.
     */
    public int getSelected() {
        return AddModeWindow.choice;
    }
}
