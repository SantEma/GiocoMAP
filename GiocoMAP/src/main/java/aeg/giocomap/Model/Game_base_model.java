/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;

import aeg.giocomap.Model.Oggetti.Spada;
import aeg.giocomap.Model.Oggetti.Oggetto;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author Andrea
 */
public class Game_base_model {
    private Connection conn;
    private Map<Integer, Oggetto> catalogoOggetti;
    
    // Costruttore che chiama la connessione al DB sin dall'inizio per poter caricare partita
    // o salvarla in seguito
    public Game_base_model(){
        connettiDatabase();
        inizializzaTabelle();
        loadOggettiDaCatologo();
    }
    
    private void inizializzaTabelle() {
    try {
        String saves = "CREATE TABLE IF NOT EXISTS saves (" +
                       "id INT PRIMARY KEY," +
                       "stanza_attuale VARCHAR(100)," +
                       "enigma_attuale INT)";

        String records = "CREATE TABLE IF NOT EXISTS records (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "nome VARCHAR(50)," +
                         "punteggio INT," +
                         "data TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        conn.prepareStatement(saves).executeUpdate();
        conn.prepareStatement(records).executeUpdate();
        System.out.println("TEST: Tabelle inizializzate");
    } catch (SQLException e) {
        System.err.println("Errore creazione tabelle: " + e.getMessage());
    }
}
    
    // Funzione che connette al Database
    private void connettiDatabase(){
        try{
            conn = DriverManager.getConnection("jdbc:h2:./saves/DB");
            System.out.println("TEST: Connessione al DB avvenuta");
            
            // Se le tabelle non esistono al primo avvio le si creano
            String querySaves = "CREATE TABLE IF NOT EXISTS saves"
                    + "(id INT PRIMARY KEY,stanza_attuale INT,enigma_attuale INT)";
            try (PreparedStatement pstmSaves = conn.prepareStatement(querySaves)) {
                pstmSaves.executeUpdate();
            }
            
            String queryRecords = "CREATE TABLE IF NOT EXISTS records (id INT PRIMARY KEY AUTO_INCREMENT, punteggio INT)";
            try (PreparedStatement pstmRecords = conn.prepareStatement(queryRecords)) {
                pstmRecords.executeUpdate();
            }         
        }
        catch (SQLException e){
            System.err.println("Errore di connessione al DB: "+e.getMessage());
        }
    }
    
    // Chiudo la connessione ad h2 se aperta
    public void chiudiConnessione(){
        try{
            if(conn != null && !conn.isClosed()){
                conn.close();
                System.out.println("DEBUG: DB chiuso");    
            } 
        }
        catch (SQLException e){
            System.out.println("DEBUG: Errore durante la chiusura del DB "+e.getMessage());
        }
    }
    
    // Metodo se l'utente crea una nuova partita
    public void NewStart(){
        String query = "DELETE FROM saves";
        PreparedStatement pstm = null;
        try{
            // Eliminiamo il salvataggio presente
            pstm = conn.prepareStatement(query);
            pstm.executeUpdate();
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        finally{
            if(pstm != null) // Controllo se è stato creato lo statement
                try {
                    pstm.close();
            } 
            catch (SQLException ex) {
                System.err.println("Errore chiusura Statement: " + ex.getMessage());
            }
        }
    }
    
    // Metodo se l'utente carica una partita
    public String[] LoadGame() {
        try {
            String query = "SELECT stanza_attuale, enigma_attuale FROM saves WHERE id = 1";
            PreparedStatement pstm = conn.prepareStatement(query);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                String stanza = rs.getString("stanza_attuale");
                String enigma = rs.getString("enigma_attuale");
                rs.close();
                pstm.close();
                return new String[]{stanza, enigma};
            }

            rs.close();
            pstm.close();
            return null;    // nessun salvataggio trovato

        } 
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    private void loadOggettiDaCatologo(){
        catalogoOggetti = new HashMap<>();
        
        try{
            //Leggiamo il file
            BufferedReader reader=new BufferedReader(new FileReader("src/main/resources/oggetti/oggetti.txt"));
            
            //Variabili temporanee che verranno man mano sovrascritte
            String linea;
            int currentId=-1;
            String currentNome="";
            String currentDesc="";
            boolean isNuovoOggetto;
            
            // Fin tanto che la linea letta da file non è nulla...
            while ((linea=reader.readLine())!=null) {
                //... e fin tanto che non è vuota..
                if (linea.trim().isEmpty()){
                    continue;
                }
                /* 
                ... allora divido la linea in diverse parti dove trovo
                il punto e virgola (deciso tra noi come separatore nel file)
                e poi salvo tutto in 3 parti, in modo da istanziare
                */
                String[] parti=linea.split(";",3);
                
                if (parti.length==3){
                    try {
                        // Rimuovo gli spazi bianchi tramite trim()
                        currentId = Integer.parseInt(parti[0].trim());
                        currentNome = parti[1].trim();
                        currentDesc = parti[2].trim();
                        inserisciOggetto(currentId, currentNome, currentDesc);
                    } catch (NumberFormatException e) {
                        System.err.println("DEBUG: Impossibile convertire ID in numero sulla linea n."+linea);
                    }
                } else {
                    System.err.println("DEBUG: Formato della riga non valido, attese 3 parti:"+ linea);
                }
            }
                reader.close();
                System.out.println("DEBUG: Catalogo oggetti: "+ catalogoOggetti.size()+" presenti all'interno");
        }
        catch(IOException e){
            System.out.println("DEBUG: Errore imprevisto: " + e);
        }
    }
    
    public Oggetto getOggettoDaCatalogo(int id){
        return catalogoOggetti.get(id);
    }
    
    private void inserisciOggetto(int id, String nome, String descrizione){
        Oggetto nuovoOggetto;
        
        switch(id){
            case 10:
                nuovoOggetto=new Spada(id, nome, descrizione);
                break;
            default:
                nuovoOggetto=new Oggetto(id, nome, descrizione);
                break;
        }
        
        catalogoOggetti.put(id, nuovoOggetto);
    }
}


