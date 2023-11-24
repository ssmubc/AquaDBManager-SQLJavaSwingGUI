package AquariumManagement.src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class TablePackage {
    private JTable table;
    private DefaultTableModel tableModel;
    private String name;
    private List<String> columns;

    public TablePackage(String name, List<String> columns) {
        this.tableModel = new DefaultTableModel(null, columns.toArray());
        this.table = new JTable(tableModel);
        this.name = name;
        this.columns = columns;
    }


    public void deleteSelectedRows (JPanel panel) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 0) {
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                tableModel.removeRow(selectedRows[i]);
            }
        } else {
            JOptionPane.showMessageDialog(panel, "No rows selected.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addNewRow() {
        // Get the number of columns
        int columnCount = tableModel.getColumnCount();
        if (columnCount == 0) {
            JOptionPane.showMessageDialog(null, "The table has no columns.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a panel to hold input fields
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField[] inputFields = new JTextField[columnCount];

        for (int i = 0; i < columnCount; i++) {
            // Add a label and text field for each column
            panel.add(new JLabel(tableModel.getColumnName(i) + ":"));
            JTextField textField = new JTextField();
            inputFields[i] = textField;
            panel.add(textField);
        }

        // Show the dialog
        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Row", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Object[] rowData = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                rowData[i] = inputFields[i].getText();
            }
            tableModel.addRow(rowData);
        }
    }

    public void addNewColumn() {
        // Prompt the user to enter the name of the new column
        String columnName = JOptionPane.showInputDialog("Enter the name of the new column:");
        if (columnName != null && !columnName.trim().isEmpty()) {
            // Add the new column to the table model
            tableModel.addColumn(columnName);
        } else if (columnName != null) {
            // If user entered a blank name
            JOptionPane.showMessageDialog(null, "Column name cannot be blank.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        // Note: If the user cancels the dialog, columnName will be null, and nothing happens
    }

    public void deleteSelectedColumn() {
        int selectedColumn = table.getSelectedColumn();
        if (selectedColumn == -1) {
            JOptionPane.showMessageDialog(null, "No column selected.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm before deleting
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this column?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Create a new table model without the selected column
        DefaultTableModel newModel = new DefaultTableModel();
        int columnCount = tableModel.getColumnCount();

        // Add columns to the new model (excluding the selected one)
        for (int i = 0; i < columnCount; i++) {
            if (i != selectedColumn) {
                newModel.addColumn(tableModel.getColumnName(i));
            }
        }

        // Add data to the new model
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Vector<Object> rowData = new Vector<>();
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                if (col != selectedColumn) {
                    rowData.add(tableModel.getValueAt(row, col));
                }
            }
            newModel.addRow(rowData);
        }

        // Set the new model to the table
        table.setModel(newModel);
        tableModel = newModel;
    }

    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public String getName() {
        return name;
    }



}