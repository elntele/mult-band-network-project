package br.cns24.services;

import java.util.Random;

/**
 * by Jorge Candeias.
 */
public enum Bands {
  NOBAND(0),
  CBAND(1),
  LBAND(2),
  CLBAND(3),
  SBAND(4),
  CSBAND(5),
  LSBAND(6),
  CLSBAND(7);

  Bands(Integer band) {
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

  public static int getValue(Bands band) {
    switch (band) {
      case CBAND -> {
        return 1;
      }
      case CLBAND -> {
        return 3;
      }
      case CLSBAND -> {
        return 7;
      }
      default -> throw new IllegalStateException("Unexpected value: " + band);
    }
  }

  public static int getBandForThisNode(int node) {
    Random random = new Random();
    var percent = random.nextDouble();
    switch (node) {
      case 1, 2, 3, 4 -> {
        return 1;
      }
      case 5, 6, 7, 8 -> {
        if (percent <= 0.8) {
          return 3;
        } else {
          return 1;
        }
      }
      case 9, 10, 11, 12 -> {
        if (percent <= 0.8) {
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

  /**
   * The number w of wavelength path will be a number from
   * 10 to 100 and will represent the percentage of channels
   * deployed in the last bands, with the previous bands 100%
   * deployed. So, this method will return the sum between
   * the before bands fulfilled and the percent of major band.
   *
   * @param bands
   * @param percent
   */

  public static int getTotalChannels(Bands bands, Double percent) {
    switch (bands) {
      case NOBAND -> {
        return 0;
      }
      case CBAND -> {
        double value = Equipments.cBandChannels * (percent / 100);
        return (int) Math.floor(value);
      }
      case CLBAND -> {
        double value = Equipments.lBandChannels * (percent / 100);
        int clFloor = (int) Math.floor(value);
        return Equipments.cBandChannels + clFloor;
      }
      case CLSBAND -> {
        double value = Equipments.sBandChannels * (percent / 100);
        int clsFloor = (int) Math.floor(value);
        return Equipments.cBandChannels + Equipments.lBandChannels + clsFloor;
      }
      default -> throw new RuntimeException("illegal band in getTotalChannels method");
    }
  }
}

