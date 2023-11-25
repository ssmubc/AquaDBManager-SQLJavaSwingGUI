package AquariumManagement.src;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ManagerInputField {
    private JTextField textField;
    private JLabel label;
    public ManagerInputField(String displayName){
        this.label = new JLabel(displayName + " :");
        this.textField = new JTextField();
    }

    public ManagerInputField(String displayName, String placeholder){
        this.label = new JLabel(displayName + " :");
        this.textField = new JTextField(placeholder);
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
}
