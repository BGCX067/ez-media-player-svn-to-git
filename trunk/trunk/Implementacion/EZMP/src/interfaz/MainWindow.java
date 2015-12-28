package interfaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import mediaPlayer.Log;
import biblioteca.MediaManager;
import biblioteca.XML;
import mediaPlayer.ElementPlayer;

/**
 * Main view of the graphical user interface. In default state, it represents
 * the idle state of the application.
 */
public class MainWindow extends JPanel implements ActionListener, ListSelectionListener {

    /**
     * For the anonymous file adding thread.
     */
    private static File selectedFile;
    /**
     * For the anonymous file adding thread.
     */
    private static String path;

    /**
     * Sets the pause button to the default icon and enabled status.
     */
    private static void resetPauseButton() {
        MainWindow.singleton.buttonForPause.setEnabled(true);
        String imageName = XML.getAttribute("buttonData", "name", "pause", "icon3", Initializer.getDataURI());
        MainWindow.singleton.buttonForPause.setIcon(new ImageIcon(MainWindow.class
                .getResource(Initializer.getResources() + imageName)));
        imageName = XML.getAttribute("buttonData", "name", "play", "icon2", Initializer.getDataURI());
        MainWindow.singleton.buttonForPlay.setIcon(new ImageIcon(MainWindow.class
                .getResource(Initializer.getResources() + imageName)));
    }
    /**
     * The file chooser for browsing for the file.
     */
    private final JFileChooser fileChooser = new JFileChooser(XML.getAttribute("systemInfo", "name", "main", "fileChooserDefaultPath", Initializer.getDataURI()));
    /**
     * The model for the list that will contain the media elements.
     */
    private static final DefaultListModel modelForListForElements = new DefaultListModel();
    /**
     * The GUI buttons.
     */
    public final JButton buttonForPlay = new JButton(), buttonForLoop = new JButton(), buttonForPause = new JButton(), buttonForAdd = new JButton(), buttonForUpdate = new JButton(), buttonForDelete = new JButton(), buttonForDownload = new JButton();
    /**
     * The GUI labels.
     */
    private final JLabel labelForHeader = new JLabel(), labelForLibraryHeader = new JLabel(), labelForName = new JLabel(), labelForAuthor = new JLabel(), labelForYear = new JLabel(), labelForGenre = new JLabel();
    /**
     * The list for the media elements.
     */
    private final JList listForElements = new JList(MainWindow.modelForListForElements);
    /**
     * The text fields where the element data is displayed.
     */
    private final JTextField fieldForName = new JTextField(), fieldForAuthor = new JTextField(), fieldForYear = new JTextField(), fieldForGenre = new JTextField();
    /**
     * The system font.
     */
    private static Font systemFont;
    /**
     * The colors for the text fields.
     */
    private static Color defaultFieldBackgroundColor, defaultFieldForegroundColor, wrongFieldBackgroundColor, wrongFieldForegroundColor;
    /**
     * The maximum length for any user-editable text field.
     */
    private static int maxFieldLength;
    /**
     * Singleton class object.
     */
    public static MainWindow singleton;

    /**
     * Provides the singleton instance of this class.
     *
     * @return {@link MainWindow} The single instance of this class.
     */
    public static MainWindow getInstance() {
        if (MainWindow.singleton == null) {
            MainWindow.singleton = new MainWindow();
        }

        return MainWindow.singleton;
    }

