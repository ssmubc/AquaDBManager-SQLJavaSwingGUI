package AquariumManagement.src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class AquariumManagementDB {
    private static final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1522:stu";
    private Connection connection = null;
    public AquariumManagementDB() {
        try {
            // Load the Oracle JDBC driver
            // Note that the path could change for new drivers
            // Source: https://www.students.cs.ubc.ca/~cs-304/resources/jdbc-oracle-resources/jdbc-java-looking-through-code.html
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (SQLException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    public boolean getConnection(String username, String password) {
        // connecting to oracle database
        try {
            if (connection != null) {
                connection.close();
            }
            // for debugging
            System.out.println("Ok we make it till here");
            System.out.println(username);
            connection = DriverManager.getConnection(ORACLE_URL, username, password);
            // closing connection (for now) just testing if it works
            connection.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("The connection is not working");
            return false;
        }
    }
    // Source: https://www.students.cs.ubc.ca/~cs-304/resources/jdbc-oracle-resources/jdbc-java-looking-through-code.html
    /*
    public static void closeDB() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    */
}