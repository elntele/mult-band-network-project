package br.cns24.services;

import java.util.Collections;
import java.util.List;
import java.util.Random;

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

  public static boolean isLinkNotSupportedForThisNode(int node, int link) {
    switch (node) {
      case 0, 1, 2, 3, 4 -> {
        if (link>1) return true;
      }
      case 5,6,7,8 ->{
          if (link>3) return true;
      }
      case 9,10,11,12 -> {
        return false;
      }
    }
    return false;
  }


}
