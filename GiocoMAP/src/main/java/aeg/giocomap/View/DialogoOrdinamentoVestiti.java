package aeg.giocomap.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 *
 * @author emanuele
 */
public class DialogoOrdinamentoVestiti extends JDialog {

    private final JLabel[] slotManichini;
    private final Map<String, JButton> bottoniVestiti = new HashMap<>();

    public DialogoOrdinamentoVestiti(JFrame owner, List<String> vestiti, Consumer<String> alClick) {
        super(owner, "Enigma della Principessa - Ordina i vestiti", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.DARK_GRAY);

        // Come i JOptionPane usati altrove nel gioco, ESC chiude il popup senza obbligare a completarlo
        JComponent radice = getRootPane();
        radice.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "chiudiPopup");
        radice.getActionMap().put("chiudiPopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JLabel titolo = new JLabel("Disponi i vestiti sui manichini nell'ordine che ritieni corretto", SwingConstants.CENTER);
        titolo.setFont(new Font("Arial", Font.BOLD, 16));
        titolo.setForeground(Color.WHITE);
        titolo.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(titolo, BorderLayout.NORTH);

        JPanel pannelloSlot = new JPanel(new GridLayout(1, vestiti.size(), 10, 10));
        pannelloSlot.setBackground(Color.DARK_GRAY);
        pannelloSlot.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        slotManichini = new JLabel[vestiti.size()];
        for (int i = 0; i < vestiti.size(); i++) {
            JLabel slot = new JLabel("Manichino " + (i + 1), SwingConstants.CENTER);
            slot.setOpaque(true);
            slot.setBackground(Color.LIGHT_GRAY);
            slot.setForeground(Color.BLACK);
            slot.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            slot.setPreferredSize(new Dimension(120, 80));
            slotManichini[i] = slot;
            pannelloSlot.add(slot);
        }
        add(pannelloSlot, BorderLayout.CENTER);

        JPanel pannelloVestiti = new JPanel(new GridLayout(1, vestiti.size(), 10, 10));
        pannelloVestiti.setBackground(Color.DARK_GRAY);
        pannelloVestiti.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        for (String vestito : vestiti) {
            JButton bottone = new JButton("<html><center>" + vestito.replace(" ", "<br>") + "</center></html>");
            bottone.setBackground(coloreVestito(vestito));
            bottone.setFocusPainted(false);
            bottone.setPreferredSize(new Dimension(120, 70));
            bottone.addActionListener(e -> alClick.accept(vestito));
            bottoniVestiti.put(vestito, bottone);
            pannelloVestiti.add(bottone);
        }
        add(pannelloVestiti, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    public void posizionaVestito(int posizione, String vestito) {
        if (posizione < 0 || posizione >= slotManichini.length) return;
        JLabel slot = slotManichini[posizione];
        slot.setText(vestito);
        slot.setBackground(coloreVestito(vestito));
    }

    public void disabilitaVestito(String vestito) {
        JButton bottone = bottoniVestiti.get(vestito);
        if (bottone != null) bottone.setEnabled(false);
    }

    private Color coloreVestito(String vestito) {
        switch (vestito) {
            case "Seta Celeste": return new Color(135, 206, 250);
            case "Velluto Cremisi": return new Color(153, 0, 0);
            case "Broccato Dorato": return new Color(212, 175, 55);
            case "Lino Candido": return Color.WHITE;
            case "Damasco Verde Smeraldo": return new Color(0, 128, 96);
            default: return Color.GRAY;
        }
    }
}
