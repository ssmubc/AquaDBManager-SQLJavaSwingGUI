package AquariumManagement.src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class AquariumManagementUI extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private Map<String, JPanel> categoryPanels; // HashMap to store category panels
    // declaring the database
    private AquariumManagementDB db;

    public AquariumManagementUI() {
        super("Aquarium Manager");
        categoryPanels = new HashMap<>();
        initializeComponents();
    }

    private void initializeComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        JPanel homePanel = createHomePanel();
        cardsPanel.add(homePanel, "HomePanel");

        // adds the button for connecting to DB
        JButton connectButton = new JButton("Connect to Aquarium Database");
        connectButton.addActionListener(e -> connectDBPanel());
        homePanel.add(connectButton);

        // Create and add each category panel to the card layout and the HashMap
        String[] categories = {"Animal", "Exhibit", "Custodian", "Aquarist", "Veterinarian"};
        for (String category : categories) {
            JPanel categoryPanel = createCategoryPanel(category);
            cardsPanel.add(categoryPanel, category + "Panel");
            categoryPanels.put(category, categoryPanel); // Store the panel in the HashMap

            JButton button = new JButton("Manage " + category);
            button.addActionListener(e -> cardLayout.show(cardsPanel, category + "Panel"));
            homePanel.add(button);
        }

        add(cardsPanel, BorderLayout.CENTER);
        // adds the button for closing DB
        JButton inventoryButton = new JButton("Manage Inventory");
        inventoryButton.addActionListener(e -> inventoryPanel());
        homePanel.add(inventoryButton);


        // adds the button for closing DB
        JButton staffButton = new JButton("Manage Staff");
        staffButton.addActionListener(e -> staffPanel());
        homePanel.add(staffButton);

        // adds the button for closing DB
        JButton plantButton = new JButton("Manage Plant");
        plantButton.addActionListener(e -> grownInPlantPanel());
        homePanel.add(plantButton);

        JButton vendorReputationButton = new JButton("Manage Vendor Reputation");
        vendorReputationButton.addActionListener(e -> VendorReputationPanel());
        homePanel.add(vendorReputationButton);

        JButton vendorLogisticsButton = new JButton("Manage Vendor Logistics");
        vendorLogisticsButton.addActionListener(e -> vendorLogisticsPanel());
        homePanel.add(vendorLogisticsButton);

        // adds the button for closing DB
        JButton closeButton = new JButton("Close connection");
        closeButton.addActionListener(e -> closeDBPanel());
        homePanel.add(closeButton);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }

    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new GridLayout(0, 1)); // Adjusted for layout purposes
        homePanel.add(new JLabel("Welcome to Aquarium Manager!"));
        return homePanel;
    }

    private JPanel createCategoryPanel(String category) {
        JPanel categoryPanel = new JPanel(new BorderLayout());
        JTable table = new JTable();

        // Sample data for demonstration purposes
        Object[][] sampleData = {
                {"Sample 1", "Data 1", "Description 1"},
                {"Sample 2", "Data 2", "Description 2"},
                {"Sample 3", "Data 3", "Description 3"}
        };
        String[] columnNames = {"Name", "Details", "Description"};

        DefaultTableModel tableModel = new DefaultTableModel(sampleData, columnNames);
        table.setModel(tableModel);
        categoryPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Input fields
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        List<JTextField> inputFields = new ArrayList<>();

        // Function to add a new text field to the input panel
        Consumer<String> addInputField = (text) -> {
            JTextField field = new JTextField(text);
            inputFields.add(field);
            inputPanel.add(field);
            inputPanel.add(Box.createRigidArea(new Dimension(5, 0))); // Spacer
        };

        // Add existing columns as input fields
        for (String colName : columnNames) {
            addInputField.accept("");
        }

        categoryPanel.add(inputPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add " + category);
        JButton removeButton = new JButton("Remove " + category);
        JButton backButton = new JButton("Back to Home");
        JButton addAttributeButton = new JButton("Add Attribute");
        JButton removeAttributeButton = new JButton("Remove Attribute");
        // Add an action listener to the add attribute button
        addAttributeButton.addActionListener(e -> {
            String attributeName = JOptionPane.showInputDialog(categoryPanel, "Enter the name of the new attribute:");
            if (attributeName != null && !attributeName.trim().isEmpty()) {
                tableModel.addColumn(attributeName);
                addInputField.accept(""); // Add a new input field for the new attribute
                inputPanel.revalidate();  // Refresh the input panel
                inputPanel.repaint();
            }
        });

        // Add an action listener to the add button for adding new data rows
        addButton.addActionListener(e -> {
            // Check if input fields are filled out
            if (inputFields.stream().allMatch(field -> !field.getText().trim().isEmpty())) {
                Object[] row = inputFields.stream().map(JTextField::getText).toArray();
                tableModel.addRow(row);
                inputFields.forEach(field -> field.setText("")); // Clear input fields
            } else {
                JOptionPane.showMessageDialog(categoryPanel, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        removeAttributeButton.addActionListener(e -> {
            String attributeName = JOptionPane.showInputDialog(categoryPanel, "Enter the name of the attribute to remove:");
            if (attributeName != null && !attributeName.trim().isEmpty()) {
                // Find the column index by the attribute (column) name
                int colIndex = tableModel.findColumn(attributeName);
                if (colIndex != -1) {
                    TableColumn toRemove = table.getColumnModel().getColumn(colIndex);
                    table.removeColumn(toRemove); // Remove the column from the view
                    tableModel.setColumnCount(tableModel.getColumnCount() - 1); // Update the column count

                    // Remove the corresponding input field
                    JTextField fieldToRemove = inputFields.get(colIndex);
                    inputPanel.remove(fieldToRemove);
                    inputFields.remove(fieldToRemove);

                    // Refresh the input panel
                    inputPanel.revalidate();
                    inputPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(categoryPanel, "Attribute not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        backButton.addActionListener(e -> cardLayout.show(cardsPanel, "HomePanel"));

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(backButton);
        buttonPanel.add(addAttributeButton);
        buttonPanel.add(removeAttributeButton);
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
        connectButton.addActionListener(e -> connectDB(DBframe, usernameTextField, passwordField));

        DBpanel.add(usernameLabel);
        DBpanel.add(usernameTextField);
        DBpanel.add(passwordLabel);
        DBpanel.add(passwordField);
        DBpanel.add(connectButton);

        DBframe.setVisible(true);
    };

    private void connectDB(JFrame DBframe, JTextField usernameTextField, JPasswordField passwordField) {
        // converts fields to strings
        String username = usernameTextField.getText();
        char[] passw = passwordField.getPassword();
        String password = new String(passw);
        // calls method from AquariumManagementDB()
        db = new AquariumManagementDB();
        boolean status = db.getConnection(username, password);
        if (status) {
            JOptionPane.showMessageDialog(DBframe, "Connected to Oracle DB successfully!");
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
        JTextField locationField = new JPasswordField(20);
        // Connect Oracle button
        JButton addButton = new JButton("Add to Oracle DB");
        addButton.addActionListener(e -> addInventoryPanel(DBframe, idTextField, locationField));

        DBpanel.add(idLabel);
        DBpanel.add(idTextField);
        DBpanel.add(locationLabel);
        DBpanel.add(locationField);
        DBpanel.add(addButton);

        DBframe.setVisible(true);
    }

    private void addInventoryPanel(JFrame DBFrame, JTextField idTextField, JTextField locationField) {
        // calls method from AquariumManagementDB()
        // converts fields to strings
        int id = Integer.parseInt(idTextField.getText());
        String location = locationField.getText();

        boolean status = db.insertInventory(id, location);
        if (status) {
            JOptionPane.showMessageDialog(DBFrame, "Entry has been added successfully");
        } else {
            JOptionPane.showMessageDialog(DBFrame, "Error adding entry to database");
        }
    }

    // FOR STAFF
    private void staffPanel() {
        JFrame DBframe = new JFrame("Staff");
        DBframe.setSize(400, 200);

        // Create a panel to hold the components
        JPanel DBpanel = new JPanel();
        DBpanel.setLayout(new GridLayout(0, 2));
        DBframe.add(DBpanel);

        // Create labels and text fields for staff details
        JLabel idLabel = new JLabel("Id:");
        JTextField idTextField = new JTextField(20);

        JLabel salaryLabel = new JLabel("Salary:");
        JTextField salaryTextField = new JTextField(20);

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameTextField = new JTextField(20);

        JLabel dateHiredLabel = new JLabel("Date Hired (YYYY-MM-DD):");
        JTextField dateHiredTextField = new JTextField(20);

        // Add Staff to Oracle DB button
        JButton addButton = new JButton("Add Staff to Oracle DB");
        addButton.addActionListener(e -> addStaffPanel(DBframe, idTextField, salaryTextField, nameTextField, dateHiredTextField));

        // Update Staff in Oracle DB button
        JButton updateButton = new JButton("Update Staff in Oracle DB");
        updateButton.addActionListener(e -> updateStaffPanel(DBframe, idTextField, salaryTextField, nameTextField, dateHiredTextField));

        JButton deleteButton = new JButton("Delete Staff from Oracle DB");
        deleteButton.addActionListener(e -> deleteStaffPanel(DBframe, idTextField));
        DBpanel.add(deleteButton);


        // Add components to panel
        DBpanel.add(idLabel);
        DBpanel.add(idTextField);
        DBpanel.add(salaryLabel);
        DBpanel.add(salaryTextField);
        DBpanel.add(nameLabel);
        DBpanel.add(nameTextField);
        DBpanel.add(dateHiredLabel);
        DBpanel.add(dateHiredTextField);
        DBpanel.add(addButton);
        DBpanel.add(updateButton);

        // Show the frame
        DBframe.setVisible(true);
    }

    private void addStaffPanel(JFrame DBFrame, JTextField idTextField, JTextField salaryTextField, JTextField nameTextField, JTextField dateHiredTextField) {
        try {
            // Convert fields to appropriate types
            int id = Integer.parseInt(idTextField.getText());
            float salary = Float.parseFloat(salaryTextField.getText());
            String name = nameTextField.getText();
            String dateHired = dateHiredTextField.getText();

            System.out.println("Here it works");

            // Call method from AquariumManagementDB
            boolean status = db.insertStaff(id, salary, name, dateHired);
            if (status) {
                System.out.println("Here it works");
                JOptionPane.showMessageDialog(DBFrame, "Staff entry has been added successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "Error adding staff entry to database");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid input. Please check the data types.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStaffPanel(JFrame DBFrame, JTextField idTextField, JTextField salaryTextField, JTextField nameTextField, JTextField dateHiredTextField) {
        try {
            // Convert fields to appropriate types
            int id = Integer.parseInt(idTextField.getText());
            float salary = Float.parseFloat(salaryTextField.getText());
            String name = nameTextField.getText();
            String dateHired = dateHiredTextField.getText();

            // Call method from AquariumManagementDB to update staff
            boolean status = db.updateStaff(id, salary, name, dateHired);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Staff entry has been updated successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "Error updating staff entry in database");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid input. Please check the data types.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DBFrame, "An error occurred: " + ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStaffPanel(JFrame DBFrame, JTextField idTextField) {
        try {
            // Convert ID field to appropriate type
            int id = Integer.parseInt(idTextField.getText());

            // Call method from AquariumManagementDB to delete staff
            boolean status = db.deleteStaff(id);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Staff entry has been deleted successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "No such entry exists, nothing to delete");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid ID input. Please enter a numeric ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DBFrame, "An error occurred: " + ex.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void grownInPlantPanel() {
        JFrame DBframe = new JFrame("Grown In Plant");
        DBframe.setSize(400, 300); // Adjusted for additional fields

        // Create a panel to hold the components
        JPanel DBpanel = new JPanel();
        DBpanel.setLayout(new GridLayout(0, 2)); // 2 columns for label and text field
        DBframe.add(DBpanel);

        // Create labels and text fields for plant details
        JLabel plantIdLabel = new JLabel("Plant ID:");
        JTextField plantIdTextField = new JTextField(20);
        JLabel speciesLabel = new JLabel("Species:");
        JTextField speciesTextField = new JTextField(20);
        JLabel tempLabel = new JLabel("Living Temp:");
        JTextField tempTextField = new JTextField(20);
        JLabel lightLabel = new JLabel("Living Light:");
        JTextField lightTextField = new JTextField(20);
        JLabel waterTankIdLabel = new JLabel("Water Tank ID:");
        JTextField waterTankIdTextField = new JTextField(20);

        // Add Plant to Oracle DB button
        JButton addButton = new JButton("Add Plant to Oracle DB");
        addButton.addActionListener(e -> addPlantPanel(DBframe, plantIdTextField, speciesTextField, tempTextField, lightTextField, waterTankIdTextField));

        // Update Plant in Oracle DB button (assuming you have an update method)
        JButton updateButton = new JButton("Update Plant in Oracle DB");
        updateButton.addActionListener(e -> updatePlantPanel(DBframe, plantIdTextField, speciesTextField, tempTextField, lightTextField, waterTankIdTextField));

        // Delete Plant from Oracle DB button (assuming you have a delete method)
        JButton deleteButton = new JButton("Delete Plant from Oracle DB");
        deleteButton.addActionListener(e -> deletePlantPanel(DBframe, plantIdTextField));

        // Add components to panel
        DBpanel.add(plantIdLabel);
        DBpanel.add(plantIdTextField);
        DBpanel.add(speciesLabel);
        DBpanel.add(speciesTextField);
        DBpanel.add(tempLabel);
        DBpanel.add(tempTextField);
        DBpanel.add(lightLabel);
        DBpanel.add(lightTextField);
        DBpanel.add(waterTankIdLabel);
        DBpanel.add(waterTankIdTextField);
        DBpanel.add(addButton);
        DBpanel.add(updateButton);
        DBpanel.add(deleteButton); // You may need to adjust the layout for this extra button

        // Show the frame
        DBframe.setVisible(true);
    }

    private void addPlantPanel(JFrame DBFrame, JTextField plantIdTextField, JTextField speciesTextField, JTextField tempTextField, JTextField lightTextField, JTextField waterTankIdTextField) {
        try {
            int plantId = Integer.parseInt(plantIdTextField.getText());
            String species = speciesTextField.getText();
            float livingTemp = Float.parseFloat(tempTextField.getText());
            float livingLight = Float.parseFloat(lightTextField.getText());
            int waterTankId = Integer.parseInt(waterTankIdTextField.getText());

            boolean status = db.insertPlant(plantId, species, livingTemp, livingLight, waterTankId);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Plant entry has been added successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "Error adding plant entry to database");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid input. Please check the data types.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePlantPanel(JFrame DBFrame, JTextField plantIdTextField, JTextField speciesTextField, JTextField tempTextField, JTextField lightTextField, JTextField waterTankIdTextField) {
        try {
            int plantId = Integer.parseInt(plantIdTextField.getText());
            String species = speciesTextField.getText();
            float livingTemp = Float.parseFloat(tempTextField.getText());
            float livingLight = Float.parseFloat(lightTextField.getText());
            int waterTankId = Integer.parseInt(waterTankIdTextField.getText());

            boolean status = db.updatePlant(plantId, species, livingTemp, livingLight, waterTankId);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Plant entry has been updated successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "Error updating plant entry in database");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid input. Please check the data types.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePlantPanel(JFrame DBFrame, JTextField plantIdTextField) {
        try {
            int plantId = Integer.parseInt(plantIdTextField.getText());

            boolean status = db.deletePlant(plantId);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Plant entry has been deleted successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "No such plant entry exists, nothing to delete");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid plant ID input. Please enter a numeric plant ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void VendorReputationPanel() {
        JFrame DBframe = new JFrame("Vendor Reputation");
        DBframe.setSize(400, 200); // Size adjusted for fewer fields

        // Create a panel to hold the components
        JPanel DBpanel = new JPanel();
        DBpanel.setLayout(new GridLayout(0, 2)); // 2 columns for label and text field
        DBframe.add(DBpanel);

        // Create labels and text fields for vendor details
        JLabel vendorNameLabel = new JLabel("Vendor Name:");
        JTextField vendorNameTextField = new JTextField(20);
        JLabel marketRatingLabel = new JLabel("Market Rating:");
        JTextField marketRatingTextField = new JTextField(20);

        // Add Vendor to Oracle DB button
        JButton addButton = new JButton("Add Vendor to Oracle DB");
        addButton.addActionListener(e -> addVendorPanel(DBframe, vendorNameTextField, marketRatingTextField));

        // Update Vendor in Oracle DB button (assuming you have an update method)
        JButton updateButton = new JButton("Update Vendor in Oracle DB");
        updateButton.addActionListener(e -> updateVendorPanel(DBframe, vendorNameTextField, marketRatingTextField));

        // Delete Vendor from Oracle DB button (assuming you have a delete method)
        JButton deleteButton = new JButton("Delete Vendor from Oracle DB");
        deleteButton.addActionListener(e -> deleteVendorPanel(DBframe, vendorNameTextField));

        // Add components to panel
        DBpanel.add(vendorNameLabel);
        DBpanel.add(vendorNameTextField);
        DBpanel.add(marketRatingLabel);
        DBpanel.add(marketRatingTextField);
        DBpanel.add(addButton);
        DBpanel.add(updateButton);
        DBpanel.add(deleteButton);

        // Show the frame
        DBframe.setVisible(true);
    }







    // Method to add a new vendor reputation record
    private void addVendorPanel(JFrame DBFrame, JTextField vendorNameTextField, JTextField marketRatingTextField) {
        try {
            String vendorName = vendorNameTextField.getText();
            String marketRating = marketRatingTextField.getText();

            boolean status = db.insertVendor(vendorName, marketRating);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Vendor reputation entry has been added successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "Error adding vendor reputation entry to database");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DBFrame, "An error occurred: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to update an existing vendor reputation record
    private void updateVendorPanel(JFrame DBFrame, JTextField vendorNameTextField, JTextField marketRatingTextField) {
        try {
            String vendorName = vendorNameTextField.getText();
            String marketRating = marketRatingTextField.getText();

            boolean status = db.updateVendor(vendorName, marketRating);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Vendor reputation entry has been updated successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "Error updating vendor reputation entry in database");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DBFrame, "An error occurred: " + ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to delete an existing vendor reputation record
    private void deleteVendorPanel(JFrame DBFrame, JTextField vendorNameTextField) {
        try {
            String vendorName = vendorNameTextField.getText();

            boolean status = db.deleteVendor(vendorName);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Vendor reputation entry has been deleted successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "No such vendor reputation entry exists, nothing to delete");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DBFrame, "An error occurred: " + ex.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void vendorLogisticsPanel() {
        JFrame DBframe = new JFrame("Vendor Logistics");
        DBframe.setSize(500, 300);

        // Create a panel to hold the components
        JPanel DBpanel = new JPanel();
        DBpanel.setLayout(new GridLayout(0, 2));
        DBframe.add(DBpanel);

        // Create labels and text fields for vendor logistics details
        JLabel idLabel = new JLabel("ID:");
        JTextField idTextField = new JTextField(20);
        JLabel logisticsNameLabel = new JLabel("Logistics Name:");
        JTextField logisticsNameTextField = new JTextField(20);
        JLabel addressLabel = new JLabel("Address:");
        JTextField addressTextField = new JTextField(20);

        // Add Vendor Logistics to Oracle DB button
        JButton addButton = new JButton("Add Vendor Logistics to Oracle DB");
        addButton.addActionListener(e -> addVendorLogisticsPanel(DBframe, idTextField, logisticsNameTextField, addressTextField));

        // Update Vendor Logistics in Oracle DB button
        JButton updateButton = new JButton("Update Vendor Logistics in Oracle DB");
        updateButton.addActionListener(e -> updateVendorLogisticsPanel(DBframe, idTextField, logisticsNameTextField, addressTextField));

        // Delete Vendor Logistics from Oracle DB button
        JButton deleteButton = new JButton("Delete Vendor Logistics from Oracle DB");
        deleteButton.addActionListener(e -> deleteVendorLogisticsPanel(DBframe, idTextField));

        // Add components to panel
        DBpanel.add(idLabel);
        DBpanel.add(idTextField);
        DBpanel.add(logisticsNameLabel);
        DBpanel.add(logisticsNameTextField);
        DBpanel.add(addressLabel);
        DBpanel.add(addressTextField);
        DBpanel.add(addButton);
        DBpanel.add(updateButton);
        DBpanel.add(deleteButton);

        // Show the frame
        DBframe.setVisible(true);
    }

    // Method to add a new vendor logistics record
    private void addVendorLogisticsPanel(JFrame DBFrame, JTextField idTextField, JTextField logisticsNameTextField, JTextField addressTextField) {
        try {
            int id = Integer.parseInt(idTextField.getText());
            String logisticsName = logisticsNameTextField.getText();
            String address = addressTextField.getText();

            boolean status = db.insertVendorLogistics(id, logisticsName, address);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Vendor logistics entry has been added successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "Error adding vendor logistics entry to database");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid input for ID. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DBFrame, "An error occurred: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to update an existing vendor logistics record
    private void updateVendorLogisticsPanel(JFrame DBFrame, JTextField idTextField, JTextField logisticsNameTextField, JTextField addressTextField) {
        try {
            int id = Integer.parseInt(idTextField.getText());
            String logisticsName = logisticsNameTextField.getText();
            String address = addressTextField.getText();

            boolean status = db.updateVendorLogistics(id, logisticsName, address);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Vendor logistics entry has been updated successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "Error updating vendor logistics entry in database");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid input for ID. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DBFrame, "An error occurred: " + ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to delete an existing vendor logistics record
    private void deleteVendorLogisticsPanel(JFrame DBFrame, JTextField idTextField) {
        try {
            int id = Integer.parseInt(idTextField.getText());

            boolean status = db.deleteVendorLogistics(id);
            if (status) {
                JOptionPane.showMessageDialog(DBFrame, "Vendor logistics entry has been deleted successfully");
            } else {
                JOptionPane.showMessageDialog(DBFrame, "No such vendor logistics entry exists, nothing to delete");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(DBFrame, "Invalid input for ID. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(DBFrame, "An error occurred: " + ex.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
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


}