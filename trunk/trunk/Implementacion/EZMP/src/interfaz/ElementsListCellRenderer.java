package interfaz;

import biblioteca.MediaManager;
import biblioteca.XML;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.lang.reflect.Field;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * Custom implementation of the {@link ListCellRenderer} interface for the media
 * elements list.
 */
public class ElementsListCellRenderer extends JLabel implements ListCellRenderer {

    /**
     * The cell height to use.
     */
    private static final int cellHeight = Integer.parseInt(XML.getAttribute("listData", "name", "elements", "cellHeight", Initializer.getDataURI()));
    /**
     * The style of the font to use.
     */
    private static int fontStyle;
    /**
     * The colors to use.
     */
    private static Color audioItemColor, videoItemColor, selectedItemColor, selectedForeground, unselectedForeground;
    /**
     * The name of the font to use.
     */
    private static String fontName;
    /**
     * Singleton class object.
     */
    private static ElementsListCellRenderer singleton;

    /**
     * Provides the singleton instance of this class.
     * @return {@link ElementsListCellRenderer} The single instance of this class.
     */
    public static ElementsListCellRenderer getInstance() {
        if (ElementsListCellRenderer.singleton == null) {
            ElementsListCellRenderer.singleton = new ElementsListCellRenderer();
        }

        return ElementsListCellRenderer.singleton;
    }

    /**
     * Singleton class constructor method.
     */
    private ElementsListCellRenderer() {
        Field field;
        int cellAlignment = SwingConstants.CENTER;

        try {
            field = SwingConstants.class.getField(XML.getAttribute("listData", "name", "elements", "cellAlignment", Initializer.getDataURI()));
            cellAlignment = (int) field.get(null);
            field = Color.class.getField(XML.getAttribute("listData", "name", "elements", "audioItemColor", Initializer.getDataURI()));
            ElementsListCellRenderer.audioItemColor = (Color) field.get(null);
            field = Color.class.getField(XML.getAttribute("listData", "name", "elements", "videoItemColor", Initializer.getDataURI()));
            ElementsListCellRenderer.videoItemColor = (Color) field.get(null);
            field = Color.class.getField(XML.getAttribute("listData", "name", "elements", "selectedItemColor", Initializer.getDataURI()));
            ElementsListCellRenderer.selectedItemColor = (Color) field.get(null);
            field = Color.class.getField(XML.getAttribute("listData", "name", "elements", "selectedForeground", Initializer.getDataURI()));
            ElementsListCellRenderer.selectedForeground = (Color) field.get(null);
            field = Color.class.getField(XML.getAttribute("listData", "name", "elements", "unselectedForeground", Initializer.getDataURI()));
            ElementsListCellRenderer.unselectedForeground = (Color) field.get(null);
            field = Font.class.getField(XML.getAttribute("listData", "name", "elements", "fontStyle", Initializer.getDataURI()));
            ElementsListCellRenderer.fontStyle = (int) field.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            Initializer.logAndForceExit(ex);
        }

        ElementsListCellRenderer.fontName = XML.getAttribute("listData", "name", "elements", "fontName", Initializer.getDataURI());

        this.setHorizontalAlignment(cellAlignment);
        Font listFont = new Font(ElementsListCellRenderer.fontName, ElementsListCellRenderer.fontStyle, ElementsListCellRenderer.cellHeight);
        this.setFont(listFont);

        this.setSize(this.getSize().width, ElementsListCellRenderer.cellHeight);
    }

    /**
     * Gets the {@link Component} component of this
     * {@link CustomListCellRenderer} object.
     *
     * @param list {@link JList} The list.
     * @param value {@link Object} The value of this object in the previously
     * given list.
     * @param index An int representing the index of this object in the
     * previously given list.
     * @param isSelected A boolean telling whether this object in the previously
     * given list is selected or not.
     * @param cellHasFocus A boolean telling whether this object in the
     * previously given list has focus or not.
     * @return {@link Component} The component of this
     * {@link CustomListCellRenderer} object.
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        this.setText(value.toString());
        this.setOpaque(true);
        this.setEnabled(list.isEnabled());

        if (isSelected) {
            this.setBackground(ElementsListCellRenderer.selectedItemColor);
            this.setForeground(ElementsListCellRenderer.selectedForeground);
        } else {
            this.setForeground(ElementsListCellRenderer.unselectedForeground);
            switch (MediaManager.getTypeOf(value.toString())) {
                case MediaManager.AUDIO_TYPE:
                    this.setBackground(ElementsListCellRenderer.audioItemColor);
                    break;
                case MediaManager.VIDEO_TYPE:
                    this.setBackground(ElementsListCellRenderer.videoItemColor);
                    break;
                default:
                    throw new IllegalArgumentException(Initializer.getMessage(13));
            }
        }

        return this;
    }
}
