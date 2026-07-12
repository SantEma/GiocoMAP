package aeg.giocomap.Model;

import java.util.*;

/**
 *
 * @author emanuele
 */
public class Stanza {
    
    private final String nomeStanza;
    private boolean primaEntrata; // Controllo sulla prima volta in una stanza, utile per i dialoghi
    private final Map<String, Stanza> uscite;
    
    public Stanza(String nome) {
        this.nomeStanza=nome;
        this.primaEntrata=true;
        this.uscite = new HashMap<>();
    }

    public void entra() {
        if (primaEntrata) {
            System.out.println("DEBUG: Entrata in " + nomeStanza + " per la prima volta, avvio dialogo unico");
            // Qui bisognerebbe far partire il dialogo unico, da vedere come deve essere strutturato
            setPrimaEntrata(false); 
        } else {
            System.out.println("DEBUG: Tornato in " + nomeStanza + ".");
        }
    }

    public boolean isPrimaEntrata() {
        return primaEntrata;
    }

    public void setPrimaEntrata(boolean primaEntrata) {
        this.primaEntrata=primaEntrata;
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