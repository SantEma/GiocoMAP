/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;

import aeg.giocomap.Network.GameClient;
import aeg.giocomap.Network.Message;
import aeg.giocomap.Network.TipoMessaggio;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 *
 * @author murgo
 */

public class ChatPanel extends JComponent {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private final JTextArea areaMessaggi;
    private final JTextField campoTesto;
    private final JButton btnInvia;
    private GameClient client;

    public ChatPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 400));
        setOpaque(true);
        setBackground(new Color(20, 20, 20));

        JComponent top = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isOpaque()) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        top.setLayout(new BorderLayout());
        top.setOpaque(true);
        top.setBackground(new Color(20, 20, 20));
        JLabel titolo = new JLabel("Chat", SwingConstants.LEFT);
        titolo.setForeground(Color.WHITE);
        titolo.setFont(new Font("Arial", Font.BOLD, 14));
        titolo.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 0));
        top.add(titolo, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        areaMessaggi = new JTextArea();
        areaMessaggi.setEditable(false);
        areaMessaggi.setBackground(new Color(30, 30, 30));
        areaMessaggi.setForeground(Color.WHITE);
        areaMessaggi.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaMessaggi.setLineWrap(true);
        areaMessaggi.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(areaMessaggi);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JComponent bottom = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isOpaque()) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        bottom.setLayout(new BorderLayout());
        bottom.setOpaque(true);
        bottom.setBackground(new Color(20, 20, 20));

        campoTesto = new JTextField();
        campoTesto.setBackground(new Color(40, 40, 40));
        campoTesto.setForeground(Color.WHITE);
        campoTesto.setCaretColor(Color.WHITE);
        campoTesto.setFont(new Font("Monospaced", Font.PLAIN, 13));
        campoTesto.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        btnInvia = new JButton("→");
        btnInvia.setBackground(new Color(60, 60, 60));
        btnInvia.setForeground(Color.WHITE);
        btnInvia.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnInvia.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bottom.add(campoTesto, BorderLayout.CENTER);
        bottom.add(btnInvia, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        btnInvia.addActionListener(e -> inviaMessaggio());
        campoTesto.addActionListener(e -> inviaMessaggio());
    }

    public void setClient(GameClient client) {
        this.client = client;

        client.getThreadRicezione().setOnMessaggio(() -> {
            List<String> messaggi = client.getThreadRicezione().getMessaggiRicevuti();
            areaMessaggi.setText("");
            for (String msg : messaggi) {
                areaMessaggi.append(msg + "\n");
            }
            areaMessaggi.setCaretPosition(areaMessaggi.getDocument().getLength());
        });
    }

    private void inviaMessaggio() {
        if (client == null || !client.isConnesso()) return;
        String testo = campoTesto.getText().trim();
        if (testo.isEmpty()) return;

        client.invia(new Message(
            TipoMessaggio.CHAT,
            client.getNomeGiocatore(),
            testo
        ));
        campoTesto.setText("");
    }

    public void aggiungiMessaggio(String msg) {
        areaMessaggi.append(msg + "\n");
        areaMessaggi.setCaretPosition(areaMessaggi.getDocument().getLength());
    }
}
