/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Network;

import java.io.*;
import java.net.*;
import java.util.*;
/**
 *
 * @author murgo
 */

public class GameServer implements Runnable {

    public static final int PORTA = 12345;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clientConnessi;
    private final List<String> nomiConnessi;
    private boolean running;

    public GameServer() {
        clientConnessi = new ArrayList<>();
        nomiConnessi = new ArrayList<>();
        running = false;
    }

    public void avvia() {
        try {
            serverSocket = new ServerSocket(PORTA);
        } catch (IOException e) {
            System.err.println("Errore avvio server: " + e.getMessage());
            return;
        }
        running = true;
        new Thread(this).start();
        System.out.println("DEBUG: Server avviato sulla porta " + PORTA);
    }

    public void ferma() {
        running = false;
        broadcast(new Message(TipoMessaggio.LEAVE, "SERVER", "Server disconnesso"));
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Errore chiusura server: " + e.getMessage());
        }
        System.out.println("DEBUG: Server fermato");
    }

    @Override
    public void run() {
        try {
            while (running) {
                Socket client = serverSocket.accept();
                ClientHandler handler = new ClientHandler(client, this);
                clientConnessi.add(handler);
                new Thread(handler).start();
                System.out.println("DEBUG: Nuovo client connesso");
            }
        } catch (IOException e) {
            if (running) System.err.println("Errore server: " + e.getMessage());
        }
    }

    // verifica se il nome è disponibile
    public synchronized boolean nomeDisponibile(String nome) {
        return !nomiConnessi.contains(nome.toLowerCase());
    }

    // aggiunge il nome alla lista
    public synchronized void aggiungiNome(String nome) {
        nomiConnessi.add(nome.toLowerCase());
    }

    // rimuove il nome alla disconnessione
    public synchronized void rimuoviNome(String nome) {
        nomiConnessi.remove(nome.toLowerCase());
    }

    // manda un messaggio a tutti i client connessi
    public synchronized void broadcast(Message messaggio) {
        String raw = MessageParser.serializza(messaggio);
        for (ClientHandler client : clientConnessi) {
            client.invia(raw);
        }
    }

    public synchronized void rimuoviClient(ClientHandler handler) {
        clientConnessi.remove(handler);
    }
}