package aeg.giocomap.Model.Personaggi;

/**
 *
 * @author emanuele
 */
public class Fantoccio extends Personaggio{

    public Fantoccio() {
        // Dando fantoccio come nome può recuperare i dialoghi del fantoccio (che sono quelli a schermo cammuffati con nessun nome)
        super("Fantoccio");
    }
}
