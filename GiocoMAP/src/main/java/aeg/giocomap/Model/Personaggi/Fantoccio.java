/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Personaggi;

import aeg.giocomap.Model.Giocatore.Giocatore;
import aeg.giocomap.Model.Oggetti.Oggetto;

/**
 *
 * @author emanuele
 */
public class Fantoccio extends Personaggio{

    public Fantoccio() {
        // Dando fantoccio come nome può recuperare i dialoghi del fantoccio (che sono quelli a schermo cammuffati)
        super("Fantoccio");
    }

    public void daiOggetto(Oggetto oggettoDaConsegnare, Giocatore giocatore) {
        if (oggettoDaConsegnare != null && giocatore != null) {
            giocatore.getInventario().aggiungiOggetto(oggettoDaConsegnare);
            
            System.out.println("DEBUG: Il fantoccio ha inserito silenziosamente l'oggetto [" 
                    + oggettoDaConsegnare.getNomeOggetto() + "] nell'inventario.");
        }
    }
    
    
}
