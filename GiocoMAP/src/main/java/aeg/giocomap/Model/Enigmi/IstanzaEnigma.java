package aeg.giocomap.Model.Enigmi;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Util.JsonLoader;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author giulio
 */
public class IstanzaEnigma {

    // Metodo privato per caricare gli aiuti dal file dialoghi
    private static List<String> caricaAiuti(String enigmaId) {
        JsonObject root = JsonLoader.caricaJson("/dialoghi/dialoghi_hint.json");
        if (root == null) return Collections.emptyList();
        JsonObject aiuti = root.getAsJsonObject("Aiuti_Enigmi");
        if (aiuti == null || !aiuti.has(enigmaId)) return Collections.emptyList();
        
        com.google.gson.JsonArray arr = aiuti.getAsJsonArray(enigmaId);
        String[] ops = new String[arr.size()];
        for(int i=0; i<arr.size(); i++){
            ops[i] = arr.get(i).getAsString();
        }
        return Arrays.asList(ops);
    }

    private static List<String> caricaOpzioni(String enigmaId) {
        JsonObject root = JsonLoader.caricaJson("/dialoghi/dialoghi_hint.json");
        if (root == null) return Collections.emptyList();
        JsonObject opzioni = root.getAsJsonObject("Opzioni_Enigmi");
        if (opzioni == null || !opzioni.has(enigmaId)) return Collections.emptyList();

        com.google.gson.JsonArray arr = opzioni.getAsJsonArray(enigmaId);
        String[] ops = new String[arr.size()];
        for(int i=0; i<arr.size(); i++){
            ops[i] = arr.get(i).getAsString();
        }
        return Arrays.asList(ops);
    }

    // Metodo privato per caricare la lista dei vestiti (o l'ordine corretto) dell'enigma dei vestiti finale
    private static List<String> caricaListaVestiti(String enigmaId, String campo) {
        JsonObject root = JsonLoader.caricaJson("/dialoghi/dialoghi_hint.json");
        if (root == null) return Collections.emptyList();
        JsonObject vestitiEnigmi = root.getAsJsonObject("Vestiti_Enigmi");
        if (vestitiEnigmi == null || !vestitiEnigmi.has(enigmaId)) return Collections.emptyList();
        JsonObject enigmaVestiti = vestitiEnigmi.getAsJsonObject(enigmaId);
        if (enigmaVestiti == null || !enigmaVestiti.has(campo)) return Collections.emptyList();

        com.google.gson.JsonArray arr = enigmaVestiti.getAsJsonArray(campo);
        String[] valori = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            valori[i] = arr.get(i).getAsString();
        }
        return Arrays.asList(valori);
    }

    // Serve per caricare il testo dal wall of text
    private static String caricaTesto(String chiave) {
        JsonObject root = JsonLoader.caricaJson("/dialoghi/walloftext.json");
        if (root == null) return "";
        JsonObject schermo = root.getAsJsonObject("Schermo");
        if (schermo == null) return "";
        return JsonLoader.estraiStringa(schermo, chiave);
    }

    public static EnigmaTestuale creaEnigma1(Oggetto reward) {
        return new EnigmaTestuale(
            "Enigma_1_Porto",
            caricaTesto("Enigma_1_Lettera"),
            caricaAiuti("Enigma_1_Porto"),
            reward,
            "porto"
        );
    }

    public static EnigmaTestuale creaEnigma2(Oggetto reward) {
        return new EnigmaTestuale(
            "Enigma_2_Germi",
            caricaTesto("Enigma_2_Germi"),
            caricaAiuti("Enigma_2_Germi"),
            reward,
            "59"
        );
    }

    public static EnigmaSceltaMultipla creaEnigma3(Oggetto reward) {
        List<String> opzioni = caricaOpzioni("Enigma_3_Fiori");
        return new EnigmaSceltaMultipla(
            "Enigma_3_Fiori",
            caricaTesto("Cartello_Esploratori"),
            caricaAiuti("Enigma_3_Fiori"),
            reward,
            opzioni,
            2
        );
    }

    public static EnigmaTestuale creaEnigma4(Oggetto reward) {
        return new EnigmaTestuale(
            "Enigma_4_Orologio",
            caricaTesto("Enigma_4_Orologio"),
            caricaAiuti("Enigma_4_Orologio_Fucina"),
            reward,
            "10"
        );
    }

    public static EnigmaSceltaMultipla creaEnigma5(Oggetto reward) {
        List<String> opzioni = caricaOpzioni("Enigma_5_Vincolo");
        return new EnigmaSceltaMultipla(
            "Enigma_5_Vincolo",
            caricaTesto("Enigma_5_Vincolo") + "\n" + caricaTesto("Enigma_5_Vincolo_domanda"),
            caricaAiuti("Enigma_5_Vincolo"),
            reward,
            opzioni,
            0
        );
    }

    public static EnigmaOrdinamento creaEnigmaFinale(Oggetto reward) {
        List<String> vestiti = caricaListaVestiti("Enigma_Finale_Principessa", "vestiti");
        List<String> ordineCorretto = caricaListaVestiti("Enigma_Finale_Principessa", "ordine_corretto");
        return new EnigmaOrdinamento(
            "Enigma_Finale_Principessa",
            "",
            caricaAiuti("Enigma_Finale_Mercanti_Collaborano"),
            reward,
            vestiti,
            ordineCorretto
        );
    }
}