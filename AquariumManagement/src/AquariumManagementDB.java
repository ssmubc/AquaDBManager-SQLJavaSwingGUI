package AquariumManagement.src;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AquariumManagementDB {
    private static final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1522:stu";
    private static Connection connection = null;
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
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("The connection is not working");
            return false;
        }
    }
    // Source: https://www.students.cs.ubc.ca/~cs-304/resources/jdbc-oracle-resources/jdbc-java-looking-through-code.htm
    public boolean closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
            return true;
        } catch (SQLException e) {
            System.out.println(" " + e.getMessage());
            return false;
        }
    }
    public boolean insertInventory(int id, String location) {
        String sql = "INSERT INTO INVENTORY (ID, LOCATION) VALUES (?, ?)";

        try {
            InventoryHelper(id, location, sql);

            System.out.println("Data inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public static List<String> getColumnNames(String tableName) {
        List<String> columnNames = new ArrayList<>();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                columnNames.add(columnName);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return columnNames;
    }

    public boolean updateInventory(int id, String location) {
        String sql = "UPDATE INVENTORY SET LOCATION = ? WHERE ID = ?";

        try {
            InventoryHelper(id, location, sql);

            System.out.println("Data inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean deleteInventory(int id) {
        String sql = "DELETE FROM INVENTORY WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            System.out.println("Data inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean listInventory() {
        String sql = "SELECT * FROM INVENTORY";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet inventoryResult = preparedStatement.executeQuery();

            while (inventoryResult .next()) {
                int id = inventoryResult .getInt("id");
                String location = inventoryResult.getString("location");

                System.out.println("ID: " + id + ", Location: " + location);
            }

            System.out.println("Data was listed successfully");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not listed properly");
            return false;
        }
    }


    private static void InventoryHelper(int id, String location, String sql) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setInt(1, id);
        preparedStatement.setString(2, location);

        preparedStatement.executeUpdate();
    }




}