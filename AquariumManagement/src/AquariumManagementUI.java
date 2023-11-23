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

    private Map<String, String[]> categoryColumns;
    public AquariumManagementUI() {
        super("Aquarium Manager");
        categoryPanels = new HashMap<>();
        initializeCategoryColumns();
        initializeComponents();
    }

    /*
    TODO: change this to parse data read from DB instead of hard coding if time allows
     */
    private void initializeCategoryColumns() {
        categoryColumns = new HashMap<>();
        categoryColumns.put("Animal", new String[]{"ID", "Name", "Species", "Name", "LivingTemp"});
        categoryColumns.put("Staff", new String[]{"ID", "Name", "Salary", "DateHired"});
        categoryColumns.put("WaterTank", new String[]{"ID", "Name", "Volume", "Temperature", "LightingLevel"});

        // Add other categories and their columns in a similar way
        // categoryColumns.put("Plant", new String[]{"Column1", "Column2", ...});
        // ...
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
        for (String category : categoryColumns.keySet()) {
            JPanel categoryPanel = createCategoryPanel(category, categoryColumns.get(category));
            cardsPanel.add(categoryPanel, category + "Panel");
            categoryPanels.put(category, categoryPanel); // Store the panel in the HashMap

            JButton button = new JButton("Manage " + category);
            button.addActionListener(e -> cardLayout.show(cardsPanel, category + "Panel"));
            homePanel.add(button);
        }

        add(cardsPanel, BorderLayout.CENTER);



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

    private JPanel createCategoryPanel(String category, String[] columnNames) {
        JPanel categoryPanel = new JPanel(new BorderLayout());
        JTable table = new JTable();

        // Sample data for demonstration purposes

        DefaultTableModel tableModel = new DefaultTableModel(null, columnNames);
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

        // Inner function for addAttributeButton action
        Runnable addAttributeAction = () -> {
            String attributeName = JOptionPane.showInputDialog(categoryPanel, "Enter the name of the new attribute:");
            if (attributeName != null && !attributeName.trim().isEmpty()) {
                tableModel.addColumn(attributeName);
                addInputField.accept(""); // Add a new input field for the new attribute
                inputPanel.revalidate();  // Refresh the input panel
                inputPanel.repaint();
            }
        };

        // Inner function for addButton action
        Runnable addRowAction = () -> {
            if (inputFields.stream().allMatch(field -> !field.getText().trim().isEmpty())) {
                Object[] row = inputFields.stream().map(JTextField::getText).toArray();
                tableModel.addRow(row);
                inputFields.forEach(field -> field.setText("")); // Clear input fields
            } else {
                JOptionPane.showMessageDialog(categoryPanel, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        // Inner function for removeAttributeButton action
        Runnable removeAttributeAction = () -> {
            String attributeName = JOptionPane.showInputDialog(categoryPanel, "Enter the name of the attribute to remove:");
            if (attributeName != null && !attributeName.trim().isEmpty()) {
                int colIndex = tableModel.findColumn(attributeName);
                if (colIndex != -1) {
                    TableColumn toRemove = table.getColumnModel().getColumn(colIndex);
                    table.removeColumn(toRemove); // Remove the column from the view
                    tableModel.setColumnCount(tableModel.getColumnCount() - 1); // Update the column count
                    JTextField fieldToRemove = inputFields.get(colIndex);
                    inputPanel.remove(fieldToRemove);
                    inputFields.remove(fieldToRemove);
                    inputPanel.revalidate();
                    inputPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(categoryPanel, "Attribute not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // TODO: Add remove row button here

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add " + category);
        JButton removeButton = new JButton("Remove " + category);
        JButton backButton = new JButton("Back to Home");
        JButton addAttributeButton = new JButton("Add Attribute");
        JButton removeAttributeButton = new JButton("Remove Attribute");

        // Add an action listener to each button
        addAttributeButton.addActionListener(e -> addAttributeAction.run());
        addButton.addActionListener(e -> addRowAction.run());
        removeAttributeButton.addActionListener(e -> removeAttributeAction.run());

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
        AquariumManagementDB db = new AquariumManagementDB();
        boolean status = db.getConnection(username, password);

        if (status) {
            JOptionPane.showMessageDialog(DBframe, "Connected to Oracle DB successfully!");
        } else {
            JOptionPane.showMessageDialog(DBframe, "Failed to connect to Oracle DB.");
        }
    };
}