    /**
     * Singleton class constructor method.
     */
    private MainWindow() {
        this.setLayout(null);
        if (MainWindow.systemFont == null) {
            final String systemFontName = XML.getAttribute("systemInfo", "name", "main", "systemFontName", Initializer.getDataURI());
            final int systemFontSize = Integer.parseInt(XML.getAttribute("systemInfo", "name", "main", "systemFontSize", Initializer.getDataURI()));
            int systemFontStyle = Font.PLAIN;

            Field field;

            try {
                field = Font.class.getField(XML.getAttribute("systemInfo", "name", "main", "systemFontStyle", Initializer.getDataURI()));
                systemFontStyle = (int) field.get(null);
                field = Color.class.getField(XML.getAttribute("systemInfo", "name", "main", "defaultFieldBackgroundColor", Initializer.getDataURI()));
                MainWindow.defaultFieldBackgroundColor = (Color) field.get(null);
                field = Color.class.getField(XML.getAttribute("systemInfo", "name", "main", "defaultFieldForegroundColor", Initializer.getDataURI()));
                MainWindow.defaultFieldForegroundColor = (Color) field.get(null);
                field = Color.class.getField(XML.getAttribute("systemInfo", "name", "main", "wrongFieldBackgroundColor", Initializer.getDataURI()));
                MainWindow.wrongFieldBackgroundColor = (Color) field.get(null);
                field = Color.class.getField(XML.getAttribute("systemInfo", "name", "main", "wrongFieldForegroundColor", Initializer.getDataURI()));
                MainWindow.wrongFieldForegroundColor = (Color) field.get(null);
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
                Initializer.logAndForceExit(ex);
            }
            MainWindow.systemFont = new Font(systemFontName, systemFontStyle, systemFontSize);
            MainWindow.maxFieldLength = Integer.parseInt(XML.getAttribute("systemInfo", "name", "main", "maxFieldLength", Initializer.getDataURI()));
        }
        this.initializeAndAdd("pause", this.buttonForPause, false);
        this.initializeAndAdd("play", this.buttonForPlay, false);
        this.initializeAndAdd("add", this.buttonForAdd, true);
        this.initializeAndAdd("elements", this.listForElements, true);
        this.initializeAndAdd("loop", this.buttonForLoop, true);
        this.initializeAndAdd("download", this.buttonForDownload, true);
        this.initializeAndAdd("update", this.buttonForUpdate, true);
        this.buttonForUpdate.setVisible(false);
        this.initializeAndAdd("delete", this.buttonForDelete, true);
        this.buttonForDelete.setVisible(false);
        this.initializeAndAdd("header", this.labelForHeader, true);
        this.initializeAndAdd("libraryHeader", this.labelForLibraryHeader, true);
        this.initializeAndAdd("name", this.labelForName, false);
        this.initializeAndAdd("author", this.labelForAuthor, false);
        this.initializeAndAdd("year", this.labelForYear, false);
        this.initializeAndAdd("genre", this.labelForGenre, false);
        this.initializeAndAdd("name", this.fieldForName, false);
        this.initializeAndAdd("author", this.fieldForAuthor, false);
        this.initializeAndAdd("year", this.fieldForYear, false);
        this.initializeAndAdd("genre", this.fieldForGenre, false);

        this.fetchLibrary();
    }

