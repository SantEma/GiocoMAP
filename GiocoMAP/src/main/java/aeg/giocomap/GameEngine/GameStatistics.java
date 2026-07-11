package aeg.giocomap.GameEngine;

import aeg.giocomap.Model.Giocatore.Giocatore;
import aeg.giocomap.Model.Storage.ModelDB;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.TitoliDiCoda;
import aeg.giocomap.Model.Enigmi.Enigma;
import javax.swing.JOptionPane;
import java.util.List;

public class GameStatistics {

    private long tempoInizioEnigma = 0;
    private int punteggioTotale = 0;
    private TimerEnigma timerEnigma;

    private final MainFrame frame;
    private final Giocatore giocatore;
    private final ModelDB db;
    private final MusicPlayer music_player;
    private final SceneManager sceneManager;

    public GameStatistics(MainFrame frame, Giocatore giocatore, ModelDB db, MusicPlayer music_player, SceneManager sceneManager) {
        this.frame = frame;
        this.giocatore = giocatore;
        this.db = db;
        this.music_player = music_player;
        this.sceneManager = sceneManager;
    }

    public void iniziaEnigma() {
        timerEnigma = new TimerEnigma(() -> {
            System.out.println("DEBUG: Secondi: " + timerEnigma.getSecondi());
        });
        timerEnigma.avvia();
        System.out.println("DEBUG: Enigma iniziato");
    }

    public void enigmaRisolto(Enigma enigma) {
        if (timerEnigma != null) timerEnigma.ferma();

        int secondi = timerEnigma != null ? timerEnigma.getSecondi() : 0;
        int punti = calcolaPunti(secondi);
        punteggioTotale += punti;

        if (enigma.getReward() != null) {
            giocatore.getInventario().aggiungiOggetto(enigma.getReward());
            System.out.println("DEBUG: Reward aggiunto: " + enigma.getReward().getNomeOggetto());
        }

        System.out.println("DEBUG: " + secondi + "s, " + punti + " punti, totale: " + punteggioTotale);
    }

    private int calcolaPunti(int secondi) {
        int fascia;
        if (secondi <= 100)      fascia = 1;
        else if (secondi <= 150) fascia = 2;
        else if (secondi <= 220) fascia = 3;
        else if (secondi <= 380) fascia = 4;
        else                     fascia = 5;

        return switch (fascia) {
            case 1 -> 1000;
            case 2 -> 700;
            case 3 -> 500;
            case 4 -> 300;
            default -> 100;
        };
    }

    public void Statistiche(boolean fineGioco) {
        String nome = "";
        int punteggio = 0;

        if (fineGioco) {
            punteggio = punteggioTotale;

            while (nome == null || nome.trim().isEmpty()) {
                nome = JOptionPane.showInputDialog(frame,
                    "Inserisci il tuo nome per salvare il punteggio:",
                    "Fine gioco!", JOptionPane.PLAIN_MESSAGE);
                if (nome == null || nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                        "Devi inserire un nome per continuare!",
                        "Attenzione", JOptionPane.WARNING_MESSAGE);
                }
            }
            giocatore.setNomePlayer(nome.trim());
            db.salvaSeNecessario(giocatore.getNomePlayer(), punteggio);
        }

        List<String[]> records = db.getRecords();
        String nome_passato = (giocatore != null && giocatore.getNomePlayer() != null
            ? giocatore.getNomePlayer() : "");
        music_player.stopMusic();
        if (fineGioco) music_player.playMusic(MusicPlayer.END_TITLE_MUSIC);
        TitoliDiCoda titoli = new TitoliDiCoda(records, punteggio, nome_passato);
        
        titoli.addIndietroListener(e -> {
            music_player.stopMusic();
            music_player.playMusic(MusicPlayer.TITLE_SCREEN_MUSIC);
            sceneManager.mostraScena("MENU_PRINCIPALE");
        });

        sceneManager.registraScena("TITOLI_CODA", titoli);
        sceneManager.mostraScena("TITOLI_CODA");
    }
}
