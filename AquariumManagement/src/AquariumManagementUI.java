package AquariumManagement.src;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AquariumManagementUI extends JFrame {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    public static Dimension buttonSize = new Dimension(50, 15);

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    // declaring the database
    private AquariumManagementDB db;

    private List<TablePackage> showAllTablePackages;
    private HashMap<String, ManagerPanelPackage> managerPanelPackageMap;
    public AquariumManagementUI() {
        super("Aquarium Manager");
        showAllTablePackages = new ArrayList<TablePackage>();
        managerPanelPackageMap = new HashMap<String, ManagerPanelPackage>();
        connectDBPanel(); // connect DB and launch app
    }

    /*
    TODO: change this to parse data read from DB instead of hard coding if time allows
     */
    private void initializeShowALlTables() {
        showAllTablePackages.add(new TablePackage("Plant", db::listPlants));
        showAllTablePackages.add(new TablePackage("Vendor", db::listVendors));
        showAllTablePackages.add(new TablePackage("Inventory", db::listInventory));
        showAllTablePackages.add(new TablePackage("Tank", db::listWaterTank));
    }

    private void initializeManagers() {
        // TODO: Add DB NAME and Change inputFieldMap use DB Field name as key
        // each entry = {DB_FIELD_NAME, DISPLAY_NAME, PLACE_HOLDER(optional)}
        addManageInventory();
        addManageAnimal();
        addManagePlant();


    }

    private void addManageInventory() {
        String[][] fieldNames = {{"ID","ID", "True", "Enter ID"},{"LOCATION", "Location", "False", "Enter Location"}};
        ManagerPanelPackage InventoryManager = new ManagerPanelPackage("Inventory", fieldNames);
        InventoryManager.getSearchButton().addActionListener(e -> {
            int id =  Integer.parseInt(InventoryManager.getFieldText("ID"));
            JSONObject dataFound = db.getInventoryByID(id);
            System.out.println(db.getInventoryByID(id).toString());
            InventoryManager.showDbData(dataFound);
        });
        managerPanelPackageMap.put("Inventory",InventoryManager);
    }

    // TODO: Currently No data in ANIMAL. TEST this after data is correctly inserted
    private void addManageAnimal() {
        String[][] fieldNames = {{"ID","ID", "True", "Enter ID"},
                {"ANIMAL_NAME", "Name", "False","Enter Name"}, {"SPECIES", "False", "Species"}, {"AGE", "False", "Age"},
                {"LIVINGTEMP", "Living Temperature(°C)", "False", "Enter Number"},
                {"WATER_TANK_ID","In Water Tank(ID)", "True"}, {"VETERINARIAN_ID", "Assigned Vet(ID)", "True"}};
        ManagerPanelPackage managerPanelPackage = new ManagerPanelPackage("Animal", fieldNames);
        managerPanelPackage.getSearchButton().addActionListener(e -> {
            int id =  Integer.parseInt(managerPanelPackage.getFieldText("ID"));
            JSONObject dataFound = db.getAnimalByID(id);
            if(dataFound != null){
                managerPanelPackage.showDbData(dataFound);
            } else {
                System.out.println("Animal with ID "+ id+ "does not exist");
            }

        });
        managerPanelPackageMap.put("Animal",managerPanelPackage);
    }

    private void addManagePlant() {
        String[][] fieldNames = {{"Plant_ID","ID", "True", "Enter ID"}, {"Species", "Species", "True"},
                {"Living_Temp", "Living Temperature(°C)", "True", "Enter Number"},
                {"Water_Tank_ID","In Water Tank(ID)", "True"}, {"Living_Light", "Light Level", "True"}};
        ManagerPanelPackage managerPanelPackage = new ManagerPanelPackage("Plant", fieldNames);

        // Search
        managerPanelPackage.getSearchButton().addActionListener(e -> {
            if(managerPanelPackage.isFieldTextExists("Plant_ID")){
                int id =  Integer.parseInt(managerPanelPackage.getFieldText("Plant_ID"));
                JSONObject dataFound = db.getPlantByID(id);
                if(dataFound != null){
                    managerPanelPackage.showDbData(dataFound);
                } else {
                    JOptionPane.showMessageDialog(managerPanelPackage.getButtonPanel(),
                            "Plant(ID: "+ id + ") does not exist\n",
                            "Invalid Data", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Delete
        managerPanelPackage.getDeleteButton().addActionListener(e -> {
            if(managerPanelPackage.isFieldTextExists("Plant_ID")){
                int id =  Integer.parseInt(managerPanelPackage.getFieldText("Plant_ID"));
                boolean success = db.deletePlant(id);
                if(success){
                    JOptionPane.showMessageDialog(managerPanelPackage.getButtonPanel(),
                            "Plant(ID: "+ id + ") was successfully deleted\n",
                            "Invalid Data", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(managerPanelPackage.getButtonPanel(),
                            "Plant(ID: "+ id + ") does not exist\n",
                            "Invalid Data", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Add
        managerPanelPackage.getAddButton().addActionListener(e -> {
            if(managerPanelPackage.checkMandatoryFields()){
                try{
                    boolean success = db.insertPlant(Integer.parseInt(managerPanelPackage.getFieldText("Plant_ID")),
                            managerPanelPackage.getFieldText("Species"),
                            Float.parseFloat(managerPanelPackage.getFieldText("Living_Temp")),
                            Float.parseFloat(managerPanelPackage.getFieldText("Living_Light")),
                            Integer.parseInt(managerPanelPackage.getFieldText("Water_Tank_ID"))
                    );
                    if(success){
                        JOptionPane.showMessageDialog(managerPanelPackage.getButtonPanel(),
                                "Data Inserted Successfully",
                                "Insert Data", JOptionPane.WARNING_MESSAGE);
                    }

                } catch (NumberFormatException err) {
                    JOptionPane.showMessageDialog(managerPanelPackage.getButtonPanel(),
                            "Please fill in the fields with valid data:\n",
                            "Invalid Data", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Update
        managerPanelPackage.getUpdateButton().addActionListener(e -> {
            if(managerPanelPackage.checkMandatoryFields()){
                try{
                    boolean success = db.updatePlant(Integer.parseInt(managerPanelPackage.getFieldText("Plant_ID")),
                            managerPanelPackage.getFieldText("Species"),
                            Float.parseFloat(managerPanelPackage.getFieldText("Living_Temp")),
                            Float.parseFloat(managerPanelPackage.getFieldText("Living_Light")),
                            Integer.parseInt(managerPanelPackage.getFieldText("Water_Tank_ID"))
                    );

                    if(success){
                        JOptionPane.showMessageDialog(managerPanelPackage.getButtonPanel(),
                                "Data Updated Successfully",
                                "Update Data", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (NumberFormatException err) {
                    JOptionPane.showMessageDialog(managerPanelPackage.getButtonPanel(),
                            "Please fill in the fields with valid data:\n",
                            "Invalid Data", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        managerPanelPackageMap.put("Plant",managerPanelPackage);
    }

    private void initializeComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout); // show and hide panels using their names

        JPanel homePanel = createHomePanel();
        cardsPanel.add(homePanel, "HomePanel");

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 0 means any number of rows, and 2 columns
        homePanel.add(buttonPanel, BorderLayout.CENTER);

        // Add show all list
        for (TablePackage tp : showAllTablePackages) {
            JPanel categoryPanel = createListAllPanel(tp);
            cardsPanel.add(categoryPanel, tp.getName() + "Panel");

            JButton button = new JButton("Show All " + tp.getName());
            button.setPreferredSize(buttonSize);
            button.addActionListener(e -> {
                tp.populateTable();
                cardLayout.show(cardsPanel, tp.getName() + "Panel");
            }
            );
            buttonPanel.add(button);
        }

        add(cardsPanel, BorderLayout.CENTER);


        for(ManagerPanelPackage pPackage: managerPanelPackageMap.values()){
            buttonPanel.add(pPackage.getMainButton());
        }

//
//
//
//        // adds the button for closing DB
//        JButton inventoryButton = new JButton("Manage Inventory");
//        inventoryButton.setPreferredSize(buttonSize);
//        inventoryButton.addActionListener(e -> inventoryPanel());
//        buttonPanel.add(inventoryButton);
//

        // adds the button for closing DB
        JButton closeButton = new JButton("Close connection");
        closeButton.setPreferredSize(buttonSize);
        closeButton.addActionListener(e -> closeDBPanel());
        buttonPanel.add(closeButton);


        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }

    private JPanel createHomePanel() {
        // we want welcomeLabel to take the top row by itself
        JPanel homePanel = new JPanel(new BorderLayout(10, 10)); // margins between components
        JLabel welcomeLabel = new JLabel("Welcome to Aquarium Manager!", SwingConstants.CENTER);
        // Add the welcome label to the top of the homePanel
        homePanel.add(welcomeLabel, BorderLayout.NORTH);
        return homePanel;
    }

    private JPanel createListAllPanel(TablePackage tablePackage) {
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.add(new JScrollPane(tablePackage.getTable()), BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton backButton = new JButton("Back to Home");

        backButton.addActionListener(e -> cardLayout.show(cardsPanel, "HomePanel"));
        buttonPanel.add(backButton);

        categoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        return categoryPanel;
    }

    private void connectDBPanel() {
        JFrame DBframe = new JFrame("Oracle DB Connection");
        DBframe.setSize(400, 200);

        // Create a panel to hold the components
        JPanel DBpanel = new JPanel();
        DBpanel.setLayout(new GridLayout(0, 2));
        DBframe.add(DBpanel);

        // Create labels, text fields for username and password
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameTextField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        // Connect Oracle button
        JButton connectButton = new JButton("Connect to Oracle DB");
        connectButton.addActionListener(e -> connectDBAndLaunchApp(DBframe, usernameTextField, passwordField));

        DBpanel.add(usernameLabel);
        DBpanel.add(usernameTextField);
        DBpanel.add(passwordLabel);
        DBpanel.add(passwordField);
        DBpanel.add(connectButton);

        DBframe.setVisible(true);
    };

    private void connectDBAndLaunchApp(JFrame DBframe, JTextField usernameTextField, JPasswordField passwordField) {
        // converts fields to strings
        String username = usernameTextField.getText();
        char[] passw = passwordField.getPassword();
        String password = new String(passw);
        // calls method from AquariumManagementDB()
        db = new AquariumManagementDB();
        boolean status = db.getConnection(username, password);
        if (status) {
            JOptionPane.showMessageDialog(DBframe, "Connected to Oracle DB successfully!");
            DBframe.dispose();
            initializeShowALlTables();
            initializeManagers();
            initializeComponents();
        } else {
            JOptionPane.showMessageDialog(DBframe, "Failed to connect to Oracle DB.");
        }
    };

    private void inventoryPanel() {
        JFrame DBframe = new JFrame("Inventory");
        DBframe.setSize(400, 200);

        // Create a panel to hold the components
        JPanel DBpanel = new JPanel();
        DBpanel.setLayout(new GridLayout(0, 2));
        DBframe.add(DBpanel);

        // Create labels, text fields for username and password
        JLabel idLabel = new JLabel("Id:");
        JTextField idTextField = new JTextField(20);

        JLabel locationLabel = new JLabel("Location:");
        JTextField locationField = new JTextField(20);

        JLabel shelfNumberLabel = new JLabel("Shelf Number:");
        JTextField shelfNumberField = new JTextField(20);

        JLabel IsFullLabel = new JLabel("Is Full:");
        JTextField isFullField = new JTextField(20);

        // Connect Oracle button
        JButton addButton = new JButton("Add to Oracle DB");
        addButton.addActionListener(e -> addInventoryPanel(DBframe, idTextField, locationField, shelfNumberField, isFullField));

        DBpanel.add(idLabel);
        DBpanel.add(idTextField);
        DBpanel.add(locationLabel);
        DBpanel.add(locationField);
        DBpanel.add(addButton);

        DBframe.setVisible(true);
    }

    private void addInventoryPanel(JFrame DBFrame, JTextField idTextField, JTextField locationField,
                                   JTextField shelfNumberField, JTextField isFullField) {
        // calls method from AquariumManagementDB()
        // converts fields to strings
        int id = Integer.parseInt(idTextField.getText());
        String location = locationField.getText();
        int shelf_number  = Integer.parseInt(shelfNumberField.getText());
        String isFull = isFullField.getText();

        boolean status = db.insertInventory(id, location, shelf_number, isFull);
        if (status) {
            JOptionPane.showMessageDialog(DBFrame, "Entry has been added successfully");
        } else {
            JOptionPane.showMessageDialog(DBFrame, "Error adding entry to database");
        }
    }

    private void closeDBPanel() {
        JFrame DBframe = new JFrame("Oracle DB Connection");
        DBframe.setSize(400, 200);

        // Create a panel to hold the components
        JPanel DBpanel = new JPanel();
        DBpanel.setLayout(new FlowLayout());
        DBframe.add(DBpanel);

        // adds the yes and no buttons and their event listeners
        JButton yesButton = new JButton("Yes");
        yesButton.addActionListener(e -> closeDB(DBframe));
        JButton noButton = new JButton("No");
        noButton.addActionListener(e -> cardLayout.show(cardsPanel, "HomePanel"));

        DBpanel.add(yesButton);
        DBpanel.add(noButton);

        DBframe.setVisible(true);

    }

    private void closeDB(JFrame DBframe) {
        // calls method from AquariumManagementDB()
        boolean status = db.closeConnection();
        if (status) {
            JOptionPane.showMessageDialog(DBframe, "Connection has been closed successfully");
        } else {
            JOptionPane.showMessageDialog(DBframe, "Failed to close connection to Oracle DB.");
        }
    };

    private void InventoryHelper(JFrame DBFrame, JTextField idTextField, JTextField locationField,
                                 JTextField shelfNumberField, JTextField isFullField, String operation) {
        // calls method from AquariumManagementDB()
        // converts fields to strings
        int id = Integer.parseInt(idTextField.getText());
        String location = locationField.getText();
        int shelf_number  = Integer.parseInt(shelfNumberField.getText());
        String isFull = isFullField.getText();
        boolean success = false;

        if (operation == "ADDITION") {
            success = db.insertInventory(id, location, shelf_number, isFull);
        } else if (operation == "UPDATE") {
            success = db.updateInventory(id, location, shelf_number, isFull);
        } else if (operation == "DELETE") {
            success = db.deleteInventory(id, shelf_number);
        }

        if (success) {
            JOptionPane.showMessageDialog(DBFrame, "Operation on INVENTORY has been performed successfully");
        } else {
            JOptionPane.showMessageDialog(DBFrame, "Operation on INVENTORY encountered an error");
        }
    }

    private ArrayList<ArrayList<Object>> InventoryListHelper(JFrame DBFrame, JTextField idTextField, JTextField locationField, String operation) {
        return null;
    }

    private void ExhibitHelper(JFrame DBFrame, JTextField idTextField, JTextField nameField, JTextField statusField, String operation) {
        // calls method from AquariumManagementDB()
        // converts fields to strings
        int id = Integer.parseInt(idTextField.getText());
        String name = nameField.getText();
        String status = statusField.getText();

        boolean success = false;

        if (operation == "ADDITION") {
            success = db.insertExhibit(id, name, status);
        } else if (operation == "UPDATE") {
            success = db.updateExhibit(id, name, status);
        } else if (operation == "DELETE") {
            success = db.deleteExhibit(id);
        }

        if (success) {
            JOptionPane.showMessageDialog(DBFrame, "Operation on EXHIBIT has been performed successfully");
        } else {
            JOptionPane.showMessageDialog(DBFrame, "Operation on EXHIBIT encountered an error");
        }
    }

    /*

    private void AnimalHelper(JFrame DBFrame, JTextField idTextField, JTextField nameField, JTextField speciesField,
                              JTextField ageField, JTextField livingTempField, JTextField waterTankIDField, JTextField veterinarianIDField, String operation) {
        // calls method from AquariumManagementDB()
        // converts fields to strings
        int id = Integer.parseInt(idTextField.getText());
        String name = nameField.getText();
        String species = speciesField.getText();
        int age = Integer.parseInt(ageField.getText());
        String living_temp = livingTempField.getText();
        int waterTankID = Integer.parseInt(waterTankIDField.getText());
        int veterinarianID = Integer.parseInt(veterinarianIDField.getText());

        boolean success = false;

        if (operation == "ADDITION") {
            success = db.insertAnimal(id, name, species, age, living_temp, waterTankID, veterinarianID);
        } else if (operation == "UPDATE") {
            success = db.updateAnimal(id, name, species, age, living_temp, waterTankID, veterinarianID);
        } else if (operation == "DELETE") {
            success = db.deleteAnimal(id);
        }

        if (success) {
            JOptionPane.showMessageDialog(DBFrame, "Operation on ANIMAL has been performed successfully");
        } else {
            JOptionPane.showMessageDialog(DBFrame, "Operation on ANIMAL encountered an error");
        }
    }

    private void ItemHelper(JFrame DBFrame, JTextField idTextField, JTextField nameField, JTextField quantityField, JTextField unitField, String operation) {
        // calls method from AquariumManagementDB()
        // converts fields to strings or ints
        int id = Integer.parseInt(idTextField.getText());
        String name = nameField.getText();
        int quantity = Integer.parseInt(quantityField.getText());
        String unit = unitField.getText();

        boolean success = false;

        if (operation == "ADDITION") {
            success = db.insertItem(id, name, quantity, unit);
        } else if (operation == "UPDATE") {
            success = db.updateItem(id, name, quantity, unit);
        } else if (operation == "DELETE") {
            success = db.deleteItem(id);
        }

        if (success) {
            JOptionPane.showMessageDialog(DBFrame, "Operation on ITEM has been performed successfully");
        } else {
            JOptionPane.showMessageDialog(DBFrame, "Operation on ITEM encountered an error");
        }
    }

    private void WaterTankHelper(JFrame DBFrame, JTextField idTextField, JTextField nameField, JTextField volumeField,
                                 JTextField temperatureField, JTextField lightingLevelField, JTextField exhibitIDField, JTextField pHField, String operation) {
        // calls method from AquariumManagementDB()
        // converts fields to strings
        int id = Integer.parseInt(idTextField.getText());
        String name = nameField.getText();
        float volume = Float.parseFloat(volumeField.getText());
        float temperature = Float.parseFloat(temperatureField.getText());
        String lighting_level = lightingLevelField.getText();
        int exhibitID = Integer.parseInt(exhibitIDField.getText());
        float pH = Float.parseFloat(pHField.getText());

        boolean success = false;

        if (operation == "ADDITION") {
            success = db.insertWaterTank(id, name, volume, temperature, lighting_level, exhibitID, pH);
        } else if (operation == "UPDATE") {
            success = db.updateWaterTank(id, name, volume, temperature, lighting_level, exhibitID, pH);
        } else if (operation == "DELETE") {
            success = db.deleteWaterTank(id);
        }

        if (success) {
            JOptionPane.showMessageDialog(DBFrame, "Operation on WATERTANK has been performed successfully");
        } else {
            JOptionPane.showMessageDialog(DBFrame, "Operation on WATERTANK encountered an error");
        }
    }
    */


}