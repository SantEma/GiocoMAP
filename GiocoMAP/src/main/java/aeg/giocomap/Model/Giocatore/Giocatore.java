package aeg.giocomap.Model.Giocatore;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Oggetti.Spada;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

/**
 *
 * @author emanuele
 */
public class Giocatore {
    private String nome_player;
    private Inventario<Oggetto> inventario;
    private boolean possiedeInventario;
    private boolean possiedeMappa;

    // ID degli enigmi gia' risolti (per non rifarli al ricaricamento)
    private final Set<String> enigmiRisolti = new HashSet<>();

    public Giocatore(){
        this.nome_player="";// Inizialmente vuoto poi il player lo inserirà come sta nelle stats
        this.possiedeInventario=false; // Non ancora ottenuto quando lo si crea
        this.possiedeMappa=false; // Non ha ancora la mappa all'inizio
    }

    // ---- gestione enigmi risolti ----
    public void aggiungiEnigmaRisolto(String idEnigma){
        if (idEnigma != null) {
            getEnigmiRisolti().add(idEnigma);
        }
    }

    // Fa avanzare di uno step la sincronia della Spada Sincro, se posseduta
    public void ricaricaSpadaSincro(){
        Oggetto spada = getInventario().cercaOggetto("Spada Sincro");
        if (spada instanceof Spada spadas) {
            spadas.reagisciRisoluzioneEnigma();
        }
    }

    public boolean isEnigmaRisolto(String idEnigma){
        return getEnigmiRisolti().contains(idEnigma);
    }

    public Set<String> getEnigmiRisolti(){
        return enigmiRisolti;
    }

    // Ripristina l'insieme degli enigmi risolti (usato al caricamento di una partita)
    public void setEnigmiRisolti(Collection<String> ids){
        enigmiRisolti.clear();
        if (ids != null) enigmiRisolti.addAll(ids);
    }
    
    public Inventario<Oggetto> getInventario(){
        //Ccreamo l'inventario solo alla prima chiamata
        if(this.inventario==null) this.inventario=new Inventario<>();
        
        return this.inventario;
    }
    
    public boolean isPossiedeInventario(){
        return possiedeInventario;
    }
    
    public void setPossiedeInventario(boolean possiedeInventario){
        this.possiedeInventario=possiedeInventario;
    }
    
    public String getNomePlayer(){
        return this.nome_player;
    }
    
    public void setNomePlayer(String nome){
        this.nome_player=nome;
    }
    
    public boolean isPossiedeMappa(){
        return possiedeMappa;
    }
    
    // Facendo così andiamo ad agevolare il salvataggio della mappa
    public void setPossiedeMappa(boolean possiedeMappa){
        this.possiedeMappa=possiedeMappa;
    }
}
