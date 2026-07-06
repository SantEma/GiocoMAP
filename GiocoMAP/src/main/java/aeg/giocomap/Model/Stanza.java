/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;

import java.util.ArrayList;
import java.util.List;

public abstract class Stanza {
    
    private String nomeStanza;
    private String descrizioneStanza;
    private boolean primaEntrata; 
    
    public Stanza(String nome, String descrizione) {
        this.nomeStanza = nome;
        this.descrizioneStanza = descrizione;
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

}

