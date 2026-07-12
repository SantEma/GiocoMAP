/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Giocatore;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Stanza;
import aeg.giocomap.Model.Enigmi.Enigma;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

/**
 *
 * @author emanuele
 */
public class Giocatore {
    private final String nome_lore;
    private String nome_player;
    private Stanza stanza_corrente;
    private Inventario<Oggetto> inventario;
    private boolean possiedeInventario;
    private boolean possiedeMappa;
    private Enigma enigma_corrente;

    // id degli enigmi gia' risolti (per non rifarli al ricaricamento)
    private final Set<String> enigmiRisolti = new HashSet<>();

    public Giocatore(String nome){
        this.nome_lore=nome;
        this.nome_player="";// Inizialmente vuoto poi il player lo inserirà come sta in "Statistiche"
        this.possiedeInventario=false; // Non ancora ottenuto quando lo si crea
        this.possiedeMappa=false; // Non ha ancora la mappa all'inizio
    }

    // ---- gestione enigmi risolti ----
    public void aggiungiEnigmaRisolto(String idEnigma){
        if (idEnigma != null) enigmiRisolti.add(idEnigma);
    }

    public boolean isEnigmaRisolto(String idEnigma){
        return enigmiRisolti.contains(idEnigma);
    }

    public Set<String> getEnigmiRisolti(){
        return enigmiRisolti;
    }

    // ripristina l'insieme degli enigmi risolti (usato in caricamento)
    public void setEnigmiRisolti(Collection<String> ids){
        enigmiRisolti.clear();
        if (ids != null) enigmiRisolti.addAll(ids);
    }
    
    public Inventario<Oggetto> getInventario(){
        // creamo l'inventario solo alla prima chiamata
        if(this.inventario==null) this.inventario=new Inventario<>();
        
        return this.inventario; // se non esiste da questo inventario perché non ha ancora quello esistente
    }
    
    public boolean isPossiedeInventario(){
        return possiedeInventario;
    }
    
    public void setPossiedeInventario(boolean possiedeInventario){
        this.possiedeInventario=possiedeInventario;
    }
    
    public Stanza getStanzaCorrente(){
        return stanza_corrente;
    }
    
    public Enigma getIdEnigmaCorrente(){
        return enigma_corrente;
    }
    
    public String getNomePlayer(){
        return this.nome_player;
    }
    
    public void setNomePlayer(String nome){
        this.nome_player=nome;
    }
    
    public void setStanzaCorrente(Stanza stanza_corrente){
        this.stanza_corrente=stanza_corrente; // così sappiamo in che stanza è eryndor ogni volta che si sposta
    }
    
    public boolean isPossiedeMappa(){
        return possiedeMappa;
    }
    
    // facendo così andiamo ad agevolare il salvataggio
    public void setPossiedeMappa(boolean possiedeMappa){
        this.possiedeMappa=possiedeMappa;
    }
}
