package br.cns24.services;

/**
 * by Jorge Candeias.
 */
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

  /**
   * this method receives a edge and
   * returns a type
   *
   * @param edge
   */

  public static Bands getBand(Integer edge) {
    switch (edge) {
      case 0 -> {
        return Bands.NOBAND;
      }
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

