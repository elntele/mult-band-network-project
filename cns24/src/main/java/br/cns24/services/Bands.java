package br.cns24.services;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * by Jorge Candeias.
 */
public enum Bands {
  NOBAND(0),
  CBAND(1),
  CLBAND(2),
  CLSBAND(3);

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
        return Bands.CLBAND;
      }
      case 3 -> {
        return Bands.CLSBAND;
      }
      default -> throw new IllegalStateException("Unexpected value: " + edge);
    }

  }

  /**
   * this method receives a band and
   * return it value accord this
   * enum
   * @param band
   * @return
   */
  public static int getValue(Bands band) {
    switch (band) {
      case NOBAND -> {
        return 0;
      }
      case CBAND -> {
        return 1;
      }
      case CLBAND -> {
        return 2;
      }
      case CLSBAND -> {
        return 3;
      }
      default -> throw new IllegalStateException("Unexpected value: " + band);
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
      default -> throw new RuntimeException("illegal band in getTotalChannels method:" +bands);
    }
  }

  public static List<Integer> getThreeAleloValue(Integer link) {
    switch (link) {
      case 0: return List.of(0, 0, 0);
      case 1: return List.of(0, 0, 1);
      case 2: return List.of(0, 0, 2);
      case 3: return List.of(0, 0, 3);
      case 4: return List.of(0, 1, 1);
      case 5: return List.of(0, 1, 2);
      case 6: return List.of(0, 1, 3);
      case 7: return List.of(0, 2, 2);
      case 8: return List.of(0, 2, 3);
      case 9: return List.of(0, 3, 3);
      case 10: return List.of(1, 1, 1);
      case 11: return List.of(1, 1, 2);
      case 12: return List.of(1, 1, 3);
      case 13: return List.of(1, 2, 2);
      case 14: return List.of(1, 2, 3);
      case 15: return List.of(1, 3, 3);
      case 16: return List.of(2, 2, 2);
      case 17: return List.of(2, 2, 3);
      case 18: return List.of(2, 3, 3);
      case 19: return List.of(3, 3, 3);
      default: throw new IllegalArgumentException("Invalid link value: " + link);
    }
  }

  public static boolean isDisconnection(int newEdge, int oldEdge) {
    if (oldEdge != 0 && newEdge == 0) {
      return true;
    }
    return false;
  }

  public static boolean isConnection(int newEdge, int oldEdge) {
    if (oldEdge == 0 && newEdge != 0) {
      return true;
    }
    return false;
  }


}

