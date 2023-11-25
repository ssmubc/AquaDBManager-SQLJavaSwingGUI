package AquariumManagement.src;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

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

            while (inventoryResult.next()) {
                int id = inventoryResult .getInt("ID");
                String location = inventoryResult.getString("LOCATION");

                System.out.println("ID: " + id + ", Location: " + location);
            }

            System.out.println("Data was listed successfully");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not listed properly");
            return false;
        }
    }

    public boolean insertItem(int id, String name, int quantity, String unit) {
        String sql1 = "INSERT INTO ITEMQUANTITY (ID, NAME, QUANTITY) VALUES (?, ?, ?)";
        String sql2 = "INSERT INTO ITEMUNIT (NAME, UNIT) VALUES (?, ?)";

        try {
            ItemHelper(id, name, quantity, unit, sql1, sql2);

            System.out.println("Data was inserted properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean deleteItem(int id) {
        String getItemNameSql = "SELECT NAME FROM ITEMQUANTITY WHERE ID = ?";
        String deleteItemQuantitySql = "DELETE FROM ITEMQUANTITY WHERE ID = ?";
        String deleteItemUnitSql = "DELETE FROM ITEMUNIT WHERE NAME = ?";

        try {
            // Get the item name based on ID
            PreparedStatement getItemNameStatement = connection.prepareStatement(getItemNameSql);
            getItemNameStatement.setInt(1, id);
            ResultSet resultSet = getItemNameStatement.executeQuery();

            String itemName = null;

            if (resultSet.next()) {
                itemName = resultSet.getString("NAME");

                // Delete from ITEMQUANTITY
                PreparedStatement deleteQuantityStatement = connection.prepareStatement(deleteItemQuantitySql);
                deleteQuantityStatement.setInt(1, id);
                deleteQuantityStatement.executeUpdate();
                deleteQuantityStatement.close();

                // Delete from ITEMUNIT
                PreparedStatement deleteUnitStatement = connection.prepareStatement(deleteItemUnitSql);
                deleteUnitStatement.setString(1, itemName);
                deleteUnitStatement.executeUpdate();
                deleteUnitStatement.close();

                System.out.println("Data was deleted properly");
                return true;
            } else {
                System.out.println("Item not found with ID: " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Data was not deleted properly");
            e.printStackTrace(); // Log the exception for debugging
            return false;
        }
    }

    public boolean updateItem(int id, String name, int quantity, String unit) {
        String sql1 = "UPDATE ITEMQUANTITY SET QUANTITY = ?, NAME = ? WHERE ID = ?";
        String sql2 = "UPDATE ITEMUNIT SET UNIT = ? WHERE NAME = ?";

        try {
            ItemHelper(id, name, quantity, unit, sql1, sql2);

            System.out.println("Data was inserted properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean listItems() {
        String sql = "SELECT iq.ID, iq.NAME, iq.QUANTITY, iu.UNIT " +
                "FROM ITEMQUANTITY iq " +
                "JOIN ITEMUNIT iu ON iq.NAME = iu.NAME";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("NAME");
                int quantity = resultSet.getInt("QUANTITY");
                String unit = resultSet.getString("UNIT");

                System.out.println("ID: " + id + ", NAME: " + name + ", QUANTITY: " + quantity + ", UNIT: " + unit);
            }
            return true;

        } catch (SQLException e) {
            return false;
        }
    }


    private static void ItemHelper(int id, String name, int quantity, String unit, String sql1, String sql2) throws SQLException {
        PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
        PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

        // query argument setting for statement 1
        preparedStatement1.setInt(1, id);
        preparedStatement1.setString(2, name);
        preparedStatement1.setInt(3, quantity);

        // query argument setting for statement 2
        preparedStatement2.setString(1, name);
        preparedStatement2.setString(2, unit);

        preparedStatement1.executeUpdate();
        preparedStatement2.executeUpdate();

        preparedStatement1.close();
        preparedStatement2.close();
    }

    public boolean insertWaterTank(int id, String name, float volume, float temperature, String lighting_level, int exhibit_id, float pH) {
        String sql1 = "INSERT INTO WATERTANKLOGISTICS (ID, WATER_TANK_LOGISTICS_NAME, VOLUME, TEMPERATURE, LIGHTINGLEVEL, EXHIBIT_ID) VALUES (?, ?, ?, ?, ?, ?)";
        String sql2 = "INSERT INTO WATERTANKPH (TEMPERATURE, PH) VALUES (?, ?)";

        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

            // query argument setting for statement 1
            preparedStatement1.setInt(1, id);
            preparedStatement1.setString(2, name);
            preparedStatement1.setFloat(3, volume);
            preparedStatement1.setFloat(4, temperature);
            preparedStatement1.setString(5, lighting_level);
            preparedStatement1.setInt(6, exhibit_id);

            // query argument setting for statement 2
            preparedStatement2.setFloat(1, temperature);
            preparedStatement2.setFloat(2, pH);

            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();

            System.out.println("Data was inserted properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean deleteWaterTank(int id) {
        String getWaterTankTempSql = "SELECT TEMPERATURE FROM WATERTANKLOGISTICS WHERE ID = ?";
        String deleteWaterTankLogisticsSql = "DELETE FROM WATERTANKLOGISTICS WHERE ID = ?";
        String deleteWaterTankpHSql = "DELETE FROM WATERTANKPH WHERE TEMPERATURE = ?";

        try {
            // gets the water tank temperature based on ID
            PreparedStatement getWaterTankTempStatement = connection.prepareStatement(getWaterTankTempSql);
            getWaterTankTempStatement.setInt(1, id);
            ResultSet resultSet = getWaterTankTempStatement.executeQuery();

            float waterTankTemperature;

            if (resultSet.next()) {
                waterTankTemperature = resultSet.getFloat("TEMPERATURE");

                // Delete from WATERTANKLOGISTICS
                PreparedStatement deleteQuantityStatement = connection.prepareStatement(deleteWaterTankLogisticsSql);
                deleteQuantityStatement.setInt(1, id);
                deleteQuantityStatement.executeUpdate();
                deleteQuantityStatement.close();

                // Delete from WATERTANKPH
                PreparedStatement deleteUnitStatement = connection.prepareStatement(deleteWaterTankpHSql);
                deleteUnitStatement.setFloat(1, waterTankTemperature);
                deleteUnitStatement.executeUpdate();
                deleteUnitStatement.close();

                System.out.println("Data was deleted properly");
                return true;
            } else {
                System.out.println("Item not found with ID: " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Data was not deleted properly");
            return false;
        }
    }

    public boolean updateWaterTank(int id, String name, float volume, float temperature, String lighting_level, int exhibit_id, float pH) {
        String sql1 = "UPDATE WATERTANKLOGISTICS SET WATER_TANK_LOGISTICS_NAME = ?, VOLUME = ?, " +
                "TEMPERATURE = ?, LIGHTINGLEVEL = ?, EXHIBIT_ID = ? WHERE ID = ?";
        String sql2 = "UPDATE WATERTANKPH SET PH = ? WHERE TEMPERATURE = ?";

        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

            // query argument setting for statement 1
            preparedStatement1.setString(1, name);
            preparedStatement1.setFloat(2, volume);
            preparedStatement1.setFloat(3, temperature);
            preparedStatement1.setString(4, lighting_level);
            preparedStatement1.setInt(5, exhibit_id);
            preparedStatement1.setInt(6, id);

            // query argument setting for statement 2
            preparedStatement2.setFloat(1, pH);
            preparedStatement2.setFloat(2, temperature);

            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();

            preparedStatement1.close();
            preparedStatement2.close();

            System.out.println("Data was inserted properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean listWaterTank() {
        String sql = "SELECT wl.ID, wl.WATER_TANK_LOGISTICS_NAME, wl.VOLUME, wl.TEMPERATURE, wp.PH, wl.LIGHTINGLEVEL, wl.EXHIBIT_ID " +
                "FROM WATERTANKLOGISTICS wl " +
                "JOIN WATERTANKPH wp ON wl.TEMPERATURE = wp.TEMPERATURE";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("WATER_TANK_LOGISTICS_NAME");
                float volume = resultSet.getFloat("VOLUME");
                float temperature = resultSet.getFloat("TEMPERATURE");
                float pH = resultSet.getFloat("PH");
                String lighting_level = resultSet.getString("LIGHTINGLEVEL");
                int exhibit_id = resultSet.getInt("EXHIBIT_ID");


                System.out.println("ID: " + id + ", NAME: " + name + ", VOLUME: " + volume + ", TEMPERATURE: " + temperature +
                        ", PH: " + pH + ", LIGHTINGLEVEL: " + lighting_level + ", EXHIBIT_ID: " + exhibit_id);
            }
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean insertAnimal(int id, String name, String species, int age, String living_temp, int waterTankID, int veterinarianID) {
        String sql = "INSERT INTO ANIMAL (ID, ANIMAL_NAME, SPECIES, AGE, LIVINGTEMP, WATER_TANK_ID, VETERINARIAN_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, species);
            preparedStatement.setInt(4, age);
            preparedStatement.setString(5, living_temp);
            preparedStatement.setInt(6, waterTankID);
            preparedStatement.setInt(7, veterinarianID);


            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Data inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean insertExhibit(int id, String name, String status) {
        String sql = "INSERT INTO EXHIBIT (ID, EXHIBIT_NAME, EXHIBIT_STATUS) VALUES (?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, status);

            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Data inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean updateAnimal(int id, String name, String species, int age, String living_temp, int waterTankID, int veterinarianID) {
        String sql = "UPDATE ANIMAL SET ANIMAL_NAME = ?, SPECIES = ?, AGE = ?, LIVINGTEMP = ?, WATER_TANK_ID = ?, VETERINARIAN_ID = ? WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            // query argument setting for statement 1
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, species);
            preparedStatement.setInt(3, age);
            preparedStatement.setString(4, living_temp);
            preparedStatement.setInt(5, waterTankID);
            preparedStatement.setInt(6, veterinarianID);
            preparedStatement.setInt(7, id);

            preparedStatement.executeUpdate();

            preparedStatement.close();

            System.out.println("Data updated successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean updateExhibit(int id, String name, String status) {
        String sql = "UPDATE EXHIBIT SET EXHIBIT_STATUS = ?, EXHIBIT_STATUS = ? WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            // query argument setting for statement 1
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, status);
            preparedStatement.setInt(3, id);

            preparedStatement.executeUpdate();

            preparedStatement.close();

            System.out.println("Data updated successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not inserted properly");
            return false;
        }
    }

    public boolean deleteAnimal(int id) {
        String sql = "DELETE FROM ANIMAL WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            System.out.println("Data deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not deleted properly");
            return false;
        }
    }

    public boolean deleteExhibit(int id) {
        String sql = "DELETE FROM EXHIBIT WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            System.out.println("Data deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not deleted properly");
            return false;
        }
    }

    public boolean listAnimal() {
        String sql = "SELECT * FROM ANIMAL";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet inventoryResult = preparedStatement.executeQuery();

            while (inventoryResult.next()) {
                int id = inventoryResult .getInt("ID");
                String name = inventoryResult.getString("ANIMAL_NAME");
                String species = inventoryResult .getString("SPECIES");
                int age = inventoryResult.getInt("AGE");
                String living_temp = inventoryResult .getString("LIVINGTEMP");
                int waterTankID = inventoryResult.getInt("WATER_TANK_ID");
                int veterinarianID = inventoryResult.getInt("VETERINARIAN_ID");

                System.out.println("ID: " + id + ", Name: " + name + ", Species: " + species + ", Age: " + age + ", Living Temperature: " + living_temp
                        + ", Water Tank ID: " + waterTankID + ", Veterinarian ID: " + veterinarianID);
            }

            System.out.println("Data was listed successfully");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not listed properly");
            return false;
        }
    }

    public boolean listExhibit() {
        String sql = "SELECT * FROM EXHIBIT";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet inventoryResult = preparedStatement.executeQuery();

            while (inventoryResult.next()) {
                int id = inventoryResult .getInt("ID");
                String name = inventoryResult.getString("EXHIBIT_NAME");
                String status = inventoryResult.getString("EXHIBIT_STATUS");

                System.out.println("ID: " + id + ", Name: " + name + ", Status: " + status);
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

    public boolean listStaff() {
        String sql = "SELECT id, salary, staff_name, datehired FROM Staff";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                float salary = resultSet.getFloat("salary");
                String staffName = resultSet.getString("staff_name");
                String dateHired = resultSet.getString("datehired");

                System.out.println("ID: " + id + ", Salary: " + salary + ", Staff Name: " + staffName + ", Date Hired: " + dateHired);
            }

            System.out.println("Staff data was listed successfully");
            return true;

        } catch (SQLException e) {
            System.out.println("Staff data was not listed properly: " + e.getMessage());
            return false;
        }
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

    public boolean listPlants() {
        String sql = "SELECT plant_id, species, living_temp, living_light, water_tank_id FROM Grown_In_Plant";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int plantId = resultSet.getInt("plant_id");
                String species = resultSet.getString("species");
                float livingTemp = resultSet.getFloat("living_temp");
                float livingLight = resultSet.getFloat("living_light");
                int waterTankId = resultSet.getInt("water_tank_id");

                System.out.println("Plant ID: " + plantId + ", Species: " + species +
                        ", Living Temp: " + livingTemp + ", Living Light: " +
                        livingLight + ", Water Tank ID: " + waterTankId);
            }

            System.out.println("Plant data was listed successfully");
            return true;

        } catch (SQLException e) {
            System.out.println("Plant data was not listed properly: " + e.getMessage());
            return false;
        }
    }





    public boolean insertVendor(int id, String vendorName, String marketRating, String address) {
        String sql1 = "INSERT INTO VendorReputation (vendor_name, vendor_market_rating) VALUES (?, ?)";
        String sql2 = "INSERT INTO VendorLogistics (id, vendor_logistics_name, address) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            // Insert into VendorReputation
            try (PreparedStatement preparedStatement1 = connection.prepareStatement(sql1)) {
                preparedStatement1.setString(1, vendorName);
                preparedStatement1.setString(2, marketRating);
                preparedStatement1.executeUpdate();
            }

            // Insert into VendorLogistics
            try (PreparedStatement preparedStatement2 = connection.prepareStatement(sql2)) {
                preparedStatement2.setInt(1, id);
                preparedStatement2.setString(2, vendorName);
                preparedStatement2.setString(3, address);
                preparedStatement2.executeUpdate();
            }

            connection.commit(); // Commit transaction
            System.out.println("Vendor data inserted successfully.");
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction in case of an error
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
            System.out.println("Vendor data was not inserted properly: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true); // Reset to default behavior
            } catch (SQLException ex) {
                System.out.println("Error resetting auto-commit: " + ex.getMessage());
            }
        }
    }

    public boolean updateVendor(int id, String vendorName, String marketRating, String address) {
        String sql1 = "UPDATE VendorReputation SET vendor_market_rating = ? WHERE vendor_name = ?";
        String sql2 = "UPDATE VendorLogistics SET address = ? WHERE id = ? AND vendor_logistics_name = ?";

        try {
            connection.setAutoCommit(false); // Start transaction

            // Update VendorReputation
            try (PreparedStatement preparedStatement1 = connection.prepareStatement(sql1)) {
                preparedStatement1.setString(1, marketRating);
                preparedStatement1.setString(2, vendorName);
                int rowsAffected1 = preparedStatement1.executeUpdate();
                if (rowsAffected1 == 0) {
                    System.out.println("No such vendor exists to update.");
                    connection.rollback(); // Rollback if no update occurs
                    return false;
                }
            }

            // Update VendorLogistics
            try (PreparedStatement preparedStatement2 = connection.prepareStatement(sql2)) {
                preparedStatement2.setString(1, address);
                preparedStatement2.setInt(2, id);
                preparedStatement2.setString(3, vendorName);
                int rowsAffected2 = preparedStatement2.executeUpdate();
                if (rowsAffected2 == 0) {
                    System.out.println("No such vendor logistics entry exists to update.");
                    connection.rollback(); // Rollback if no update occurs
                    return false;
                }
            }

            connection.commit(); // Commit transaction
            System.out.println("Vendor data updated successfully.");
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction in case of an error
                System.out.println("Vendor data was not updated properly: " + e.getMessage());
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true); // Reset to default behavior
            } catch (SQLException ex) {
                System.out.println("Error resetting auto-commit: " + ex.getMessage());
            }
        }
    }


    public boolean deleteVendor(String vendorName) {
        String deleteVendorLogisticsSql = "DELETE FROM VendorLogistics WHERE vendor_logistics_name = ?";
        String deleteVendorReputationSql = "DELETE FROM VendorReputation WHERE vendor_name = ?";

        try {
            connection.setAutoCommit(false); // Start transaction

            // Delete from VendorLogistics
            try (PreparedStatement deleteLogisticsStatement = connection.prepareStatement(deleteVendorLogisticsSql)) {
                deleteLogisticsStatement.setString(1, vendorName);
                deleteLogisticsStatement.executeUpdate();
            }

            // Delete from VendorReputation
            try (PreparedStatement deleteReputationStatement = connection.prepareStatement(deleteVendorReputationSql)) {
                deleteReputationStatement.setString(1, vendorName);
                int rowsAffected = deleteReputationStatement.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit(); // Commit transaction if deletion is successful
                    System.out.println("Vendor entry deleted successfully.");
                    return true;
                } else {
                    System.out.println("No such vendor entry exists.");
                    connection.rollback(); // Rollback if no entry is deleted
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction in case of an error
                System.out.println("Error during vendor deletion: " + e.getMessage());
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true); // Reset to default behavior
            } catch (SQLException ex) {
                System.out.println("Error resetting auto-commit: " + ex.getMessage());
            }
        }
    }

    public boolean listVendors() {
        String sql = "SELECT vl.ID, vr.vendor_name, vr.vendor_market_rating, vl.address " +
                "FROM VendorReputation vr " +
                "JOIN VendorLogistics vl ON vr.vendor_name = vl.vendor_logistics_name";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String vendorName = resultSet.getString("vendor_name");
                String marketRating = resultSet.getString("vendor_market_rating");
                String address = resultSet.getString("address");

                System.out.println("ID: " + id + ", Vendor Name: " + vendorName + ", Market Rating: " + marketRating +
                        ", Address: " + address);
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error retrieving vendor list: " + e.getMessage());
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