/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pca;

import java.awt.image.BufferedImage;
import syncleus.dann.data.image.GrayscaleImageData;
import syncleus.dann.data.matrix.Matrix;

/**
 *
 * @author Sanja
 */
public class PCATest {

    /**
     * Get image using specified number of principal components
     * @param image
     * @param rank
     * @return 
     */
    public static BufferedImage getImage(BufferedImage image,int rank) {
        
        double[][] niz = GrayscaleImageData.convertTo2DArray(image);
        Matrix A = Matrix.constructWithCopy(niz);
        PCA pca = new PCA(A);
        pca.pca();
        Matrix B = pca.recreateOriginalDataFromPrincipalComponents(rank);
        BufferedImage bi = GrayscaleImageData.imageFromArray(B.getArray());
        return bi;

       
    }

}
