import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AquariumManagementUI extends JFrame {
    private static final String JSON_STORE = "./data/aquarium.json";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    public AquariumManagementUI() {
        super("Aquarium Manager");
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

        // Create and add each category panel to the card layout
        String[] categories = {"Animal", "Plant", "Exhibit", "Staff", "Custodian", "Aquarist", "Veterinarian"};
        for (String category : categories) {
            JPanel categoryPanel = createCategoryPanel(category);
            cardsPanel.add(categoryPanel, category + "Panel");
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
        JPanel inputPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JTextField inputField1 = new JTextField();
        JTextField inputField2 = new JTextField();
        JTextField inputField3 = new JTextField();
        inputPanel.add(inputField1);
        inputPanel.add(inputField2);
        inputPanel.add(inputField3);
        categoryPanel.add(inputPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add " + category);
        JButton removeButton = new JButton("Remove " + category);
        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> cardLayout.show(cardsPanel, "HomePanel"));

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(backButton);
        categoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        return categoryPanel;
    }
}