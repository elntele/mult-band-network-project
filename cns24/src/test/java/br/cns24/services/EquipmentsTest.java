package br.cns24.services;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import junit.framework.TestCase;

public class EquipmentsTest {

  /**
   * // connection matrix
   * //   0     1      2        3
   * // 0 x (0-1-2) (3-4-5)   (6-7-8)
   * // 1      x    (9-10-11) (12-13-14)
   * // 2               x     (15-16-17)
   * // 3                          x
   * the index
   * i=0 (j=1- index=0);
   * i=0 (j=2- index=3);
   * i=0 (j=3- index=6)
   */
  @Test
  public void shouldReturnTheCorrectPositionOffValueInFourPositionMatrix() {
    Integer[] matrixConnection = new Integer[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };
    int sizeSet = 3;
    int numNodes = 4;
    int counter = 0;
    for (int i = 0; i < numNodes; i++) {
      for (int j = i + 1; j < numNodes; j++) {
        var matrixIndex = Equipments.getLinkPosition(i, j, numNodes, sizeSet);
        int valueInMatrix = matrixConnection[matrixIndex];
        if (counter <= matrixConnection.length - sizeSet) {
          assertEquals(valueInMatrix, counter);
        }
        counter += 3;
      }
    }
  }


  @Test
  public void shouldReturnTheCorrectPositionWhenSetIsOneFiber() {
    Integer[] matrixConnection = new Integer[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };
    int sizeSet = 1;
    int numNodes = 4;
    int counter = 0;
    for (int i = 0; i < numNodes; i++) {
      for (int j = i + 1; j < numNodes; j++) {
        var matrixIndex = Equipments.getLinkPosition(i, j, numNodes, sizeSet);
        int valueInMatrix = matrixConnection[matrixIndex];
        if (counter <= matrixConnection.length - sizeSet) {
          assertEquals(valueInMatrix, counter);
        }
        counter += 1;
      }
    }
  }


  @Test
  public void shouldReturnTheCorrectPositionWhenSetIsFiveFiber() {
    Integer[] matrixConnection = new Integer[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 };
    int sizeSet = 5;
    int numNodes = 4;
    int counter = 0;
    for (int i = 0; i < numNodes; i++) {
      for (int j = i + 1; j < numNodes; j++) {
        var matrixIndex = Equipments.getLinkPosition(i, j, numNodes, sizeSet);
        int valueInMatrix = matrixConnection[matrixIndex];
        if (counter <= matrixConnection.length - sizeSet) {
          assertEquals(valueInMatrix, counter);
        }
        counter += 5;
      }
    }
  }
}