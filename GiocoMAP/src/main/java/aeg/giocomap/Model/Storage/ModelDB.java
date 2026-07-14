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
    }

    // Funzione che connette al Database e crea/aggiorna le tabelle
    private void connettiDatabase(){
        try{
            conn = DriverManager.getConnection("jdbc:h2:./saves/DB");
            System.out.println("TEST: Connessione al DB avvenuta");

            // saves: la stanza e' il NOME della scena (testo), non un numero
            eseguiUpdate("CREATE TABLE IF NOT EXISTS saves ("
                    + "id INT PRIMARY KEY,"
                    + "stanza_attuale VARCHAR(100),"
                    + "enigma_attuale INT)");

            // migrazione per i vecchi DB dove stanza_attuale era INT:
            // converto la colonna in testo senza perdere l'eventuale riga
            try {
                eseguiUpdate("ALTER TABLE saves ALTER COLUMN stanza_attuale VARCHAR(100)");
            } catch (SQLException e) {
                // colonna gia' del tipo giusto: ignoro
            }

            // colonne per lo stato di avanzamento (aggiunte se mancano)
            try {
                eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS stato_city INT DEFAULT 0");
                eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS possiede_mappa BOOLEAN DEFAULT FALSE");
                eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS enigmi_risolti VARCHAR(1000)");
                eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS inventario VARCHAR(1000)");
                eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS primo_accesso_palazzo BOOLEAN DEFAULT TRUE");
                eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS carica_spada INT DEFAULT 0");
            } catch (SQLException e) {
                System.err.println("Errore aggiornamento colonne saves: " + e.getMessage());
            }

            // records: classifica dei punteggi
            eseguiUpdate("CREATE TABLE IF NOT EXISTS records ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nome VARCHAR(50),"
                    + "punteggio INT,"
                    + "data TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // difesa contro vecchi DB creati senza la colonna nome
            try {
                eseguiUpdate("ALTER TABLE records ADD COLUMN IF NOT EXISTS nome VARCHAR(50)");
            } catch (SQLException e) {
                // colonna gia' presente: ignoro
            }
        }
        catch (SQLException e){
            System.err.println("Errore di connessione al DB: "+e.getMessage());
        }
    }

    // piccolo helper per eseguire una CREATE/ALTER chiudendo lo statement
    private void eseguiUpdate(String sql) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.executeUpdate();
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

            // non esiste, quindi lo salva
            String insert = "INSERT INTO records (nome, punteggio) VALUES (?, ?)";
            PreparedStatement pstmInsert = conn.prepareStatement(insert);
            pstmInsert.setString(1, nome);
            pstmInsert.setInt(2, punteggio);
            pstmInsert.executeUpdate();
            pstmInsert.close();
            System.out.println("TEST: Record salvato : " + nome + " " + punteggio);

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
    public void newStart(){
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
    
    // Carica la partita. Restituisce:
    //   [0] = nome scena, [1] = statoCity, [2] = possiedeMappa,
    //   [3] = enigmi risolti (id separati da virgola), [4] = inventario,
    //   [5] = primoAccessoPalazzo, [6] = caricaSpada
    public String[] loadGame() {
        try {
            String query = "SELECT stanza_attuale, stato_city, possiede_mappa, enigmi_risolti, inventario, primo_accesso_palazzo, carica_spada FROM saves WHERE id = 1";
            PreparedStatement pstm = conn.prepareStatement(query);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                String stanza = rs.getString("stanza_attuale");
                String statoCity = String.valueOf(rs.getInt("stato_city"));
                String possiedeMappa = String.valueOf(rs.getBoolean("possiede_mappa"));
                String enigmi = rs.getString("enigmi_risolti");
                if (enigmi == null) enigmi = "";
                String inventario = rs.getString("inventario");
                if (inventario == null) inventario = "";
                String primoAccessoPalazzo = String.valueOf(rs.getBoolean("primo_accesso_palazzo"));
                String caricaSpada = String.valueOf(rs.getInt("carica_spada"));
                rs.close();
                pstm.close();
                return new String[]{stanza, statoCity, possiedeMappa, enigmi, inventario, primoAccessoPalazzo, caricaSpada};
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

    // Salva (o aggiorna) la partita corrente nella riga id=1
    public void salvaPartita(String stanzaAttuale, int statoCity,
                             boolean possiedeMappa, String enigmiRisolti, String inventario, boolean primoAccessoPalazzo,
                             int caricaSpada) {
        try {
            // MERGE = insert o update: funziona sia se il salvataggio esiste già
            // sia se è il primo, senza violare la primary key id=1
            String query = "MERGE INTO saves (id, stanza_attuale, enigma_attuale, stato_city, possiede_mappa, enigmi_risolti, inventario, primo_accesso_palazzo, carica_spada) "
                    + "KEY(id) VALUES (1, ?, 0, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstm = conn.prepareStatement(query);
            pstm.setString(1, stanzaAttuale);
            pstm.setInt(2, statoCity);
            pstm.setBoolean(3, possiedeMappa);
            pstm.setString(4, enigmiRisolti);
            pstm.setString(5, inventario);
            pstm.setBoolean(6, primoAccessoPalazzo);
            pstm.setInt(7, caricaSpada);
            pstm.executeUpdate();
            pstm.close();
            System.out.println("TEST: Partita salvata : " + stanzaAttuale
                    + " (statoCity=" + statoCity + ", enigmi=[" + enigmiRisolti + "], inventario=[" + inventario + "], primoAccessoPalazzo=" + primoAccessoPalazzo + ", caricaSpada=" + caricaSpada + ")");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
