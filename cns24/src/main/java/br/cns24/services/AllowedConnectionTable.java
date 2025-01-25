package br.cns24.services;

import java.lang.reflect.AnnotatedArrayType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * class represents the possible connections accords to
 * restriction to avoid plus band work without c band.
 * by Jorge Candeias.
 */

public class AllowedConnectionTable {
  private static final String[] noBand = { "0", "00" };
  private static final String[] c = { "1", "01" };
  private static final String[] cl = { "2", "10" };
  private static final String[] cls = { "3", "11" };
  private static final Integer[] possibleNoConnection = { 0 };
  private static final Integer[] possibleConnectionC = { 1, 4, 10 };
  private static final Integer[] possibleConnectionCl = { 1, 2, 4, 5, 7, 10, 11, 13, 16 };
  private static final Integer[] possibleConnectionCls = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

  public static String[][] getMatrixConverterPossibleConnection() {

    return new String[][]{ noBand, c, cl, cls };
  }

  public static Integer[] getPossibleConnection(int node, int setSize) {
    switch (node) {
      case 1:
        return possibleConnectionC;
      case 2:
        return possibleConnectionCl;
      default:
        return possibleConnectionCls;
    }
  }


  public static Integer[] getUniformConnectionSet(int node, int setSize) {
    Random random = new Random();
    List<Integer> connections;
    switch (node) {
      case 1:
        connections = Arrays.asList(0, 10);
      case 2:
        connections = Arrays.asList(0, 10, 16);
        // it's like 000, 111, 222 and 333 in old model
      default:
        connections = Arrays.asList(0, 10, 16, 19);
    }
    int index = random.nextInt(4);
    // Retorna o array correspondente ao índice aleatório
    Integer[] result = new Integer[setSize];
    Arrays.fill(result, connections.get(index));
    return result;
  }

  public static Integer[] randomChooseConnection(int mixedDistributionProbability, int setSize, int node) {
    var randomGenerator = new Random();
    var random = randomGenerator.nextInt(101);
    Integer[] result = new Integer[setSize];
    if (random <= mixedDistributionProbability) {
      var edges = getUniformConnectionSet(node, setSize);
      for (int i = 0; i < setSize; i++) {
        result[i] = edges[0];
      }
    } else {
      var edges = getPossibleConnection(node, setSize);
      for (int i = 0; i < setSize; i++) {
        var index = randomGenerator.nextInt(edges.length);
        result[i] = edges[index];
      }
    }
    return result;
  }

  /**
   * this function implements the probabilistic function to
   * choose a new link for a node.
   * the returns could be a number but was let as a array
   * because in other problemas a link can have more than one
   * alelo
   *
   * @param mixedDistributionProbability
   * @param setSize
   * @param nodeType
   * @param numNode
   * @param graphDensity
   * @param randomGenerator
   */

  public static Integer[] probabilisticModelChooseEdge(int mixedDistributionProbability, int setSize, int nodeType,
      int numNode,
      Double graphDensity, Random randomGenerator) {
    var random = randomGenerator.nextDouble();
    if (random < (1.0 - (graphDensity * (numNode - 1) / numNode))) {
      return possibleNoConnection;
    } else {
      switch (nodeType) {
        case 1 -> {
          var selected = randomGenerator.nextInt(possibleConnectionC.length);
          return new Integer[]{ possibleConnectionC[selected] };
        }
        case 2 -> {
          var selected = randomGenerator.nextInt(possibleConnectionCl.length);
          return new Integer[]{ possibleConnectionCl[selected] };
        }
        case 3 -> {
          var selected = randomGenerator.nextInt(possibleConnectionCls.length);
          return new Integer[]{ possibleConnectionCls[selected] };
        }
      }
    }

    return possibleNoConnection;
  }

}