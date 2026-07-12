package aeg.giocomap.Model.Oggetti;

/**
 *
 * @author emanuele
 */
public class Oggetto {
    int idOggetto;
    String nomeOggetto;
    String descrizioneOggetto;
    
    public Oggetto(int idOggetto, String nome, String descrizioneOggetto){
        this.idOggetto=idOggetto;
        this.nomeOggetto=nome;
        this.descrizioneOggetto=descrizioneOggetto;
    }

    public int getIdOggetto(){
        return idOggetto;
    }
    
    public String getNomeOggetto(){
        return nomeOggetto;
    }
    
    public String getDescrizioneOggetto(){
        return descrizioneOggetto;
    }
}