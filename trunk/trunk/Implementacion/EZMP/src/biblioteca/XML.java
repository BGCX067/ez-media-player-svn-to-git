package biblioteca;

import interfaz.Initializer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mediaPlayer.Log;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implements generic XML parsing and data retrieving.
 */
public abstract class XML {

    /**
     * Dumps the information of a media element to the library. Performs name
     * existance check prior to the dump.
     *
     * @param mediaName {@link String} The name of the media element.
     * @param author {@link String} The author of the media element.
     * @param year An integer representing the year of the media element.
     * @param genre {@link String} The genre of the media element.
     * @param pathToFile {@link Path} The path to the corresponding file.
     * @return A boolean representing the success of the operation.
     */
    public static boolean addMediaElement(String mediaName, String author, int year, String genre, Path pathToFile) {
        if (MediaManager.exists(mediaName)) {
            return false;
        }

        /*
         * XML DATA CREATION - BEGINNING
         */
        DocumentBuilder docBuilder = null;
        Document doc = null;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }
        try {
            doc = docBuilder.parse(new File(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI())));
        } catch (SAXException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(4), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(5), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }

        final String pathAsString = pathToFile.toString(), audioExtension = XML.getAttribute("systemInfo", "name", "main", "audioFormat", Initializer.getDataURI()), videoExtension = XML.getAttribute("systemInfo", "name", "main", "videoFormat", Initializer.getDataURI()), elementExtension = pathAsString.substring(pathAsString.length() - audioExtension.length()).toLowerCase();
        String elementType;

        if (elementExtension.matches(audioExtension)) {
            elementType = "audio";
        } else if (elementExtension.matches(videoExtension)) {
            elementType = "video";
        } else {
            throw new IllegalArgumentException(Initializer.getMessage(23));
        }

        Node index = doc.getFirstChild(), newElement = doc.createElement(elementType);
        NamedNodeMap elementAttributes = newElement.getAttributes();
        Attr attrName = doc.createAttribute("name");
        attrName.setValue(mediaName);
        elementAttributes.setNamedItem(attrName);
        Attr attrAuthor = doc.createAttribute("author");
        attrAuthor.setValue(author);
        elementAttributes.setNamedItem(attrAuthor);
        Attr attrYear = doc.createAttribute("year");
        attrYear.setValue(year + "");
        elementAttributes.setNamedItem(attrYear);
        Attr attrGenre = doc.createAttribute("genre");
        attrGenre.setValue(genre);
        elementAttributes.setNamedItem(attrGenre);
        Attr attrPath = doc.createAttribute("path");
        attrPath.setValue(pathAsString);
        elementAttributes.setNamedItem(attrPath);
        index.appendChild(newElement);
        /*
         * XML DATA CREATION - END
         */

        /*
         * XML DATA DUMP - BEGINNING
         */
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }

        String xmlString = result.getWriter().toString();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI()))))) {
            bufferedWriter.write(xmlString);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }
        /*
         * XML DATA DUMP - END
         */

        return true;
    }

    /**
     * Parses a XML file and retrieves the requested attribute of a certain XML
     * node.
     *
     * @param type {@link String} The type identifier of the XML node.
     * @param identifierAttribute {@link String} The name of the attribute that
     * identifies the node.
     * @param identifierValue {@link String} The value that the identifier
     * attribute will have in the requested node so it can be found.
     * @param attributeName {@link String} The name of the attribute to
     * retrieve.
     * @param pathToFile {@link Path} The path to the XML file.
     * @return The value of the requested attribute of the requested node.
     */
    public static String getAttribute(String type, String identifierAttribute, String identifierValue, String attributeName, Path pathToFile) {
        DocumentBuilder docBuilder = null;
        Document doc = null;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }

        try {
            doc = docBuilder.parse(pathToFile.toFile());
        } catch (SAXException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(4), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(5), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }

        NodeList parent = doc.getElementsByTagName(type);

        for (int i = 0; i < parent.getLength(); i++) {
            if (((Element) parent.item(i)).getAttribute(identifierAttribute).matches(identifierValue)) {
                return ((Element) parent.item(i)).getAttribute(attributeName);
            }
        }

        return null;
    }

    /**
     * Provides the names of all elements in the library.
     *
     * @return {@link ArrayList} of {@link String} The names of all elements
     * found in the library, sorted by media type.
     */
    public static ArrayList<String> getAllMediaElements() {
        DocumentBuilder docBuilder = null;
        Document doc = null;
        final ArrayList<String> ret = new ArrayList<>();

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }

        try {
            doc = docBuilder.parse(new File(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI())));
        } catch (SAXException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(4), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(5), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }

        NodeList audios = doc.getElementsByTagName("audio"), videos = doc.getElementsByTagName("video");

        for (int i = 0; i < audios.getLength(); i++) {
            ret.add(((Element) audios.item(i)).getAttribute("name"));
        }

        for (int i = 0; i < videos.getLength(); i++) {
            ret.add(((Element) videos.item(i)).getAttribute("name"));
        }

        return ret;
    }

    /**
     * Parses an inner XML file and retrieves the requested attribute of a
     * certain XML node.
     *
     * @param type {@link String} The type identifier of the XML node.
     * @param identifierAttribute {@link String} The name of the attribute that
     * identifies the node.
     * @param identifierValue {@link String} The value that the identifier
     * attribute will have in the requested node so it can be found.
     * @param attributeName {@link String} The name of the attribute to
     * retrieve.
     * @param URI {@link String} The URI to the inner XML file.
     * @return The value of the requested attribute of the requested node.
     */
    public static String getAttribute(String type, String identifierAttribute, String identifierValue, String attributeName, String URI) {
        DocumentBuilder docBuilder = null;
        Document doc = null;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }

        try {
            doc = docBuilder.parse(XML.class.getResource(URI).toURI().toString());
        } catch (SAXException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(4), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(5), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        } catch (URISyntaxException ex) {
            JOptionPane.showMessageDialog(null, Initializer.getMessage(6), "Error", JOptionPane.ERROR_MESSAGE);
            Log.log(ex);
        }

        NodeList parent = doc.getElementsByTagName(type);

        for (int i = 0; i < parent.getLength(); i++) {
            if (((Element) parent.item(i)).getAttribute(identifierAttribute).matches(identifierValue)) {
                return ((Element) parent.item(i)).getAttribute(attributeName);
            }
        }

        return null;
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
        /*
         * XML DATA EDITION - BEGINNING
         */
        DocumentBuilder docBuilder = null;
        Document doc = null;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }
        try {
            doc = docBuilder.parse(new File(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI())));
        } catch (SAXException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }

        String typeToFetch = null;

        switch (MediaManager.getTypeOf(currentName)) {
            case MediaManager.AUDIO_TYPE:
                typeToFetch = "audio";
                break;
            case MediaManager.VIDEO_TYPE:
                typeToFetch = "video";
                break;
        }

        NodeList elements = doc.getElementsByTagName(typeToFetch);
        Node node = null;

        for (int i = 0; i < elements.getLength(); i++) {
            if (currentName.matches(((Element) elements.item(i)).getAttribute("name"))) {
                node = elements.item(i);
                break;
            }
        }
        NamedNodeMap map = node.getAttributes();
        Node name = map.getNamedItem("name");
        name.setTextContent(newName);
        Node author = map.getNamedItem("author");
        author.setTextContent(newAuthor);
        Node year = map.getNamedItem("year");
        year.setTextContent(newYearString);
        Node genre = map.getNamedItem("genre");
        genre.setTextContent(newGenre);
        /*
         * XML DATA EDITION - END
         */

        /*
         * XML DATA DUMP - BEGINNING
         */
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }

        String xmlString = result.getWriter().toString();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI()))))) {
            bufferedWriter.write(xmlString);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }
        /*
         * XML DATA DUMP - END
         */
    }

    /**
     * Removes a media element from the index.
     *
     * @param mediaName {@link String} The name of the media element.
     */
    public static void removeElementFromIndex(String mediaName) {
        /*
         * XML DATA DELETION - BEGINNING
         */
        DocumentBuilder docBuilder = null;
        Document doc = null;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        try {
            doc = docBuilder.parse(new File(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI())));
        } catch (SAXException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        String typeToFetch = null;

        switch (MediaManager.getTypeOf(mediaName)) {
            case MediaManager.AUDIO_TYPE:
                typeToFetch = "audio";
                break;
            case MediaManager.VIDEO_TYPE:
                typeToFetch = "video";
                break;
        }

        NodeList elements = doc.getElementsByTagName(typeToFetch);
        Node currentElementNode;

        for (int i = 0; i < elements.getLength(); i++) {
            if (mediaName.matches(((Element) elements.item(i)).getAttribute("name"))) {
                currentElementNode = elements.item(i);
                currentElementNode.getParentNode().removeChild(currentElementNode);
                break;
            }
        }
        /*
         * XML DATA DELETION - END
         */

        /*
         * XML DATA DUMP - BEGINNING
         */
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        String xmlString = result.getWriter().toString();
        xmlString = xmlString.replaceAll("(?m)^[ \t]*\r?\n", ""); //Remove generetad blank line.
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(XML.getAttribute("systemInfo", "name", "main", "library", Initializer.getDataURI()))))) {
            bufferedWriter.write(xmlString);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        /*
         * XML DATA DUMP - END
         */
    }
}
