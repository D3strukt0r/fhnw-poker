/*
 * fhnw-jass is jass game programmed in java for a school project.
 * Copyright (C) 2020 Manuele Vaccari & Victor Hargrave & Thomas Weber & Sasa
 * Trajkova
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package jass.client.util;

import jass.client.entity.LoginEntity;
import jass.client.eventlistener.DisconnectEventListener;
import jass.client.message.Message;
import jass.lib.message.MessageData;
import jass.lib.servicelocator.Service;
import jass.lib.servicelocator.ServiceLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

/**
 * Backend utility class. Acts as an interface between the program and the
 * server.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 1.0.0
 */
public final class SocketUtil extends Thread implements Service, Closeable {
    /**
     * The logger to print to console and save in a .log file.
     */
    private static final Logger logger = LogManager.getLogger(SocketUtil.class);

    /**
     * The socket of the server.
     */
    private final Socket socket;

    /**
     * Whether the server is currently reachable. Shuts down everything when
     * false.
     */
    private volatile boolean serverReachable = true;

    /**
     * A list of all messages coming from the server.
     */
    private final ArrayList<Message> lastMessages = new ArrayList<>();

    /**
     * Creates a Socket (insecure or secure) to the backend.
     *
     * @param ipAddress A String containing the ip address to reach the server.
     * @param port      An integer containing the port which the server uses.
     * @param secure    A boolean defining whether to use SSL or not.
     *
     * @author Manuele Vaccari & https://stackoverflow.com/questions/53323855/sslserversocket-and-certificate-setup
     * @since 1.0.0
     */
    public SocketUtil(final String ipAddress, final int port, final boolean secure) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        super();
        this.setName("SocketThread");
        this.setDaemon(true);

        if (secure) {
            logger.info("Connecting to server at: " + ipAddress + ":" + port + " (with SSL)");

            // Create and initialize the SSLContext with key material
            char[] trustStorePassword = "JassGame".toCharArray();
            char[] keyStorePassword = "JassGame".toCharArray();

            // First initialize the key and trust material
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(getClass().getResourceAsStream("/ssl/client.keystore"), trustStorePassword);

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(getClass().getResourceAsStream("/ssl/client.keystore"), keyStorePassword);

            // KeyManagers decide which key material to use
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePassword);

            // TrustManagers decide whether to allow connections
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), SecureRandom.getInstanceStrong());

            SocketFactory factory = sslContext.getSocketFactory();
            socket = factory.createSocket(ipAddress, port);

            // The next line is entirely optional!
            // The SSL handshake would happen automatically, the first time we
            // send data. Or we can immediately force the handshaking with this
            // method:
            ((SSLSocket) socket).startHandshake();
        } else {
            logger.info("Connecting to server at: " + ipAddress + ":" + port);
            socket = new Socket(ipAddress, port);
        }

        // Create thread to read incoming messages
        this.start();
    }

    /**
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (serverReachable) {
                // Will wait here for complete line
                String msgText = in.readLine();

                // In case the server closes the socket
                if (msgText == null) {
                    logger.info("Server disconnected");
                    break;
                }

                // Create a message object of the correct class, using
                // reflection
                logger.info("Receiving message: " + msgText);
                MessageData msgData = MessageData.unserialize(msgText);

                if (msgData == null) {
                    logger.error("Received invalid message");
                } else {
                    // Create a message object of the correct class, using
                    // reflection
                    Message msg = Message.fromDataObject(msgData);
                    if (msg == null) {
                        logger.error("Received invalid message of type " + msgData.getMessageType());
                    } else {
                        logger.info("Received message of type " + msgData.getMessageType());
                        lastMessages.add(msg);

                        if (EventUtil.handleEventListenerOnMessage(msgData)) {
                            lastMessages.remove(msg);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.toString());
        }

        close();
        for (DisconnectEventListener listener : new ArrayList<>(EventUtil.getDisconnectListeners())) {
            logger.info("Invoking onDisconnectEvent event on " + listener.getClass().getName());
            listener.onDisconnectEvent();
        }
    }

    /**
     * Wait until the corresponding "Result" response arrives from the server.
     *
     * @param id The ID of the message.
     *
     * @return Returns the result message.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public Message waitForResult(final int id) {
        while (true) {
            if (lastMessages.size() != 0) {
                for (Message m : lastMessages) {
                    if (m.getId() == id) {
                        lastMessages.remove(m);
                        return m;
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { /* Ignore */ }
        }
    }

    /**
     * Send a message to this server. In case of an exception, log the client
     * out.
     *
     * @param msg The message to send to the server.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public void send(final Message msg) {
        try {
            LoginEntity login = ServiceLocator.get(LoginEntity.class);
            if (isLoggedIn()) {
                assert login != null;
                msg.getRawData().setToken(login.getToken());
                msg.getRawData().setUsername(login.getUsername());
            }
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            logger.info("Sending message: " + msg.toString());
            // This will send the serialized MessageData object
            out.write(msg.toString() + "\n");
            out.flush();
        } catch (IOException e) {
            logger.info("Server unreachable; logged out");
            close();
        }
    }

    /**
     * @return Returns true if logged in, otherwise false
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public boolean isLoggedIn() {
        return ServiceLocator.get(LoginEntity.class) != null;
    }

    /**
     * @return Returns a string containing the token if logged in, otherwise
     * null
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public String getToken() {
        LoginEntity login = ServiceLocator.get(LoginEntity.class);
        if (login != null) {
            return login.getToken();
        }
        return null;
    }

    /**
     * Verifies that the string is a valid ip address.
     *
     * @param ipAddress A String containing the ip address.
     *
     * @return Returns true if valid, otherwise false.
     *
     * @author https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
     * @since 1.0.0
     */
    public static boolean isValidIpAddress(final String ipAddress) {
        return ipAddress.matches("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    }

    /**
     * Verifies that the string is in the valid range of open ports.
     *
     * @param port An integer containing the port.
     *
     * @return Returns true if valid, otherwise false.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public static boolean isValidPortNumber(final int port) {
        return port >= 1024 && port <= 65535;
    }

    /**
     * Closes the socket.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @Override
    public void close() {
        logger.info("Closing connection to server");
        serverReachable = false;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) { /* Ignore */ }
        }
        ServiceLocator.remove(SocketUtil.class);
    }
}
