/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;

import java.util.ArrayList;
import java.util.List;

/*
    ToDo: servirà veramente la lista di oggetti presenti in una stanza?
    Ipotizzo di no, poichè abbiamo la possibilità di far dare tutto al fantoccio 
    ma li tengo al momento nel codice come referenza futura
*/
public abstract class Stanza {
    
    private String nomeStanza;
    private String descrizioneStanza;
    //private List<Oggetto> oggettiPresenti;
    private boolean primaEntrata; 
    
    public Stanza(String nome, String descrizione) {
        this.nomeStanza = nome;
        this.descrizioneStanza = descrizione;
        //this.oggettiPresenti = new ArrayList<>();
        this.primaEntrata = true; 
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
        this.primaEntrata = primaEntrata;
    }

    public String getNome() {
        return nomeStanza;
    }

    public String getDescrizione() {
        return descrizioneStanza;
    }

    /*
    public List<Oggetto> getOggettiPresenti() {
        return oggettiPresenti;
    }
    
    public void aggiungiOggettoNellaStanza(Oggetto o) {
        this.oggettiPresenti.add(o);
    }
    
    public void rimuoviOggettoDallaStanza(Oggetto o) {
        this.oggettiPresenti.remove(o);
    }
    */
}

