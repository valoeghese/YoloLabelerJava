package nz.valoeghese.yolo;

import javax.swing.*;
import java.util.Properties;

public class LabellingContext implements Categoriser {
    private DefaultListModel<String> model = new DefaultListModel<>();
    private String currentValue;

    private JList<String> external;
    private volatile boolean modifying = false;

    public void updateModel(Properties properties) {
        modifying = true;
        this.model.removeAllElements();

        String selectedValue = this.currentValue;
        boolean currentValueExists = false;

        for (int i = 0; ; i++) {
            String value = properties.getProperty(String.valueOf(i));
            if (value == null) break;

            this.model.addElement(value);
            if (value.equals(selectedValue))
                currentValueExists = true;
        }

        modifying = false;

        if (!model.isEmpty()) {
            if (currentValueExists) {
                // instantiate first value
                selectedValue = this.model.getElementAt(0);
            }

            if (this.external != null) {
                this.external.setSelectedValue(selectedValue, false);
            }
        }
    }

    @Override
    public String getCurrentCategory() {
        return this.currentValue;
    }

    @Override
    public String getCategoryLabel(int idx) {
        return model.getElementAt(idx);
    }

    @Override
    public int getCategoryIdx(String label) {
        for (int i = 0; i < model.getSize(); i++) {
            if (label.equals(model.getElementAt(i))) {
                return i;
            }
        }

        throw new IllegalArgumentException("No index");
    }

    public JList<String> createInterface() {
        JList<String> list = new JList<>(this.model);
        list.addListSelectionListener(e -> {
                if (!modifying) {
                    currentValue = list.getModel().getSize() ==0 ? null: getCategoryLabel(e.getFirstIndex());
                }
        });
        return this.external = list;
    }
}
