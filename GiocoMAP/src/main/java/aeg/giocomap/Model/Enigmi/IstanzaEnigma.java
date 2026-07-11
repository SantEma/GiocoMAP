/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Enigmi;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Util.JsonLoader;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author murgo
 * 
 */

public class IstanzaEnigma {

    // Metodo privato per caricare gli aiuti dal file dialoghi
    private static List<String> caricaAiuti(String enigmaId) {
        JsonObject root = JsonLoader.caricaJson("/dialoghi/dialoghi_hint.json");
        if (root == null) return Collections.emptyList();
        JsonObject aiuti = root.getAsJsonObject("Aiuti_Enigmi");
        if (aiuti == null) return Collections.emptyList();
        return JsonLoader.estraiLista(aiuti, enigmaId);
    }

    // Metodo privato per caricare il testo dal file walloftext
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
        List<String> opzioni = Arrays.asList(
            "Consegna l'Erba Rossa",
            "Consegna l'Erba Blu",
            "Consegna l'Erba Viola"
        );
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
        List<String> opzioni = Arrays.asList(
            "Uno",
            "Tre",
            "Cinque"
        );
        return new EnigmaSceltaMultipla(
            "Enigma_5_Vincolo",
            caricaTesto("Enigma_5_Vincolo"),
            caricaAiuti("Enigma_5_Vincolo"),
            reward,
            opzioni,
            0
        );
    }

    public static EnigmaSceltaMultipla creaEnigma7(Oggetto reward) {
        List<String> opzioni = Arrays.asList(
            "1.Velluto 2.Seta 3.Damasco 4.Lino 5.Broccato",
            "1.Velluto 2.Broccato 3.Lino 4.Damasco 5.Seta",
            "1.Damasco 2.Seta 3.Lino 4.Velluto 5.Broccato",
            "1.Velluto 2.Seta 3.Lino 4.Damasco 5.Broccato"
        );
        return new EnigmaSceltaMultipla(
            "Enigma_7_Principessa",
            caricaTesto("Enigma_7_Principessa"),
            caricaAiuti("Enigma_7_Mercanti_Collaborano"),
            reward,
            opzioni,
            3
        );
    }
}