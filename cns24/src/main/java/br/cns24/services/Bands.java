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

}

