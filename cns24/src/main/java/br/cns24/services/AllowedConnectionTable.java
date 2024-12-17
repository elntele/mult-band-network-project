package br.cns24.services;

/**
 * class represents the possible connections accords to
 * restriction to avoid plus band work without c band.
 *  by Jorge Candeias.
 */

public class AllowedConnectionTable {
    private static final String[] noBand = {"0", "00"};
    private static final String[] c = {"1", "01"};
    private static final String[] cl = {"2", "10"};
    private static final String[] cls = {"3", "11"};
    private static final Integer[] possibleConnection ={0,1,2,3};

    public static String[][] getMatrixConverterPossibleConnection(){

        return new String[][]{noBand, c, cl, cls};
    }

    public static Integer[] getPossibleConnection(){

        return possibleConnection;
    }
}