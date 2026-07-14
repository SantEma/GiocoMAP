package aeg.giocomap.View;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 *
 * @author andrea
 */
public class BottoneAvanti extends JButton {
    public BottoneAvanti(ActionListener listener) {
        super("Avanti \u2192");
        this.setFont(new Font("Arial", Font.BOLD, 18));
        this.setBackground(Color.BLACK);
        this.setForeground(Color.WHITE);
        this.setFocusPainted(false);
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (listener != null) {
            this.addActionListener(listener);
        }
    }
}
