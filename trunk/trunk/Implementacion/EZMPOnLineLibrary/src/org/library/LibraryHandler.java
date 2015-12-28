package org.library;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Handles all operations with the elements library.
 */
public class LibraryHandler {

    /**
     * Checks if a given element exists in the library.
     *
     * @param elementName {@link String} The name to check.
     * @return A boolean representing the existance of an element with the given
     * name in the library. <value>true</value> if any element with that name
     * exists. <value>false</value> otherwise.
     */
    public static boolean isAny(String elementName) throws ParserConfigurationException, SAXException, IOException {
        return null == LibraryHandler.retrieveElementInfo(elementName);
    }

    /**
     * Retrieves
     *
     * @param elementName {@link String} The name to check.
     * @return A boolean representing the existance of an element with the given
     * name in the library. <value>true</value> if any element with that name
     * exists. <value>false</value> otherwise.
     */
    public static String retrieveElementInfo(String elementName) throws ParserConfigurationException, SAXException, IOException {
        String audio;
        if ((audio = LibraryHandler.getAttribute("audio", "name", elementName, "name", Paths.get("library", "library.xml"))) != null || LibraryHandler.getAttribute("video", "name", elementName, "name", Paths.get("library", "library.xml")) != null) {
            StringBuilder ret;
            if (audio != null) {
                ret = new StringBuilder(LibraryHandler.getAttribute("audio", "name", elementName, "author", Paths.get("library", "library.xml")) + "|" + LibraryHandler.getAttribute("audio", "name", elementName, "genre", Paths.get("library", "library.xml")) + "|" + elementName + "|" + LibraryHandler.getAttribute("audio", "name", elementName, "year", Paths.get("library", "library.xml")));
            } else {
                ret = new StringBuilder(LibraryHandler.getAttribute("video", "name", elementName, "author", Paths.get("library", "library.xml")) + "|" + LibraryHandler.getAttribute("video", "name", elementName, "genre", Paths.get("library", "library.xml")) + "|" + elementName + "|" + LibraryHandler.getAttribute("video", "name", elementName, "year", Paths.get("library", "library.xml")));
            }
            return ret.toString();
        } else {
            return null;
        }
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
    private static String getAttribute(String type, String identifierAttribute, String identifierValue, String attributeName, Path pathToFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder docBuilder;
        Document doc;

        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = docBuilder.parse(pathToFile.toFile());


        NodeList parent = doc.getElementsByTagName(type);

        for (int i = 0; i < parent.getLength(); i++) {
            if (((Element) parent.item(i)).getAttribute(identifierAttribute).matches(identifierValue)) {
                return ((Element) parent.item(i)).getAttribute(attributeName);
            }
        }

        return null;
    }

    /**
     * Gets the path to the file of a certain element.
     *
     * @param elementName {@link String} The name of the element.
     * @return {@link String} The path to the file corresponding to the element.
     */
    public static String getPath(String elementName) throws ParserConfigurationException, SAXException, IOException {
        if (!LibraryHandler.isAny(elementName)) {
            return null;
        }
        String type = "audio";
        if (LibraryHandler.getAttribute("audio", "name", elementName, "name", Paths.get("library", "library.xml")) == null) {
            type = "video";
        }
        return LibraryHandler.getAttribute(type, "name", elementName, "path", Paths.get("library", "library.xml"));
    }
}
