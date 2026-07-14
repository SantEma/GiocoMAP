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
            this.caricaSincro+=percentuale;
            
            /*
            Controllo della ricarica, il massimo è 99 perchè ci sono solo 3
            enigmi che includono la spada
            */
            if (this.caricaSincro>CARICA_MASSIMA) {
                this.caricaSincro=CARICA_MASSIMA;
            }
            System.out.println("DEBUG: La " + getNomeOggetto() + " ha come carica attuale: " + this.caricaSincro + "%");
        }

        public int getCaricaSincro() {
            return caricaSincro;
        }

        public void setCaricaSincro(int caricaSincro) {
            this.caricaSincro = caricaSincro;
        }

        // La spada è al massimo della sincronia: usato per sbloccare momenti
        // narrativi legati al suo pieno potenziamento (es. finale della Principessa).
        public boolean isCaricaMassima() {
            return caricaSincro >= CARICA_MASSIMA;
        }
}