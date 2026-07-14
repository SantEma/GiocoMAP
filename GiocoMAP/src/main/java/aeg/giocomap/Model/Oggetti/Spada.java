package aeg.giocomap.Model.Oggetti;
/**
 *
 * @author emanuele
 */
    public class Spada extends Oggetto implements GiveObject{
        private static final int CARICA_MASSIMA = 99;

        private int caricaSincro;

        public Spada(int idSpada, String nomeSpada, String descrizioneSpada){
            super(idSpada, nomeSpada, descrizioneSpada);
            this.caricaSincro=0;
        }
        
        //Grazie all'override, il programma capisce che è la spada
        @Override
        public void reagisciRisoluzioneEnigma() {
            this.ricaricaSpada(33);
        }
        
        // Metodo per ricaricare la spada dopo un enigma
        private void ricaricaSpada(int percentuale) {
            setCaricaSincro(getCaricaSincro() + percentuale);
            System.out.println("DEBUG: La " + getNomeOggetto() + " ha come carica attuale: " + getCaricaSincro() + "%");
        }

        public int getCaricaSincro() {
            return caricaSincro;
        }

        /*
        Controllo della ricarica, il massimo è 99 perchè ci sono solo 3
        enigmi che includono la spada
        */
        public void setCaricaSincro(int caricaSincro) {
            this.caricaSincro = Math.min(caricaSincro, CARICA_MASSIMA);
        }

        // La spada è al massimo della sincronia: usato per sbloccare momenti
        // narrativi legati al suo pieno potenziamento (es. finale della Principessa).
        public boolean isCaricaMassima() {
            return getCaricaSincro() >= CARICA_MASSIMA;
        }
}