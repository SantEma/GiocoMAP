package aeg.giocomap.Network;

/**
 *
 * @author giulio
 */
public class Message {
    private final TipoMessaggio tipo;
    private final String mittente;
    private final String contenuto;

    public Message(TipoMessaggio tipo, String mittente, String contenuto) {
        this.tipo = tipo;
        this.mittente = mittente;
        this.contenuto = contenuto;
    }

    public TipoMessaggio getTipo() { return tipo; }
    public String getMittente() { return mittente; }
    public String getContenuto() { return contenuto; }
}