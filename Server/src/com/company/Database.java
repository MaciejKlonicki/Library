package com.company;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;

import java.sql.DatabaseMetaData;

/**
 * Baza danych
 */
public class Database {

    final static String DB_URL = "jdbc:oracle:thin:@ProjektJava_medium?TNS_ADMIN=C:/STUDIA/Wallet";
    final static String DB_USER = "ADMIN";
    final static String DB_PASSWORD = "ProjektJava123!";

    /**
     * Funkcja za pomoca ktorej laczymy sie z baza danych
     *
     * @return connection
     * @throws SQLException
     */
    public static OracleConnection connectDatabase() throws SQLException {
        Properties info = new Properties();
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");


        OracleDataSource ods = new OracleDataSource();
        ods.setURL(DB_URL);
        ods.setConnectionProperties(info);


        OracleConnection connection = (OracleConnection) ods.getConnection();

        DatabaseMetaData dbmd = connection.getMetaData();
        //Informacje o bazie danych i uzytkowniku
        System.out.println("Driver Name: " + dbmd.getDriverName());
        System.out.println("Driver Version: " + dbmd.getDriverVersion());

        System.out.println("Default Row Prefetch Value is: " +
                connection.getDefaultRowPrefetch());

        System.out.println();

        return connection;
    }

    /**
     * Funkcja za pomoca ktorej wyswietlamy dane o dostepnych ksiazkach w Kliencie
     *
     * @param connection
     * @return DostepneKsiazki
     * @throws SQLException
     */
    public static String printBooks(Connection connection) throws SQLException {

        StringBuilder books = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement
                    .executeQuery("select id_ksiazki, nazwa, autor, rodzaj, cena, ilosc, lokalizacja from ksiazki")) {
                while (resultSet.next())
                    books.append(resultSet.getString(1) + " |Nazwa: "
                            + resultSet.getString(2) + " |Autor: "
                            + resultSet.getString(3) + " |Rodzaj: "
                            + resultSet.getString(4) + " |Lokalizacja: "
                            + resultSet.getString(7) + " |Ilosc: "
                            + resultSet.getString(6) + " |Cena: "
                            + resultSet.getString(5) + "\n");
            }
        }
        return books.toString();
    }

    /**
     * Funkcja za pomoca ktorej wyswietlamy dane o historii sprzedazy
     *
     * @param connection
     * @return HistoriaSprzedazy
     * @throws SQLException
     */
    public static String printHistory(Connection connection) throws SQLException {
        StringBuilder history = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement
                    .executeQuery("select id_sprzedazy, id_ksiazki, ilosc_sprzedanych,  TO_CHAR(data_sprzedazy,'DD-MM-YYYY'), koszt_calkowity from sprzedaz")) {
                while (resultSet.next())
                    history.append(resultSet.getString(1) + " |ID_ksiazki: "
                            + resultSet.getString(2) + " |Ilosc_sprzedanych: "
                            + resultSet.getString(3) + " |Data: "
                            + resultSet.getString(4) + " |Koszt_calkowity: "
                            + resultSet.getString(5) + "\n");
            }
        }
        return history.toString();
    }

    /**
     * Funkcja za pomoca ktorej obslugujemy dodawanie oraz usuwanie ksiazek
     *
     * @param connection
     * @param query
     * @throws SQLException
     */
    public static void insert_delete_book(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement
                    .executeQuery(query)) {
            }
        }
    }
}