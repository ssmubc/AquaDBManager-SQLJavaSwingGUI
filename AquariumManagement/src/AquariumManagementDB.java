package AquariumManagement.src;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
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

    // retrieves data from any entity
    // Source: https://www.javatpoint.com/iterate-json-array-java
    // Source: https://www.javatpoint.com/java-stringbuilder-append-method
    public JSONArray getRawData(JSONObject relationObj) {
        JSONArray dataArray = new JSONArray();

        // return null if nothing passed
        if (relationObj.isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT ");

        String tableName = relationObj.getString("TableName");

        JSONArray fieldsArray = relationObj.getJSONArray("Fields");
        
        if (fieldsArray != null && !fieldsArray.isEmpty()) {
            for (int i = 0; i < fieldsArray.length(); i++) {
                String fieldName = fieldsArray.getString(i);
                if (i < fieldsArray.length() - 1) {
                    sql.append(fieldName).append(", ");
                } else {
                    sql.append(fieldName).append(" ");
                }
            }
        } else {
            sql.append("* ");
        }

        sql.append("FROM ").append(tableName);

        try {

            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                JSONObject tuple = new JSONObject();

                // retrieve fields and put it into the JSONObject
                // Source: https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    String fieldName = resultSet.getMetaData().getColumnName(i);
                    tuple.put(fieldName, resultSet.getObject(i));
                }

                dataArray.put(tuple);
            }

            resultSet.close();
            preparedStatement.close();

            System.out.println("Data was retrieved successfully");

        } catch (SQLException e) {
            System.out.println("Data failed while retrieving");
        }

        if (dataArray.isEmpty()) {
            return null;
        }

        return dataArray;
    }

    public String getRawDataTest(JSONObject relationObj) {
        JSONArray dataArray = new JSONArray();

        // return null if nothing passed
        if (relationObj.isEmpty()) {
            return "";
        }

        StringBuilder sql = new StringBuilder("SELECT ");

        String tableName = relationObj.getString("tableName");

        JSONArray fieldsArray = new JSONArray();

        if (relationObj.has(tableName)) {
            fieldsArray = relationObj.getJSONArray("Fields");
        }

        if (fieldsArray != null && !fieldsArray.isEmpty()) {
            for (int i = 0; i < fieldsArray.length(); i++) {
                String fieldName = fieldsArray.getString(i);
                if (i < fieldsArray.length() - 1) {
                    sql.append(fieldName).append(", ");
                } else {
                    sql.append(fieldName).append(" ");
                }
            }
        } else {
            sql.append("* ");
        }

        sql.append("FROM ").append(tableName);

        return sql.toString();

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

    public List<String> getAllTableNames() {
        List<String> tableNames = new ArrayList<>();
        if (connection != null) {
            try {
                DatabaseMetaData dbMetaData = connection.getMetaData();
                // Retrieve tables only from the current schema(user space?)
                String schemaPattern = connection.getSchema();
                System.out.println(schemaPattern);

                String[] types = {"TABLE"};
                ResultSet rs = dbMetaData.getTables(null, schemaPattern, "%", types);

                while (rs.next()) {
                    // The table name is in the third column
                    tableNames.add(rs.getString(3));
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return tableNames;
    }
    public List<String> getColumnNames(String tableName) {
        List<String> columnNames = new ArrayList<>();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            // Retrieve tables only from the current schema(user space?)
            String schemaPattern = connection.getSchema();
            ResultSet columns = metaData.getColumns(null, schemaPattern, tableName, null);
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

    // REMOVE SHELF_ON_INVENTORY SINCE ORACLE SUPPORTS ON DELETE CASCADE
    public boolean deleteInventory(int id, int shelfNumber) {
        String sql = "DELETE FROM INVENTORY WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data from INVENTORY deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from INVENTORY was not deleted properly");
            rollbackConnection();
            return false;
        }
    }

    public JSONArray listInventory() {

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
        return inventoryJSONArray;
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

    // REMOVE DELETESUPPLY SINCE ORACLE SUPPORTS ON DELETE CASCADE
    public boolean deleteItem(int id, int vendorID) {
        String getItemNameSql = "SELECT NAME FROM ITEMQUANTITY WHERE ID = ?";
        String deleteItemQuantitySql = "DELETE FROM ITEMQUANTITY WHERE ID = ?";
        String deleteItemUnitSql = "DELETE FROM ITEMUNIT WHERE NAME = ?";

        try (PreparedStatement getItemNameStatement = connection.prepareStatement(getItemNameSql)) {
            getItemNameStatement.setInt(1, id);

            try (ResultSet resultSet = getItemNameStatement.executeQuery()) {
                String itemName = "";

                if (resultSet.next()) {
                    itemName = resultSet.getString("NAME");

                    try (PreparedStatement deleteQuantityStatement = connection.prepareStatement(deleteItemQuantitySql);
                         PreparedStatement deleteUnitStatement = connection.prepareStatement(deleteItemUnitSql)) {

                        // Delete from ITEMQUANTITY
                        deleteQuantityStatement.setInt(1, id);

                        // Delete from ITEMUNIT
                        deleteUnitStatement.setString(1, itemName);

                        deleteQuantityStatement.executeUpdate();
                        deleteUnitStatement.executeUpdate();
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

    public JSONArray listItems() {
        String sql = "SELECT iq.ID, iq.NAME, iq.QUANTITY, iu.UNIT " +
                "FROM ITEMQUANTITY iq " +
                "JOIN ITEMUNIT iu ON iq.NAME = iu.NAME";

        JSONArray itemsJSONArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("NAME");
                int quantity = resultSet.getInt("QUANTITY");
                String unit = resultSet.getString("UNIT");

                JSONObject item = new JSONObject();
                item.put("ID", id);
                item.put("NAME", name);
                item.put("QUANTITY", quantity);
                item.put("UNIT", unit);

                itemsJSONArray.put(item);

                System.out.println("ID: " + id + ", NAME: " + name + ", QUANTITY: " + quantity + ", UNIT: " + unit
                        );

            }

            resultSet.close();

            System.out.println("Data FROM ITEM was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data FROM ITEM was not listed properly");
        }
            if (itemsJSONArray.isEmpty()) {
                return null;
            }
            return itemsJSONArray;
    }

    public boolean insertFood(int id, String exp_date, String food_type) {
        String sql = "INSERT INTO FOOD (ITEM_ID, EXP_DATE, FOOD_TYPE) VALUES (?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, exp_date);
            preparedStatement.setString(3, food_type);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data from FOOD inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from FOOD was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    // DONT REALLY NEED THIS SINCE DELETING tuple in ITEM will delete all foreign id references
    public boolean deleteFood(int id) {
        String sql = "DELETE FROM FOOD WHERE ITEM_ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data from FOOD deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from FOOD was not deleted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean updateFood(int id, String exp_date, String food_type) {
        String sql = "UPDATE FOOD SET EXP_DATE = ?, FOOD_TYPE = ? WHERE ITEM_ID = ?";

        try {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, exp_date);
            preparedStatement.setString(2, food_type);
            preparedStatement.setInt(3, id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data from FOOD UPDATED successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from FOOD was not updated properly");
            rollbackConnection();
            return false;
        }
    }

    public JSONArray listFood(int id) {
        String sql = "SELECT f.ITEM_ID, f.EXP_DATE, f.FOOD_TYPE " +
                "FROM FOOD f";

        JSONArray foodJSONArray = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()){

            while (resultSet.next()) {
                int food_id = resultSet.getInt("ITEM_ID");
                String exp_date = resultSet.getString("EXP_DATE");
                String food_type = resultSet.getString("FOOD_TYPE");

                JSONObject food = new JSONObject();
                food.put("ITEM_ID", food_id);
                food.put("EXP_DATE", exp_date);
                food.put("FOOD_TYPE", food_type);

                foodJSONArray.put(food);

                System.out.println("ITEM_ID: " + food_id + ", EXP_DATE: " + exp_date + ", FOOD_TYPE: " + food_type);

            }

            resultSet.close();

            System.out.println("Data FROM FOOD was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data FROM FOOD was not listed properly");
        }

        if (foodJSONArray.isEmpty()) {
            return null;
        }
        return foodJSONArray;
    }

    public JSONObject getFoodByID(int id) {
        String sql = "SELECT f.ITEM_ID, f.EXP_DATE, f.FOOD_TYPE " +
                "FROM FOOD f " +
                "WHERE f.ITEM_ID = ?";

        JSONObject food = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int food_id = resultSet.getInt("ITEM_ID");
                String exp_date = resultSet.getString("EXP_DATE");
                String food_type = resultSet.getString("FOOD_TYPE");

                food.put("ITEM_ID", food_id);
                food.put("EXP_DATE", exp_date);
                food.put("FOOD_TYPE", food_type);

                System.out.println("FOOD_ID: " + food_id + ", EXP_DATE: " + exp_date + ", FOOD_TYPE: " + food_type);

            }

            resultSet.close();

            System.out.println("Data FROM FOOD was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data FROM FOOD was not listed properly");
        }

        if (food.isEmpty()) {
            return null;
        }
        return food;
    }

    public boolean insertEquipment(int id, String equipment_function, float weight, String equipment_size, String date_installed) {
        String sql = "INSERT INTO EQUIPMENT (ITEM_ID, EQUIPMENT_FUNCTION, WEIGHT, EQUIPMENT_SIZE, DATE_INSTALLED) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, equipment_function);
            preparedStatement.setFloat(3, weight);
            preparedStatement.setString(4, equipment_size);
            preparedStatement.setString(5, date_installed);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data from EQUIPMENT inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from EQUIPMENT was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    // HANDLES DELETE IN EQUIPMENT
    // REMOVES INSTALLED SINCE ORACLE HAS ON DELETE CASCADE
    public boolean deleteEquipment(int id) {
        String sql = "DELETE FROM EQUIPMENT WHERE ITEM_ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data from EQUIPMENT deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from EQUIPMENT was not deleted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean updateEquipment(int id, String equipment_function, float weight, String equipment_size, String date_installed) {
        String sql = "UPDATE EQUIPMENT SET EQUIPMENT_FUNCTION = ?, WEIGHT = ?, EQUIPMENT_SIZE = ?, DATE_INSTALLED = ? WHERE ITEM_ID = ?";

        try {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, equipment_function);
            preparedStatement.setFloat(2, weight);
            preparedStatement.setString(3, equipment_size);
            preparedStatement.setString(4, date_installed);
            preparedStatement.setInt(5, id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data from EQUIPMENT UPDATED successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data from EQUIPMENT was not updated properly");
            rollbackConnection();
            return false;
        }
    }

    public JSONArray listEquipment(int id) {
        String sql = "SELECT e.ITEM_ID, e.EQUIPMENT_FUNCTION, e.WEIGHT, E.EQUIPMENT_SIZE, e.DATE_INSTALLED " +
                "FROM EQUIPMENT e";

        JSONArray equipmentJSONArray = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()){

            while (resultSet.next()) {
                int equipment_id = resultSet.getInt("ITEM_ID");
                String function = resultSet.getString("EQUIPMENT_FUNCTION");
                float weight = resultSet.getFloat("WEIGHT");
                String size = resultSet.getString("EQUIPMENT_SIZE");
                String date_installed = resultSet.getString("DATE_INSTALLED");

                JSONObject equipment = new JSONObject();
                equipment.put("ITEM_ID", equipment_id);
                equipment.put("EQUIPMENT_FUNCTION", function);
                equipment.put("WEIGHT", weight);
                equipment.put("EQUIPMENT_SIZE", size);
                equipment.put("DATE_INSTALLED", date_installed);

                equipmentJSONArray.put(equipment);

                System.out.println("ITEM_ID: " + equipment_id + ", FUNCTION: " + function + ", WEIGHT: " + weight
                        + ", SIZE: " + size
                        + ", DATE_INSTALLED: " + date_installed);

            }

            resultSet.close();

            System.out.println("Data FROM EQUIPMENT was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data FROM EQUIPMENT was not listed properly");
        }

        if (equipmentJSONArray.isEmpty()) {
            return null;
        }
        return equipmentJSONArray;
    }

    public JSONObject getEquipmentByID(int id) {
        String sql = "SELECT e.ITEM_ID, e.EQUIPMENT_FUNCTION, e.WEIGHT, E.EQUIPMENT_SIZE, e.DATE_INSTALLED " +
                "FROM EQUIPMENT e " +
                "WHERE e.ITEM_ID = ?";

        JSONObject equipment = new JSONObject();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()){

            while (resultSet.next()) {
                int equipment_id = resultSet.getInt("ITEM_ID");
                String function = resultSet.getString("EQUIPMENT_FUNCTION");
                float weight = resultSet.getFloat("WEIGHT");
                String size = resultSet.getString("EQUIPMENT_SIZE");
                String date_installed = resultSet.getString("DATE_INSTALLED");

                equipment.put("ITEM_ID", equipment_id);
                equipment.put("EQUIPMENT_FUNCTION", function);
                equipment.put("WEIGHT", weight);
                equipment.put("EQUIPMENT_SIZE", size);
                equipment.put("DATE_INSTALLED", date_installed);

                System.out.println("ITEM_ID: " + equipment_id + ", FUNCTION: " + function + ", WEIGHT: " + weight
                        + ", SIZE: " + size
                        + ", DATE_INSTALLED: " + date_installed);

            }

            resultSet.close();

            System.out.println("Data FROM EQUIPMENT by ID was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data FROM EQUIPMENT by ID was not listed properly");
        }

        if (equipment.isEmpty()) {
            return null;
        }
        return equipment;
    }

    // Citation: Studied: https://www.w3schools.com/sql/sql_groupby.asp

    // FUNCTION FOR "Queries: Aggregation with GROUP BY"
    public JSONArray groupByEquipmentSize() {
        String sql = "SELECT EQUIPMENT_SIZE, COUNT(*) AS EquipmentCount " +
                "FROM EQUIPMENT " +
                "GROUP BY EQUIPMENT_SIZE";

        JSONArray equipmentArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String size = resultSet.getString("EQUIPMENT_SIZE");
                int count = resultSet.getInt("EquipmentCount");

                JSONObject equipmentInfo = new JSONObject();
                equipmentInfo.put("EQUIPMENT_SIZE", size);
                equipmentInfo.put("EquipmentCount", count);

                equipmentArray.put(equipmentInfo);
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("'Query failed: " + e.getMessage());
        }

        if (equipmentArray.isEmpty()) {
            return null;
        }

        return equipmentArray;
    }

    // THIS COVERS THE ENTITIES WATERTANK (WATERTANKLOGISTICS AND WATERTANKPH) AND RELATION MAINTAINS (SEPARATE ENTITY) AND PARTOF
    public boolean insertWaterTank(int id, String name, float volume, float temperature, String lighting_level,
                                   int exhibit_id, float pH, int aquarist_id) {
        String sql1 = "INSERT INTO WATERTANKLOGISTICS " +
                "(ID, WATER_TANK_LOGISTICS_NAME, VOLUME, TEMPERATURE, LIGHTINGLEVEL, EXHIBIT_ID) VALUES (?, ?, ?, ?, ?, ?)";
        String sql2 = "INSERT INTO WATERTANKPH (TEMPERATURE, PH) VALUES (?, ?)";
        String sql3 = "INSERT INTO AQUARIST_MAINTAIN_WATERTANK (AQUARIST_ID, WATER_TANK_ID) VALUES (?, ?)";

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

        try (PreparedStatement getWaterTankTempStatement = connection.prepareStatement(getWaterTankTempSql)) {
            getWaterTankTempStatement.setInt(1, id);

            try (ResultSet resultSet = getWaterTankTempStatement.executeQuery()) {
                float waterTankTemperature;

                if (resultSet.next()) {
                    waterTankTemperature = resultSet.getFloat("TEMPERATURE");

                    try (PreparedStatement deleteQuantityStatement = connection.prepareStatement(deleteWaterTankLogisticsSql);
                         PreparedStatement deleteUnitStatement = connection.prepareStatement(deleteWaterTankpHSql)) {

                        deleteQuantityStatement.setInt(1, id);
                        deleteUnitStatement.setFloat(1, waterTankTemperature);

                        deleteQuantityStatement.executeUpdate();
                        deleteUnitStatement.executeUpdate();
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


    public boolean updateWaterTank(int id, String name, float volume, float temperature, String lighting_level,
                                   int exhibit_id, float pH, int aquarist_id) {
        String sql1 = "UPDATE WATERTANKLOGISTICS SET WATER_TANK_LOGISTICS_NAME = ?, VOLUME = ?, " +
                "TEMPERATURE = ?, LIGHTINGLEVEL = ?, EXHIBIT_ID = ? WHERE ID = ?";
        String sql2 = "UPDATE WATERTANKPH SET PH = ? WHERE TEMPERATURE = ?";
        String sql3 = "UPDATE AQUARIST_MAINTAIN_WATERTANK SET AQUARIST_ID = ? WHERE WATER_TANK_ID = ?";

        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);


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

    public JSONArray listWaterTank() {
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
                waterTank.put("WATER_TANK_LOGISTICS_NAME", name);
                waterTank.put("TEMPERATURE", temperature);
                waterTank.put("PH", pH);
                waterTank.put("LIGHTINGLEVEL", lighting_level);
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
        return waterTankArray;
    }

    public JSONObject getWaterTankByID(int id) {
        String sql = "SELECT wl.ID, wl.WATER_TANK_LOGISTICS_NAME, wl.VOLUME, wl.TEMPERATURE, wp.PH, wl.LIGHTINGLEVEL, wl.EXHIBIT_ID, m.AQUARIST_ID " +
                "FROM WATERTANKLOGISTICS wl " +
                "JOIN WATERTANKPH wp ON wl.TEMPERATURE = wp.TEMPERATURE " +
                "JOIN AQUARIST_MAINTAIN_WATERTANK m ON m.WATER_TANK_ID = wl.ID " +
                "WHERE wl.ID = ?";

        JSONObject waterTank = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int animal_id = resultSet.getInt("ID");
                String name = resultSet.getString("WATER_TANK_LOGISTICS_NAME");
                float volume = resultSet.getFloat("VOLUME");
                float temperature = resultSet.getFloat("TEMPERATURE");
                float pH = resultSet.getFloat("PH");
                String lighting_level = resultSet.getString("LIGHTINGLEVEL");
                int exhibit_id = resultSet.getInt("EXHIBIT_ID");
                int aquarist_id = resultSet.getInt("AQUARIST_ID");

                waterTank.put("ID", animal_id);
                waterTank.put("WATER_TANK_LOGISTICS_NAME", name);
                waterTank.put("VOLUME", volume);
                waterTank.put("TEMPERATURE", temperature);
                waterTank.put("PH", pH);
                waterTank.put("LIGHTINGLEVEL", lighting_level);
                waterTank.put("EXHIBIT_ID", exhibit_id);
                waterTank.put("AQUARIST_ID", aquarist_id);


                System.out.println("ID: " + id + ", NAME: " + name + ", VOLUME: " + volume + ", TEMPERATURE: " + temperature +
                        ", PH: " + pH + ", LIGHTINGLEVEL: " + lighting_level + ", EXHIBIT_ID: " + exhibit_id
                        + " , AQUARIST_ID: " + aquarist_id);
            }

            resultSet.close();

            System.out.println("Data from WATERTANK was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data from WATERTANK was not listed properly");
        }

        if (waterTank.isEmpty()) {
            return null;
        }
        return waterTank;
    }

    // SELECT METHOD
    // selects a subset of WATERTANKLOGISTICS and WATERTANKPH that meets the conditions passed
    // Source: https://www.javatpoint.com/iterate-json-array-java
    // Source: https://www.javatpoint.com/java-stringbuilder-append-method
    public JSONArray selectWaterTank(JSONArray waterTankConditions) {
        // construct SELECT query
        StringBuilder sql = new StringBuilder("SELECT wl.ID, wl.WATER_TANK_LOGISTICS_NAME, wl.VOLUME, wl.TEMPERATURE, wp.PH, wl.LIGHTINGLEVEL, wl.EXHIBIT_ID, m.AQUARIST_ID " +
                "FROM WATERTANKLOGISTICS wl " +
                "JOIN WATERTANKPH wp ON wl.TEMPERATURE = wp.TEMPERATURE");

        // Check if conditions are provided
        if (waterTankConditions != null) {
            JSONArray selectionArray = waterTankConditions;

            // create an array for AND conditions and OR conditions
            JSONArray andCondArray = new JSONArray();
            JSONArray orCondArray = new JSONArray();

            for (int i = 0; i < selectionArray.length(); i++) {
                JSONObject condition = selectionArray.getJSONObject(i);
                if ("And".equalsIgnoreCase(condition.getString("Condition"))) {
                    andCondArray.put(condition);
                } else if ("Or".equalsIgnoreCase(condition.getString("Condition"))) {
                    orCondArray.put(condition);
                }
            }

            // Add AND conditions
            if (!andCondArray.isEmpty()) {
                sql.append(" WHERE ");
                for (int i = 0; i < andCondArray.length(); i++) {
                    JSONObject cond = andCondArray.getJSONObject(i);
                    if (i > 0) {
                        sql.append(" AND ");
                    }
                    sql.append("(")
                            .append(cond.getString("Name"))
                            .append(" ")
                            .append(cond.getString("Comparison"))
                            .append(" ?)");
                }
            }

            // Add OR conditions
            if (!orCondArray.isEmpty()) {
                if (!andCondArray.isEmpty()) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                for (int i = 0; i < orCondArray.length(); i++) {
                    JSONObject cond = orCondArray.getJSONObject(i);
                    sql.append("(")
                            .append(cond.getString("Name"))
                            .append(" ")
                            .append(cond.getString("Comparison"))
                            .append(" ?)");
                    if (i < orCondArray.length() - 1) {
                        sql.append(" OR ");
                    }
                }
            }
        }

        JSONArray waterTankArray = new JSONArray();

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            // Set parameter values based on the JSONObject
            if (waterTankConditions != null) {
                JSONArray selectionArray = waterTankConditions;
                int parameterIndex = 1;
                for (int i = 0; i < selectionArray.length(); i++) {
                    JSONObject condition = selectionArray.getJSONObject(i);
                    statement.setObject(parameterIndex, condition.getString("Value"));
                    parameterIndex += 1;
                }
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int water_tank_id = resultSet.getInt("ID");
                String name = resultSet.getString("WATER_TANK_LOGISTICS_NAME");
                float volume = resultSet.getFloat("VOLUME");
                float temperature = resultSet.getFloat("TEMPERATURE");
                float pH = resultSet.getFloat("PH");
                String lighting_level = resultSet.getString("LIGHTINGLEVEL");
                int exhibit_id = resultSet.getInt("EXHIBIT_ID");
                int aquarist_id = resultSet.getInt("AQUARIST_ID");

                JSONObject waterTank = new JSONObject();

                waterTank.put("ID", water_tank_id);
                waterTank.put("WATER_TANK_LOGISTICS_NAME", name);
                waterTank.put("VOLUME", volume);
                waterTank.put("TEMPERATURE", temperature);
                waterTank.put("PH", pH);
                waterTank.put("LIGHTINGLEVEL", lighting_level);
                waterTank.put("EXHIBIT_ID", exhibit_id);
                waterTank.put("AQUARIST_ID", aquarist_id);

                waterTankArray.put(waterTank);

                System.out.println("ID: " + water_tank_id + ", NAME: " + name + ", VOLUME: " + volume + ", TEMPERATURE: " + temperature +
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
        return waterTankArray;
    }

    public String selectWaterTankTest(JSONArray waterTankConditions) {
        // construct SELECT query
        StringBuilder sql = new StringBuilder("SELECT wl.ID, wl.WATER_TANK_LOGISTICS_NAME, wl.VOLUME, wl.TEMPERATURE, wp.PH, wl.LIGHTINGLEVEL, wl.EXHIBIT_ID, m.AQUARIST_ID " +
                "FROM WATERTANKLOGISTICS wl " +
                "JOIN WATERTANKPH wp ON wl.TEMPERATURE = wp.TEMPERATURE");

        // Check if conditions are provided
        if (waterTankConditions != null) {
            JSONArray selectionArray = waterTankConditions;

            // create an array for AND conditions and OR conditions
            JSONArray andCondArray = new JSONArray();
            JSONArray orCondArray = new JSONArray();

            for (int i = 0; i < selectionArray.length(); i++) {
                JSONObject condition = selectionArray.getJSONObject(i);
                if ("And".equalsIgnoreCase(condition.getString("Condition"))) {
                    andCondArray.put(condition);
                } else if ("Or".equalsIgnoreCase(condition.getString("Condition"))) {
                    orCondArray.put(condition);
                }
            }

            // Add AND conditions
            if (!andCondArray.isEmpty()) {
                sql.append(" WHERE ");
                for (int i = 0; i < andCondArray.length(); i++) {
                    JSONObject cond = andCondArray.getJSONObject(i);
                    if (i > 0) {
                        sql.append(" AND ");
                    }
                    sql.append("(")
                            .append(cond.getString("Name"))
                            .append(" ")
                            .append(cond.getString("Comparison"))
                            .append(" ?)");
                }
            }

            // Add OR conditions
            if (!orCondArray.isEmpty()) {
                if (!andCondArray.isEmpty()) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                for (int i = 0; i < orCondArray.length(); i++) {
                    JSONObject cond = orCondArray.getJSONObject(i);
                    sql.append("(")
                            .append(cond.getString("Name"))
                            .append(" ")
                            .append(cond.getString("Comparison"))
                            .append(" ?)");
                    if (i < orCondArray.length() - 1) {
                        sql.append(" OR ");
                    }
                }
            }
        }

        return sql.toString();
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

    public boolean insertFeed(int food_id, int animal_id, int aquarist_id, int quantity, String last_fed, String method) {
        String sql = "INSERT INTO FEED (FOOD_ID, ANIMAL_ID, AQUARIST_ID, QUANTITY, LAST_FED, METHOD) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, food_id);
            preparedStatement.setInt(2, animal_id);
            preparedStatement.setInt(3, aquarist_id);
            preparedStatement.setInt(4, quantity);
            preparedStatement.setString(5, last_fed);
            preparedStatement.setString(6, method);


            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in FEED inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in FEED was not inserted properly");
            rollbackConnection();
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

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in EXHIBIT inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in EXHIBIT was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean updateFeed(int food_id, int animal_id, int aquarist_id, int quantity, String last_fed, String method) {
        String sql = "UPDATE FEED SET QUANTITY = ?, LAST_FED = ?, METHOD = ? WHERE FOOD_ID = ? AND ANIMAL_ID = ? AND AQUARIST_ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, quantity);
            preparedStatement.setString(2, last_fed);
            preparedStatement.setString(3, method);
            preparedStatement.setInt(4, food_id);
            preparedStatement.setInt(5, animal_id);
            preparedStatement.setInt(6, aquarist_id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in FEED updated successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in FEED was not updated properly");
            rollbackConnection();
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

    // Citation: Studied: https://www.w3schools.com/sql/sql_groupby.asp

    // FUNCTION FOR "Queries: Nested Aggregation with GROUP BY"
    public JSONArray groupByAnimalSpeciesAndAverageAgeAboveLivingTemp(double temperatureThreshold) {
        String sql = "SELECT a.SPECIES, AVG(a.AGE) " +
                "FROM ANIMAL a " +
                "GROUP BY a.SPECIES " +
                "HAVING AVG(a.AGE) > " +
                "(SELECT AVG(a2.AGE) " +
                "FROM ANIMAL a2 " +
                "WHERE a2.LIVINGTEMP > ?)";

        JSONArray animalArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, temperatureThreshold);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String species = resultSet.getString("SPECIES");
                int average_age = resultSet.getInt("AVG(a.AGE)");

                JSONObject animalInfo = new JSONObject();
                animalInfo.put("SPECIES", species);
                animalInfo.put("AVERAGE_AGE", average_age);

                animalArray.put(animalInfo);
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("'Query failed: " + e.getMessage());
        }

        if (animalArray.isEmpty()) {
            return null;
        }

        return animalArray;
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

    // DISCUSS HOW TO ADD THIS (WITH SHARON AND CHRIS) - MAKE SEPARATE METHOD OR DO IT WITHIN ENTITIES
    // MAKES MORE SENSE TO DO IT WITHIN ENTITIES
    // NOT NEEDED REALLY
    public boolean deleteFeed(int food_id, int animal_id, int aquarist_id) {
        return false;
    }

    // DELETES FROM ANIMAL AND FEED (SINCE ANIMAL HAS TOTAL PARTICIPATION - DISCUSS)
    public boolean deleteAnimal(int id) {
        String sql = "DELETE FROM ANIMAL WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

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

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

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
    public JSONArray listAnimal() {
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
                animal.put("ANIMAL_NAME", name);
                animal.put("SPECIES", species);
                animal.put("AGE", age);
                animal.put("LIVINGTEMP", living_temp);
                animal.put("WATER_TANK_ID", waterTankID);
                animal.put("VETERINARIAN_ID", veterinarianID);

                animalArray.put(animal);

                System.out.println("ID: " + id + ", Name: " + name + ", Species: " + species + ", Age: " + age + ", Living Temperature: " + living_temp
                        + ", Water Tank ID: " + waterTankID + ", Veterinarian ID: " + veterinarianID);
            }

            animalResult.close();

            System.out.println("Data from ANIMAL was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data from ANIMAL was not listed properly");
        }

        if (animalArray.isEmpty()) {
            return null;
        }

        return animalArray;
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


                animalItem.put("ID", animal_id);
                animalItem.put("ANIMAL_NAME", name);
                animalItem.put("SPECIES", species);
                animalItem.put("AGE", age);
                animalItem.put("LIVINGTEMP", living_temp);
                animalItem.put("WATER_TANK_ID", waterTankID);
                animalItem.put("VETERINARIAN_ID", veterinarianID);

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
    public JSONArray listExhibit() {
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

        return exhibitArray;
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
//            System.out.println("Data from EXHIBIT was retrieved successfully");
//
//        } catch (SQLException e) {
//            System.out.println("Data from EXHIBIT was not retrieved properly");
//        }
//
//        if (exhibitItem.isEmpty()) {
//            return null;
//        }
//        return exhibitItem.toString();
//    }

    public JSONObject getExhibitByID(int id) {
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

        return exhibitItem.isEmpty() ? null : exhibitItem;
    }

    public boolean insertClean(int exhibit_id, int custodian_id) {
        String sql = "INSERT INTO CUSTODIAN_CLEAN_EXHIBIT_TABLE (exhibit_id, custodian_id) VALUES (?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, exhibit_id);
            preparedStatement.setInt(2, custodian_id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in CLEAN inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in CLEAN was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean insertInstalled(int equipment_id, int water_tank_id, int quantity, String date_installed) {
        String sql = "INSERT INTO INSTALLED (equipment_id, water_tank_id, quantity, date_installed) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, equipment_id);
            preparedStatement.setInt(2, water_tank_id);
            preparedStatement.setInt(3, quantity);
            preparedStatement.setString(4, date_installed);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in INSTALLED inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in INSTALLED was not inserted properly");
            rollbackConnection();
            return false;
        }
    }

    public boolean updateInstalled(int equipment_id, int water_tank_id, int quantity, String date_installed) {
        String sql = "UPDATE INSTALLED SET QUANTITY = ?, DATE_INSTALLED = ? WHERE EQUIPMENT_ID = ? AND WATER_TANK_ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, quantity);
            preparedStatement.setString(2, date_installed);
            preparedStatement.setInt(3, equipment_id);
            preparedStatement.setInt(4, water_tank_id);

            preparedStatement.executeUpdate();

            connection.commit();

            preparedStatement.close();

            System.out.println("Data in INSTALLED updated successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Data in INSTALLED was not updated properly");
            rollbackConnection();
            return false;
        }
    }

    // FOR STAFF TABLE:
    public boolean insertStaff(int id, BigDecimal salary, String staffName, Date dateHired) {
        String sql = "INSERT INTO Staff (id, salary, staff_name, datehired) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.setBigDecimal(2, salary);
            preparedStatement.setString(3, staffName);
            preparedStatement.setDate(4, dateHired);
            preparedStatement.executeUpdate();
            connection.commit();
            preparedStatement.close();
            System.out.println("Data from STAFF inserted successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Data from STAFF was not inserted properly.");
            rollbackConnection();
            return false;
        }
    }

    private boolean staffMemberExistsInAnySubtype(int id) {
        String sqlAquarist = "SELECT COUNT(*) FROM Aquarist WHERE id = ?";
        String sqlCustodian = "SELECT COUNT(*) FROM Custodian WHERE id = ?";
        String sqlVeterinarian = "SELECT COUNT(*) FROM Veterinarian WHERE id = ?";

        try {
            // Check Aquarist
            PreparedStatement preparedStatement = connection.prepareStatement(sqlAquarist);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return true;
            }
            preparedStatement.close();

            // Check Custodian
            preparedStatement = connection.prepareStatement(sqlCustodian);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return true;
            }
            preparedStatement.close();

            // Check Veterinarian
            preparedStatement = connection.prepareStatement(sqlVeterinarian);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return true;
            }
            preparedStatement.close();

            return false;
        } catch (SQLException e) {
            System.out.println("Error checking for staff member in subtypes: " + e.getMessage());
            rollbackConnection();
            return true;
        }
    }

    // This method will insert a new staff member and an aquarist.
    public boolean insertAquarist(int id, BigDecimal divingLevel, int waterTankId) {
        if (staffMemberExistsInAnySubtype(id)) {
            System.out.println("Staff member with ID " + id + " already exists in a subtype.");
            rollbackConnection();
            return false;
        }

        String sql = "INSERT INTO Aquarist (id, diving_level, water_tank_id) VALUES (?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.setBigDecimal(2, divingLevel);
            preparedStatement.setInt(3, waterTankId);
            preparedStatement.executeUpdate();
            connection.commit();
            preparedStatement.close();
            System.out.println("Data from AQUARIST inserted successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Data from AQUARIST was not inserted properly.");
            rollbackConnection();
            return false;
        }
    }

    public boolean insertCustodian(int id, int exhibitId) {
        if (staffMemberExistsInAnySubtype(id)) {
            System.out.println("Staff member with ID " + id + " already exists in a subtype.");
            rollbackConnection();
            return false;
        }

        String sql = "INSERT INTO Custodian (id, exhibit_id) VALUES (?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, exhibitId);
            preparedStatement.executeUpdate();
            connection.commit();
            preparedStatement.close();
            System.out.println("Data from CUSTODIAN inserted successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Data from CUSTODIAN was not inserted properly.");
            rollbackConnection();
            return false;
        }
    }

    public boolean insertVeterinarian(int id) {
        if (staffMemberExistsInAnySubtype(id)) {
            System.out.println("Staff member with ID " + id + " already exists in a subtype.");
            rollbackConnection();
            return false;
        }

        String sql = "INSERT INTO Veterinarian (id) VALUES (?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            connection.commit();
            preparedStatement.close();
            System.out.println("Data from VETERINARIAN inserted successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Data from VETERINARIAN was not inserted properly.");
            rollbackConnection();
            return false;
        }
    }

    public boolean updateStaff(int id, BigDecimal salary, String staffName, Date dateHired) {
        String sql = "UPDATE Staff SET salary = ?, staff_name = ?, datehired = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBigDecimal(1, salary);
            preparedStatement.setString(2, staffName);
            preparedStatement.setDate(3, dateHired);
            preparedStatement.setInt(4, id);

            int affectedRows = preparedStatement.executeUpdate();
            connection.commit();

            if (affectedRows > 0) {
                System.out.println("Staff updated successfully.");
                return true;
            } else {
                System.out.println("No Staff found with ID " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Staff update failed: " + e.getMessage());
            rollbackConnection();
            return false;
        }
    }

    public boolean updateAquarist(int id, BigDecimal divingLevel, Integer waterTankId) {
        String sql = "UPDATE Aquarist SET diving_level = ?, water_tank_id = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBigDecimal(1, divingLevel);
            preparedStatement.setInt(2, waterTankId);
            preparedStatement.setInt(3, id);

            int affectedRows = preparedStatement.executeUpdate();
            connection.commit();

            if (affectedRows > 0) {
                System.out.println("Aquarist updated successfully.");
                return true;
            } else {
                System.out.println("No Aquarist found with ID " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Aquarist update failed: " + e.getMessage());
            rollbackConnection();
            return false;
        }
    }

    public boolean updateCustodian(int id, int exhibitId) {
        String sql = "UPDATE Custodian SET exhibit_id = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, exhibitId);
            preparedStatement.setInt(2, id);

            int affectedRows = preparedStatement.executeUpdate();
            connection.commit();

            if (affectedRows > 0) {
                System.out.println("Custodian updated successfully.");
                return true;
            } else {
                System.out.println("No Custodian found with ID " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Custodian update failed: " + e.getMessage());
            rollbackConnection();
            return false;
        }
    }

    public boolean updateVeterinarian(int id) {

        // Check if the Veterinarian exists in the Veterinarian table
        String checkSql = "SELECT COUNT(*) FROM Veterinarian WHERE id = ?";
        try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
            checkStatement.setInt(1, id);
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    System.out.println("No Veterinarian found with ID " + id);
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Veterinarian check failed: " + e.getMessage());
            rollbackConnection();
            return false;
        }

        System.out.println("Veterinarian updated successfully (no attributes to update).");
        return true;
    }

    public boolean deleteStaff(int id) {
        String sql = "DELETE FROM Staff WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            int affectedRows = preparedStatement.executeUpdate();
            connection.commit();

            if (affectedRows > 0) {
                System.out.println("Staff member deleted successfully.");
                return true;
            } else {
                System.out.println("No Staff member found with ID " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Staff deletion failed: " + e.getMessage());
            rollbackConnection();
            return false;
        }
    }

    public boolean deleteCustodian(int id) {
        String sql = "DELETE FROM Custodian WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            int affectedRows = preparedStatement.executeUpdate();
            connection.commit();

            if (affectedRows > 0) {
                System.out.println("Custodian deleted successfully.");
                return true;
            } else {
                System.out.println("No Custodian found with ID " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Custodian deletion failed: " + e.getMessage());
            rollbackConnection();
            return false;
        }
    }

    public boolean deleteVeterinarian(int id) {
        String sql = "DELETE FROM Veterinarian WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            int affectedRows = preparedStatement.executeUpdate();
            connection.commit();

            if (affectedRows > 0) {
                System.out.println("Veterinarian deleted successfully.");
                return true;
            } else {
                System.out.println("No Veterinarian found with ID " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Veterinarian deletion failed: " + e.getMessage());
            rollbackConnection();
            return false;
        }
    }

    public boolean deleteAquarist(int id) {
        String sql = "DELETE FROM Aquarist WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            int affectedRows = preparedStatement.executeUpdate();
            connection.commit();

            if (affectedRows > 0) {
                System.out.println("Aquarist deleted successfully.");
                return true;
            } else {
                System.out.println("No Aquarist found with ID " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Aquarist deletion failed: " + e.getMessage());
            rollbackConnection();
            return false;
        }
    }
    public JSONArray listStaff() {
        String sql = "SELECT ID, SALARY, STAFF_NAME, DATEHIRED FROM Staff";

        JSONArray staffJSONArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet staffResult = preparedStatement.executeQuery();

            while (staffResult.next()) {
                int id = staffResult.getInt("ID");
                BigDecimal salary = staffResult.getBigDecimal("SALARY");
                String staffName = staffResult.getString("STAFF_NAME");
                Date dateHired = staffResult.getDate("DATEHIRED");

                JSONObject staffItem = new JSONObject();
                staffItem.put("ID", id);
                staffItem.put("SALARY", salary);
                staffItem.put("STAFF_NAME", staffName);
                staffItem.put("DATEHIRED", dateHired);

                staffJSONArray.put(staffItem);

                System.out.println("ID: " + id + ", Salary: " + salary +
                        ", Staff Name: " + staffName + ", Date Hired: " + dateHired);
            }

            staffResult.close();

            System.out.println("Data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data was not listed properly: " + e.getMessage());
        }

        return staffJSONArray.isEmpty() ? null : staffJSONArray;
    }

    public JSONArray listAquarists() {
        String sql = "SELECT ID, DIVING_LEVEL, WATER_TANK_ID FROM Aquarist";

        JSONArray aquaristsJSONArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet aquaristResult = preparedStatement.executeQuery();

            while (aquaristResult.next()) {
                int id = aquaristResult.getInt("ID");
                BigDecimal divingLevel = aquaristResult.getBigDecimal("DIVING_LEVEL");
                int waterTankId = aquaristResult.getInt("WATER_TANK_ID");

                JSONObject aquaristItem = new JSONObject();
                aquaristItem.put("ID", id);
                aquaristItem.put("DIVING_LEVEL", divingLevel);
                aquaristItem.put("WATER_TANK_ID", waterTankId);

                aquaristsJSONArray.put(aquaristItem);

                System.out.println("ID: " + id + ", Diving Level: " + divingLevel +
                        ", Water Tank ID: " + waterTankId);
            }

            aquaristResult.close();

            System.out.println("Data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data was not listed properly: " + e.getMessage());
        }

        return aquaristsJSONArray.isEmpty() ? null : aquaristsJSONArray;
    }

    public JSONArray listCustodians() {
        String sql = "SELECT ID, EXHIBIT_ID FROM Custodian";

        JSONArray custodiansJSONArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet custodianResult = preparedStatement.executeQuery();

            while (custodianResult.next()) {
                int id = custodianResult.getInt("ID");
                int exhibitId = custodianResult.getInt("EXHIBIT_ID");

                JSONObject custodianItem = new JSONObject();
                custodianItem.put("ID", id);
                custodianItem.put("EXHIBIT_ID", exhibitId);

                custodiansJSONArray.put(custodianItem);

                System.out.println("ID: " + id + ", Exhibit ID: " + exhibitId);
            }

            custodianResult.close();

            System.out.println("Data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data was not listed properly: " + e.getMessage());
        }

        return custodiansJSONArray.isEmpty() ? null : custodiansJSONArray;
    }

    public JSONArray listVeterinarians() {
        String sql = "SELECT ID FROM Veterinarian";

        JSONArray veterinariansJSONArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet veterinarianResult = preparedStatement.executeQuery();

            while (veterinarianResult.next()) {
                int id = veterinarianResult.getInt("ID");

                JSONObject veterinarianItem = new JSONObject();
                veterinarianItem.put("ID", id);

                veterinariansJSONArray.put(veterinarianItem);

                System.out.println("ID: " + id);
            }

            veterinarianResult.close();

            System.out.println("Data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Data was not listed properly: " + e.getMessage());
        }

        return veterinariansJSONArray.isEmpty() ? null : veterinariansJSONArray;
    }


    public JSONObject getStaffByID(int id) {
        String sql = "SELECT ID, SALARY, STAFF_NAME, DATEHIRED FROM Staff WHERE ID = ?";

        JSONObject staff = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                staff.put("ID", resultSet.getInt("ID"));
                staff.put("SALARY", resultSet.getBigDecimal("SALARY"));
                staff.put("STAFF_NAME", resultSet.getString("STAFF_NAME"));
                staff.put("DATEHIRED", resultSet.getDate("DATEHIRED"));
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Data FROM STAFF was not retrieved properly");
        }

        return staff.isEmpty() ? null : staff;
    }

    public JSONObject getCustodianByID(int id) {
        String sql = "SELECT ID, EXHIBIT_ID FROM Custodian WHERE ID = ?";

        JSONObject custodian = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                custodian.put("ID", resultSet.getInt("ID"));
                custodian.put("EXHIBIT_ID", resultSet.getInt("EXHIBIT_ID"));
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Data FROM CUSTODIAN was not retrieved properly");
        }

        return custodian.isEmpty() ? null : custodian;
    }

    public JSONObject getAquaristByID(int id) {
        String sql = "SELECT ID, DIVING_LEVEL, WATER_TANK_ID FROM Aquarist WHERE ID = ?";

        JSONObject aquarist = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                aquarist.put("ID", resultSet.getInt("ID"));
                aquarist.put("DIVING_LEVEL", resultSet.getBigDecimal("DIVING_LEVEL"));
                aquarist.put("WATER_TANK_ID", resultSet.getInt("WATER_TANK_ID"));
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Data FROM AQUARIST was not retrieved properly");
        }

        return aquarist.isEmpty() ? null : aquarist;
    }

    public JSONObject getVeterinarianByID(int id) {
        String sql = "SELECT ID FROM Veterinarian WHERE ID = ?";

        JSONObject veterinarian = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                veterinarian.put("ID", resultSet.getInt("ID"));
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Data FROM VETERINARIAN was not retrieved properly");
        }

        return veterinarian.isEmpty() ? null : veterinarian;
    }

    // Citation: Studied: https://www.freecodecamp.org/news/sql-having-how-to-group-and-count-with-a-having-statement/
    // #:~:text=In%20SQL%2C%20you%20use%20the,when%20used%20with%20aggregate%20functions.

    // FUNCTION FOR "Queries: Aggregation with Having"
    public JSONArray getSalariesWithHighEarningStaffCounts(BigDecimal salaryThreshold) {
        String sql = "SELECT SALARY, COUNT(*) AS StaffCount " +
                "FROM Staff " +
                "GROUP BY SALARY " +
                "HAVING SALARY > ?";

        JSONArray salariesArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setBigDecimal(1, salaryThreshold);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                BigDecimal salary = resultSet.getBigDecimal("SALARY");
                int count = resultSet.getInt("StaffCount");

                JSONObject salaryInfo = new JSONObject();
                salaryInfo.put("SALARY", salary);
                salaryInfo.put("StaffCount", count);

                salariesArray.put(salaryInfo);
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }

        return salariesArray.isEmpty() ? null : salariesArray;
    }

    // Citation: Studied:https://www.geeksforgeeks.org/sql-division/
    // FUNCTION FOR "Queries: Division"
    // Veterinarians Who Worked With All Of Specific Species
    public JSONArray getAnimalExpertVets(String species) {
        String sql = "SELECT v.ID " +
                "FROM VETERINARIAN v " +
                "WHERE NOT EXISTS (" +
                "SELECT a.ID " +
                "FROM ANIMAL a " +
                "WHERE a.SPECIES = ? " +
                "AND NOT EXISTS (" +
                "SELECT * " +
                "FROM ANIMAL a2 " +
                "WHERE a2.VETERINARIAN_ID = v.ID AND a2.SPECIES = ?" +
                "))";

        JSONArray veterinariansJSONArray = new JSONArray();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, species);
            preparedStatement.setString(2, species);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                JSONObject veterinarian = new JSONObject();
                veterinarian.put("Veterinarian_Id", resultSet.getInt("ID"));
                veterinarian.put("Animal_Name", species);

                veterinariansJSONArray.put(veterinarian);
            }

            resultSet.close();

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }

        return veterinariansJSONArray;
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
        String sql = "SELECT PLANT_ID, SPECIES, LIVING_TEMP, LIVING_LIGHT, WATER_TANK_ID FROM Grown_In_Plant";
        JSONArray plantsJSONArray = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int plantId = resultSet.getInt("PLANT_ID");
                String species = resultSet.getString("SPECIES");
                float livingTemp = resultSet.getFloat("LIVING_TEMP");
                float livingLight = resultSet.getFloat("LIVING_LIGHT");
                int waterTankId = resultSet.getInt("WATER_TANK_ID");

                JSONObject plantObject = new JSONObject();
                plantObject.put("PLANT_ID", plantId);
                plantObject.put("SPECIES", species);
                plantObject.put("LIVING_TEMP", livingTemp);
                plantObject.put("LIVING_LIGHT", livingLight);
                plantObject.put("WATER_TANK_ID", waterTankId);

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
        String sql = "SELECT PLANT_ID, SPECIES, LIVING_TEMP, LIVING_LIGHT, WATER_TANK_ID FROM Grown_In_Plant WHERE PLANT_ID = ?";
        JSONObject plantObject = new JSONObject();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, plantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    plantObject.put("PLANT_ID", resultSet.getInt("PLANT_ID"));
                    plantObject.put("SPECIES", resultSet.getString("SPECIES"));
                    plantObject.put("LIVING_TEMP", resultSet.getBigDecimal("LIVING_TEMP"));
                    plantObject.put("LIVING_LIGHT", resultSet.getBigDecimal("LIVING_LIGHT"));
                    plantObject.put("WATER_TANK_ID", resultSet.getInt("WATER_TANK_ID"));

                    System.out.println("Plant_ID: " + plantId +
                            ", Species: " + plantObject.getString("SPECIES") +
                            ", Living Temp: " + plantObject.getBigDecimal("LIVING_TEMP") +
                            ", Living Light: " + plantObject.getBigDecimal("LIVING_LIGHT") +
                            ", Water Tank ID: " + plantObject.getInt("WATER_TANK_ID"));
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

    public JSONArray listVendors() {
        // Ensure the column names used in the SELECT statement match the actual column names in the database tables
        String sql = "SELECT vl.ID, vl.VENDOR_LOGISTICS_NAME, vr.VENDOR_MARKET_RATING, vl.ADDRESS " +
                "FROM VendorLogistics vl " +
                "JOIN VendorReputation vr ON vl.VENDOR_LOGISTICS_NAME = vr.VENDOR_NAME";
        JSONArray vendorsArray = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String vendorName = resultSet.getString("VENDOR_LOGISTICS_NAME");
                String marketRating = resultSet.getString("VENDOR_MARKET_RATING");
                String address = resultSet.getString("ADDRESS");

                JSONObject vendorObject = new JSONObject();
                vendorObject.put("ID", id);
                vendorObject.put("VENDOR_NAME", vendorName);
                vendorObject.put("VENDOR_MARKET_RATING", marketRating);
                vendorObject.put("ADDRESS", address);

                vendorsArray.put(vendorObject);

                System.out.println("ID: " + id + ", Vendor Name: " + vendorName + ", Market Rating: " + marketRating +
                        ", Address: " + address);
            }

            System.out.println("Vendor data was listed successfully");

        } catch (SQLException e) {
            System.out.println("Error retrieving vendor list: " + e.getMessage());
            return null;
        }

        return vendorsArray.isEmpty() ? null : vendorsArray;
    }

    public JSONObject getVendorByID(int id) {
        String sql = "SELECT vl.ID, vl.VENDOR_LOGISTICS_NAME, vl.ADDRESS, vr.VENDOR_MARKET_RATING " +
                "FROM VendorLogistics vl " +
                "JOIN VendorReputation vr ON vl.VENDOR_LOGISTICS_NAME = vr.VENDOR_NAME " +
                "WHERE vl.ID = ?";
        JSONObject vendorItem = new JSONObject();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet vendorResult = preparedStatement.executeQuery();

            if (vendorResult.next()) {
                String vendorName = vendorResult.getString("VENDOR_LOGISTICS_NAME");
                String address = vendorResult.getString("ADDRESS");
                String marketRating = vendorResult.getString("VENDOR_MARKET_RATING");

                vendorItem.put("ID", id);
                vendorItem.put("VENDOR_NAME", vendorName);
                vendorItem.put("ADDRESS", address);
                vendorItem.put("VENDOR_MARKET_RATING", marketRating);

                System.out.println("ID: " + id + ", Name: " + vendorName + ", Address: " + address + ", Market Rating: " + marketRating);
            } else {
                System.out.println("No vendor found with ID: " + id);
            }

            vendorResult.close();
            System.out.println("Data from VENDOR was retrieved successfully");

        } catch (SQLException e) {
            System.out.println("Data from VENDOR was not retrieved properly: " + e.getMessage());
            return null;
        }

        return vendorItem.isEmpty() ? null : vendorItem;
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