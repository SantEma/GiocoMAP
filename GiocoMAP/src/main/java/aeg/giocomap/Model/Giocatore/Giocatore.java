/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Giocatore;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Stanza;
import aeg.giocomap.Model.Enigmi;

/**
 *
 * @author emanuele
 */
public class Giocatore {
    private String nome_lore;
    private String nome_player;
    private Stanza stanza_corrente;
    private Inventario<Oggetto> inventario;
    private boolean possiedeInventario;
    private boolean possiedeMappa;
    private Enigma enigma_corrente;
    
    public Giocatore(String nome){
        this.nome_lore=nome;
        this.nome_player="";// Inizialmente vuoto poi il player lo inserirà come sta in "Statistiche"
        this.possiedeInventario=false; // Non ancora ottenuto quando lo si crea
        this.possiedeMappa=false; // Non ha ancora la mappa all'inizio
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
