package aeg.giocomap.Network;

import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.ChatPanel;
import aeg.giocomap.GameEngine.SceneManager;
import javax.swing.JOptionPane;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 *
 * @author emanuele
 */
public class GameNetwork {
    private GameServer gameServer;
    private GameClient gameClient;
    private ChatPanel chatPanel;

    private final MainFrame frame;
    private final SceneManager sceneManager;

    public GameNetwork(MainFrame frame, SceneManager sceneManager) {
        this.frame = frame;
        this.sceneManager = sceneManager;
    }

    public void toggleChat() {
        if (gameClient == null) {
            gameServer = new GameServer();
            gameServer.avvia();

            gameClient = new GameClient("Eryndor");
            chatPanel = new ChatPanel();
            gameClient.connetti("127.0.0.1");
            chatPanel.setClient(gameClient);

            JOptionPane.showMessageDialog(
                frame,
                "Chat avviata! Il tuo IP: " + ottieniIP() +
                "\nCondividilo con i tuoi amici!",
                "Server Online",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        sceneManager.toggleChat(chatPanel);
    }

    public void connettiComeClient(String ip) {
        if (gameServer != null) {
            JOptionPane.showMessageDialog(
                frame,
                "Sei già l'host! Non puoi unirti nuovamente come client.",
                "Errore Host",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (gameClient != null && gameClient.isConnesso()) {
            JOptionPane.showMessageDialog(
                frame,
                "Sei già connesso a una chat!",
                "Errore Connessione",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String nome = JOptionPane.showInputDialog(
            frame,
            "Inserisci il tuo nome:",
            "Partecipa alla Chat",
            JOptionPane.PLAIN_MESSAGE
        );

        if (nome == null || nome.trim().isEmpty()) return;

        if (nome.trim().equalsIgnoreCase("Eryndor")) {
            JOptionPane.showMessageDialog(
                frame,
                "Il nome Eryndor è riservato all'host!",
                "Nome non valido",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (nome.contains("|") || nome.contains("\n") || nome.contains("\r")) {
            JOptionPane.showMessageDialog(
                frame,
                "Il nome non può contenere i caratteri '|' o a capo!",
                "Nome non valido",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        gameClient = new GameClient(nome.trim());
        chatPanel = new ChatPanel();
        boolean ok = gameClient.connetti(ip);

        if (ok) {
            gameClient.getThreadRicezione().setOnNomeDuplicato(() -> {
                JOptionPane.showMessageDialog(
                    frame,
                    "Nome già in uso! Riprova con un altro nome.",
                    "Nome duplicato",
                    JOptionPane.WARNING_MESSAGE
                );
                gameClient.disconnetti();
                gameClient = null;
            });

            chatPanel.setClient(gameClient);
            JOptionPane.showMessageDialog(
                frame,
                "Connesso alla chat!",
                "Connessione riuscita",
                JOptionPane.INFORMATION_MESSAGE
            );
            sceneManager.toggleChat(chatPanel);
        } else {
            JOptionPane.showMessageDialog(
                frame,
                "Server non trovato! Assicurati che l'host abbia avviato la chat.",
                "Errore connessione",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String ottieniIP() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            if (ip != null && !ip.equals("0.0.0.0")) return ip;
        } catch (Exception e) {
            System.err.println("Errore rilevamento IP (routing): " + e.getMessage());
        }

        try {
            Enumeration<NetworkInterface> interfacce =
                NetworkInterface.getNetworkInterfaces();

            while (interfacce.hasMoreElements()) {
                NetworkInterface ni = interfacce.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;

                Enumeration<InetAddress> indirizzi = ni.getInetAddresses();
                while (indirizzi.hasMoreElements()) {
                    InetAddress addr = indirizzi.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Errore fallback ricerca IP: " + e.getMessage());
        }
        return "127.0.0.1";
    }

    public void fermaNetwork() {
        if (gameClient != null) gameClient.disconnetti();
        if (gameServer != null) gameServer.ferma();
    }
}
