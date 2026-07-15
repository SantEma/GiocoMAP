package aeg.giocomap.GameEngine;

/**
 *
 * @author andrea
 */
public enum StatoStoria {
    INIZIO(0),
    MISSIONE_COOPER_ACCETTATA(1),
    PARLATO_CONTADINO(2),
    ENIGMA_PESCIVENDOLO_RISOLTO(3),
    CONSEGNATA_CENA(4),
    CAROTE_CONSEGNATE(5),
    INCONTRO_FOX(6),
    ENIGMA_FIORI_RISOLTO(7),
    INCONTRO_CLOCK(8),
    CHIAVE_CONSEGNATA(9),
    INCONTRO_ERIPETA(10),
    ACCUSA_ERIPETA_SUPERATA(11),
    DAVID_INTERPELLATO(12),
    ENIGMA_VINCOLO_RISOLTO(13),
    AMPOLLA_CONSEGNATA(15);

    private final int valore;

    StatoStoria(int valore) {
        this.valore = valore;
    }

    public int getValore() {
        return valore;
    }

    public static StatoStoria daValore(int valore) {
        for (StatoStoria s : values()) {
            if (s.valore == valore) return s;
        }
        return INIZIO; // Default fallback
    }
}
