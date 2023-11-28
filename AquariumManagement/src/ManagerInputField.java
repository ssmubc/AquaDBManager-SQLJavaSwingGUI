package AquariumManagement.src;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ManagerInputField {
    private JTextField textField;
    private JLabel label;
    private boolean mandatory; // is it mandatory when user insert data?
    public ManagerInputField(String displayName, boolean isMandatory){
        this.label = new JLabel(displayName + " :");
        this.textField = new JTextField();
        this.mandatory = isMandatory;
    }

    public ManagerInputField(String displayName, boolean isMandatory, String placeholder){
        this.label = new JLabel(displayName + " :");
        this.textField = new JTextField();
        textField.setToolTipText(placeholder);
        this.mandatory = isMandatory;
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                }
            }
        });
    }

    public JTextField getTextField(){
        return textField;
    }

    public JLabel getLabel(){
        return label;
    }

    public boolean isMandatory() { return mandatory;}


}
