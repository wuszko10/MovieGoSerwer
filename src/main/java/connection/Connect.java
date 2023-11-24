package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {

    private String driver = "com.mysql.cj.jdbc.Driver";
    private String user = "login";
    private String pass = "password";

    private String url = "jdbc:mysql://w10.domenomania.eu/wiktor10_kino";
    private Connection connection;

    public Connect() {
        connection = makeConnection();
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException sqle) {
            System.err.println("Blad przy zamykaniu polaczenia: " + sqle);
        }
    }

    public Connection makeConnection() {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, pass);
            return connection;
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Blad ladowania sterownika: " + cnfe);
            return null;
        } catch (SQLException sqle) {
            System.err.println("Blad przy nawiązywaniu polaczenia: " + sqle);
            return null;
        }
    }
}
