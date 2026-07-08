/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Giocatore;

import aeg.giocomap.Model.Stanza;
import aeg.giocomap.Model.Oggetti.Oggetto;

/**
 *
 * @author emanuele
 */

public class Giocatore {
    
    private String nome;
    private Stanza stanzaCorrente;
    private Inventario<Oggetto> inventario;
    private int punteggio;
    //private int idEnigma; Da scommentare appena enigma è implementato
    
    public Giocatore() {
        this.inventario = new Inventario<>();
        this.punteggio = 0;
    }

    public String getNome(){
        return nome; 
    }
    
    public Stanza getStanzaCorrente() { 
        return stanzaCorrente; 
    }
    
    public int getPunteggio() { 
        return punteggio; 
    }
    
    public Inventario<Oggetto> getInventario() { 
        return inventario; 
    }
    
    public void setStanzaCorrente(Stanza stanza) { 
        this.stanzaCorrente = stanza;
        if(this.stanzaCorrente != null){
            this.stanzaCorrente.entra();
        }
    }
    
    public void aggiungiPunteggio(int punti) { 
        this.punteggio += punti; 
    }

    public boolean spostati(String direzione) {
        if (stanzaCorrente != null) {
            Stanza prossimaStanza = stanzaCorrente.getStanzaAdiacente(direzione);
            if (prossimaStanza != null) {
                setStanzaCorrente(prossimaStanza);
                return true;
            }
        }
        return false;
    }
    
    /*
    ToDo: bisogna controllare se questa cosa verrà utilizzata, probabilmente sì
    per la progressione della storia
    public boolean haOggetto(String nomeOggetto) {
        return inventario.cercaOggetto(nomeOggetto)!=null;
    }
    */
}