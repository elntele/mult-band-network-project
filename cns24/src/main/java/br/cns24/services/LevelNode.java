package br.cns24.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum LevelNode {
  BANDC,
  BANDCL,
  BANDCLS;

  /**
   * this method receive an int that represents
   * a technological level of a wss in node and
   * returns an Enum how represents the adaptability
   * of this equipment level to the link technological.
   * e.g.: equipment 1 address with link BANDC.
   *
   * @param type
   */
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

  /**
   * this method receive an int that represents
   * a technological level over a wss in node and
   * returns a random type between all equipments
   * in this level. Is it a horizontal variation,
   * e.g.: if receive 1 its can returns between 1 and
   * 4 that is equipments to c band.
   *
   * @param levelWanted
   */
  public static int updateForThisLevel(int levelWanted) {

    switch (levelWanted) {

      case 0, 1, 2, 3, 4 -> {
        Integer[] array = { 1, 2, 3, 4 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);

      }
      case 5, 6, 7, 8 -> {
        Integer[] array = { 5, 6, 7, 8 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }

      case 9, 10, 11, 12 -> {
        Integer[] array = { 9, 10, 11, 12 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }
      default -> throw new IllegalStateException("Unexpected value: " + levelWanted);
    }
  }

  /**
   * this method receive an int that represents
   * a technological level over a wss in node and
   * returns the next level:
   * e.g: if it receives 1 that represents equipments
   * that work with c band, it will return a random
   * chosen between 5 and 8 that represents the equipment
   * level for equipments in c,l bands.
   *
   * @param actualLevel
   */
  public static int nexLevel(int actualLevel) {
    switch (actualLevel) {
      case 1, 2, 3, 4 -> {
        Integer[] array = { 5, 6, 7, 8 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }
      case 5, 6, 7, 8, 9, 10, 11, 12 -> {

        Integer[] array = { 9, 10, 11, 12 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }

      default -> throw new IllegalStateException("Unexpected value: " + actualLevel);
    }
  }

  /**
   * this method receive an int that represents
   * a technological level over a wss in node and
   * returns the below level:
   * e.g: if it receives 9 that represents equipments
   * that work with c,l,s band, it will return a random
   * chosen between 5 and 8 that represents the equipment
   * level for equipments in c,l bands.
   *
   * @param actualLevel
   */

  public static int belowLevel(int actualLevel) {
    switch (actualLevel) {
      case 9, 10, 11, 12 -> {

        Integer[] array = { 5, 6, 7, 8 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }
      case 1, 2, 3, 4, 5, 6, 7, 8 -> {
        Integer[] array = { 1, 2, 3, 4 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }

      default -> throw new IllegalStateException("Unexpected value: " + actualLevel);
    }
  }

  /**
   * this method receive an int that represents
   * a technological level over a wss in node and
   * returns int that represent a technological fiber:
   * e.g: if it receives 1, 2, 3, 4 that represents the
   * technological node level that work with c band,
   * it will return 1 that represents a c band in the
   * chromosome accord the design of this problem.
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

  /**
   * this method receive an ENUM Bands that represents
   * a technological level over a link band like
   * CBAND represents links for c band, accord the design
   * of this problem, and returns an int that represent
   * a technological node level that match with band.
   * e.g: if it receives CBAND that represents the
   * technological link level that work with c band,
   * it will return a random number between  1 and 4
   * that represents the nodes who work with c band
   * accord the design of this problem.
   *
   * @param bands
   */

  public static int howIsTheNodeForThisBand(Bands bands) {
    switch (bands) {
      case Bands.CBAND -> {
        Integer[] array = { 1, 2, 3, 4 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }
      case Bands.CLBAND -> {
        Integer[] array = { 5, 6, 7, 8 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }

      case Bands.CSBAND, Bands.CLSBAND -> {

        Integer[] array = { 9, 10, 11, 12 };
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(array));
        Collections.shuffle(result);
        return result.get(0);
      }
      default -> throw new IllegalStateException("node or link not expected " + bands.name());
    }
  }

  /**
   * this function receives a link and a node
   * and return true if the node have technological
   * match level with link and false if not.
   * @param node
   * @param band
   * @return
   */

  public static boolean thisNodeAddressThisLink(int node, int band) {
    return switch (node) {
      case 1, 2, 3, 4 -> (band == 0 || band == 1);
      case 5, 6, 7, 8 -> (band >= 0 && band <= 3);
      case 9, 10, 11, 12 -> (band >= 0 && band <= 7);
      default -> false;
    };
  }

  /**
   * This method receives a link level and draws
   * with a 65% chance of returning the same link
   * or a random choice between the same link and
   * links with a lower technological level. This
   * serves to give a node with high technology the
   * chance to serve links with low technology.
   * @param link
   * @return
   */
  public static int chosenLevelLink(int link){
    Random random = new Random();
    Integer[] levelC = { 0, 1 };
    Integer[] levelCl = { 0, 1, 3 };
    Integer[] levelClS = { 0, 1, 3, 7};


    switch (link){
      case 0,1->{
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(levelC));
        if (random.nextDouble()>=0.65) return 1;
        Collections.shuffle(result);
        return result.get(0);
      }

      case 3, 5->{
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(levelCl));
        if (random.nextDouble()>=0.65) return 3;
        Collections.shuffle(result);
        return result.get(0);
      }

      case  7->{
        List<Integer> result = new ArrayList<Integer>(Arrays.asList(levelClS));
        if (random.nextDouble()>=0.65) return 7 ;
        Collections.shuffle(result);
        return result.get(0);
      }
      default -> {
        return 1;
      }

    }
  }
}
