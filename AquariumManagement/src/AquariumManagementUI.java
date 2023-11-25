package AquariumManagement.src;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static AquariumManagement.src.AquariumManagementDB.getColumnNames;


public class AquariumManagementUI extends JFrame {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    Dimension buttonSize = new Dimension(50, 15);

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private Map<String, JPanel> categoryPanels; // HashMap to store category panels
    // declaring the database
    private AquariumManagementDB db;

    private List<TablePackage> tablePackages;
    public AquariumManagementUI() {
        super("Aquarium Manager");
        tablePackages = new ArrayList<TablePackage>();;
        categoryPanels = new HashMap<>();
        connectDBPanel(); // connect DB and launch app
    }

    /*
    TODO: change this to parse data read from DB instead of hard coding if time allows
     */
    private void initializeTables() {
        tablePackages.add(new TablePackage("Animal", getColumnNames("ANIMAL")));
        tablePackages.add(new TablePackage("Staff", getColumnNames("STAFF")));
        tablePackages.add(new TablePackage("Item", getColumnNames("ITEMQUANTITY")));
        tablePackages.add(new TablePackage("Item", getColumnNames("ITEMQUANTITY")));
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

        // Create and add each category panel to the card layout and the HashMap
        for (TablePackage tp : tablePackages) {
            JPanel categoryPanel = createCategoryPanel(tp);
            cardsPanel.add(categoryPanel, tp.getName() + "Panel");

            JButton button = new JButton("Show All " + tp.getName());
            button.setPreferredSize(buttonSize);
            button.addActionListener(e -> cardLayout.show(cardsPanel, tp.getName() + "Panel"));
            buttonPanel.add(button);
        }

        add(cardsPanel, BorderLayout.CENTER);
        // adds the button for closing DB
        JButton inventoryButton = new JButton("Manage Inventory");
        inventoryButton.setPreferredSize(buttonSize);
        inventoryButton.addActionListener(e -> inventoryPanel());
        buttonPanel.add(inventoryButton);


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

    private JPanel createCategoryPanel(TablePackage tablePackage) {
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.add(new JScrollPane(tablePackage.getTable()), BorderLayout.CENTER);


        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add " + tablePackage.getName());
        JButton removeButton = new JButton("Remove " + tablePackage.getName());
        JButton backButton = new JButton("Back to Home");
        JButton addAttributeButton = new JButton("Add Attribute");
        JButton removeAttributeButton = new JButton("Remove Attribute");

        // Add an action listener to each button
        addAttributeButton.addActionListener(e -> {
            tablePackage.addNewColumn();
        });
        addButton.addActionListener(e -> {
            tablePackage.addNewRow();
        });
        removeButton.addActionListener(e -> {
            tablePackage.deleteSelectedRows(categoryPanel);
        });
        removeAttributeButton.addActionListener(e -> {
            tablePackage.deleteSelectedColumn();
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
            initializeTables();
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
        Integer id = Integer.parseInt(idTextField.getText());
        String location = locationField.getText();

        boolean status = db.insertInventory(id, location);
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


}