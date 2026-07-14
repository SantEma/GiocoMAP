package aeg.giocomap.Network;

/**
 *
 * @author giulio
 */
public class MessageParser {
    // definisco un separatore
    private static final String SEP = "|";

    // converte Message in stringa da mandare sul socket
    // ogni stringa viene divisa in 3 parti (tipo, mittente, contenuto)
    public static String serializza(Message m) {
        return m.getTipo() + SEP + m.getMittente() + SEP + m.getContenuto();
    }

    // converte  una stringa ricevuta dal socket in Message
    public static Message deserializza(String raw) {
        try {
            String[] parti = raw.split("\\|", 3);
            TipoMessaggio tipo = TipoMessaggio.valueOf(parti[0]);
            String mittente = parti[1];
            String contenuto = parti[2];
            return new Message(tipo, mittente, contenuto);
        } catch (Exception e) {
            System.err.println("Errore parsing messaggio: " + raw);
            return null;
        }
    }
}