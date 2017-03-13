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

    Matrix multiply(Matrix rhs) {
        return null;
    }
}
