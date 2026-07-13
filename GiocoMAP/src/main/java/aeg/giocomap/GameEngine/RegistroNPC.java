package aeg.giocomap.GameEngine;

import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.Model.Personaggi.Fantoccio;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Registro dei personaggi attivi nella partita.
 *
 * Estratto da {@link ProgressioneStoria} come primo passo di scorporamento
 * della god class: raccoglie la creazione/registrazione degli NPC e dei
 * Fantocci, senza alterarne il comportamento.
 * 
 * @author Giulio
 */
public class RegistroNPC {

    private final Map<String, Personaggio> registro = new HashMap<>();

    /**
     * Crea un Personaggio con i dialoghi indicati, lo registra e lo restituisce.
     */
    public Personaggio registraNPC(String nome, List<String> dialoghi) {
        Personaggio pg = new Personaggio(nome);
        pg.setDialoghi(dialoghi);
        registro.put(nome, pg);
        return pg;
    }

    /**
     * Crea un Fantoccio con i dialoghi indicati, lo registra sotto la chiave
     * data e lo restituisce.
     */
    public Fantoccio registraFantoccio(String chiaveRegistro, List<String> dialoghi) {
        Fantoccio f = new Fantoccio();
        f.setDialoghi(dialoghi);
        registro.put(chiaveRegistro, f);
        return f;
    }
}
