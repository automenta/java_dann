/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package annarqi;

import java.io.IOException;
import syncleus.dann.learn.deepbelief.DBN;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.math.random.XORShiftRandom;
import syncleus.dann.solve.mnist.MNIST;


public class DBMMnist {
    private final DBN dbn;

    public DBMMnist() throws IOException {
        String path = "/home/me/Downloads";
        
        int trainingCases = 300;
        
        MNIST tm = new MNIST(path,trainingCases,9);
        int numRows = tm.numRows;
        int numCols = tm.numCols;
        System.out.println(numRows + "x" + numCols);
        
        
        dbn = new DBN(trainingCases, numRows*numCols, new int[] { numRows }, 10, new XORShiftRandom());

        double pretrain_lr = 0.1;
        int pretraining_epochs = 1000;
        int k = 1;
        double finetune_lr = 0.1;
        int finetune_epochs = 500;

        double[][] train_X = tm.getImageVectors();
        double[][] train_Y = tm.getLabelVectors(true);
        
        
        
        // pretrain
        dbn.pretrain(train_X, pretrain_lr, k, pretraining_epochs);

        // finetune
        dbn.finetune(train_X, train_Y, finetune_lr, finetune_epochs);        
        
        
        int testingCases = 50;
        MNIST sm = new MNIST(path, testingCases, 9, trainingCases);
        
        // test data
        double[][] test_X = sm.getImageVectors();		        
        double[][] test_Y = sm.getLabelVectors(false);
		
        // test
        for(int i=0; i<testingCases; i++) {
                dbn.predict(test_X[i], test_Y[i]);
                int actual = sm.images.get(i).label;
                System.out.print(actual + ": ");
                int predicted = EngineArray.maxIndex(test_Y[i]);
                System.out.print(" ?=" + predicted);
                System.out.print(" " + (predicted==actual) + " ");
                for(int j=0; j<10; j++) {
                    
                        System.out.print((int)(100.0 * test_Y[i][j]) + " ");
                }
                System.out.println();
        }        
    }

    
    public static void main(String[] args) throws Exception {
        new DBMMnist();
    }
}
