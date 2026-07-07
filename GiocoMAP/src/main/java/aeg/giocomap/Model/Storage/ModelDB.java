/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Storage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author emanuele
 */
public class ModelDB {
    private Connection conn;
    
    public ModelDB(){
        connettiDatabase();
        inizializzaTabelle();
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
    

    public void salvaSeNecessario(String nome, int punteggio) {
        try {
            // controlla se esiste già quel nome con quel punteggio
            String query = "SELECT id FROM records WHERE nome = ? AND punteggio = ?";
            PreparedStatement pstm = conn.prepareStatement(query);
            pstm.setString(1, nome);
            pstm.setInt(2, punteggio);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                System.out.println("TEST: Record già esistente, non salvato");
                rs.close();
                pstm.close();
                return;
            }

            rs.close();
            pstm.close();

            // non esiste → salva
            String insert = "INSERT INTO records (nome, punteggio) VALUES (?, ?)";
            PreparedStatement pstmInsert = conn.prepareStatement(insert);
            pstmInsert.setString(1, nome);
            pstmInsert.setInt(2, punteggio);
            pstmInsert.executeUpdate();
            pstmInsert.close();
            System.out.println("TEST: Record salvato → " + nome + " " + punteggio);

        } 
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public List<String[]> getRecords() {
        List<String[]> lista = new ArrayList<>();
        try {
            String query = "SELECT nome, punteggio FROM records ORDER BY punteggio DESC";
            PreparedStatement pstm = conn.prepareStatement(query);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("nome"),
                    String.valueOf(rs.getInt("punteggio"))
                });
            }
            rs.close();
            pstm.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return lista;
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
    
}
