package aeg.giocomap;

import aeg.giocomap.View.MainFrame;
import aeg.giocomap.GameEngine.GameEngine;
import aeg.giocomap.Model.Storage.*;

/**
 *
 * @author emanuele
 */
public class GiocoMAP {
    
    public static void main(String args[]){
 
        // View principale
        MainFrame frame = new MainFrame();
        
        // Controller che gestisce Model e View
        GameEngine engine = new GameEngine(frame);
        
        // Finestra
        frame.setVisible(true);
        
    }
}
