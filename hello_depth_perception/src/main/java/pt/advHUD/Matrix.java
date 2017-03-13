package pt.advHUD;

/**
 * Created by Ryan on 3/13/2017.
 */

public class Matrix {
    private int numRows;
    private int numCols;
    private double[] elements;

    public Matrix(int nr,int nc, double[] el) {
        numCols = nc;
        numRows = nr;

        for (int i = 0; i < nc*nr; i++)
            elements[i] = el[i];
    }

    Matrix multiply(Matrix rhs) {
        int m1ColLength = rhs[0].length; //rhs columns length
        int m2Rowlength = Matrix.length; //Matrix row length 
        if (m1ColLength != m2RowLength) return null; //Matrix multiplication not possible
        int mRRowLength = rhs.length; //matrix result rows
        int mRColLength = Matrix[0].length; //matrix result col rows 
        double [][] FinalMatrix = new double[mRRowLength][mRColLength];
            for(int i =0; i< mRRowLength; i++){
                for( int j=0; j<mRColLength; j++){
                    for( int k=0; k < m1ColLength; k++){
                        FinalMatrix[i][j] += rhs[i][k]*Matrix[k][j]
                    }
                }
            }
        return FinalMatrix;
                      

    }
}
