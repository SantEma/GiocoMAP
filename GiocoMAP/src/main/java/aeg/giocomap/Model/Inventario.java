/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author emanuele
 * @param <T>
 */

// Classe con lista già tipizzata (Generics), come descritto nelle idee implementative
public class Inventario<T extends Oggetto> {
    
    private List<T> listaOggetti;

    public Inventario() {
        this.listaOggetti = new ArrayList<>();
    }

    public void aggiungiOggetto(T oggetto) {
        listaOggetti.add(oggetto);
    }

    public void rimuoviOggetto(T oggetto) {
        listaOggetti.remove(oggetto);
    }

    /*
    Con questa funzione:
    - Filtriamo per nome oggetto (in lowercase)
        - Se lo trova, si ferma alla prima corrispondenza
        - Altrimenti restituiamo null
    */ 
    
    public T cercaOggetto(String nome) {
        return listaOggetti.stream()
                .filter(obj -> obj.getNomeOggetto().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

    public List<T> getListaOggetti() {
        return listaOggetti;
    }
}
