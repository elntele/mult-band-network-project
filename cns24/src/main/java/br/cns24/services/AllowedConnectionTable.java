package br.cns24.services;

import java.util.Random;

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
    private static final Integer[] possibleConnection ={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};

    public static String[][] getMatrixConverterPossibleConnection(){

        return new String[][]{noBand, c, cl, cls};
    }

    public static Integer[] getPossibleConnection(){

        return possibleConnection;
    }


    public static Integer[] getUniformConnectionSet() {
        Random random = new Random();
        int index = random.nextInt(4);
        // Retorna o array correspondente ao índice aleatório
        Integer[] result = new Integer[3];
        result[0] = possibleConnection[index];
        result[1] = possibleConnection[index];
        result[2] = possibleConnection[index];
        return result;
    }
}