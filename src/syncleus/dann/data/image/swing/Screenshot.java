/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.data.image.swing;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import syncleus.dann.data.image.ImageManipulation;

/**
 *
 * @author me
 */
public class Screenshot {
    
    public final Robot robot;
    
    public Screenshot() throws AWTException {
        
        robot = new Robot();
        
    }
    
    public BufferedImage capture() {
        Dimension size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        return robot.createScreenCapture(new Rectangle(0,0,size.width,size.height));
    }
    
    /** image area around cursor */
    public BufferedImage captureCursor(int sw, int sh) {
        Point p = MouseInfo.getPointerInfo().getLocation();
        int x1 = p.x - sw/2;
        int y1 = p.y - sh/2;
        return robot.createScreenCapture(new Rectangle(x1, y1, sw, sh));
    }
    
    public static void main(String[] args) throws Exception {
        //BufferedImage i = new Screenshot().capture();
        BufferedImage i = new Screenshot().captureCursor(40, 40);
        
        i = new ImageManipulation(i).scale(0.5, 0.5);
        
        JImagePanel p = new JImagePanel( i );
     
        JFrame j= new JFrame();
        j.setContentPane(new JScrollPane(p));
        j.setSize(400, 400);
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j.setVisible(true);
    }
    
    
}
