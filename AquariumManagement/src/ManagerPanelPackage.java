package AquariumManagement.src;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

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

            if (inputFields[i].length > 1){
                managerField = new ManagerInputField(inputFields[i][0], inputFields[i][1]);
            } else {
                managerField = new ManagerInputField(inputFields[i][0]);
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);
    }
    public String getFieldText(String fieldName){
        return inputFieldMap.get(fieldName).getTextField().getText();
    }

    public String getTitle(){
        return title;
    }

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
}
