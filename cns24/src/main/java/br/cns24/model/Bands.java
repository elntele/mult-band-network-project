package br.cns24.model;

public enum Bands {
    NOBAND("000"),
    CBAND("001"),
    LBAND("010"),
    CLBAND("011"),
    SBAND("100"),
    CSBAND("101"),
    LSBAND("110"),
    CLSBAND("111");

    Bands(String band) {
    }

    public static Bands getBand(Integer edge){
        switch (edge) {
            case 1 -> {
                return Bands.CBAND;
            }
            case 2 -> {
                return Bands.LBAND;
            }
            case 3 -> {
                return Bands.CLBAND;
            }
            case 4 -> {
                return Bands.SBAND;
            }
            case 5 -> {
                return Bands.CSBAND;
            }
            case 6 -> {
                return Bands.LSBAND;
            }
            case 7 -> {
                return Bands.CLSBAND;
            }
            default -> throw new IllegalStateException("Unexpected value: " + edge);
        }

    }
}

