/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Network;

import java.io.*;
import java.net.*;
/**
 *
 * @author murgo
 */

public class GameClient {

    private Socket socket;
    private PrintWriter output;
    private ThreadRicezione threadRicezione;
    private final String nomeGiocatore;
    private boolean connesso;

    public GameClient(String nomeGiocatore) {
        this.nomeGiocatore = nomeGiocatore;
        this.connesso = false;
    }
    
    public boolean isConnesso() { 
        return connesso; 
    }
    
    public ThreadRicezione getThreadRicezione() { 
        return threadRicezione; 
    }
    
    public String getNomeGiocatore() { 
        return nomeGiocatore; 
    }

    public boolean connetti(String host) {
        try {
            socket = new Socket(host, GameServer.PORTA);
            output = new PrintWriter(socket.getOutputStream(), true);
            connesso = true;

            // avvia thread ricezione
            threadRicezione = new ThreadRicezione(socket);
            new Thread(threadRicezione).start();

            // manda JOIN
            invia(new Message(TipoMessaggio.JOIN, nomeGiocatore, "entrato"));
            System.out.println("DEBUG: Connesso al server " + host);
            return true;
        } catch (IOException e) {
            System.err.println("DEBUG: Server non trovato → " + e.getMessage());
            connesso = false;
            return false;
        }
    }

    public void invia(Message messaggio) {
        if (output != null && connesso) {
            output.println(MessageParser.serializza(messaggio));
        }
    }

    public void disconnetti() {
        connesso = false;
        invia(new Message(TipoMessaggio.LEAVE, nomeGiocatore, "uscito"));
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Errore disconnessione: " + e.getMessage());
        }
    }


}
