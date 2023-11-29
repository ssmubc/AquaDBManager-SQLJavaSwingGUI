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
    private JPanel searchPanel;
    private Runnable showHome;
    private List<String[]> colNames; // used for advanced search
    // List to hold references to input components for each row
    private List<RowInputComponents> rowInputComponentsList = new ArrayList<>();


    private Supplier<JSONArray> dataSupplier;
    public TablePackage(Runnable showHome, String name, Supplier<JSONArray> dataSupplier) {
        this.dataSupplier = dataSupplier;
        this.tableModel = new DefaultTableModel();
        this.table = new JTable(tableModel);
        this.name = name;
        this.showHome = showHome;
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
        this.colNames = Arrays.asList(new String[][]{{"colname1", "String"}, {"colname2", "Int"}, {"colname3", "Float"}});

        backButton.addActionListener(e -> showHome.run());
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

        this.searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

        // Add header row
        searchPanel.add(createHeaderRow());

        // Create rows for each column name
        for (String[] colName : colNames) {
            searchPanel.add(createRowPanel(colName[0], colName[1]));
        }

        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("Submit");
        JButton closeButton = new JButton("Close");

        submitButton.addActionListener(e -> {
            if(checkInputs()) {
                JSONArray res = collectSearchCriteria();
                System.out.println(res.toString());
            }
        });

        closeButton.addActionListener(e -> searchDialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(closeButton);

        searchDialog.add(searchPanel, BorderLayout.CENTER);
        searchDialog.add(buttonPanel, BorderLayout.SOUTH);
        searchDialog.setVisible(true);
    }

    public void setColNames(List<String[]> colNames){
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
        JPanel headerPanel = new JPanel(new GridLayout(1, 4));
        headerPanel.add(new JLabel("Name"));
        headerPanel.add(new JLabel("Condition"));
        headerPanel.add(new JLabel("Comparison"));
        headerPanel.add(new JLabel("Value"));
        return headerPanel;
    }

    private JPanel createRowPanel(String colName, String type) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 4));
        rowPanel.add(new JLabel(colName));
        String[] comparisonOptions;
        if(type.equals("String")){
            comparisonOptions = new String[]{"=="};
        } else{
            comparisonOptions = new String[]{">", "<", ">=", "<=", "=="};
        }

        JComboBox<String> conditionComboBox = new JComboBox<>(new String[]{"None", "AND", "OR"});
        JComboBox<String> comparisonField = new JComboBox<>(comparisonOptions);
        JTextField valueField = new JTextField(10);

        // Initially hide comparisonField and valueField
        comparisonField.setVisible(false);
        valueField.setVisible(false);

        // Add action listener to conditionComboBox
        conditionComboBox.addActionListener(e -> {
            String selectedCondition = (String) conditionComboBox.getSelectedItem();
            boolean isNone = selectedCondition.equals("None");
            comparisonField.setVisible(!isNone);
            valueField.setVisible(!isNone);
            rowPanel.revalidate();
        });


        rowPanel.add(conditionComboBox);
        rowPanel.add(comparisonField);
        rowPanel.add(valueField);

        // Store references to input components
        rowInputComponentsList.add(new RowInputComponents(colName, conditionComboBox, comparisonField, valueField, type));

        return rowPanel;
    }


    // Inner class to hold references to input components of a row
    private class RowInputComponents {
        String columnName;
        JComboBox<String> conditionComboBox;
        JComboBox<String> comparisonField;
        JTextField valueField;

        String type;

        RowInputComponents(String columnName, JComboBox<String> conditionComboBox,
                           JComboBox<String> comparisonField, JTextField valueField, String type) {
            this.columnName = columnName;
            this.conditionComboBox = conditionComboBox;
            this.comparisonField = comparisonField;
            this.valueField = valueField;
            this.type = type;
        }
        public boolean checkInput(){
            String selectedCondition = (String) conditionComboBox.getSelectedItem();
            boolean isNone = selectedCondition.equals("None");
            if(isNone){
                return true;
            }
            String inputValue = valueField.getText();
            if(valueField.getText().equals("")){
                JOptionPane.showMessageDialog(searchPanel,
                        "Please fill " + columnName +" value",
                        "Missing Data", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            if(type.equals("Int")){
                try{
                    Integer.parseInt(inputValue);
                } catch (NumberFormatException err){
                    JOptionPane.showMessageDialog(searchPanel,
                            "Please use number(Integer) for " + columnName +" value",
                            "Invalid Data", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }

            if(type.equals("Float")){
                try{
                    Integer.parseInt(inputValue);
                } catch (NumberFormatException err){
                    JOptionPane.showMessageDialog(searchPanel,
                            "Please use number for " + columnName +" value",
                            "Invalid Data", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
            return true;
        }
    }

    private boolean checkInputs(){
        for(RowInputComponents ric : rowInputComponentsList){
            if(!ric.checkInput()){
                return false;
            }
        }
        return true;
    }

    private JSONArray collectSearchCriteria() {
        JSONArray searchCriteriaArray = new JSONArray();

        for (RowInputComponents components : rowInputComponentsList) {
            JSONObject criteria = new JSONObject();
            criteria.put("Name", components.columnName);
            criteria.put("Condition", components.conditionComboBox.getSelectedItem().toString());
            criteria.put("Comparison", components.comparisonField.getSelectedItem().toString()); // Assuming a 'Comparison' field is text input
            criteria.put("Value", components.valueField.getText());

            searchCriteriaArray.put(criteria);
        }

        return searchCriteriaArray;
    }



}