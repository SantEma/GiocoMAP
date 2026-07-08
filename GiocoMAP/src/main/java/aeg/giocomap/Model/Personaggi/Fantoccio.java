/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Personaggi;

/**
 *
 * @author emanuele
 */
public class Fantoccio extends Entity{

    public Fantoccio() {
        super("System_Fantoccio_Invisibile");
    }
    /*    
    DA INSERIRE:
    Giocatore deve essere ancora implementato, per questo ora il fantoccio è 
    sottocommento per poter permettere al team di andare avanti
    
    public void daiOggetto(Oggetto oggettoDaConsegnare, Giocatore giocatore) {
        if (oggettoDaConsegnare != null && giocatore != null) {
            giocatore.getInventario().aggiungi(oggettoDaConsegnare);
            System.out.println("DEBUG: Il fantoccio ha inserito l'oggetto " + oggettoDaConsegnare.getNomeOggetto() +" nell'inventario.");
        }
        }
    }
*/
}
