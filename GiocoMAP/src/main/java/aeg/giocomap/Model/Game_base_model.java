/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Andrea
 */
public class Game_base_model {
    private Connection conn;
    
    // Costruttore che chiama la connessione al DB sin dall'inizio per poter caricare partita
    // o salvarla in seguito
    public Game_base_model(){
        connettiDatabase();
    }
    
    // Funzione che connette al Database
    private void connettiDatabase(){
        try{
            conn = DriverManager.getConnection("jdbc:h2:./saves/DB");
            System.out.println("TEST: Connessione al DB avvenuta");
        }
        catch (SQLException e){
            System.err.println("Errore di connessione al DB: "+e.getMessage());
        }
    }
    
    // Metodo se l'utente crea una nuova partita
    public void NewStart(){
        try{
            // Eliminiamo il salvataggio presente
            String query = "DELETE FROM saves";
            PreparedStatement pstm = conn.prepareStatement(query);
            pstm.executeUpdate();
            pstm.close();
        }
        catch (SQLException e){
            e.getErrorCode();
        }
    }
    
    // Metodo se l'utente carica una partita
    public void LoadGame(){
        try{
            // Andiamo ad estrapolare il salvataggio
            String query = "SELECT stanza_attuale, enigma_attuale FROM saves WHERE id = 1";
            PreparedStatement pstm = conn.prepareStatement(query);
            ResultSet rs = pstm.executeQuery();
            
            // Scorro i risultati dell'interrogazione
            if(rs.next()){
                String stanza = rs.getString(1); // stanza_attuale
                int enigma = rs.getInt(2); //enigma_attuale
            }
            //Se non ci sono salvataggi rs restituisce false
            else{ 
                System.out.println("TEST: Nessun salvataggio trovato");
            }
            rs.close();
            pstm.close();
        }
        catch (SQLException e){
            e.getErrorCode();
        }
    }
}
