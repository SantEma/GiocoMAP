package aeg.giocomap.Network;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author giulio
 */
public class ThreadRicezione implements Runnable {

    private final Socket socket;
    private final List<String> messaggiRicevuti;
    private Runnable onMessaggio;
    private Runnable onNomeDuplicato;

    public ThreadRicezione(Socket socket) {
        this.socket = socket;
        this.messaggiRicevuti = new ArrayList<>();
    }
    
    
    public List<String> getMessaggiRicevuti() {
        synchronized (messaggiRicevuti) {
            return new ArrayList<>(messaggiRicevuti);
        }
    }

    public void setOnMessaggio(Runnable callback) {
        this.onMessaggio = callback;
    }

    public void setOnNomeDuplicato(Runnable callback) {
        this.onNomeDuplicato = callback;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            String raw;
            while ((raw = input.readLine()) != null) {
                Message msg = MessageParser.deserializza(raw);
                if (msg == null) continue;

                // Gestisci nome duplicato separatamente
                if (msg.getTipo() == TipoMessaggio.NOME_DUPLICATO) {
                    if (onNomeDuplicato != null) {
                        javax.swing.SwingUtilities.invokeLater(onNomeDuplicato);
                    }
                    break;
                }

                // Tutti gli altri messaggi vanno in chat
                String testo = formatta(msg);
                synchronized (messaggiRicevuti) {
                    messaggiRicevuti.add(testo);
                }

                if (onMessaggio != null) {
                    javax.swing.SwingUtilities.invokeLater(onMessaggio);
                }

                // Se il server si disconnette esci
                if (msg.getTipo() == TipoMessaggio.LEAVE &&
                    msg.getMittente().equals("SERVER")) {
                    System.out.println("DEBUG: Server disconnesso");
                    break;
                }
            }
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted())
                System.err.println("Errore ricezione: " + e.getMessage());
        }
    }

    private String formatta(Message msg) {
        switch (msg.getTipo()) {
            case JOIN:  return "*** " + msg.getMittente() + " è entrato ***";
            case LEAVE: return "*** " + msg.getMittente() + " è uscito ***";
            case CHAT:  return "[" + msg.getMittente() + "]: " + msg.getContenuto();
            default:    return msg.getContenuto();
        }
    }

}