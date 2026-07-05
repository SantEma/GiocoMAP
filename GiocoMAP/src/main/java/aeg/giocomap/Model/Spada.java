/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;

/**
 *
 * @author emanuele
 */
    public class Spada extends Oggetto {
        private int caricaSincro;
            public Spada(int id, String nome){
                super(id, nome);
                this.caricaSincro=0;
            }
    
        // Metodo per ricaricare la spada dopo un enigma
        public void ricaricaSpada(int percentuale) {
            this.caricaSincro+=percentuale;
                if (this.caricaSincro>99) {
                    this.caricaSincro=99;
                }
            System.out.println("La " + getNomeOggetto() + " ha come carica attuale: " + this.caricaSincro + "%");
        }

        public int getCaricaSincro() {
            return caricaSincro;
        }
}