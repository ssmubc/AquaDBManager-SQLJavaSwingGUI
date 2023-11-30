package AquariumManagement.src;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.function.Function;

import static AquariumManagement.src.AquariumManagementUI.buttonSize;

public class ManagerPanelPackage {
    private String title;
    private JButton mainButton;
    private JButton searchButton;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;

    private JPanel panel;
    private JPanel buttonPanel;
    // Map DB Field Name to InputField
    // Key: DB_FIELD_NAME
    // Value: ManagerInputField
    private HashMap<String, ManagerInputField> inputFieldMap;

    // input fields: {{String displayName, String placeholder(optional)}}
    public ManagerPanelPackage(String title, String[][] inputFields){
        this.inputFieldMap = new HashMap<>();
        this.title = title;
        this.mainButton = new JButton("Manage "+ title);
        mainButton.setPreferredSize(buttonSize);
        panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(0, 2));

        for (int i=0; i < inputFields.length; i++){
            ManagerInputField managerField;
            boolean isMandatory = inputFields[i][2].equals("True");
            if (inputFields[i].length > 3){
                managerField = new ManagerInputField(inputFields[i][1], isMandatory, inputFields[i][3]);
            } else {
                managerField = new ManagerInputField(inputFields[i][1], isMandatory);
            }


            inputPanel.add(managerField.getLabel());
            inputPanel.add(managerField.getTextField());

            inputFieldMap.put(inputFields[i][0], managerField);
        }

        panel.add(inputPanel, BorderLayout.CENTER);

        // Initialize buttons
        searchButton = new JButton("Search");
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");

        // Create a panel to hold the buttons in one row
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(searchButton);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        mainButton.addActionListener(e-> popupPanel());

    }

    private void popupPanel(){
        JFrame frame = new JFrame(title);
        frame.setSize(400, 300);
        frame.add(panel);
        frame.setVisible(true);
    }
    public String getFieldText(String fieldName){
        return inputFieldMap.get(fieldName).getTextField().getText();
    }

    public String getTitle(){
        return title;
    }
    public JPanel getButtonPanel() {return buttonPanel;}

    public JButton getMainButton(){
        return mainButton;
    }

    public JButton getAddButton(){
        return addButton;
    }

    public JButton getDeleteButton(){
        return deleteButton;
    }

    public JButton getUpdateButton(){
        return updateButton;
    }

    public JButton getSearchButton(){
        return searchButton;
    }

    /*
    * if anything miss, show warning and return false
    * otherwise return true
    * */
    public boolean checkMandatoryFields() {
        StringBuilder missingFields = new StringBuilder();
        boolean isAnyFieldMissing = false;

        for (String key : inputFieldMap.keySet()) {
            ManagerInputField field = inputFieldMap.get(key);
            if (field.isMandatory()) {
                JTextField textField = field.getTextField();
                String text = textField.getText().trim(); // Trim to remove leading/trailing whitespaces
                String placeholder = textField.getToolTipText(); // Retrieve placeholder from tooltip

                if (text.isEmpty() || text.equals(placeholder)) {
                    missingFields.append(field.getLabel().getText()).append("\n");
                    isAnyFieldMissing = true;
                }
            }
        }

        if (isAnyFieldMissing) {
            // Show popup message with missing fields
            JOptionPane.showMessageDialog(panel,
                    "Please fill in the following mandatory fields:\n" + missingFields.toString(),
                    "Missing Data", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean fieldTextExists(String fieldName) {
        ManagerInputField field = inputFieldMap.get(fieldName);
        if (field != null) {
            JTextField textField = field.getTextField();
            String text = textField.getText().trim(); // Trim to remove leading/trailing whitespaces
            String placeholder = textField.getToolTipText(); // Retrieve placeholder from tooltip

            // Check if the text is not empty and not equal to the placeholder
            if (text.isEmpty() || text.equals(placeholder)) {
                // Show popup message indicating the field with invalid data
                JOptionPane.showMessageDialog(panel,
                        "Please provide valid data for the field: " + field.getLabel().getText(),
                        "Invalid Data", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true; // Text is valid
        }

        // Show popup message indicating that the field was not found
        JOptionPane.showMessageDialog(panel,
                "Field not found: " + fieldName,
                "Field Not Found", JOptionPane.ERROR_MESSAGE);
        return false; // Field not found
    }

    private void setFieldText(String dbFieldName, String text){
        if(inputFieldMap.containsKey(dbFieldName)){
            inputFieldMap.get(dbFieldName).getTextField().setText(text);
        }
        // TODO: Handle Error
    }

    public void addSearchAction(String idFieldName, Function<Integer, JSONObject> searchFunction){
        searchButton.addActionListener(e -> {
            if(fieldTextExists(idFieldName)){
                int id =  Integer.parseInt(getFieldText(idFieldName));
                JSONObject dataFound = searchFunction.apply(id);
                if(dataFound != null){
                    showDbData(dataFound);
                } else {
                    idNotExistPopup(id);
                }
            }
        });
    }

    public void addDeleteAction(String idFieldName, Function<Integer, Boolean> deleteFunction){
        deleteButton.addActionListener(e -> {
            if(fieldTextExists(idFieldName)){
                int id =  Integer.parseInt(getFieldText(idFieldName));
                boolean success = deleteFunction.apply(id);
                if(success){
                    deleteSuccessPopup(id);
                } else {
                    idNotExistPopup(id);
                }
            }
        });
    }

    public float getFieldAsFloat(String fieldName) throws NumberFormatException {
        return Float.parseFloat(getFieldText(fieldName));
    }

    public int getFieldAsInt(String fieldName) throws NumberFormatException {
        return Integer.parseInt(getFieldText(fieldName));
    }

    public void showDbData(JSONObject dbData) {
        for(String key: dbData.keySet()){
            setFieldText(key, dbData.get(key).toString());
        }
    }

    public void idNotExistPopup(int id) {
        JOptionPane.showMessageDialog(buttonPanel,
                title + "(ID: "+ id + ") does not exist\n",
                "Invalid Data", JOptionPane.WARNING_MESSAGE);
    }

    public void updateSuccessPopup(int id) {
        JOptionPane.showMessageDialog(buttonPanel,
                title + "(ID: "+id+") was updated successfully",
                "Update Data", JOptionPane.WARNING_MESSAGE);
    }

    public void invalidDataPopup() {
        JOptionPane.showMessageDialog(buttonPanel,
                "Please fill in the fields with valid data:\n",
                "Invalid Data", JOptionPane.WARNING_MESSAGE);
    }

    public void insertSuccessPopup(int id) {
        JOptionPane.showMessageDialog(buttonPanel,
                title+ "(ID: "+id+") was inserted successfully",
                "Insert Data", JOptionPane.WARNING_MESSAGE);
    }

    public void deleteSuccessPopup(int id) {
        JOptionPane.showMessageDialog(buttonPanel,
                title+"(ID: "+ id + ") was successfully deleted\n",
                "Delete Success", JOptionPane.WARNING_MESSAGE);
    }

}
