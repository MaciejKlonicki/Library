package com.company;

import oracle.jdbc.OracleConnection;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Serwer
 */
public class Host {
    public static final int PORT = 51000;
    static Socket sock;
    static ServerSocket serv;
    static BufferedReader inp;



    /**
     * Funkcja za pomoca ktorej wlaczamy serwer oraz odczytujemy komunikaty od klienta o wyswietlaniu ksiazek, wyswietlaniu historii sprzedazy oraz zapytaniach INSERT i DELETE
     *
     * @param args
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String args[]) throws IOException, SQLException {


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

            //Tworzenie gniazda serwerowego
            serv = new ServerSocket(PORT);

            //Oczekiwanie na polaczenie i tworzenie gniazda sieciowego
            System.out.println("Oczekuje na polaczenie " + serv);
            sock = serv.accept();
            System.out.println("Jest polaczenie: " + sock);
            logger.info("Nawiazana polaczenie z: " + sock);



            //Tworzenie strumienia danych pobieranych z gniazda sieciowego

            inp = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            //Komunikacja - czytanie danych ze strumienia
            String str;
            OracleConnection dbConnection = Database.connectDatabase();
            PrintWriter outp;
            outp = new PrintWriter(sock.getOutputStream());
            while (true) {
                str = inp.readLine();
                //Odczytywanie komunikatu od klienta do wyswietlania ksiazek
                if (str.equals("Ksiazki")) {
                    Database.printBooks(dbConnection);
                    outp.println(Database.printBooks(dbConnection));
                    outp.flush();
                }
                //Odczytywanie komunikatu od klienta do wyswietlnia sprzedazy
                if (str.equals("Sprzedaz")) {
                    Database.printHistory(dbConnection);
                    //Zapytanie do bazy
                    outp.println(Database.printHistory(dbConnection));
                    outp.flush();
                }
                //Odczytywanie komunikatu do tworzenia zapytan
                if (str.contains("INSERT") || str.contains("DELETE")) {
                    Database.insert_delete_book(dbConnection, str);
                }
            }
        }



        //Zamykanie polaczenia
        @Override
        public void finalize () throws IOException {
            inp.close();
            sock.close();
            serv.close();
        }

    }




