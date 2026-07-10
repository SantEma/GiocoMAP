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


public class ClientHandler implements Runnable {

    private final Socket socket;
    private final GameServer server;
    private PrintWriter output;
    private String nomeGiocatore;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            String raw;
            while ((raw = input.readLine()) != null) {
                Message msg = MessageParser.deserializza(raw);
                if (msg == null) continue;

                switch (msg.getTipo()) {
                    case JOIN:
                        nomeGiocatore = msg.getMittente();

                        // verifica se il nome è disponibile
                        if (!server.nomeDisponibile(nomeGiocatore)) {
                            invia(MessageParser.serializza(
                                new Message(TipoMessaggio.NOME_DUPLICATO, "SERVER",
                                    "Nome già in uso! Scegline un altro.")));
                            disconnetti();
                            return;
                        }

                        server.aggiungiNome(nomeGiocatore);
                        System.out.println("DEBUG: " + nomeGiocatore + " entrato");
                        server.broadcast(msg);
                        break;

                    case CHAT:
                        server.broadcast(msg);
                        break;

                    case LEAVE:
                        server.broadcast(msg);
                        disconnetti();
                        return;

                    default:
                        break;
                }
            }
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted())
                System.err.println("Errore client: " + e.getMessage());
        } finally {
            disconnetti();
        }
    }

    public void invia(String raw) {
        if (output != null) output.println(raw);
    }

    private void disconnetti() {
        // rimuove il nome dalla lista dei connessi
        if (nomeGiocatore != null) {
            server.rimuoviNome(nomeGiocatore);
        }
        server.rimuoviClient(this);
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Errore chiusura socket: " + e.getMessage());
        }
        System.out.println("DEBUG: " + nomeGiocatore + " disconnesso");
    }
}