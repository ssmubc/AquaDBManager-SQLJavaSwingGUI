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

        // Create and add each category panel to the card layout and the HashMap
        String[] categories = {"Animal", "Plant", "Exhibit", "Staff", "Custodian", "Aquarist", "Veterinarian"};
        for (String category : categories) {
            JPanel categoryPanel = createCategoryPanel(category);
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
}