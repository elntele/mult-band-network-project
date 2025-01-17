package br.cns24.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public enum LevelNode {
  BANDC,
  BANDCL,
  BANDCLS;


 /* *//**
   * this function receives a link and a node
   * and return true if the node have technological
   * match level with link and false if not.
   *
   * @param node
   * @param band
   *//*

  public static boolean thisNodeAddressThisLink(int node, int band) {
    return switch (node) {
      case 1 -> (band <= 1);
      case 2 -> (band <= 2);
      case 3 -> (band <= 3);
      default -> false;
    };
  }*/

  /**
   * this function receives a link and a node
   * and return true if the node have technological
   * match level with link and false if not.
   *
   * @param node
   * @param band
   */

  public static boolean thisNodeAddressThisLink(int node, int band) {
    var listFiberBand= Bands.getThreeAleloValue(band);
    var max = listFiberBand.stream().max(Comparator.naturalOrder()).orElse(null);
    if (max==null) return false;
    return switch (node) {
      case 1 -> (max <= 1);
      case 2 -> (max <= 2);
      case 3 -> (max <= 3);
      default -> false;
    };
  }



}
