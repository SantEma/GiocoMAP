
package aeg.giocomap.Util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author emanuele
 */
public class Parser {
    
    // Controlla se la parola chiave è presente nell'input usando regex
    public static boolean contieneParolaChiave(String input, String parolaChiave) {
        // Se non mette nulla o non è quella giusta, dare indietro false
        if (input == null || parolaChiave == null) return false;
        
        // Rimuovo gli spazi dalla parola chiave 
        String chiavePulita = parolaChiave.replaceAll("\\s+", "");
        
        // Regex che accetta spazi opzionali tra ogni carattere della parola chiave
        StringBuilder regexChiave = new StringBuilder();
        for (int i = 0; i < chiavePulita.length(); i++) {
            regexChiave.append(Pattern.quote(String.valueOf(chiavePulita.charAt(i))));
            if (i < chiavePulita.length() - 1) {
                regexChiave.append("\\s*");
            }
        }
        
        String regex = "(?i).*\\b" + regexChiave.toString() + "\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    /*
     * Controllo input con pattern wildcard
     */
    public static boolean contieneRadiceParola(String input, String pattern) {
        if (input == null || pattern == null) return false;

        // Rimozione gli spazi dal pattern
        String patternPulito = pattern.replaceAll("\\s+", "");

        // Separazione radice da wildcard finale
        boolean haWildcard = patternPulito.endsWith("*");
        String radice = haWildcard ? patternPulito.substring(0, patternPulito.length() - 1) : patternPulito;

        if (radice.isEmpty()) return false;

        // Costruisco la regex: ogni carattere della radice con spazi opzionali in mezzo
        StringBuilder regexRadice = new StringBuilder();
        for (int i=0;i<radice.length();i++) {
            regexRadice.append(Pattern.quote(String.valueOf(radice.charAt(i))));
            if (i<radice.length()-1) {
                regexRadice.append("\\s*");
            }
        }

        // Se c'è il wildcard, accetto qualsiasi suffisso di lettere dopo la radice
        // Altrimenti, mi comporto come contieneParolaChiave (match esatto)
        String regex;
        if (haWildcard==true) {
            regex = "(?i).*\\b" + regexRadice.toString() + "\\w*\\b.*";
        } else {
            regex = "(?i).*\\b" + regexRadice.toString() + "\\b.*";
        }

        Pattern compilato = Pattern.compile(regex);
        Matcher matcher = compilato.matcher(input);
        return matcher.matches();
    }

    /*
     * Controlla se l'input contiene almeno una parola che corrisponde
     * ad uno qualsiasi dei pattern forniti.
     */
    public static boolean contieneRadiceParola(String input, String... patterns) {
        if (input == null || patterns == null) return false;
        for (String pattern : patterns) {
            if (contieneRadiceParola(input, pattern)) {
                return true;
            }
        }
        return false;
    }
}
