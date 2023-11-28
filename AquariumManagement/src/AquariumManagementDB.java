package AquariumManagement.src;

import org.json.JSONArray;
import org.json.JSONObject;

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
            connection.setAutoCommit(false);
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
    // In this relation, I am accounting for the entities: INVENTORY and SHELF
    public boolean insertInventory(int id, String location, int shelfNumber, String isFull) {
        String sql = "INSERT INTO INVENTORY (ID, LOCATION) VALUES (?, ?)";
        String sql2 = "INSERT INTO SHELFININVENTORY(shelf_number, inventory_id, is_full) VALUES (?, ?, ?)";

        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

            preparedStatement1.setInt(1, id);
            preparedStatement1.setString(2, location);


            preparedStatement2.setInt(1, shelfNumber);
            preparedStatement2.setInt(2, id);
            preparedStatement2.setString(3, isFull);

            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();

            connection.commit();

            preparedStatement1.close();
            preparedStatement2.close();

            System.out.println("Data from INVENTORY inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from INVENTORY was not inserted properly");
            rollbackConnection();
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

    public boolean updateInventory(int id, String location, int shelfNumber, String isFull) {
        String sql = "UPDATE INVENTORY SET LOCATION = ? WHERE ID = ?";
        String sql2 = "UPDATE SHELFININVENTORY SET IS_FULL = ? WHERE SHELF_NUMBER = ? AND INVENTORY_ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, location);
            preparedStatement.setInt(2, id);

            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

            preparedStatement2.setString(1, isFull);
            preparedStatement2.setInt(2, shelfNumber);
            preparedStatement2.setInt(3, id);

            preparedStatement.executeUpdate();
            preparedStatement2.executeUpdate();

            connection.commit();

            preparedStatement.close();
            preparedStatement2.close();

            System.out.println("Data from INVENTORY updated successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from INVENTORY was not updated properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean deleteInventory(int id, int shelfNumber) {
        String sql = "DELETE FROM INVENTORY WHERE ID = ?";
        String sql2 = "DELETE FROM SHELFININVENTORY WHERE ID = ? AND SHELF_NUMBER = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

            preparedStatement.setInt(1, id);


            preparedStatement2.setInt(1, id);
            preparedStatement2.setInt(2, shelfNumber);

            preparedStatement.executeUpdate();
            preparedStatement2.executeUpdate();

            connection.commit();

            preparedStatement.close();
            preparedStatement2.close();

            System.out.println("Data from INVENTORY deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from INVENTORY was not deleted properly");
            rollbackConnection();
            return false;
        }
    }

    public String listInventory() {
        String sql = "SELECT i.ID, i.LOCATION, s.SHELF_NUMBER, s.IS_FULL " +
                "FROM INVENTORY i " +
                "JOIN SHELFININVENTORY s ON i.ID = s.INVENTORY_ID";
        JSONArray inventoryJSONArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet inventoryResult = preparedStatement.executeQuery();

            while (inventoryResult.next()) {
                int id = inventoryResult.getInt("ID");
                String location = inventoryResult.getString("LOCATION");
                int shelf_number = inventoryResult.getInt("SHELF_NUMBER");
                int is_full = inventoryResult.getInt("IS_FULL");

                JSONObject inventoryItem = new JSONObject();
                inventoryItem.put("ID", id);
                inventoryItem.put("LOCATION", location);
                inventoryItem.put("SHELF_NUMBER", shelf_number);
                inventoryItem.put("IS_FULL", is_full);

                inventoryJSONArray.put(inventoryItem);

                System.out.println("ID: " + id + ", Location: " + location +
                        ", Shelf Number: " + shelf_number + ", Is Full: " + is_full);
            }

            inventoryResult.close();

            System.out.println("Data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data was not listed properly");
        }

        if (inventoryJSONArray.isEmpty()) {
            return null;
        }
        return inventoryJSONArray.toString();
    }

    public JSONObject getInventoryByID(int itemID) {
        String sql = "SELECT i.ID, i.LOCATION, s.SHELF_NUMBER, s.IS_FULL " +
                "FROM INVENTORY i " +
                "JOIN SHELFININVENTORY s ON i.ID = s.INVENTORY_ID " +
                "WHERE i.ID = ?";
        JSONObject inventoryItem = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, itemID);
            ResultSet inventoryResult = preparedStatement.executeQuery();

            while (inventoryResult.next()) {
                int id = inventoryResult.getInt("ID");
                String location = inventoryResult.getString("LOCATION");

                inventoryItem.put("ID", id);
                inventoryItem.put("LOCATION", location);

                System.out.println("ID: " + id + ", Location: " + location);
            }

            inventoryResult.close();

            System.out.println("Data from INVENTORY was retrieved successfully");

        } catch (SQLException e) {
            System.out.println("Data from INVENTORY was not retrieved properly");
        }

        if (inventoryItem.isEmpty()) {
            return null;
        }
        return inventoryItem;

    }
    // THIS COVERS THE ENTITIES ITEM (ITEMQUANTITY AND ITEMUNIT) AND THE RELATION SUPPLY
    public boolean insertItem(int id, String name, int quantity, String unit, int vendorID) {
        String sql1 = "INSERT INTO ITEMQUANTITY (ID, NAME, QUANTITY) VALUES (?, ?, ?)";
        String sql2 = "INSERT INTO ITEMUNIT (NAME, UNIT) VALUES (?, ?)";
        String sql3 = "INSERT INTO SUPPLY (ItemID, VendorID) VALUES (?, ?)";


        try {

            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);

            // query argument setting for statement 1
            preparedStatement1.setInt(1, id);
            preparedStatement1.setString(2, name);
            preparedStatement1.setInt(3, quantity);

            // query argument setting for statement 2
            preparedStatement2.setString(1, name);
            preparedStatement2.setString(2, unit);

            // query argument setting for statement 3
            preparedStatement3.setInt(1, id);
            preparedStatement3.setInt(2, vendorID);

            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            preparedStatement3.executeUpdate();

            connection.commit();

            preparedStatement1.close();
            preparedStatement2.close();
            preparedStatement3.close();

            System.out.println("Data from ITEM was inserted properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data from ITEM was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean deleteItem(int id, int vendorID) {
        String getItemNameSql = "SELECT NAME FROM ITEMQUANTITY WHERE ID = ?";
        String deleteItemQuantitySql = "DELETE FROM ITEMQUANTITY WHERE ID = ?";
        String deleteSupplySql = "DELETE FROM SUPPLY WHERE ITEMID = ? AND VENDORID = ?";
        String deleteItemUnitSql = "DELETE FROM ITEMUNIT WHERE NAME = ?";

        try (PreparedStatement getItemNameStatement = connection.prepareStatement(getItemNameSql)) {
            getItemNameStatement.setInt(1, id);

            try (ResultSet resultSet = getItemNameStatement.executeQuery()) {
                String itemName = "";

                if (resultSet.next()) {
                    itemName = resultSet.getString("NAME");

                    try (PreparedStatement deleteQuantityStatement = connection.prepareStatement(deleteItemQuantitySql);
                         PreparedStatement deleteUnitStatement = connection.prepareStatement(deleteItemUnitSql);
                         PreparedStatement deleteSupplyStatement = connection.prepareStatement(deleteSupplySql)) {

                        // Delete from ITEMQUANTITY
                        deleteQuantityStatement.setInt(1, id);
                        deleteQuantityStatement.executeUpdate();

                        // Delete from ITEMUNIT
                        deleteUnitStatement.setString(1, itemName);
                        deleteUnitStatement.executeUpdate();

                        // DELETE FROM SUPPLY
                        deleteSupplyStatement.setInt(1, id);
                        deleteSupplyStatement.setInt(2, vendorID);
                        deleteSupplyStatement.executeUpdate();
                    }

                    System.out.println("Data from ITEM and SUPPLY was deleted properly");
                    return true;
                } else {
                    System.out.println("Item not found with ID: " + id);
                    rollbackConnection();
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Data from ITEM was not deleted properly");
            rollbackConnection();
            return false;
        } finally {
            try {
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Issue with committing transaction: " + e.getMessage());
            }
        }
    }


    // DISCUSS IF SUPPLY CAN BE UPDATED (DESIGN DECISION)
    // I THINK SINCE ITS A MANY-TO-MANY THIS IS UNNECESSARY (ADD AND DELETE SHOULD BE ENOUGH)
    public boolean updateItem(int id, String name, int quantity, String unit) {
        String sql1 = "UPDATE ITEMQUANTITY SET QUANTITY = ?, NAME = ? WHERE ID = ?";
        String sql2 = "UPDATE ITEMUNIT SET UNIT = ? WHERE NAME = ?";

        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

            // query argument setting for statement 1
            preparedStatement1.setInt(1, quantity);
            preparedStatement1.setString(2, name);
            preparedStatement1.setInt(3, id);

            // query argument setting for statement 2
            preparedStatement2.setString(1, unit);
            preparedStatement2.setString(2, name);

            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();

            connection.commit();

            preparedStatement1.close();
            preparedStatement2.close();

            System.out.println("Data from ITEM was inserted properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data from ITEM was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    public String listItems() {
        String sql = "SELECT iq.ID, iq.NAME, iq.QUANTITY, iu.UNIT, s.VENDORID " +
                "FROM ITEMQUANTITY iq " +
                "JOIN ITEMUNIT iu ON iq.NAME = iu.NAME " +
                "JOIN SUPPLY s on iq.ID = s.ITEMID";

        JSONArray itemsJSONArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("NAME");
                int quantity = resultSet.getInt("QUANTITY");
                String unit = resultSet.getString("UNIT");
                String vendorID = resultSet.getString("VENDORID");

                JSONObject item = new JSONObject();
                item.put("ID", id);
                item.put("NAME", name);
                item.put("QUANTITY", quantity);
                item.put("UNIT", unit);
                item.put("VENDORID", vendorID);

                itemsJSONArray.put(item);

                System.out.println("ID: " + id + ", NAME: " + name + ", QUANTITY: " + quantity + ", UNIT: " + unit
                        + ", VENDORID: " + vendorID);

            }

            resultSet.close();

            System.out.println("Data FROM ITEM was listed successfully");

            } catch (SQLException e) {
                System.out.println("Data FROM ITEM was not listed properly");
            }

            if (itemsJSONArray.isEmpty()) {
                return null;
            }
            return itemsJSONArray.toString();
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

    // THIS COVERS THE ENTITIES WATERTANK (WATERTANKLOGISTICS AND WATERTANKPH) AND RELATION MAINTAINS (SEPARATE ENTITY) AND PARTOF
    public boolean insertWaterTank(int id, String name, float volume, float temperature, String lighting_level, int exhibit_id, float pH, int aquarist_id) {
        String sql1 = "INSERT INTO WATERTANKLOGISTICS (ID, WATER_TANK_LOGISTICS_NAME, VOLUME, TEMPERATURE, LIGHTINGLEVEL, EXHIBIT_ID) VALUES (?, ?, ?, ?, ?, ?)";
        String sql2 = "INSERT INTO WATERTANKPH (TEMPERATURE, PH) VALUES (?, ?)";
        String sql3 = "INSERT INTO AQUARIST_MAINTAIN_WATERTANK (AQUARIST_ID, WATERTANK_ID) VALUES (?, ?)";

        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);

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

            // query argument setting for statement 3
            preparedStatement3.setInt(1, aquarist_id);
            preparedStatement3.setInt(2, id);

            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            preparedStatement3.executeUpdate();

            connection.commit();

            preparedStatement1.close();
            preparedStatement2.close();
            preparedStatement3.close();

            System.out.println("Data from WATERTANK was inserted properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data from WATERTANK was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean deleteWaterTank(int id) {
        String getWaterTankTempSql = "SELECT TEMPERATURE FROM WATERTANKLOGISTICS WHERE ID = ?";
        String deleteWaterTankLogisticsSql = "DELETE FROM WATERTANKLOGISTICS WHERE ID = ?";
        String deleteWaterTankpHSql = "DELETE FROM WATERTANKPH WHERE TEMPERATURE = ?";
        String maintainWaterTankSql = "DELETE FROM AQUARIST_MAINTAIN_WATERTANK WHERE WATER_TANK_ID = ?";

        try (PreparedStatement getWaterTankTempStatement = connection.prepareStatement(getWaterTankTempSql)) {
            getWaterTankTempStatement.setInt(1, id);

            try (ResultSet resultSet = getWaterTankTempStatement.executeQuery()) {
                float waterTankTemperature;

                if (resultSet.next()) {
                    waterTankTemperature = resultSet.getFloat("TEMPERATURE");

                    try (PreparedStatement deleteQuantityStatement = connection.prepareStatement(deleteWaterTankLogisticsSql);
                         PreparedStatement deleteUnitStatement = connection.prepareStatement(deleteWaterTankpHSql);
                         PreparedStatement deleteMaintainStatement = connection.prepareStatement(maintainWaterTankSql)) {

                        deleteQuantityStatement.setInt(1, id);
                        deleteUnitStatement.setFloat(1, waterTankTemperature);
                        deleteMaintainStatement.setInt(1, id);

                        deleteQuantityStatement.executeUpdate();
                        deleteUnitStatement.executeUpdate();
                        deleteMaintainStatement.executeUpdate();
                    }

                    System.out.println("Data from WATERTANK was deleted properly");
                    return true;
                } else {
                    System.out.println("Item not found with ID: " + id);
                    rollbackConnection();
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Data from WATERTANK was not deleted properly");
            rollbackConnection();
            return false;
        } finally {
            try {
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Unable to commit transaction: " + e.getMessage());
            }
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

            connection.commit();

            preparedStatement1.close();
            preparedStatement2.close();

            System.out.println("Data from WATERTANK was inserted properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data from WATERTANK was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    // UPDATE METHOD FOR MAINTAIN (MADE IT SEPARATE)
    public boolean updateMaintain(int id, int aquarist_id) {
        String sql = "UPDATE AQUARIST_MAINTAIN_WATERTANK SET AQUARIST_ID = ? WHERE WATER_TANK_ID = ?";

        try {
            PreparedStatement maintainStatement = connection.prepareStatement(sql);

            maintainStatement.setInt(1, aquarist_id);
            maintainStatement.setInt(2, id);

            maintainStatement.executeUpdate();

            connection.commit();

            maintainStatement.close();

            System.out.println("Data in MAINTAIN was updated properly");
            return true;
        } catch (SQLException e) {
            System.out.println("Data in MAINTAIN was not updated properly");
            rollbackConnection();
            return false;
        }
    }

    public String listWaterTank() {
        String sql = "SELECT wl.ID, wl.WATER_TANK_LOGISTICS_NAME, wl.VOLUME, wl.TEMPERATURE, wp.PH, wl.LIGHTINGLEVEL, wl.EXHIBIT_ID, m.AQUARIST_ID " +
                "FROM WATERTANKLOGISTICS wl " +
                "JOIN WATERTANKPH wp ON wl.TEMPERATURE = wp.TEMPERATURE " +
                "JOIN AQUARIST_MAINTAIN_WATERTANK m ON m.WATER_TANK_ID = wl.ID";

        JSONArray waterTankArray = new JSONArray();

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
                int aquarist_id = resultSet.getInt("AQUARIST_ID");

                JSONObject waterTank = new JSONObject();
                waterTank.put("ID", id);
                waterTank.put("NAME", name);
                waterTank.put("TEMPERATURE", temperature);
                waterTank.put("PH", pH);
                waterTank.put("LIGHTING_LEVEL", lighting_level);
                waterTank.put("EXHIBIT_ID", exhibit_id);
                waterTank.put("AQUARIST_ID", aquarist_id);

                waterTankArray.put(waterTank);


                System.out.println("ID: " + id + ", NAME: " + name + ", VOLUME: " + volume + ", TEMPERATURE: " + temperature +
                        ", PH: " + pH + ", LIGHTINGLEVEL: " + lighting_level + ", EXHIBIT_ID: " + exhibit_id
                + " , AQUARIST_ID: " + aquarist_id);
            }

            resultSet.close();

            System.out.println("Data from WATERTANK was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data from WATERTANK was not listed properly");
        }

        if (waterTankArray.isEmpty()) {
            return null;
        }
        return waterTankArray.toString();
    }

    // COVERS ENTITIES ANIMAL, FEED, EXHIBIT AND CLEAN (NEED TO FINISH CLEAN AND FEED)
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

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in ANIMAL inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in ANIMAL was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean insertFeed(int animal_id, int food_id, int aquarist_id, int quantity, String last_fed, String method) {
        return false;
    }

    public boolean insertExhibit(int id, String name, String status) {
        String sql = "INSERT INTO EXHIBIT (ID, EXHIBIT_NAME, EXHIBIT_STATUS) VALUES (?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, status);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in ANIMAL inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in ANIMAL was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean updateFeed(int animal_id, int food_id, int aquarist_id) {
        return false;
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

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in ANIMAL updated successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in ANIMAL was not inserted properly");
            rollbackConnection();
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

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in EXHIBIT updated successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in EXHIBIT was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    // DELETES FROM ANIMAL AND FEED (SINCE ANIMAL HAS TOTAL PARTICIPATION - DISCUSS)
    public boolean deleteAnimal(int id) {
        String sql = "DELETE FROM ANIMAL WHERE ID = ?";
        String sql2 = "DELETE FROM FEED WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

            preparedStatement.setInt(1, id);
            preparedStatement2.setInt(1, id);

            preparedStatement.executeUpdate();
            preparedStatement2.executeUpdate();

            connection.commit();

            preparedStatement.close();
            preparedStatement2.close();

            System.out.println("Data deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not deleted properly");
            rollbackConnection();
            return false;
        }
    }

    // DELETES FROM EXHIBIT AND CLEAN
    public boolean deleteExhibit(int id) {
        String sql = "DELETE FROM EXHIBIT WHERE ID = ?";
        String sql2 = "DELETE FROM CUSTODIAN_CLEAN_EXHIBIT_TABLE WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

            preparedStatement.setInt(1, id);
            preparedStatement2.setInt(1, id);

            preparedStatement.executeUpdate();
            preparedStatement2.executeUpdate();

            connection.commit();

            preparedStatement.close();
            preparedStatement2.close();

            System.out.println("Data deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data was not deleted properly");
            rollbackConnection();
            return false;
        }
    }

    // DISCUSS IF WE WANT TO DISPLAY FEED SEPARATELY OR AT ALL
    // FOR NOW RETURNS ALL FIELDS OF ANIMAL
    public String listAnimal() {
        String sql = "SELECT * FROM ANIMAL";

        JSONArray animalArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet animalResult = preparedStatement.executeQuery();

            while (animalResult.next()) {
                int id = animalResult.getInt("ID");
                String name = animalResult.getString("ANIMAL_NAME");
                String species = animalResult.getString("SPECIES");
                int age = animalResult.getInt("AGE");
                String living_temp = animalResult.getString("LIVINGTEMP");
                int waterTankID = animalResult.getInt("WATER_TANK_ID");
                int veterinarianID = animalResult.getInt("VETERINARIAN_ID");

                JSONObject animal = new JSONObject();
                animal.put("ID", id);
                animal.put("NAME", name);
                animal.put("SPECIES", species);
                animal.put("AGE", age);
                animal.put("LIVING_TEMP", living_temp);
                animal.put("WATER_TANK_ID", waterTankID);
                animal.put("VETERINARIAN_ID", veterinarianID);

                animalArray.put(animal);

                System.out.println("ID: " + id + ", Name: " + name + ", Species: " + species + ", Age: " + age + ", Living Temperature: " + living_temp
                        + ", Water Tank ID: " + waterTankID + ", Veterinarian ID: " + veterinarianID);
            }

            animalResult.close();

            System.out.println("Data from EXHIBIT was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data from EXHIBIT was not listed properly");
        }

        if (animalArray.isEmpty()) {
            return null;
        }

        return animalArray.toString();
    }

    public JSONObject getAnimalByID(int id) {
        String sql = "SELECT a.ID, a.ANIMAL_NAME, a.SPECIES, a.AGE, a.LIVINGTEMP, a.WATER_TANK_ID, a.VETERINARIAN_ID " +
                "FROM ANIMAL a " +
                "WHERE a.ID = ?";

        JSONObject animalItem = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet animalResult = preparedStatement.executeQuery();

            while (animalResult.next()) {
                int animal_id = animalResult.getInt("ID");
                String name = animalResult.getString("ANIMAL_NAME");
                String species = animalResult.getString("SPECIES");
                int age = animalResult.getInt("AGE");
                String living_temp = animalResult.getString("LIVINGTEMP");
                int waterTankID = animalResult.getInt("WATER_TANK_ID");
                int veterinarianID = animalResult.getInt("VETERINARIAN_ID");

                JSONObject animal = new JSONObject();
                animal.put("ID", animal_id);
                animal.put("NAME", name);
                animal.put("SPECIES", species);
                animal.put("AGE", age);
                animal.put("LIVING_TEMP", living_temp);
                animal.put("WATER_TANK_ID", waterTankID);
                animal.put("VETERINARIAN_ID", veterinarianID);

                System.out.println("ID: " + id + ", Name: " + name + ", Species: " + species + ", Age: " + age + ", Living Temperature: " + living_temp
                        + ", Water Tank ID: " + waterTankID + ", Veterinarian ID: " + veterinarianID);
            }
            animalResult.close();
            System.out.println("Data from EXHIBIT was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data from EXHIBIT was not listed properly");
        }

        if (animalItem.isEmpty()) {
            return null;
        }

        return animalItem;
    }

    // FOR THIS, SINCE ITS ONE EXTRA FIELD, I WILL ADD THE CUSTODIAN ID (in next commit)
    // IF NOT LET ME KNOW (NEED TO DISCUSS)
    public String listExhibit() {
        String sql = "SELECT * FROM EXHIBIT";

        JSONArray exhibitArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet exhibitResult = preparedStatement.executeQuery();

            while (exhibitResult.next()) {
                int id = exhibitResult.getInt("ID");
                String name = exhibitResult.getString("EXHIBIT_NAME");
                String status = exhibitResult.getString("EXHIBIT_STATUS");

                JSONObject exhibit = new JSONObject();
                exhibit.put("ID", id);
                exhibit.put("EXHIBIT_NAME", name);
                exhibit.put("EXHIBIT_STATUS", status);

                exhibitArray.put(exhibit);

                System.out.println("ID: " + id + ", Name: " + name + ", Status: " + status);
            }

            exhibitResult.close();

            System.out.println("Data from EXHIBIT was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data from EXHIBIT was not listed properly");
        }

        if (exhibitArray.isEmpty()) {
            return null;
        }

        return exhibitArray.toString();
    }

    // THIS getExhibitByID(int) function was giving error for line int id = exhibit... so I modified
    // the code and it is running now.
//    public String getExhibitByID(int id) {
//        String sql = "SELECT e.ID, e.EXHIBIT_NAME, e.EXHIBIT_STATUS " +
//                "FROM EXHIBIT e " +
//                "WHERE e.ID = ?";
//        JSONObject exhibitItem = new JSONObject();
//
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setInt(1, id);
//            ResultSet exhibitResult = preparedStatement.executeQuery();
//
//            while (exhibitResult.next()) {
//                int id = exhibitResult.getInt("ID");
//                String name = exhibitResult.getString("EXHIBIT_NAME");
//                String status = exhibitResult.getString("EXHIBIT_STATUS");
//
//                JSONObject exhibit = new JSONObject();
//                exhibit.put("ID", id);
//                exhibit.put("EXHIBIT_NAME", name);
//                exhibit.put("EXHIBIT_STATUS", status);
//
//                System.out.println("ID: " + id + ", Name: " + name + ", Status: " + status);
//            }
//
//            exhibitResult.close();
//
//            System.out.println("Data from EXHIBIT was retrived successfully");
//
//        } catch (SQLException e) {
//            System.out.println("Data from EXHIBIT was not retrived properly");
//        }
//
//        if (exhibitItem.isEmpty()) {
//            return null;
//        }
//        return exhibitItem.toString();
//    }

    public String getExhibitByID(int id) {
        String sql = "SELECT e.ID, e.EXHIBIT_NAME, e.EXHIBIT_STATUS " +
                "FROM EXHIBIT e " +
                "WHERE e.ID = ?";
        JSONObject exhibitItem = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet exhibitResult = preparedStatement.executeQuery();

            if (exhibitResult.next()) {
                String name = exhibitResult.getString("EXHIBIT_NAME");
                String status = exhibitResult.getString("EXHIBIT_STATUS");

                exhibitItem.put("ID", id);
                exhibitItem.put("EXHIBIT_NAME", name);
                exhibitItem.put("EXHIBIT_STATUS", status);

                System.out.println("ID: " + id + ", Name: " + name + ", Status: " + status);
            }

            exhibitResult.close();
            System.out.println("Data from EXHIBIT was retrieved successfully");

        } catch (SQLException e) {
            System.out.println("Data from EXHIBIT was not retrieved properly");
        }

        return exhibitItem.isEmpty() ? null : exhibitItem.toString();
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

    public String listStaff() {
        String sql = "SELECT id, salary, staff_name, datehired FROM Staff";
        JSONArray staffJSONArray = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                float salary = resultSet.getFloat("salary");
                String staffName = resultSet.getString("staff_name");
                String dateHired = resultSet.getString("datehired");

                JSONObject staffObject = new JSONObject();
                staffObject.put("ID", id);
                staffObject.put("Salary", salary);
                staffObject.put("Staff Name", staffName);
                staffObject.put("Date Hired", dateHired);

                staffJSONArray.put(staffObject);

                System.out.println("ID: " + id + ", Salary: " + salary + ", Staff Name: " + staffName + ", Date Hired: " + dateHired);
            }

            System.out.println("Staff data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Staff data was not listed properly: " + e.getMessage());
            return null;
        }

        return staffJSONArray.isEmpty() ? null : staffJSONArray.toString();
    }

    public String getStaffByID(int staffId) {
        String sql = "SELECT id, salary, staff_name, datehired FROM Staff WHERE id = ?";
        JSONObject staffObject = new JSONObject();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, staffId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    staffObject.put("ID", resultSet.getInt("id"));
                    staffObject.put("Salary", resultSet.getFloat("salary"));
                    staffObject.put("Staff Name", resultSet.getString("staff_name"));
                    staffObject.put("Date Hired", resultSet.getString("datehired"));

                    System.out.println("ID: " + staffObject.getInt("ID") +
                            ", Salary: " + staffObject.getFloat("Salary") +
                            ", Staff Name: " + staffObject.getString("Staff Name") +
                            ", Date Hired: " + staffObject.getString("Date Hired"));
                } else {
                    System.out.println("No staff member found with ID: " + staffId);
                    return null;
                }
            }

            System.out.println("Data for Staff ID " + staffId + " was retrieved successfully");

        } catch (SQLException e) {
            System.out.println("Data for Staff ID " + staffId + " was not retrieved properly: " + e.getMessage());
            return null;
        }

        return staffObject.toString();
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
    public JSONArray listPlants() {
        String sql = "SELECT plant_id, species, living_temp, living_light, water_tank_id FROM Grown_In_Plant";
        JSONArray plantsJSONArray = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int plantId = resultSet.getInt("plant_id");
                String species = resultSet.getString("species");
                float livingTemp = resultSet.getFloat("living_temp");
                float livingLight = resultSet.getFloat("living_light");
                int waterTankId = resultSet.getInt("water_tank_id");

                JSONObject plantObject = new JSONObject();
                plantObject.put("Plant ID", plantId);
                plantObject.put("Species", species);
                plantObject.put("Living Temp", livingTemp);
                plantObject.put("Living Light", livingLight);
                plantObject.put("Water Tank ID", waterTankId);

                plantsJSONArray.put(plantObject);

                System.out.println("Plant ID: " + plantId + ", Species: " + species +
                        ", Living Temp: " + livingTemp + ", Living Light: " +
                        livingLight + ", Water Tank ID: " + waterTankId);
            }

            System.out.println("Plant data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Plant data was not listed properly: " + e.getMessage());
            return null;
        }

        return plantsJSONArray.isEmpty() ? null : plantsJSONArray;
    }

    public JSONObject getPlantByID(int plantId) {
        String sql = "SELECT plant_id, species, living_temp, living_light, water_tank_id FROM Grown_In_Plant WHERE plant_id = ?";
        JSONObject plantObject = new JSONObject();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, plantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    plantObject.put("Plant_ID", resultSet.getInt("plant_id"));
                    plantObject.put("Species", resultSet.getString("species"));
                    plantObject.put("Living_Temp", resultSet.getFloat("living_temp"));
                    plantObject.put("Living_Light", resultSet.getFloat("living_light"));
                    plantObject.put("Water_Tank_ID", resultSet.getInt("water_tank_id"));

                    System.out.println("Plant_ID: " + plantId +
                            ", Species: " + plantObject.getString("Species") +
                            ", Living Temp: " + plantObject.getFloat("Living_Temp") +
                            ", Living Light: " + plantObject.getFloat("Living_Light") +
                            ", Water Tank ID: " + plantObject.getInt("Water_Tank_ID"));
                } else {
                    System.out.println("No plant found with ID: " + plantId);
                    return null;
                }
            }

            System.out.println("Data for Plant ID " + plantId + " was retrieved successfully");

        } catch (SQLException e) {
            System.out.println("Data for Plant ID " + plantId + " was not retrieved properly: " + e.getMessage());
            return null;
        }

        return plantObject;
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

            connection.commit();
            System.out.println("Vendor data inserted successfully.");
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
            System.out.println("Vendor data was not inserted properly: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
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

    public String listVendors() {
        String sql = "SELECT vl.ID, vr.vendor_name, vr.vendor_market_rating, vl.address " +
                "FROM VendorReputation vr " +
                "JOIN VendorLogistics vl ON vr.vendor_name = vl.vendor_logistics_name";
        JSONArray vendorsArray = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String vendorName = resultSet.getString("vendor_name");
                String marketRating = resultSet.getString("vendor_market_rating");
                String address = resultSet.getString("address");

                JSONObject vendorObject = new JSONObject();
                vendorObject.put("ID", id);
                vendorObject.put("Vendor Name", vendorName);
                vendorObject.put("Market Rating", marketRating);
                vendorObject.put("Address", address);

                vendorsArray.put(vendorObject);

                System.out.println("ID: " + id + ", Vendor Name: " + vendorName + ", Market Rating: " + marketRating +
                        ", Address: " + address);
            }

            System.out.println("Vendor data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Error retrieving vendor list: " + e.getMessage());
            return null;
        }

        return vendorsArray.isEmpty() ? null : vendorsArray.toString();
    }

    // Source: https://github.students.cs.ubc.ca/CPSC304/CPSC304_Java_Project
    private void rollbackConnection() {
        try  {
            connection.rollback();
        } catch (SQLException e) {
            System.out.println("ROLLBACK ERROR" + " " + e.getMessage());
        }
    }







}