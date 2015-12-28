package biblioteca;

import java.nio.file.Path;

/**
 * Envoltorio de atributos para un elemento estándar.
 */
public class Element {

    private String name, author, genre;
    private int year;
    private Path path;

    /**
     * Constructor method.
     *
     * @param name {@link String} The name of the element.
     * @param author {@link String} The author of the element.
     * @param year An integer telling the year of the element
     * @param genre {@link String} The genre of the element.
     * @param path {@link Path} The path to the element file.
     */
    public Element(String name, String author, int year, String genre, Path path) {
        this.name = name;
        this.author = author;
        this.year = year;
        this.genre = genre;
        this.path = path;
    }

    /**
     * Getter for name.
     *
     * @return {@link String} name of the element
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for author.
     *
     * @return {@link String} author of the element
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Getter for element.
     *
     * @return {@link String} genre of the element
     */
    public String getGenre() {
        return this.genre;
    }

    /**
     * Getter for year.
     *
     * @return year of the element
     */
    public int getYear() {
        return this.year;
    }

    /**
     * Getter for path.
     *
     * @return {@link Path} path to the file of the element
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Setter for name.
     *
     * @param name name of the element
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setter for author.
     *
     * @param author {@link String} author of the element
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Setter for genre.
     *
     * @param genre {@link String} genre of the element
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Setter for year.
     *
     * @param year An integer telling year of the element
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Setter for path.
     *
     * @param path {@link Path} path to the file of the element
     */
    public void setPath(Path path) {
        this.path = path;
    }
}