    /**
     * Handles actions to perform when a button is pressed.
     *
     * @param e {@link ActionEvent} The thrown event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.buttonForLoop) {
            this.toggleButtonImage("loop");
            ElementPlayer.toggleAudioElementLoopStatus();
        } else if (e.getSource() == this.buttonForPause) {
            this.toggleButtonImage("pause");
            ElementPlayer.toggleAudioElementPlayingStatus();
        } else if (e.getSource() == this.buttonForDelete) {
            this.deleteElement();
        } else if (e.getSource() == this.buttonForUpdate) {
            this.updateElementInformation();
        } else if (e.getSource() == this.buttonForAdd) {
            this.addNewElement();
        } else if (e.getSource() == this.buttonForPlay) {
            ElementPlayer.play(this.listForElements.getSelectedValue().toString());
            if (MediaManager.getTypeOf(this.listForElements.getSelectedValue().toString()) == MediaManager.AUDIO_TYPE) {
                MainWindow.resetPauseButton();
            }
        } else if (e.getSource() == this.buttonForDownload) {
            DownloadWindow.getInstance();
        }
        this.refreshElementInfo();
    }

    /**
     * Handles actions to perform when the selected element in a list changes.
     *
     * @param e {@link ListSelectionEvent} The thrown event.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        this.updateElementButtonsAliveness();
        this.refreshElementInfo();
    }

    /**
     * Initializes a component and adds it to the panel.
     *
     * @param componentName {@link String} The name by which the component is
     * identified in the configuration file.
     * @param component {@link JComponent} The component.
     * @param enabledAtCreation A boolean represeting the initial enabled (or
     * visible for non-interactive items) status of the component.
     */
    private void initializeAndAdd(String componentName, JComponent component, boolean enabledAtCreation) {

        if (component instanceof JButton) {
            JButton asButton = (JButton) component;
            asButton.addActionListener(this);
            String imageName = XML.getAttribute("buttonData", "name", componentName, "icon1", Initializer.getDataURI());
            try {
                asButton.setIcon(new ImageIcon(MainWindow.class
                        .getResource(Initializer.getResources() + imageName)));
            } catch (NullPointerException ex) {
                //The button doesn't have any images.
            }
            asButton.setBounds(Integer.parseInt(XML.getAttribute("buttonData", "name", componentName, "x", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("buttonData", "name", componentName, "y", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("buttonData", "name", componentName, "w", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("buttonData", "name", componentName, "h", Initializer.getDataURI())));
            component.setEnabled(enabledAtCreation);
            asButton.setContentAreaFilled(false);
        } else if (component instanceof JList) {
            JList asList = (JList) component;
            JScrollPane scrollPane = new JScrollPane(asList);
            scrollPane.setBounds(Integer.parseInt(XML.getAttribute("listData", "name", componentName, "x", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("listData", "name", componentName, "y", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("listData", "name", componentName, "w", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("listData", "name", componentName, "h", Initializer.getDataURI())));
            asList.setCellRenderer(ElementsListCellRenderer.getInstance());
            asList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            asList.addListSelectionListener(this);
            this.add(scrollPane);
            return;
        } else if (component instanceof JLabel) {
            JLabel asLabel = (JLabel) component;
            component.setBounds(Integer.parseInt(XML.getAttribute("labelData", "name", componentName, "x", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", componentName, "y", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", componentName, "w", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("labelData", "name", componentName, "h", Initializer.getDataURI())));
            String imageName = XML.getAttribute("labelData", "name", componentName, "icon1", Initializer.getDataURI());
            if (!"".equals(imageName)) {
                asLabel.setHorizontalAlignment(JLabel.CENTER);
                asLabel.setVerticalAlignment(JLabel.CENTER);
                asLabel.setIcon(new ImageIcon(MainWindow.class
                        .getResource(Initializer.getResources() + imageName)));
            } else {
                asLabel.setText(XML.getAttribute("labelData", "name", componentName, "text1", Initializer.getDataURI()));
            }
            asLabel.setFont(MainWindow.systemFont);
            component.setVisible(enabledAtCreation);
        } else if (component instanceof JTextField) {
            JTextField asTextField = (JTextField) component;
            component.setBounds(Integer.parseInt(XML.getAttribute("textFieldData", "name", componentName, "x", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("textFieldData", "name", componentName, "y", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("textFieldData", "name", componentName, "w", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("textFieldData", "name", componentName, "h", Initializer.getDataURI())));
            asTextField.setFont(MainWindow.systemFont);
            asTextField.setBackground(MainWindow.defaultFieldBackgroundColor);
            asTextField.setForeground(MainWindow.defaultFieldForegroundColor);
            component.setVisible(enabledAtCreation);
            asTextField.setEditable(true);
        }

        this.add(component);
    }

    /**
     * Updates the image of a button. It should be only for buttons which image
     * may vary between two different ones and only in the
     * {@link MainWindow#actionPerformed(java.awt.event.ActionEvent)} method.
     *
     * @param buttonName {@link String} The name of the button which name must
     * be toggled.
     */
    private void toggleButtonImage(String buttonName) {
        switch (buttonName) {
            case "loop":
                try {
                    if (!ElementPlayer.isLoopEnabled()) {
                        this.buttonForLoop.setIcon(new ImageIcon(MainWindow.class
                                .getResource(Initializer.getResources() + XML.getAttribute("buttonData", "name", "loop", "icon2", Initializer.getDataURI()))));
                    } else {
                        this.buttonForLoop.setIcon(new ImageIcon(MainWindow.class
                                .getResource(Initializer.getResources() + XML.getAttribute("buttonData", "name", "loop", "icon1", Initializer.getDataURI()))));
                    }
                } catch (NullPointerException ex) {//If the user tries to change loop status when player has not yet been initialized, do nothing.
                }
                break;
            case "pause":
                if (ElementPlayer.isPlayingAudio()) {
                    this.buttonForPause.setIcon(new ImageIcon(MainWindow.class
                            .getResource(Initializer.getResources() + XML.getAttribute("buttonData", "name", "pause", "icon2", Initializer.getDataURI()))));
                } else {
                    this.buttonForPause.setIcon(new ImageIcon(MainWindow.class
                            .getResource(Initializer.getResources() + XML.getAttribute("buttonData", "name", "pause", "icon3", Initializer.getDataURI()))));
                }
                break;
            default:
                throw new IllegalArgumentException(Initializer.getMessage(11));
        }
    }

    /**
     * Loads the media data from the library file. Updates the list content with
     * the names of the stored media contents.
     */
    private void fetchLibrary() {
        for (Iterator<String> it = MediaManager.getAllMediaElements().iterator(); it.hasNext();) {
            this.addEntryToElementsList(it.next());
        }
    }

    /**
     * Adds a new entry to the elements list.
     *
     * @param newEntry {@link String} The name of the new entry.
     */
    private void addEntryToElementsList(String newEntry) {
        DefaultListModel defaultListModel = (DefaultListModel) this.listForElements.getModel();
        defaultListModel.addElement(newEntry);
    }

    /**
     * Overrides the background drawing method in order to allow the usage of a
     * gradient color as background.
     *
     * @param grphcs {@link Graphics} The tool to draw the background.
     */
    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2d = (Graphics2D) grphcs;
        Field field;
        Color backgroundColor1 = Color.WHITE, backgroundColor2 = Color.BLUE;



        try {
            field = Color.class
                    .getField(XML.getAttribute("systemInfo", "name", "main", "backgroundColor1", Initializer.getDataURI()));
            backgroundColor1 = (Color) field.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            Initializer.logAndForceExit(ex);


        }

        try {
            field = Color.class
                    .getField(XML.getAttribute("systemInfo", "name", "main", "backgroundColor2", Initializer.getDataURI()));
            backgroundColor2 = (Color) field.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            Initializer.logAndForceExit(ex);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(Integer.parseInt(XML.getAttribute("systemInfo", "name", "main", "gradientx1", Initializer.getDataURI())), Integer.parseInt(XML.getAttribute("systemInfo", "name", "main", "gradienty1", Initializer.getDataURI())), backgroundColor1, 0, getHeight(), backgroundColor2);

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        super.paintComponent(grphcs);
    }

    /**
     * Updates the enabled status of the button for media playing.
     */
    private void updateElementButtonsAliveness() {
        if (this.listForElements.getSelectedValue() != null) {
            this.buttonForPlay.setEnabled(true);
            this.buttonForUpdate.setVisible(true);
            this.buttonForDelete.setVisible(true);
        } else {
            this.buttonForPlay.setEnabled(false);
            this.buttonForUpdate.setVisible(false);
            this.buttonForDelete.setVisible(false);
        }
    }

    /**
     * Manages the showing of the information of the selected element, if any.
     */
    private void refreshElementInfo() {
        if (this.listForElements.getSelectedValue() != null) {
            this.fieldForName.setBackground(MainWindow.defaultFieldBackgroundColor);
            this.fieldForName.setForeground(MainWindow.defaultFieldForegroundColor);
            this.fieldForAuthor.setBackground(MainWindow.defaultFieldBackgroundColor);
            this.fieldForAuthor.setForeground(MainWindow.defaultFieldForegroundColor);
            this.fieldForYear.setBackground(MainWindow.defaultFieldBackgroundColor);
            this.fieldForYear.setForeground(MainWindow.defaultFieldForegroundColor);
            this.fieldForGenre.setBackground(MainWindow.defaultFieldBackgroundColor);
            this.fieldForGenre.setForeground(MainWindow.defaultFieldForegroundColor);
            this.labelForName.setVisible(true);
            this.fieldForName.setText(this.listForElements.getSelectedValue().toString());
            this.fieldForName.setVisible(true);
            this.labelForAuthor.setVisible(true);
            this.fieldForAuthor.setText(MediaManager.getAuthorOf(this.listForElements.getSelectedValue().toString()));
            this.fieldForAuthor.setVisible(true);
            this.labelForYear.setVisible(true);
            this.fieldForYear.setText("" + MediaManager.getYearOf(this.listForElements.getSelectedValue().toString()));
            this.fieldForYear.setVisible(true);
            this.labelForGenre.setVisible(true);
            this.fieldForGenre.setText(MediaManager.getGenreOf(this.listForElements.getSelectedValue().toString()));
            this.fieldForGenre.setVisible(true);
        } else {
            MainWindow.resetPauseButton();
            if (!ElementPlayer.isPlayingAudio()) {
                this.buttonForPause.setEnabled(false);
            }
            String imageName = XML.getAttribute("buttonData", "name", "pause", "icon3", Initializer.getDataURI());
            MainWindow.singleton.buttonForPause.setIcon(new ImageIcon(MainWindow.class
                    .getResource(Initializer.getResources() + imageName)));
            this.labelForName.setVisible(false);
            this.labelForAuthor.setVisible(false);
            this.labelForYear.setVisible(false);
            this.labelForGenre.setVisible(false);
            this.fieldForName.setVisible(false);
            this.fieldForAuthor.setVisible(false);
            this.fieldForYear.setVisible(false);
            this.fieldForGenre.setVisible(false);
        }
    }

    /**
     * Handles the deletion of an element from the library.
     */
    private void deleteElement() {
        String selectedMedia = this.listForElements.getSelectedValue().toString();
        int choice = JOptionPane.showConfirmDialog(null, Initializer.getMessage(17), XML.getAttribute("windowData", "name", "main", "title", Initializer.getDataURI()), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        switch (choice) {
            case JOptionPane.YES_OPTION:
            case JOptionPane.NO_OPTION:
                ((DefaultListModel) this.listForElements.getModel()).removeElement(selectedMedia);
                this.listForElements.clearSelection();
                MediaManager.deleteElement(selectedMedia, choice == JOptionPane.YES_OPTION);
                break;
            case JOptionPane.CANCEL_OPTION:
                return;
            default:
                JOptionPane.showMessageDialog(null, Initializer.getMessage(8), "Error", JOptionPane.ERROR_MESSAGE);
                Log.log(new IllegalArgumentException(Initializer.getMessage(8)));
                break;
        }
    }

    /**
     * Picks the current content of each field and validates it in order to
     * update the information of the element. If any fields are wrong, it is
     * indicated and no information is modified.
     */
    private void updateElementInformation() {
        final String currentName = this.listForElements.getSelectedValue().toString(), newName = this.fieldForName.getText(), newAuthor = this.fieldForAuthor.getText(), newYearString = this.fieldForYear.getText(), newGenre = this.fieldForGenre.getText();
        boolean valid = true;

        if ((currentName == null ? newName != null : !currentName.equals(newName)) && MediaManager.exists(newName) || newName.matches("UseMeAsDefault")) {
            valid = false;
            this.fieldForName.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForName.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForName.setText(Initializer.getMessage(20));
        } else if (newName.length() > MainWindow.maxFieldLength) {
            valid = false;
            this.fieldForName.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForName.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForName.setText(Initializer.getMessage(22));
        }

        try {
            if (Integer.parseInt(newYearString) < 1) {
                this.fieldForYear.setBackground(MainWindow.wrongFieldBackgroundColor);
                this.fieldForYear.setForeground(MainWindow.wrongFieldForegroundColor);
                this.fieldForYear.setText(Initializer.getMessage(21));
                valid = false;
            }
        } catch (NumberFormatException ex) {
            valid = false;
            this.fieldForYear.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForYear.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForYear.setText(Initializer.getMessage(21));
        }

        if (newName.matches("")) {
            valid = false;
            this.fieldForName.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForName.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForName.setText(Initializer.getMessage(25));
        }

        if (newAuthor.matches("")) {
            valid = false;
            this.fieldForAuthor.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForAuthor.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForAuthor.setText(Initializer.getMessage(25));
        }

        if (newYearString.matches("")) {
            valid = false;
            this.fieldForYear.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForYear.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForYear.setText(Initializer.getMessage(25));
        }

        if (newGenre.matches("")) {
            valid = false;
            this.fieldForGenre.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForGenre.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForGenre.setText(Initializer.getMessage(25));
        }

        if (newAuthor.length() > MainWindow.maxFieldLength) {
            valid = false;
            this.fieldForAuthor.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForAuthor.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForAuthor.setText(Initializer.getMessage(22));
        }

        if (newYearString.length() > MainWindow.maxFieldLength) {
            valid = false;
            this.fieldForYear.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForYear.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForYear.setText(Initializer.getMessage(22));
        }

        if (newGenre.length() > MainWindow.maxFieldLength) {
            valid = false;
            this.fieldForGenre.setBackground(MainWindow.wrongFieldBackgroundColor);
            this.fieldForGenre.setForeground(MainWindow.wrongFieldForegroundColor);
            this.fieldForGenre.setText(Initializer.getMessage(22));
        }

        if (!valid) {
            return;
        }

        int index = this.listForElements.getSelectedIndex();

        MediaManager.updateElementInfo(currentName, newName, newAuthor, newYearString, newGenre);

        this.listForElements.clearSelection();
        ((DefaultListModel) this.listForElements.getModel()).remove(index);
        ((DefaultListModel) this.listForElements.getModel()).add(index, newName);
        this.listForElements.setSelectedIndex(index);

        this.fieldForName.setBackground(MainWindow.defaultFieldBackgroundColor);
        this.fieldForAuthor.setBackground(MainWindow.defaultFieldBackgroundColor);
        this.fieldForYear.setBackground(MainWindow.defaultFieldBackgroundColor);
        this.fieldForGenre.setBackground(MainWindow.defaultFieldBackgroundColor);
        this.fieldForName.setForeground(MainWindow.defaultFieldForegroundColor);
        this.fieldForAuthor.setForeground(MainWindow.defaultFieldForegroundColor);
        this.fieldForYear.setForeground(MainWindow.defaultFieldForegroundColor);
        this.fieldForGenre.setForeground(MainWindow.defaultFieldForegroundColor);
    }

    /**
     * Starts the process of adding a new media element to the library.
     */
    private void addNewElement() {
        int addMode = AddModeWindow.getInstance().getSelected();
        if (addMode == AddModeWindow.CANCEL) {
            return;
        }
        MainWindow.selectedFile = null;
        String name = "", author = "", year = "", genre = "";
        MainWindow.path = null;

        try {
            for (Iterator<String> it = NewElementForm.getInstance().getInfo().iterator(); it.hasNext();) {
                if ("".equals(name)) {
                    name = it.next();
                } else if ("".equals(author)) {
                    author = it.next();
                } else if ("".equals(year)) {
                    year = it.next();
                } else {
                    genre = it.next();
                }
            }
        } catch (NullPointerException ex) {
            //If the user closes the form.
            return;
        }

        FileFilter videos, audios;

        videos = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().endsWith(XML.getAttribute("systemInfo", "name", "main", "videoFormat", Initializer.getDataURI())) || f.isDirectory()) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Vídeos";
            }
        };

        audios = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().endsWith(XML.getAttribute("systemInfo", "name", "main", "audioFormat", Initializer.getDataURI())) || f.isDirectory()) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Audios";
            }
        };

        this.fileChooser.resetChoosableFileFilters();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.addChoosableFileFilter(audios);

        switch (addMode) {
            case AddModeWindow.AS_LINK:
                if (this.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    selectedFile = this.fileChooser.getSelectedFile();
                } else {
                    return;
                }
                this.addEntryToElementsList(name);
                if (selectedFile != null) {
                    this.fileChooser.setCurrentDirectory(selectedFile);
                    MainWindow.path = selectedFile.getAbsolutePath();
                } else {
                    return;
                }
                break;
            case AddModeWindow.AS_COPY:
                this.fileChooser.addChoosableFileFilter(videos);
                if (this.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    selectedFile = this.fileChooser.getSelectedFile();
                    if (selectedFile.getName().endsWith(XML.getAttribute("systemInfo", "name", "main", "audioFormat", Initializer.getDataURI()))) {
                        MainWindow.path = XML.getAttribute("systemInfo", "name", "main", "audiosPath", Initializer.getDataURI()) + name + "." + XML.getAttribute("systemInfo", "name", "main", "audioFormat", Initializer.getDataURI());
                    } else {
                        MainWindow.path = XML.getAttribute("systemInfo", "name", "main", "videosPath", Initializer.getDataURI()) + name + "." + XML.getAttribute("systemInfo", "name", "main", "videoFormat", Initializer.getDataURI());
                    }
                } else {
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Files.copy(MainWindow.selectedFile.toPath(), Paths.get(MainWindow.path));
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, Initializer.getMessage(26), "Error", JOptionPane.ERROR_MESSAGE);
                            Initializer.logAndForceExit(ex);
                        }
                    }
                }.start();
                this.addEntryToElementsList(name);
                if (MainWindow.selectedFile != null) {
                    this.fileChooser.setCurrentDirectory(MainWindow.selectedFile);
                } else {
                    return;
                }
                break;
        }

        XML.addMediaElement(name, author, Integer.parseInt(year), genre, Paths.get(MainWindow.path));
    }

    /**
     * Getter for the background color for wrong fields.
     *
     * @return {@link Color} The background color for wrong fields.
     */
    public static Color getWrongFieldBackgroundColor() {
        return MainWindow.wrongFieldBackgroundColor;
    }

    /**
     * Getter for the background color for non-wrong fields.
     *
     * @return {@link Color} The background color for non-wrong fields.
     */
    public static Color getDefaultFieldBackgroundColor() {
        return MainWindow.defaultFieldBackgroundColor;
    }

    /**
     * Getter for the foreground color for wrong fields.
     *
     * @return {@link Color} The foreground color for wrong fields.
     */
    public static Color getWrongFieldForegroundColor() {
        return MainWindow.wrongFieldForegroundColor;
    }

    /**
     * Getter for the foreground color for non-wrong fields.
     *
     * @return {@link Color} The foreground color for non-wrong fields.
     */
    public static Color getDefaultFieldForegroundColor() {
        return MainWindow.defaultFieldForegroundColor;
    }

    /**
     * Getter for the maximum field length.
     *
     * @return An integer representing the maximum field length.
     */
    public static int getMaxFieldLength() {
        return MainWindow.maxFieldLength;
    }

    /**
     * Used to allow the player to disable the pause button when an element
     * finishes playing.
     */
    public static void disablePauseButton() {
        MainWindow.singleton.buttonForPause.setEnabled(false);
        String imageName = XML.getAttribute("buttonData", "name", "play", "icon1", Initializer.getDataURI());
        MainWindow.singleton.buttonForPlay.setIcon(new ImageIcon(MainWindow.class
                .getResource(Initializer.getResources() + imageName)));
    }
}
