package AquariumManagement.src;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;


public class TablePackage {
    private JTable table;
    private DefaultTableModel tableModel;
    private String name;
    private boolean columnInitialized = false;
    private JPanel packagePanel;
    private CardLayout mainLayout;
    private List<String> colNames; // used for advanced search
    // List to hold references to input components for each row
    private List<RowInputComponents> rowInputComponentsList = new ArrayList<>();


    private Supplier<JSONArray> dataSupplier;
    public TablePackage(CardLayout cardLayout, String name, Supplier<JSONArray> dataSupplier) {
        this.dataSupplier = dataSupplier;
        this.tableModel = new DefaultTableModel();
        this.table = new JTable(tableModel);
        this.name = name;
        this.mainLayout = cardLayout;
        initializePanel();
    }

    private void initializePanel(){
        packagePanel = new JPanel(new BorderLayout());
        packagePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        JButton backButton = new JButton("Back to Home");

        JButton searchButton = new JButton("Advanced Search"); // New button for advanced search
        searchButton.addActionListener(e -> showAdvancedSearchPanel());
        buttonPanel.add(searchButton);
        this.colNames = Arrays.asList(new String[]{"colname1", "colName2", "asdasd"});

        backButton.addActionListener(e -> mainLayout.show(packagePanel, "HomePanel"));
        buttonPanel.add(backButton);

        packagePanel.add(buttonPanel, BorderLayout.SOUTH);


    }

    public void populateTable() {
        //clear table
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        
        JSONArray dataArray = dataSupplier.get();
        if (dataArray != null && !dataArray.isEmpty()) {
            // Extract columns from the first JSONObject
            JSONObject firstRowObject = dataArray.getJSONObject(0);
            Set<String> columnNames = firstRowObject.keySet();

            // add column names
            if(!columnInitialized){
                for (String columnName : columnNames) {
                    tableModel.addColumn(columnName);
                }
                columnInitialized = true;
            }


            // Populate rows
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject rowObject = dataArray.getJSONObject(i);
                Vector<Object> row = new Vector<>();
                for (String columnName : columnNames) {
                    row.add(rowObject.opt(columnName)); // Using opt to handle potential missing columns
                }
                tableModel.addRow(row);
            }
        }
    }

    private void showAdvancedSearchPanel() {
        JDialog searchDialog = new JDialog();
        searchDialog.setTitle("Advanced Search");
        searchDialog.setLayout(new BorderLayout());
        searchDialog.setSize(400, 300);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

        // Add header row
        searchPanel.add(createHeaderRow());

        // Create rows for each column name
        for (String colName : colNames) {
            searchPanel.add(createRowPanel(colName));
        }

        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("Submit");
        JButton closeButton = new JButton("Close");

        submitButton.addActionListener(e -> {

        });

        closeButton.addActionListener(e -> searchDialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(closeButton);

        searchDialog.add(searchPanel, BorderLayout.CENTER);
        searchDialog.add(buttonPanel, BorderLayout.SOUTH);
        searchDialog.setVisible(true);
    }

    public void setColNames(List<String> colNames){
        this.colNames = colNames;
    }

    public JTable getTable() {
        return table;
    }

    public JPanel getPackagePanel() {return packagePanel;}

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public String getName() {
        return name;
    }

    private JPanel createHeaderRow() {
        JPanel headerPanel = new JPanel(new GridLayout(1, 5));
        headerPanel.add(new JLabel("Name"));
        headerPanel.add(new JLabel("Display"));
        headerPanel.add(new JLabel("Condition"));
        headerPanel.add(new JLabel("Comparison"));
        headerPanel.add(new JLabel("Value"));
        return headerPanel;
    }

    private JPanel createRowPanel(String colName) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 5));
        rowPanel.add(new JLabel(colName));

        JCheckBox displayCheckBox = new JCheckBox();
        JComboBox<String> conditionComboBox = new JComboBox<>(new String[]{"Condition 1", "Condition 2"});
        JTextField comparisonField = new JTextField(10); // Assuming a comparison field is needed
        JTextField valueField = new JTextField(10);

        rowPanel.add(displayCheckBox);
        rowPanel.add(conditionComboBox);
        rowPanel.add(comparisonField);
        rowPanel.add(valueField);

        // Store references to input components
        rowInputComponentsList.add(new RowInputComponents(colName, displayCheckBox, conditionComboBox, comparisonField, valueField));

        return rowPanel;
    }


    // Inner class to hold references to input components of a row
    private class RowInputComponents {
        String columnName;
        JCheckBox displayCheckBox;
        JComboBox<String> conditionComboBox;
        JTextField comparisonField;
        JTextField valueField;

        RowInputComponents(String columnName, JCheckBox displayCheckBox, JComboBox<String> conditionComboBox, JTextField comparisonField, JTextField valueField) {
            this.columnName = columnName;
            this.displayCheckBox = displayCheckBox;
            this.conditionComboBox = conditionComboBox;
            this.comparisonField = comparisonField;
            this.valueField = valueField;
        }
    }

    private JSONArray collectSearchCriteria() {
        JSONArray searchCriteriaArray = new JSONArray();

        for (RowInputComponents components : rowInputComponentsList) {
            JSONObject criteria = new JSONObject();
            criteria.put("Name", components.columnName);
            criteria.put("Display", components.displayCheckBox.isSelected());
            criteria.put("Condition", components.conditionComboBox.getSelectedItem().toString());
            criteria.put("Comparison", components.comparisonField.getText()); // Assuming a 'Comparison' field is text input
            criteria.put("Value", components.valueField.getText());

            searchCriteriaArray.put(criteria);
        }

        return searchCriteriaArray;
    }



}