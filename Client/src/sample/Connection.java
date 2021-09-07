package sample;

import java.io.*;
import java.net.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * Klasa za pomoca ktorej laczymy sie z serwerem
 */
public class Connection {
    public static final int PORT = 51000;
    public static final String HOST = "127.0.0.1";
    public static Socket sock;


    /**
     * Funkcja odpowiadajaca za polaczenie sie z serwerem
     * @throws IOException
     */
    public static void connect() throws IOException {


        //Obsluga logera
        Logger logger = Logger.getLogger("sample");
        try {
            FileInputStream properties = new FileInputStream("log.properties");
            LogManager.getLogManager().readConfiguration(properties);
            properties.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.warning("Problem z logerem" + e.getMessage());
        }
        //Nawiazanie polaczenia z serwerem
        sock = new Socket(HOST, PORT);
        System.out.println("Nawiazalem polaczenie: " + sock);
        //Tworzenie logow
        logger.info("Zostalo nawiazane polaczenie"+sock);
    }
}
