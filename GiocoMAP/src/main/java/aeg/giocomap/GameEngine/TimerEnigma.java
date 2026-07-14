package aeg.giocomap.GameEngine;

import javax.swing.Timer;

/**
 *
 * @author murgo
 *
 */
public class TimerEnigma {

    private long inizioMs;
    private int secondiFinali;
    private boolean attivo;

    // usato solo per l'azione periodica di debug, eseguita sull'EDT
    private final Timer tickTimer;

    public TimerEnigma(Runnable onTick) {
        this.tickTimer = new Timer(1000, e -> {
            if (onTick != null) onTick.run();
        });
        this.secondiFinali = 0;
        this.attivo = false;
    }

    public boolean isAttivo() {
        return attivo;
    }

    // Secondi trascorsi: calcolati in tempo reale mentre e' attivo,
    // oppure il valore finale congelato dopo ferma()
    public int getSecondi() {
        if (isAttivo()) {
            return (int) ((System.currentTimeMillis() - inizioMs) / 1000);
        }
        return secondiFinali;
    }

    // Avvia il timer
    public void avvia() {
        inizioMs = System.currentTimeMillis();
        secondiFinali = 0;
        attivo = true;
        tickTimer.start();
        System.out.println("DEBUG: Timer avviato");
    }

    // Ferma il timer e congela i secondi trascorsi
    public void ferma() {
        if (isAttivo()) {
            secondiFinali = (int) ((System.currentTimeMillis() - inizioMs) / 1000);
            attivo = false;
        }
        tickTimer.stop();
        System.out.println("DEBUG: Timer fermato a " + secondiFinali + "s");
    }
}
