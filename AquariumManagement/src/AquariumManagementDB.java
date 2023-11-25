package AquariumManagement.src;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AquariumManagementDB {
    private static final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1522:stu";
    // private static final String ORACLE_URL = "jdbc:oracle:thin:@dbhost.students.cs.ubc.ca:1522:stu";
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

    // FOR STAFF TABLE:
    public boolean insertStaff(int id, float salary, String staff_name, String datehired) {
        System.out.println("Inside Insert Staff");

        String sql = "INSERT INTO Staff (ID, SALARY, STAFF_NAME, DATEHIRED) VALUES (?, ?, ?, ?)";
        try {
            StaffHelper(id, salary, staff_name, datehired, sql);
            System.out.println("Data inserted successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Data was not inserted properly: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStaff(int id, float salary, String staff_name, String datehired) {
        String sql = "UPDATE STAFF SET SALARY = ?, STAFF_NAME = ?, DATEHIRED = ? WHERE ID = ?";

        try {
            int rowsAffected = StaffHelper(id, salary, staff_name, datehired, sql);
            if (rowsAffected > 0) {
                System.out.println("Data updated successfully.");
                return true;
            } else {
                System.out.println("ID not found, no data updated.");
                return false; // Or throw an exception if that's your preferred behavior
            }
        } catch (SQLException e) {
            System.out.println("Data was not updated properly: " + e.getMessage());
            return false;
        }
    }


    public boolean deleteStaff(int id) {
        String sql = "DELETE FROM STAFF WHERE ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Staff entry deleted successfully.");
                return true;
            } else {
                System.out.println("No such entry exists.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error during deletion: " + e.getMessage());
            return false;
        }
    }


    private static int StaffHelper(int id, float salary, String staff_name, String date_hired, String sql)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        // The order of parameters here is incorrect based on your SQL statement.
        // It should match the order in which they appear in the SQL query.
        preparedStatement.setFloat(1, salary);
        preparedStatement.setString(2, staff_name);
        preparedStatement.setString(3, date_hired);
        preparedStatement.setInt(4, id);

        // executeUpdate() returns the number of rows affected by the query
        int rowsAffected = preparedStatement.executeUpdate();
        return rowsAffected;
    }

    public boolean insertPlant(int plantId, String species, float livingTemp, float livingLight, int waterTankId) {
        System.out.println("Inside Insert Plant");

        String sql = "INSERT INTO Grown_In_Plant (plant_id, species, living_temp, living_light, water_tank_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, plantId);
            preparedStatement.setString(2, species);
            preparedStatement.setFloat(3, livingTemp);
            preparedStatement.setFloat(4, livingLight);
            preparedStatement.setInt(5, waterTankId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Plant data inserted successfully.");
                return true;
            } else {
                System.out.println("Plant data was not inserted.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Plant data was not inserted properly: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePlant(int plantId, String species, float livingTemp, float livingLight, int waterTankId) {
        System.out.println("Inside Update Plant");

        String sql = "UPDATE Grown_In_Plant SET species = ?, living_temp = ?, living_light = ?, water_tank_id = ? WHERE plant_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, species);
            preparedStatement.setFloat(2, livingTemp);
            preparedStatement.setFloat(3, livingLight);
            preparedStatement.setInt(4, waterTankId);
            preparedStatement.setInt(5, plantId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Plant data updated successfully.");
                return true;
            } else {
                System.out.println("No such plant exists to update.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Plant data was not updated properly: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePlant(int plantId) {
        System.out.println("Inside Delete Plant");

        String sql = "DELETE FROM Grown_In_Plant WHERE plant_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, plantId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Plant entry deleted successfully.");
                return true;
            } else {
                System.out.println("No such plant entry exists.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error during plant deletion: " + e.getMessage());
            return false;
        }
    }

    public boolean insertVendor(String vendorName, String marketRating) {
        System.out.println("Inside Insert Vendor");

        String sql = "INSERT INTO VendorReputation (vendor_name, vendor_market_rating) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, vendorName);
            preparedStatement.setString(2, marketRating);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vendor data inserted successfully.");
                return true;
            } else {
                System.out.println("Vendor data was not inserted.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Vendor data was not inserted properly: " + e.getMessage());
            return false;
        }
    }

    public boolean updateVendor(String vendorName, String marketRating) {
        System.out.println("Inside Update Vendor");

        String sql = "UPDATE VendorReputation SET vendor_market_rating = ? WHERE vendor_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, marketRating);
            preparedStatement.setString(2, vendorName);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vendor data updated successfully.");
                return true;
            } else {
                System.out.println("No such vendor exists to update.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Vendor data was not updated properly: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteVendor(String vendorName) {
        System.out.println("Inside Delete Vendor");

        String sql = "DELETE FROM VendorReputation WHERE vendor_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, vendorName);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vendor entry deleted successfully.");
                return true;
            } else {
                System.out.println("No such vendor entry exists.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error during vendor deletion: " + e.getMessage());
            return false;
        }
    }

    public boolean insertVendorLogistics(int id, String logisticsName, String address) {
        System.out.println("Inside Insert Vendor Logistics");

        String sql = "INSERT INTO VendorLogistics (id, vendor_logistics_name, address) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, logisticsName);
            preparedStatement.setString(3, address);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vendor logistics data inserted successfully.");
                return true;
            } else {
                System.out.println("Vendor logistics data was not inserted.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Vendor logistics data was not inserted properly: " + e.getMessage());
            return false;
        }
    }

    public boolean updateVendorLogistics(int id, String logisticsName, String address) {
        System.out.println("Inside Update Vendor Logistics");

        String sql = "UPDATE VendorLogistics SET vendor_logistics_name = ?, address = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, logisticsName);
            preparedStatement.setString(2, address);
            preparedStatement.setInt(3, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vendor logistics data updated successfully.");
                return true;
            } else {
                System.out.println("No such vendor logistics entry exists to update.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Vendor logistics data was not updated properly: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteVendorLogistics(int id) {
        System.out.println("Inside Delete Vendor Logistics");

        String sql = "DELETE FROM VendorLogistics WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Vendor logistics entry deleted successfully.");
                return true;
            } else {
                System.out.println("No such vendor logistics entry exists.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error during vendor logistics deletion: " + e.getMessage());
            return false;
        }
    }






//
//    public boolean listInventory() {
//        String sql = "SELECT * FROM INVENTORY";
//
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//
//            ResultSet inventoryResult = preparedStatement.executeQuery();
//
//            while (inventoryResult .next()) {
//                int id = inventoryResult .getInt("id");
//                String location = inventoryResult.getString("location");
//
//                System.out.println("ID: " + id + ", Location: " + location);
//            }
//
//            System.out.println("Data was listed successfully");
//            return true;
//
//        } catch (SQLException e) {
//            System.out.println("Data was not listed properly");
//            return false;
//        }
//    }
//
//
//    private static void InventoryHelper(int id, String location, String sql) throws SQLException {
//        PreparedStatement preparedStatement = connection.prepareStatement(sql);
//
//        preparedStatement.setInt(1, id);
//        preparedStatement.setString(2, location);
//
//        preparedStatement.executeUpdate();
//    }




}