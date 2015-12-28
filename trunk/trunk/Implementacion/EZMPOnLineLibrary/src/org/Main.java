package org;

import java.io.IOException;
import org.tcp.Server;

/**
 * Main class.
 */
public class Main {

    private static final int PORT = 5999;
    
    /**
     * Main method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Server().start(Main.PORT);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
