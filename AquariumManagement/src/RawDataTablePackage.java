package AquariumManagement.src;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class RawDataTablePackage {
    private JTable table;
    private DefaultTableModel tableModel;
    private String name;

    private JPanel packagePanel;
    private JPanel searchPanel;
    private JPanel buttonPanel;
    private Runnable showHome;
    private List<String> DBfieldNames = new ArrayList<>();

    private AquariumManagementDB db;

    public RawDataTablePackage(Runnable showHome, String name, AquariumManagementDB db) {
        this.db = db;
        this.tableModel = new DefaultTableModel();
        this.table = new JTable(tableModel);
        this.name = name;
        this.showHome = showHome;
        initializePanel();
    }

    private void initializePanel(){
        packagePanel = new JPanel(new BorderLayout());
        JPanel selectPanel = getSelectPanel();
        packagePanel.add(selectPanel, BorderLayout.NORTH);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> showHome.run());
        buttonPanel.add(backButton);
        packagePanel.add(buttonPanel, BorderLayout.SOUTH);

        packagePanel.add(new JScrollPane(table), BorderLayout.CENTER);
    }


    private JPanel getSelectPanel() {
        // New panel for dropdown and select data button
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Using FlowLayout for horizontal alignment

        // Instruction label
        JLabel instructionLabel = new JLabel("Select Raw DB Table: ");
        selectPanel.add(instructionLabel);

        List<String> tableNamesList = db.getAllTableNames();
        System.out.println(tableNamesList.toString());
        String[] tableNamesArray = tableNamesList.toArray(new String[tableNamesList.size()]);

        // cache the field names for each table when selected
        Map<String, List<String>> tableFieldsCache = new HashMap<>();

        // Dropdown for tables
        JComboBox<String> tableDropdown = new JComboBox<>(tableNamesArray);
        if (tableNamesArray.length>0){
            // cache field names of default table to prevent not-selected error case
            List<String> firstFieldNames = db.getColumnNames(tableNamesArray[0]);
            tableFieldsCache.put(tableNamesArray[0], firstFieldNames);
        }

        tableDropdown.addActionListener(e -> {
            String selectedTable = (String) tableDropdown.getSelectedItem();
            if (!tableFieldsCache.containsKey(selectedTable)) {
                // Fetch and cache the field names for the selected table
                List<String> fieldNames = db.getColumnNames(selectedTable);
                tableFieldsCache.put(selectedTable, fieldNames);
            }
        });
        selectPanel.add(tableDropdown);

        // Select Data button
        JButton selectDataButton = new JButton("Select Data");
        selectDataButton.addActionListener(e -> {
            String selectedTable = (String) tableDropdown.getSelectedItem();
            if ("Select Table".equals(selectedTable)) {
                JOptionPane.showMessageDialog(selectPanel, "Please select a table", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                List<String> fieldNames = tableFieldsCache.get(selectedTable);
                showFieldSelectionPopup(selectedTable, fieldNames);
            }
        });
        selectPanel.add(selectDataButton);

        return selectPanel;
    }

    private void showFieldSelectionPopup(String selectedTable, List<String> fieldNames) {
        JDialog fieldSelectionDialog = new JDialog();
        fieldSelectionDialog.setTitle("Select fields to display");
        fieldSelectionDialog.setLayout(new BorderLayout());

        JPanel fieldsPanel = new JPanel(new GridLayout(0, 1));
        Map<String, JCheckBox> checkBoxes = new HashMap<>();

        for (String fieldName : fieldNames) {
            JPanel rowPanel = new JPanel(new GridLayout(1, 2));
            rowPanel.add(new JLabel(fieldName));
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(true);
            checkBoxes.put(fieldName, checkBox);
            rowPanel.add(checkBox);
            fieldsPanel.add(rowPanel);
        }

        JButton viewDataButton = new JButton("View Data");
        viewDataButton.addActionListener(e -> {
            List<String> selectedFields = checkBoxes.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (selectedFields.isEmpty()) {
                JOptionPane.showMessageDialog(fieldSelectionDialog, "Please select at least one field", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JSONObject requestData = new JSONObject();
                requestData.put("TableName", selectedTable);
                requestData.put("Fields", new JSONArray(selectedFields));
                System.out.println(requestData.toString());
                JSONArray tableData = db.getRawData(requestData);
                System.out.println(tableData.toString());
                updateTableWithData(tableData);
                fieldSelectionDialog.dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> fieldSelectionDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewDataButton);
        buttonPanel.add(cancelButton);

        fieldSelectionDialog.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);
        fieldSelectionDialog.add(buttonPanel, BorderLayout.SOUTH);

        fieldSelectionDialog.pack();
        fieldSelectionDialog.setVisible(true);
    }

    private void clearTable(){
        // Clear existing data from the table model
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
    }

    public void updateTableWithData(JSONArray dbData) {
        clearTable();
        if(dbData.isEmpty()){
            noDataPopup();
            return;
        }
        // get colNames from dbData
        List<String> colNames = new ArrayList<>();
        JSONObject firstRow = dbData.getJSONObject(0);
        Iterator<String> keys = firstRow.keys();

        // Add the column names to the table model
        while (keys.hasNext()) {
            String name = keys.next();
            colNames.add(name);
            tableModel.addColumn(name);
        }


        // Iterate through each entry in the dbData array
        for (int i = 0; i < dbData.length(); i++) {
            JSONObject rowObject = dbData.getJSONObject(i);
            Vector<Object> row = new Vector<>();
            for (String fieldName : colNames) {
                row.add(rowObject.opt(fieldName));
            }
            // Add the row to the table model
            tableModel.addRow(row);
        }

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

    public void noDataPopup() {
        JOptionPane.showMessageDialog(packagePanel,
                "No Data Found\n",
                "No Data", JOptionPane.WARNING_MESSAGE);
    }




}