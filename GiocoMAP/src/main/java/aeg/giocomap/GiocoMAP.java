/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package aeg.giocomap;

import aeg.giocomap.Model.Game_base_model;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.GameEngine.GameEngine;
/**
 *
 * @author emanuele
 */
public class GiocoMAP {
    
    public static void main(String args[]){
        // Game_base Model
        Game_base_model model = new Game_base_model();
        
        // View principale
        MainFrame frame = new MainFrame();
        
        // Controller che gestisce Model e View
        GameEngine engine = new GameEngine(model, frame);
        
        // Finestra
        frame.setVisible(true);
        
    }
}
