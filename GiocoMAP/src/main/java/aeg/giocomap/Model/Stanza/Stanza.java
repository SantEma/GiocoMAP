package aeg.giocomap.Model.Stanza;

import java.util.*;

/**
 *
 * @author emanuele
 */
public class Stanza {
    
    private final String nomeStanza;
    private final Map<String, Stanza> uscite;
    
    public Stanza(String nome) {
        this.nomeStanza=nome;
        this.uscite = new HashMap<>();
    }
    
    public String getNome() {
        return nomeStanza;
    }

    public void impostaUscita(String direzione, Stanza stanza) {
        uscite.put(direzione.toUpperCase(),stanza);
    }
    
    public Stanza getStanzaAdiacente(String direzione) {
        return uscite.get(direzione.toUpperCase());
    }
}