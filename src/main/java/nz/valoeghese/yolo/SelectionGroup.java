package nz.valoeghese.yolo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class SelectionGroup {
    public SelectionGroup(JButton ...buttons) {
        this.buttons = buttons;
    }

    private JButton[] buttons;

    @SafeVarargs
    public final <T> void bind(Consumer<T> onChange, T... values) {
        if (values.length < buttons.length)
            throw new IllegalArgumentException("Not enough values for button group");

        AtomicReference<JButton> selected = new AtomicReference<>(buttons[0]);
        buttons[0].setEnabled(false);

        Iterator<T> i = Arrays.stream(values).iterator();
        for (JButton button : buttons) {
            T value = i.next();
            button.addActionListener(e -> {
                selected.getAndSet(button).setEnabled(true);
                button.setEnabled(false);
                onChange.accept(value);
            });
        }

        buttons[0].getActionListeners()[0].actionPerformed(new ActionEvent("",0,""));
    }
}
