package aeg.giocomap.Model.Personaggi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emanuele
 */
public class Personaggio extends Entity {
    
    private List<String> alberoDialoghi;
    private int indiceDialogoAttuale;

    public Personaggio(String nomePersonaggio){
        super(nomePersonaggio);
        this.alberoDialoghi=new ArrayList<>();
        this.indiceDialogoAttuale=0;
    }
    
    public void setDialoghi(List<String> dialoghi) {
        this.alberoDialoghi=dialoghi;
        this.indiceDialogoAttuale=0;
    }
    
    // Restituisce la battuta del PG e se ha finito da null
    public String parla() {
        if (alberoDialoghi==null || alberoDialoghi.isEmpty() || indiceDialogoAttuale>=alberoDialoghi.size()) {
            return null; 
        }
        String battuta = alberoDialoghi.get(indiceDialogoAttuale);
        indiceDialogoAttuale++;
        return battuta;
    }
    
    // Utile se l'NPC deve cambiare argomento (da vedere se verrà utilizzato)
    public void resetDialogo() {
        this.indiceDialogoAttuale=0;
    }
}