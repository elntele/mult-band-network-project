package br.cns24.services;

import java.util.Collections;
import java.util.List;

public enum LevelNode {
  BANDC,
  BANDCL,
  BANDCLS;

  public static LevelNode getLevel(int type) {
    switch (type) {
      case 1, 2, 3, 4 -> {
        return BANDC;
      }
      case 5, 6, 7, 8 -> {
        return BANDCL;
      }
      case 9, 10, 11, 12 -> {
        return BANDCLS;
      }
      default -> throw new IllegalStateException("Unexpected value: " + type);
    }
  }

  public static int updateForThisLevel(int levelWanted) {

    switch (levelWanted) {

      case 0, 1, 2, 3, 4 -> {
        var result = List.of(1, 2, 3, 4);
        Collections.shuffle(result);
        return result.get(0);

      }
      case 5, 6, 7, 8 -> {
        var result = List.of(5, 6, 7, 8);
        Collections.shuffle(result);
        return result.get(0);

      }

      case 9, 10, 11, 12 -> {
        var result = List.of(9, 10, 11, 12);
        Collections.shuffle(result);
        return result.get(0);

      }
      default -> throw new IllegalStateException("Unexpected value: " + levelWanted);
    }
  }

  public static int nexLevel(int actualLevel) {
    switch (actualLevel) {
      case 1, 2, 3, 4 -> {
        var result = List.of(5, 6, 7, 8);
        Collections.shuffle(result);
        return result.get(0);

      }
      case 5, 6, 7, 8, 9, 10, 11, 12 -> {
        var result = List.of(9, 10, 11, 12);
        Collections.shuffle(result);
        return result.get(0);

      }

      default -> throw new IllegalStateException("Unexpected value: " + actualLevel);
    }
  }

  public static int belowLevel(int actualLevel) {
    switch (actualLevel) {
      case 9, 10, 11, 12 -> {
        var result = List.of(5, 6, 7, 8);
        Collections.shuffle(result);
        return result.get(0);

      }
      case 1, 2, 3, 4, 5, 6, 7, 8 -> {
        var result = List.of(1, 2, 3, 4);
        Collections.shuffle(result);
        return result.get(0);

      }

      default -> throw new IllegalStateException("Unexpected value: " + actualLevel);
    }
  }

  /**
   * this method compare the level of a node and a link.
   * if the level node lower than level link returns -1
   * (this is bad because node don't support link).
   * if level node in the same level link, return 0.
   * if level node is higher than level link
   *
   * @param node
   * @throws Exception
   */

  public static int howIsTheBandForThisNode(int node) {
    switch (node) {
      case 1, 2, 3, 4 -> {
        return 1;
      }
      case 5, 6, 7, 8 -> {
        return 3;

      }
      case 9, 10, 11, 12 -> {
        return 7;
      }
      default -> throw new IllegalStateException("node or link not expected " + node);

    }

  }

  public static int howIsTheNodeForThisBand(Bands bands) {
    switch (bands) {
      case Bands.CBAND -> {
        var result = List.of(1, 2, 3, 4);
        Collections.shuffle(result);
        return result.get(0);
      }
      case Bands.CLBAND -> {
        var result = List.of(5, 6, 7, 8);
        Collections.shuffle(result);
        return result.get(0);
      }
      case Bands.CLSBAND -> {
        var result = List.of(9, 10, 11, 12);
        Collections.shuffle(result);
        return result.get(0);
      }
      default -> throw new IllegalStateException("node or link not expected " + bands.name());
    }


  }
}
