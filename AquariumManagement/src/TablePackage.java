package AquariumManagement.src;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Set;
import java.util.Vector;
import java.util.function.Supplier;


public class TablePackage {
    private JTable table;
    private DefaultTableModel tableModel;
    private String name;
    private boolean columnInitialized = false;

    private Supplier<JSONArray> dataSupplier;
    public TablePackage(String name, Supplier<JSONArray> dataSupplier) {
        this.dataSupplier = dataSupplier;
        this.tableModel = new DefaultTableModel();
        this.table = new JTable(tableModel);
        this.name = name;
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