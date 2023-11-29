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
        showAllTablePackages.add(new TablePackage("Animal", db::listAnimal));
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
                {"ANIMAL_NAME", "Name", "False","Enter Name"}, {"SPECIES", "Species","False"}, {"AGE", "Age", "False"},
                {"LIVINGTEMP", "Living Temperature(°C)", "False", "Enter Number"},
                {"WATER_TANK_ID","In Water Tank(ID)", "True"}, {"VETERINARIAN_ID", "Assigned Vet(ID)", "True"}};
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("Animal", fieldNames);
        //Search
        panelPkg.getSearchButton().addActionListener(e -> {
            int id =  Integer.parseInt(panelPkg.getFieldText("ID"));
            JSONObject dataFound = db.getAnimalByID(id);
            if(dataFound != null){
                panelPkg.showDbData(dataFound);
            } else {
                panelPkg.idNotExistPopup(id);
            }
        });
        managerPanelPackageMap.put("Animal",panelPkg);
    }

    private void addManagePlant() {
        String[][] fieldNames = {{"Plant_ID","ID", "True", "Enter ID"}, {"Species", "Species", "True"},
                {"Living_Temp", "Living Temperature(°C)", "True", "Enter Number"},
                {"Water_Tank_ID","In Water Tank(ID)", "True"}, {"Living_Light", "Light Level", "True"}};
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("Plant", fieldNames);
        // Search
        panelPkg.addSearchAction("Plant_ID", db::getPlantByID);
        // Delete
        panelPkg.addDeleteAction("Plant_ID", db::deletePlant);
        // Add
        panelPkg.getAddButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try{
                    int id = Integer.parseInt(panelPkg.getFieldText("Plant_ID"));
                    boolean success = db.insertPlant(id,
                            panelPkg.getFieldText("Species"),
                            Float.parseFloat(panelPkg.getFieldText("Living_Temp")),
                            Float.parseFloat(panelPkg.getFieldText("Living_Light")),
                            Integer.parseInt(panelPkg.getFieldText("Water_Tank_ID"))
                    );
                    if(success){
                        panelPkg.insertSuccessPopup(id);
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });
        // Update
        panelPkg.getUpdateButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try{
                    int id = Integer.parseInt(panelPkg.getFieldText("Plant_ID"));
                    boolean success = db.updatePlant(id,
                            panelPkg.getFieldText("Species"),
                            Float.parseFloat(panelPkg.getFieldText("Living_Temp")),
                            Float.parseFloat(panelPkg.getFieldText("Living_Light")),
                            Integer.parseInt(panelPkg.getFieldText("Water_Tank_ID"))
                    );
                    if(success){
                        panelPkg.updateSuccessPopup(id);
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });
        managerPanelPackageMap.put("Plant",panelPkg);
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

}