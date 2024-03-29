package AquariumManagement.src;

import org.json.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
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

    private List<TablePackage> tablePackages;
    private HashMap<String, ManagerPanelPackage> managerPanelPackageMap;
    public AquariumManagementUI() {
        super("Aquarium Manager");
        tablePackages = new ArrayList<TablePackage>();
        managerPanelPackageMap = new HashMap<String, ManagerPanelPackage>();
        connectDBPanel(); // connect DB and launch app
    }

    /*
    TODO: change this to parse data read from DB instead of hard coding if time allows
     */
    private void initializeTablePackages() {
        tablePackages.add(new TablePackage(this::showHome,"Plants", db::listPlants));
        tablePackages.add(new TablePackage(this::showHome,"Vendors", db::listVendors));
        tablePackages.add(new TablePackage(this::showHome,"Inventories", db::listInventory));
        tablePackages.add(new TablePackage(this::showHome,"Items", db::listItems));

        TablePackage equipTablePkg = new TablePackage(this::showHome,"Equipment", db::listEquipment);
        TablePackage tankTablePkg = new TablePackage(this::showHome,"Tank", db::listWaterTank);
        TablePackage staffTablePkg = new TablePackage(this::showHome,"Staffs", db::listStaff);
        TablePackage animalTablePkg = new TablePackage(this::showHome,"Animals", db::listAnimal);
        TablePackage plants = new TablePackage(this::showHome,"Plants", db::listPlants);

        
        tankTablePkg.setAdvancedSearch(getTankFields(), db::selectWaterTank);
        equipTablePkg.getButtonPanel().add(equipBySizeBtn(equipTablePkg));
        animalTablePkg.getButtonPanel().add(findAnimalExpertBtn(animalTablePkg));
        animalTablePkg.getButtonPanel().add(ageAboveTempBtn(animalTablePkg));
        plants.getButtonPanel().add(findDivingExpertInEachTankBtn(plants));


        tablePackages.add(staffTablePkg);
        tablePackages.add(animalTablePkg);
        tablePackages.add(equipTablePkg);
        tablePackages.add(tankTablePkg);
        tablePackages.add(plants);


    }

    private JButton findDivingExpertInEachTankBtn(TablePackage tp) {
        JButton btn = new JButton("Find Biodiversity in Tanks");

        btn.addActionListener(e -> {
            JDialog dialog = new JDialog();
            dialog.setTitle("Find Biodiversity in Tanks");
            dialog.setLayout(new BorderLayout());
            dialog.setSize(400, 150);

            JPanel inputPanel = new JPanel();
            JTextField inputField = new JTextField(20);
            inputPanel.add(new JLabel("Minimum Species Number"));
            inputPanel.add(inputField);

            JPanel buttonPanel = new JPanel();
            JButton findButton = new JButton("Find");
            JButton cancelButton = new JButton("Cancel");

            findButton.addActionListener(ev -> {
                String minLevelString = inputField.getText();
                int minLevel;
                try{
                    minLevel = Integer.parseInt(minLevelString);
                } catch (NumberFormatException err){
                    tp.invalidDataPopup();
                    return;
                }
                JSONArray dbData = db.getPlantDiversityInTanksAboveThreshold(minLevel);
                if (dbData == null || dbData.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "No Tanks Found", "Result", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    tp.updateTableWithAnyData(dbData);
                }

                dialog.dispose();
            });

            // Add action listener to cancelButton
            cancelButton.addActionListener(ev -> dialog.dispose());

            buttonPanel.add(findButton);
            buttonPanel.add(cancelButton);

            dialog.add(inputPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });

        return btn;
    }

    private JButton ageAboveTempBtn(TablePackage tp) {
        JButton btn = new JButton("Species & Age Above Temp");

        btn.addActionListener(e -> {
            JDialog dialog = new JDialog();
            dialog.setTitle("Specie & Age Above Temp");
            dialog.setLayout(new BorderLayout());
            dialog.setSize(300, 150);

            JPanel inputPanel = new JPanel();
            JTextField textField = new JTextField(20);
            inputPanel.add(new JLabel("Minimum Temperature:"));
            inputPanel.add(textField);

            JPanel buttonPanel = new JPanel();
            JButton findButton = new JButton("Find");
            JButton cancelButton = new JButton("Cancel");

            findButton.addActionListener(ev -> {

                double temp;
                try{
                    temp = Double.valueOf(textField.getText());
                } catch (NumberFormatException err){
                    tp.invalidDataPopup();
                    return;
                }


                JSONArray dbData = db.groupByAnimalSpeciesAndAverageAgeAboveLivingTemp((float)temp);
                if (dbData == null || dbData.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "No animal found", "Result", JOptionPane.INFORMATION_MESSAGE);
                    tp.clearTable();
                } else {
                    tp.updateTableWithAnyData(dbData);
                }

                dialog.dispose();
            });

            // Add action listener to cancelButton
            cancelButton.addActionListener(ev -> dialog.dispose());

            buttonPanel.add(findButton);
            buttonPanel.add(cancelButton);

            dialog.add(inputPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });

        return btn;
    }


    private JButton equipBySizeBtn(TablePackage tp){
        JButton btn = new JButton("Counts By Size");
        btn.addActionListener(e -> {
                JSONArray dbData = db.groupByEquipmentSize();
                if (dbData == null || dbData.isEmpty()) {
                    tp.noDataPopup();
                } else {
                    tp.updateTableWithAnyData(dbData);
                }
            });
        return btn;
    }
    private JButton findAnimalExpertBtn(TablePackage tp) {
        JButton btn = new JButton("Find Expert Vet");

        btn.addActionListener(e -> {
            JDialog dialog = new JDialog();
            dialog.setTitle("Find Expert Vet");
            dialog.setLayout(new BorderLayout());
            dialog.setSize(300, 150);

            JPanel inputPanel = new JPanel();
            JTextField speciesField = new JTextField(20);
            inputPanel.add(new JLabel("Species Name:"));
            inputPanel.add(speciesField);

            JPanel buttonPanel = new JPanel();
            JButton findButton = new JButton("Find");
            JButton cancelButton = new JButton("Cancel");

            findButton.addActionListener(ev -> {
                String species = speciesField.getText();
                JSONArray dbData = db.getAnimalExpertVets(species);
                if (dbData == null || dbData.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "No experts found for " + species, "Result", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    tp.updateTableWithAnyData(dbData);
                }

                dialog.dispose();
            });

            // Add action listener to cancelButton
            cancelButton.addActionListener(ev -> dialog.dispose());

            buttonPanel.add(findButton);
            buttonPanel.add(cancelButton);

            dialog.add(inputPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });

        return btn;
    }

    private JButton getHighSalaryBtn(){
        JButton btn = new JButton("Find High Salary");



        return btn;
    }

    private JSONArray getTankFields(){

        String tankFieldsData = "[{\"DB_NAME\":\"ID\", \"DISPLAY_NAME\":\"ID\", \"TYPE\":\"Int\"}," +
                "{\"DB_NAME\":\"WATER_TANK_LOGISTICS_NAME\", \"DISPLAY_NAME\":\"Name\", \"TYPE\":\"String\"}," +
                "{\"DB_NAME\":\"VOLUME\", \"DISPLAY_NAME\":\"Volume(l)\", \"TYPE\":\"Float\"}," +
                "{\"DB_NAME\":\"TEMPERATURE\", \"DISPLAY_NAME\":\"Temperature(°C)\", \"TYPE\":\"Float\"}," +
                "{\"DB_NAME\":\"LIGHTINGLEVEL\", \"DISPLAY_NAME\":\"Lighting Level\", \"TYPE\":\"String\"}," +
                "{\"DB_NAME\":\"EXHIBIT_ID\", \"DISPLAY_NAME\":\"In Exhibit(ID)\", \"TYPE\":\"Int\"}," +
                "{\"DB_NAME\":\"PH\", \"DISPLAY_NAME\":\"Ph\", \"TYPE\":Float}," +
                "{\"DB_NAME\":\"AQUARIST_ID\", \"DISPLAY_NAME\": \"Aquarist(ID)\", \"TYPE\":\"Int\"}]";
        return new JSONArray(tankFieldsData);
    }

    public void showHome(){
        cardLayout.show(cardsPanel, "HomePanel");
    }




    private void initializeManagers() {
        // TODO: Add DB NAME and Change inputFieldMap use DB Field name as key
        // each entry = {DB_FIELD_NAME, DISPLAY_NAME, PLACE_HOLDER(optional)}
        addManageInventory();
        addManageAnimal();
        addManagePlant();
        addManageWaterTank();

        // Recent addManagers
        addManageAquarist();
        addManageCustodian();
        addManageVeterinarian();
        addManageStaff();



        // Aggregation UI code
//        addAggregationPanel();


    }

    private void addManageInventory() {
        String[][] fieldNames = {{"ID","ID", "True", "Enter ID"},{"LOCATION", "Location", "True", "Enter Location"},
                {"SHELF_NUMBER", "Shelf Number", "True", "Enter Shelf_Number"}, {"IS_FULL", "Is_Full", "True", "Enter State"}};

        ManagerPanelPackage InventoryManager = new ManagerPanelPackage("Inventory", fieldNames);
        //Delete
        InventoryManager.addDeleteAction("ID", db::deleteInventory);
        // Add
        InventoryManager.getAddButton().addActionListener(e -> {
            if(InventoryManager.checkMandatoryFields()){
                try{
                    int id = Integer.parseInt(InventoryManager.getFieldText("ID"));
                    boolean success = db.insertInventory(id,
                            InventoryManager.getFieldText("LOCATION"),
                            InventoryManager.getFieldAsInt("SHELF_NUMBER"),
                            InventoryManager.getFieldText("IS_FULL")
                    );
                    if(success){
                        InventoryManager.insertSuccessPopup(id);
                    }
                } catch (NumberFormatException err) {
                    InventoryManager.invalidDataPopup();
                }
            }
        });
        // Update
        InventoryManager.getUpdateButton().addActionListener(e -> {
            if(InventoryManager.checkMandatoryFields()){
                try{
                    int id = Integer.parseInt(InventoryManager.getFieldText("ID"));
                    boolean success = db.updateInventory(id,
                            InventoryManager.getFieldText("LOCATION"),
                            InventoryManager.getFieldAsInt("SHELF_NUMBER"),
                            InventoryManager.getFieldText("IS_FULL")
                    );
                    if(success){
                        InventoryManager.insertSuccessPopup(id);
                    }
                } catch (NumberFormatException err) {
                    InventoryManager.invalidDataPopup();
                }
            }
        });

        InventoryManager.addSearchAction("ID", db::getInventoryByID);
        managerPanelPackageMap.put("Inventory",InventoryManager);
    }

    private void addManageAnimal() {
        String[][] fieldNames = {{"ID","ID", "True", "Enter ID"},
                {"ANIMAL_NAME", "Name", "False","Enter Name"}, {"SPECIES", "Species","False"}, {"AGE", "Age", "False"},
                {"LIVINGTEMP", "Living Temperature(°C)", "False", "Enter Number"},
                {"WATER_TANK_ID","In Water Tank(ID)", "True"}, {"VETERINARIAN_ID", "Assigned Vet(ID)", "True"}};
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("Animal", fieldNames);
        //Search
        panelPkg.addSearchAction("ID", db::getAnimalByID);
        //Delete
        panelPkg.addDeleteAction("ID", db::deleteAnimal);
        //Add
        panelPkg.getAddButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try{
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    boolean success = db.insertAnimal(id,
                            panelPkg.getFieldText("ANIMAL_NAME"),
                            panelPkg.getFieldText("SPECIES"),
                            panelPkg.getFieldAsInt("AGE"),
                            panelPkg.getFieldAsFloat("LIVINGTEMP"),
                            panelPkg.getFieldAsInt("WATER_TANK_ID"),
                            panelPkg.getFieldAsInt("VETERINARIAN_ID")
                    );
                    if(success){
                        panelPkg.insertSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
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
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    boolean success = db.updateAnimal(id,
                            panelPkg.getFieldText("ANIMAL_NAME"),
                            panelPkg.getFieldText("SPECIES"),
                            panelPkg.getFieldAsInt("AGE"),
                            panelPkg.getFieldAsFloat("LIVINGTEMP"),
                            panelPkg.getFieldAsInt("WATER_TANK_ID"),
                            panelPkg.getFieldAsInt("VETERINARIAN_ID")
                    );
                    if(success){
                        panelPkg.updateSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        managerPanelPackageMap.put("Animal",panelPkg);
    }

    private void addManagePlant() {
        String[][] fieldNames = {{"PLANT_ID","ID", "True", "Enter ID"}, {"SPECIES", "Species", "True"},
                {"LIVING_TEMP", "Living Temperature(°C)", "True", "Enter Number"},
                {"WATER_TANK_ID","In Water Tank(ID)", "True"}, {"LIVING_LIGHT", "Light Level", "True"}};
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("Plant", fieldNames);
        // Search
        panelPkg.addSearchAction("PLANT_ID", db::getPlantByID);
        // Delete
        panelPkg.addDeleteAction("PLANT_ID", db::deletePlant);
        // Add
        panelPkg.getAddButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try{
                    int id = Integer.parseInt(panelPkg.getFieldText("PLANT_ID"));
                    boolean success = db.insertPlant(id,
                            panelPkg.getFieldText("SPECIES"),
                            Float.parseFloat(panelPkg.getFieldText("LIVING_TEMP")),
                            Float.parseFloat(panelPkg.getFieldText("LIVING_LIGHT")),
                            Integer.parseInt(panelPkg.getFieldText("WATER_TANK_ID"))
                    );
                    if(success){
                        panelPkg.insertSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
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
                    int id = Integer.parseInt(panelPkg.getFieldText("PLANT_ID"));
                    boolean success = db.updatePlant(id,
                            panelPkg.getFieldText("SPECIES"),
                            Float.parseFloat(panelPkg.getFieldText("LIVING_TEMP")),
                            Float.parseFloat(panelPkg.getFieldText("LIVING_LIGHT")),
                            Integer.parseInt(panelPkg.getFieldText("WATER_TANK_ID"))
                    );
                    if(success){
                        panelPkg.updateSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });
        managerPanelPackageMap.put("Plant",panelPkg);
    }
    private void addManageAquarist() {
        String[][] fieldNames = {
                {"ID", "ID", "True", "Enter ID"},
                {"DIVING_LEVEL", "Diving Level", "True", "Enter Diving Level"},
                {"WATER_TANK_ID", "Water Tank ID", "True", "Enter Water Tank ID"}
        };

        // Create the ManagerPanelPackage for Aquarist
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("Aquarist", fieldNames);

        // Search action: Retrieve details of an Aquarist by ID
        panelPkg.addSearchAction("ID", db::getAquaristByID);

        // Delete action: Remove an Aquarist entry by ID
        panelPkg.addDeleteAction("ID", db::deleteAquarist);

        // Add action: Create a new Aquarist entry
        panelPkg.getAddButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try {
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    BigDecimal divingLevel = new BigDecimal(panelPkg.getFieldText("DIVING_LEVEL"));
                    int waterTankId = Integer.parseInt(panelPkg.getFieldText("WATER_TANK_ID"));
                    boolean success = db.insertAquarist(id, divingLevel, waterTankId);
                    if(success){
                        panelPkg.insertSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        // Update action: Modify an existing Aquarist entry
        panelPkg.getUpdateButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try {
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    BigDecimal divingLevel = new BigDecimal(panelPkg.getFieldText("DIVING_LEVEL"));
                    int waterTankId = Integer.parseInt(panelPkg.getFieldText("WATER_TANK_ID"));
                    boolean success = db.updateAquarist(id, divingLevel, waterTankId);
                    if(success){
                        panelPkg.updateSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        managerPanelPackageMap.put("Aquarist", panelPkg);
    }

    private void addManageCustodian() {
        String[][] fieldNames = {
                {"ID", "ID", "True", "Enter ID"},
                {"EXHIBIT_ID", "Exhibit ID", "True", "Enter Exhibit ID"}
        };

        // Create the ManagerPanelPackage for Custodian
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("Custodian", fieldNames);

        // Search action: Retrieve details of a Custodian by ID
        panelPkg.addSearchAction("ID", db::getCustodianByID);

        // Delete action: Remove a Custodian entry by ID
        panelPkg.addDeleteAction("ID", db::deleteCustodian);

        // Add action: Create a new Custodian entry
        panelPkg.getAddButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try {
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    int exhibitId = Integer.parseInt(panelPkg.getFieldText("EXHIBIT_ID"));
                    boolean success = db.insertCustodian(id, exhibitId);
                    if(success){
                        panelPkg.insertSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        // Update action: Modify an existing Custodian entry
        panelPkg.getUpdateButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try {
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    int exhibitId = Integer.parseInt(panelPkg.getFieldText("EXHIBIT_ID"));
                    boolean success = db.updateCustodian(id, exhibitId);
                    if(success){
                        panelPkg.updateSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        managerPanelPackageMap.put("Custodian", panelPkg);
    }

    private void addManageVeterinarian() {
        String[][] fieldNames = {
                {"ID", "ID", "True", "Enter ID"}
        };

        // Create the ManagerPanelPackage for Veterinarian
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("Veterinarian", fieldNames);

        // Search action: Retrieve details of a Veterinarian by ID
        panelPkg.addSearchAction("ID", db::getVeterinarianByID);

        // Delete action: Remove a Veterinarian entry by ID
        panelPkg.addDeleteAction("ID", db::deleteVeterinarian);

        // Add action: Create a new Veterinarian entry
        panelPkg.getAddButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try {
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    boolean success = db.insertVeterinarian(id);
                    if(success){
                        panelPkg.insertSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        managerPanelPackageMap.put("Veterinarian", panelPkg);
    }

    private void addManageStaff() {
        String[][] fieldNames = {
                {"ID", "ID", "True", "Enter ID"},
                {"SALARY", "Salary", "True", "Enter Salary"},
                {"STAFF_NAME", "Staff Name", "True", "Enter Staff Name"},
                {"DATEHIRED", "Date Hired", "True", "Enter Date Hired (YYYY-MM-DD)"}
        };

        // Create the ManagerPanelPackage for Staff
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("Staff", fieldNames);

        // Search action: Retrieve details of a Staff by ID
        panelPkg.addSearchAction("ID", db::getStaffByID);

        // Delete action: Remove a Staff entry by ID
        panelPkg.addDeleteAction("ID", db::deleteStaff);

        // Add action: Create a new Staff entry
        panelPkg.getAddButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try {
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    BigDecimal salary = new BigDecimal(panelPkg.getFieldText("SALARY"));
                    String staffName = panelPkg.getFieldText("STAFF_NAME");
                    Date dateHired = Date.valueOf(panelPkg.getFieldText("DATEHIRED")); // Assumes the date is entered in the format YYYY-MM-DD
                    boolean success = db.insertStaff(id, salary, staffName, dateHired);
                    if(success){
                        panelPkg.insertSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (IllegalArgumentException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        // Update action: Modify an existing Staff entry
        panelPkg.getUpdateButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try {
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    BigDecimal salary = new BigDecimal(panelPkg.getFieldText("SALARY"));
                    String staffName = panelPkg.getFieldText("STAFF_NAME");
                    Date dateHired = Date.valueOf(panelPkg.getFieldText("DATEHIRED")); // Assumes the date is entered in the format YYYY-MM-DD
                    boolean success = db.updateStaff(id, salary, staffName, dateHired);
                    if(success){
                        panelPkg.updateSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (IllegalArgumentException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        managerPanelPackageMap.put("Staff", panelPkg);
    }





    // TODO: Currently no supporting data available. test this when data is ready
    private void addManageWaterTank() {
        String[][] fieldNames = {{"ID","ID", "True", "Enter ID"},
                {"WATER_TANK_LOGISTICS_NAME", "Name", "True","Enter Name"},
                {"VOLUME", "Volume(l)","True"}, {"TEMPERATURE", "Temperature(°C)", "True"},
                {"LIGHTINGLEVEL", "Lighting Level", "True"},
                {"EXHIBIT_ID","In Exhibit(ID)", "True"},
                {"PH","Ph", "True"}, {"AQUARIST_ID","Aquarist Assigned(ID)", "True"}};
        ManagerPanelPackage panelPkg = new ManagerPanelPackage("WaterTank", fieldNames);
        //Search
        panelPkg.addSearchAction("ID", db::getWaterTankByID);
        //Delete
        panelPkg.addDeleteAction("ID", db::deleteWaterTank);
        //Add
        panelPkg.getAddButton().addActionListener(e -> {
            if(panelPkg.checkMandatoryFields()){
                try{
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    boolean success = db.insertWaterTank(id,
                            panelPkg.getFieldText("WATER_TANK_LOGISTICS_NAME"),
                            panelPkg.getFieldAsFloat("VOLUME"),
                            panelPkg.getFieldAsFloat("TEMPERATURE"),
                            panelPkg.getFieldText("LIGHTINGLEVEL"),
                            panelPkg.getFieldAsInt("EXHIBIT_ID"),
                            panelPkg.getFieldAsFloat("PH"),
                            panelPkg.getFieldAsInt("AQUARIST_ID")
                    );
                    if(success){
                        panelPkg.insertSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
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
                    int id = Integer.parseInt(panelPkg.getFieldText("ID"));
                    boolean success = db.updateWaterTank(id,
                            panelPkg.getFieldText("WATER_TANK_LOGISTICS_NAME"),
                            panelPkg.getFieldAsFloat("VOLUME"),
                            panelPkg.getFieldAsFloat("TEMPERATURE"),
                            panelPkg.getFieldText("LIGHTINGLEVEL"),
                            panelPkg.getFieldAsInt("EXHIBIT_ID"),
                            panelPkg.getFieldAsFloat("PH"),
                            panelPkg.getFieldAsInt("AQUARIST_ID")
                    );
                    if(success){
                        panelPkg.updateSuccessPopup(id);
                    } else {
                        panelPkg.invalidDataPopup();
                    }
                } catch (NumberFormatException err) {
                    panelPkg.invalidDataPopup();
                }
            }
        });

        managerPanelPackageMap.put("WaterTank",panelPkg);
    }
    private void initializeComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(new BorderLayout());
        this.cardLayout = new CardLayout();

        cardsPanel = new JPanel(cardLayout); // show and hide panels using their names

        JPanel homePanel = createHomePanel();
        cardsPanel.add(homePanel, "HomePanel");

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 0 means any number of rows, and 2 columns
        homePanel.add(buttonPanel, BorderLayout.CENTER);
        initializeTablePackages();
        initializeManagers();
        // Add show all list
        for (TablePackage tp : tablePackages) {
            cardsPanel.add(tp.getPackagePanel(), tp.getName() + "Panel");
            JButton button = new JButton("Show " + tp.getName() +" List");
            button.setPreferredSize(buttonSize);
            button.addActionListener(e -> {
                tp.populateTable();
                cardLayout.show(cardsPanel, tp.getName() + "Panel");
            }
            );
            buttonPanel.add(button);
        }
        RawDataTablePackage rdtp = new RawDataTablePackage(this::showHome, "View DB Tables", db);
        cardsPanel.add(rdtp.getPackagePanel(), rdtp.getName());
        JButton btn = new JButton(rdtp.getName());
        btn.setPreferredSize(buttonSize);
        btn.addActionListener(e ->{
            cardLayout.show(cardsPanel, rdtp.getName());
        });
        buttonPanel.add(btn);


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