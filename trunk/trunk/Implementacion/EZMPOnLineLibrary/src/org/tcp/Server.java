package org.tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import org.library.LibraryHandler;
import org.xml.sax.SAXException;

/**
 * An abstract TCP server implementation. Realize that this implementation,
 * because of a server work procedure, should not be directly used. It requires
 * customization by overriding the
 * {@link Server#processString(java.lang.String)}, {@link Server#processSerializable(java.io.Serializable}
 * and {@link Server#processFile()} methods.
 *
 * @author See <a href="the Google Code page of the easy-utils
 * library">http://code.google.com/p/easy-utils/</a> for collaborators and other
 * information.
 */
public class Server {

    private final HashMap<Socket, Server.ServerDataBag> dataTable = new HashMap<>();

    /**
     * Starts listening for inputs at the given port.
     *
     * @param port An integer representing the port to listen at.
     * @throws IOException If any error happens.
     */
    public void start(int port) throws IOException {
        //Selector for incoming clients.
        Selector acceptSelector = SelectorProvider.provider().openSelector();

        //Create a new server socket and set to non blocking mode.
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        //Bind the server socket to the local host and the required port.
        InetAddress lh = InetAddress.getLocalHost();
        InetSocketAddress isa = new InetSocketAddress(lh, port);
        ssc.socket().bind(isa);

        //Register accepts on the server socket with the selector. This
        //step tells the selector that the socket wants to be put on the
        //ready list when accept operations occur, so allowing multiplexed
        //non-blocking I/O to take place.
        ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);
        //Here's where everything happens. The select method will
        //return when any operations registered above have occurred, the
        //thread has been interrupted, etc.
        while (acceptSelector.select() > 0) {
            //Someone is ready for I/O, get the ready keys.
            Set readyKeys = acceptSelector.selectedKeys();
            Iterator i = readyKeys.iterator();

            //Walk through the ready keys collection and process date requests.
            while (i.hasNext()) {
                //Clean the data table.
                for (Iterator<Socket> it = this.dataTable.keySet().iterator(); it.hasNext();) {
                    Socket x = it.next();
                    if (x.isClosed() || !x.isConnected()) {
                        if (!x.isClosed()) {
                            x.close();
                        }
                    }
                    this.dataTable.remove(x);
                }
                SelectionKey sk = (SelectionKey) i.next();
                i.remove();
                // The key indexes into the selector so you
                // can retrieve the socket that's ready for I/O.
                ServerSocketChannel nextReady = (ServerSocketChannel) sk.channel();
                // Accept the request and process input.
                final Socket s = nextReady.accept().socket();
                this.dataTable.put(s, new Server.ServerDataBag());
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Server.this.processInput(s);
                        } catch (ParserConfigurationException | SAXException | IOException ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                }.start();
            }
        }
    }

    /**
     * Processes input from a connected client.
     *
     * @param socket {@link Socket} The socket of the client.
     */
    @SuppressWarnings("empty-statement")
    private void processInput(Socket socket) throws ParserConfigurationException, SAXException, IOException {
        byte[] bytes;
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException ex) {
            System.err.println("Couldn't get socket streams - " + ex.getMessage());
        }
        switch (this.dataTable.get(socket).getDataType()) {
            case "size":
                bytes = new byte[1024]; //For sure the data size won't be more than this.
                try {
                    while (input.read(bytes) == -1);
                } catch (IOException ex) {
                    System.err.println("Couldn't read the 'dataType,dataSize' message - " + ex.getMessage());
                }
                StringTokenizer tokenizer = new StringTokenizer(new String(bytes).trim(), ",");
                this.dataTable.get(socket).setDataType(tokenizer.nextToken());
                this.dataTable.get(socket).setDataSize(Integer.parseInt(tokenizer.nextToken()));
                this.dataTable.get(socket).setData(ByteBuffer.allocate(this.dataTable.get(socket).getDataSize()));
                this.processInput(socket);
                return;
            case "string":
                while (true) {
                    bytes = new byte[Math.min(this.dataTable.get(socket).getDataSize(), 1024)]; //Try to avoid too fast reading.
                    try {
                        output.write("next".getBytes());
                    } catch (IOException ex) {
                        System.err.println("Couldn't write the first 'next' signal - " + ex.getMessage());
                    }
                    try {
                        input.read(bytes, 0, bytes.length);
                    } catch (IOException ex) {
                        System.err.println("Couldn't read a piece of plain-text message - " + ex.getMessage());
                    }
                    this.dataTable.get(socket).setDataSize(this.dataTable.get(socket).getDataSize() - bytes.length);
                    this.dataTable.get(socket).getData().put(bytes);
                    if (this.dataTable.get(socket).getDataSize() == 0) {
                        break;
                    }
                }
                this.dataTable.get(socket).getData().clear(); //Most likely not needed since what will be used is the byte array, not the ByteBuffer object.
                this.processString(new String(this.dataTable.get(socket).getData().array()), socket);
                break;
        }
        //Reset the server state so it can receive new data.
        this.dataTable.get(socket).setDataType("size");

        //And keep listening to the client requests.
        this.processInput(socket);
    }

    /**
     * Sends a plain-text message to a connected address. It should only be used
     * inside the methods for data arrival processing. Though, it is declared as
     * public so it can be used in overridings.
     *
     * @param message {@link String} The message to send.
     * @param socket {@link Socket} The socket to send the message to.
     */
    private void sendString(String message, Socket socket) {
        String stringInfo = "" + message.length();
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException ex) {
            System.err.println("Couldn't get socket streams - " + ex.getMessage());
        }

        try {
            output.write(stringInfo.getBytes());
        } catch (IOException ex) {
            System.err.println("Couldn't send plain-text message " + message + " because an exception occured - " + ex.getMessage() + " - Please check if this object 'sc' attribute is not null and if it is connected to an address.");
        }

        ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
        byte[] bridge;

        ByteBuffer nextDst;

        buf.clear();

        while (buf.hasRemaining()) {
            nextDst = ByteBuffer.allocate(4);
            try {
                input.read(nextDst.array());
            } catch (IOException ex) {
                System.err.println("Couldn't read the 'next' signal in plain-text sending.");
            }
            String real;
            if (!(real = new String(nextDst.array())).matches("next")) {
                throw new IllegalArgumentException("Received " + real + " when a 'next' signal was expected.");
            }
            bridge = new byte[Math.min(buf.remaining(), 1024)];
            buf.get(bridge);
            try {
                output.write(bridge);
            } catch (IOException ex) {
                System.err.println("Couldn't send plain-text message " + message + " because an exception occured - " + ex.getMessage() + " - Please check if this object 'sc' attribute is not null and if it is connected to an address.");
            }
            if (!buf.hasRemaining()) {
                break;
            }
        }
    }

    /**
     * Performs actions when a plain-text message is received. To be overriden.
     * Realize it's declared as public just to allow access to it when
     * overriden, but it is automatically called when a plain-text message is
     * received and therefore it shouldn't be manually used.
     *
     * @param receivedString {@link String} The received message.
     * @param socket {@link Socket} The socket where the message has been
     * received.
     */
    public void processString(String receivedString, Socket socket) throws ParserConfigurationException, SAXException, IOException {
        if (receivedString.startsWith("fetch")) {//Suponemos que ésto es la búsqueda del archivo.
            if (LibraryHandler.isAny(receivedString.substring("fetch".length()))) {
                this.sendString("yes", socket);
            } else {
                this.sendString("no", socket);
            }
        } else if (receivedString.startsWith("retrieveType")) {
            this.sendString(LibraryHandler.getPath(receivedString.substring("retrieveType".length())).endsWith(".mp4") ? "video" : "audio", socket);
        } else if (receivedString.startsWith("retrieveFile")) {//Suponemos que ésto significa la confirmación de que se quiere el archivo.
            this.sendFile(Paths.get(LibraryHandler.getPath(receivedString.substring("retrieveFile".length()))), socket);
        } else if (receivedString.startsWith("retrieveData")) {//Suponemos que ésto significa la confirmación de que se quieren los datos del elemento.
            this.sendString(receivedString.substring("retrieveData".length()) + "|" + LibraryHandler.retrieveElementInfo(receivedString), socket);
        }
    }

    /**
     * Sends a file to the connected address. It should only be used inside the
     * methods for data arrival processing. Though, it is declared as public so
     * it can be used in overridings.
     *
     * @param pathToFile {@link Path} The path of the file to send.
     * @param socket {@link Socket} The socket to send the object to.
     */
    private void sendFile(Path pathToFile, Socket socket) {
        String stringInfo;
        int fileSize = 0;

        try {
            fileSize = (int) Files.size(pathToFile);
        } catch (IOException ex) {
            System.err.println("Error when trying to get the size of the file " + pathToFile + " to be sent - " + ex.getMessage());
        }

        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException ex) {
            System.err.println("Couldn't get socket streams - " + ex.getMessage());
        }

        stringInfo = "" + fileSize;

        ByteBuffer buf = ByteBuffer.wrap(stringInfo.getBytes());

        try {
            output.write(buf.array());
        } catch (IOException ex) {
            System.err.println("Couldn't send file " + pathToFile + " because an exception occured - " + ex.getMessage() + " - Please check if this object 'sc' attribute is not null and if it is connected to an address.");
        }

        buf = ByteBuffer.allocate(fileSize);
        try (FileChannel inFromFile = FileChannel.open(pathToFile)) {
            inFromFile.read(buf);
        } catch (IOException ex) {
            System.err.println("IOException occured when trying to read file " + pathToFile + " which had to be sent - " + ex.getMessage());
        }


        byte[] bridge;

        ByteBuffer bufferForNextSignal;

        buf.clear();

        while (buf.hasRemaining()) {
            bufferForNextSignal = ByteBuffer.allocate(4);
            try {
                input.read(bufferForNextSignal.array());
            } catch (IOException ex) {
                System.err.println("Couldn't read the 'next' signal in file sending.");
            }
            String real;
            if (!(real = new String(bufferForNextSignal.array())).matches("next")) {
                throw new IllegalArgumentException("Received " + real + " when a 'next' signal was expected.");
            }
            bridge = new byte[Math.min(buf.remaining(), 1024)];
            buf.get(bridge);
            try {
                output.write(bridge);
            } catch (IOException ex) {
                System.err.println("Couldn't send file " + pathToFile + " because an exception occured - " + ex.getMessage() + " - Please check if this object 'sc' attribute is not null and if it is connected to an address.");
            }
            if (!buf.hasRemaining()) {
                break;
            }
        }
    }

    /**
     * Since the server needs to handle each connection data individually, this
     * class offers support by keeping together the set of attributes that the
     * server will need for each client.
     */
    private class ServerDataBag {

        private String dataType = "size";
        private Path pathForNextFile;
        private ByteBuffer data;
        private int dataSize;

        /**
         * Getter for the {@link ServerDataBag#dataType} attribute.
         *
         * @return {@link String} The {@link ServerDataBag#dataType} attribute.
         */
        private String getDataType() {
            return this.dataType;
        }

        /**
         * Getter for the {@link ServerDataBag#pathForNextFile} attribute.
         *
         * @return {@link Path} The {@link ServerDataBag#pathForNextFile}
         * attribute.
         */
        private Path getPathForNextFile() {
            return this.pathForNextFile;
        }

        /**
         * Getter for the {@link ServerDataBag#data} attribute.
         *
         * @return {@link ByteBuffer} The {@link ServerDataBag#data} attribute.
         */
        private ByteBuffer getData() {
            return this.data;
        }

        /**
         * Getter for the {@link ServerDataBag#dataSize} attribute.
         *
         * @return An integer representing the {@link ServerDataBag#dataSize}
         * attribute.
         */
        private int getDataSize() {
            return this.dataSize;
        }

        /**
         * Setter for the {@link ServerDataBag#dataType} attribute.
         *
         * @param newDataType {@link String} The new value for the
         * {@link ServerDataBag#dataType} attribute.
         */
        private void setDataType(String newDataType) {
            switch (newDataType) {
                case "size":
                case "string":
                case "serializable":
                case "file":
                    this.dataType = newDataType;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal server status - " + newDataType);
            }
        }

        /**
         * Setter for the {@link ServerDataBag#data} attribute.
         *
         * @param newData {@link ByteBuffer} The new value for the
         * {@link ServerDataBag#data} attribute.
         */
        private void setData(ByteBuffer newData) {
            this.data = newData;
        }

        /**
         * Setter for the {@link ServerDataBag#dataSize} attribute.
         *
         * @param newDataSize An integer representing the new value for the
         * {@link ServerDataBag#dataSize} attribute.
         */
        private void setDataSize(int newDataSize) {
            this.dataSize = newDataSize;
        }
    }
}
