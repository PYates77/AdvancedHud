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

    double get(int r, int c) {
        return elements[c + r*numCols];
    }

//    Matrix multiply(Matrix rhs) {
//        Matrix A = this;
//        if (A.numCols != rhs.numRows) throw new RuntimeException("Illegal matrix dimensions.");
//        Matrix C = new Matrix(A.numRows, rhs.numCols)
//        for(int i =0; i< C.numRows; i++){
//            for( int j=0; j<C.numCols; j++){
//                for( int k=0; k < A.numRows; k++){
//                    C.elements[i][j] += (A.elements[i][k] * rhs.elements[k][j]);
//                }
//            }
//        }
//        return C; //returns matrix
//    }
}
