package br.cns24.services;

import java.util.Random;

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

  public static int getBandForTheNode(int node) {
    Random random = new Random();
    var percent = random.nextDouble();
    switch (node) {
      case 1, 2, 3, 4 -> {
        return 1;
      }
      case 5, 6, 7, 8 -> {

        if (percent >= 0.8) {
          return 3;
        } else {
          return 1;
        }

      }
      case 9, 10, 11, 12 -> {
        if (percent < 0.8) {
          return 7;
        } else if (percent < 0.9) {
          return 3;
        } else {
          return 1;
        }
      }

      default -> throw new IllegalStateException("Unexpected value: " + node);
    }

  }
}

