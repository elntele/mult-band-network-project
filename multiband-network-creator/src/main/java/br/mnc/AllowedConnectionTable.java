package br.mnc;

/**
 * class represents the possible connections accords to
 * restriction to avoid plus band work without c band.
 *
 */

public class AllowedConnectionTable {
    private static final String[] noBand = {"0", "000"};
    private static final String[] c = {"1", "001"};
    private static final String[] cl = {"3", "011"};
    private static final String[] cs = {"5", "101"};
    private static final String[] cls = {"7", "111"};
    private static final Integer[] possibleConnection ={0,1,3,5,7};

    public static String[][] getMatrixConverterPossibleConnection(){
        return new String[][]{noBand, c, cl, cs, cls};
    }
    public static Integer[] getPossibleConnection(){
        return possibleConnection;
    }
}