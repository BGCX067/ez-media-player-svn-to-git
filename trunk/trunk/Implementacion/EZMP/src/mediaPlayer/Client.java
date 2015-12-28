package mediaPlayer;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * An abstract TCP client implementation.
 *
 * @author See <a href="the Google Code page of the easy-utils
 * library">http://code.google.com/p/easy-utils/</a> for collaborators and other
 * information.
 */
public class Client {

    private ByteBuffer data;
    private int dataSize;
    private SocketChannel sc;

    /**
     * Constructor method.
     */
    public Client() {
        try {
            this.sc = SocketChannel.open();
        } catch (IOException ex) {
            System.err.println("Couldn't open SocketChannel - " + ex.getMessage());
        }
    }

    /**
     * Connects to a server.
     *
     * @param address {@link String} The address of the server.
     * @param port An integer representing the port to use.
     * @throws Exception If any error happens.
     */
    public void connect(String address, int port) throws Exception {
        if (this.sc == null) {
            this.sc = SocketChannel.open();
        }
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(address), port);
        this.sc.connect(addr);
    }

    /**
     * Disconnects from the currently connected server (if any).
     *
     * @throws Exception If any error happens.
     */
    public void disconnect() throws Exception {
        if (this.sc == null || !this.sc.isConnected()) {
            return;
        }
        this.sc.close();
        this.sc = null;
    }

    /**
     * Sends a plain-text message to the connected address. If this client is
     * not connected to any addresses, the methods returns immediately.
     *
     * @param message {@link String} The message to send.
     * @throws Exception If any error happens.
     */
    public void sendString(String message) throws Exception {
        if (this.sc == null || !this.sc.isConnected()) {
            return;
        }

        String stringInfo = "string," + message.length();
        ByteBuffer buf = ByteBuffer.wrap(stringInfo.getBytes());

        this.sc.write(buf);

        buf = ByteBuffer.wrap(message.getBytes());
        byte[] bridge;

        ByteBuffer nextDst;

        buf.clear();

        while (buf.hasRemaining()) {
            nextDst = ByteBuffer.allocate(4);
            this.sc.read(nextDst);
            String real;
            if (!(real = new String(nextDst.array())).matches("next")) {
                throw new IllegalArgumentException("Received " + real + " when a 'next' signal was expected.");
            }
            bridge = new byte[Math.min(buf.remaining(), 1024)];
            buf.get(bridge);
            this.sc.write(ByteBuffer.wrap(bridge));
            if (!buf.hasRemaining()) {
                break;
            }
        }
    }

    /**
     * Receives a plain-text message.
     *
     * @return {@link String} The received message.
     * @throws Exception If any error happens.
     */
    @SuppressWarnings("empty-statement")
    public String receiveString() throws Exception {
        byte[] bytes = new byte[1024]; //For sure the data size won't be more than this.
        InputStream input;
        OutputStream output;
        Socket socket = this.sc.socket();

        input = socket.getInputStream();
        output = socket.getOutputStream();


        while (input.read(bytes) == -1);
        this.dataSize = Integer.parseInt(new String(bytes).trim());
        output.write("next".getBytes());
        this.data = ByteBuffer.allocate(this.dataSize);

        while (true) {
            bytes = new byte[Math.min(this.dataSize, 1024)]; //Try to avoid too fast reading.
            input.read(bytes, 0, bytes.length);
            this.dataSize -= bytes.length;
            this.data.put(bytes);
            if (this.dataSize == 0) {
                break;
            }
            output.write("next".getBytes());
        }
        this.data.clear(); //Most likely not needed since what will be used is the byte array, not the ByteBuffer object.
        return new String(this.data.array());
    }

    /**
     * Receives a file.
     *
     * @param destinationPath {@link Path} The path to dump the file to.
     * @throws Exception If any error happens.
     */
    @SuppressWarnings("empty-statement")
    public void receiveFile(Path destinationPath) throws Exception {
        byte[] bytes = new byte[1024]; //For sure the data size won't be more than this.
        InputStream input = null;
        OutputStream output = null;
        Socket socket = this.sc.socket();

        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException ex) {
            System.err.println("Couldn't get socket streams - " + ex.getMessage());
        }

        try {
            while (input.read(bytes) == -1);
        } catch (IOException ex) {
            System.err.println("Couldn't read the 'dataSize' message - " + ex.getMessage());
        }
        this.dataSize = Integer.parseInt(new String(bytes).trim());
        try {
            output.write("next".getBytes());
        } catch (IOException ex) {
            System.err.println("Couldn't write the first 'next' signal - " + ex.getMessage());
        }
        this.data = ByteBuffer.allocate(this.dataSize);

        while (true) {
            bytes = new byte[Math.min(this.dataSize, 1024)]; //Try to avoid too fast reading.
            try {
                input.read(bytes, 0, bytes.length);
            } catch (IOException ex) {
                System.err.println("Couldn't read a piece of a file - " + ex.getMessage());
            }
            this.dataSize -= bytes.length;
            this.data.put(bytes);
            if (this.dataSize == 0) {
                break;
            }
            try {
                output.write("next".getBytes());
            } catch (IOException ex) {
                System.err.println("Couldn't write the first 'next' signal - " + ex.getMessage());
            }
        }
        this.data.clear(); //Most likely not needed since what will be used is the byte array, not the ByteBuffer object.
        Files.createFile(destinationPath);
        try (FileChannel channelForWritingToFile = FileChannel.open(destinationPath, StandardOpenOption.WRITE)) {
            channelForWritingToFile.write(this.data);
        } catch (IOException ex1) {
            System.err.println("IOException ocurred while dumping file " + ex1.getLocalizedMessage());
            try {
                if (Files.deleteIfExists(destinationPath)) {
                    System.err.println("File " + destinationPath + " was, as a consecuence, deleted.");
                }
            } catch (IOException ex2) {
                System.err.println("Couldn't delete dumped file " + destinationPath + " - " + ex2.getMessage());
            }
        }
    }

    /**
     * Getter for {@link Client#sc}. Use only for information retrieving
     * purposes.
     *
     * @return {@link SocketChannel} This object {@link Client#sc} attribute.
     */
    public SocketChannel getSocketChannel() {
        return this.sc;
    }
}