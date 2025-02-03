package org.uma.jmetal.operator.crossover.impl;

import static br.cns24.services.Bands.isConnection;
import static br.cns24.services.Bands.isDisconnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.utilities.CrossOverParam;

import br.cns24.services.Equipments;
import br.cns24.services.PrintPopulation;

/**
 * This class allows to apply a Technological Control of Nodes and Edges crossover operator using two
 * parent solutions (Integer encoding)
 */
public class IntegerTCNECrossover implements CrossoverOperator<IntegerSolution> {
  private Double crossoverProbability;
  private Random randomGenerator;
  private int numNodes;
  private int setSize;

  /**
   * Constructor
   */
  public IntegerTCNECrossover(
      Double crossoverProbability,
      Random randomGenerator,
      int numNodes,
      int setSize
  ) {
    Check.probabilityIsValid(crossoverProbability);
    this.crossoverProbability = crossoverProbability;
    this.randomGenerator = randomGenerator;
    this.numNodes = numNodes;
    this.setSize = setSize;
  }

  /**
   * Execute() method
   */
  @Override
  public List<IntegerSolution> execute(List<IntegerSolution> solutions) {
    if (null == solutions) {
      throw new JMetalException("Null parameter");
    } else if (solutions.size() != 2) {
      throw new JMetalException("There must be two parents instead of " + solutions.size());
    }

    return doCrossover(crossoverProbability, (DefaultIntegerSolution) solutions.get(0),
        (DefaultIntegerSolution) solutions.get(1));
  }

  /**
   * doCrossover method
   */
  public List<IntegerSolution> doCrossover(
      double probability, DefaultIntegerSolution p1, DefaultIntegerSolution p2) {
    //test(parent1, parent2);
    List<Pair<Integer, Integer>> crossed1 = new ArrayList<>();
    List<Pair<Integer, Integer>> crossed2 = new ArrayList<>();
    System.out.println("Operador de cruzamento");
    print(p1, p2, crossed1, crossed1, "pai", 0, 0);

    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    var san1 = p1.copy();
    var san2 = p2.copy();

    var temp1 = randomGenerator.nextInt(numNodes);
    int temp2 = temp1 + 4;
    if (temp2 >= numNodes - 1) temp2 = numNodes - 1;

    var initialNodeSelected = Math.min(temp1, temp2);
    initialNodeSelected += 1;
    var finalNodeSelected = Math.max(temp1, temp2);
    finalNodeSelected -= 1;
    var offset = 0;
    if (randomGenerator.nextDouble() < probability) {
      for (int i = 0; i <= finalNodeSelected + 1; i++) {
        if (i != initialNodeSelected) {
          if (i < initialNodeSelected) {
            var matrixIndexOne = Equipments.getLinkPosition(i, initialNodeSelected, numNodes, setSize);
            var matrixIndexTwo = Equipments.getLinkPosition(i, finalNodeSelected, numNodes, setSize);
            CrossOverParam cp1 = new CrossOverParam(san1, p2, matrixIndexOne, matrixIndexTwo,
                crossed1, i, initialNodeSelected);
            CrossOverParam cp2 = new CrossOverParam(san2, p1, matrixIndexOne, matrixIndexTwo, crossed2, i,
                initialNodeSelected);
            updateEdge(cp1);
            updateEdge(cp2);
          } else {
            var jMoved = initialNodeSelected + offset;
            var lastJ = numNodes - 1;
            var matrixIndex1 = Equipments.getLinkPosition(i, jMoved, numNodes, setSize);
            var matrixIndex2 = Equipments.getLinkPosition(jMoved, lastJ, numNodes, setSize);
            CrossOverParam cp1 = new CrossOverParam(san1, p2, matrixIndex1, matrixIndex2, crossed1, i, jMoved);
            CrossOverParam cp2 = new CrossOverParam(san2, p1, matrixIndex1, matrixIndex2, crossed2, i, jMoved);
            updateEdge(cp1);
            updateEdge(cp2);
            offset += 1;
          }
        }
      }

    }
    //   w crossover
    if (randomGenerator.nextDouble() < probability) {
      var size = p1.variables().size() - 1;
      san1.variables().set(size, p2.variables().get(size));
      san2.variables().set(size, p1.variables().get(size));

    }

    print(san1, san2, crossed1, crossed2, "filho", initialNodeSelected, finalNodeSelected);
    offspring.add(san1);
    offspring.add(san2);
    return offspring;
  }

  @Override
  public double crossoverProbability() {
    return 0;
  }

  @Override
  public int numberOfRequiredParents() {
    return 2;
  }

  @Override
  public int numberOfGeneratedChildren() {
    return 2;
  }


  private void print(Solution s1, Solution s2, List<Pair<Integer, Integer>> crossed1,
      List<Pair<Integer, Integer>> crossed2, String familiar, int begin, int end) {

    System.out.println("inicio " + begin);
    System.out.println("fim " + end);
    System.out.println("solução " + familiar + " 1");
    var constraint1 = s1.constraints()[0];
    var constraint2 = s1.constraints()[1];
    PrintPopulation.printMatrixFull(s1.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), crossed1, setSize);
    System.out.println("inicio " + begin);
    System.out.println("fim " + end);
    System.out.println("solução " + familiar + " 2");
    constraint1 = s2.constraints()[0];
    constraint2 = s2.constraints()[1];
    PrintPopulation.printMatrixFull(s2.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), crossed2, setSize);
  }

  private void updateEdge(CrossOverParam parameters) {
    // san Solution.
    var s = parameters.san();
    // father solution.
    var p = parameters.parent();
    //index in matrix connection to first selected node.
    var matrixIndexOne = parameters.matrixIndexOne();
    // index in matrix connection to second node selected, has 3 column between this and the other one(included)
    var matrixIndexTwo = parameters.matrixIndexTwo();
    // list fot print
    var crossed = parameters.crossed();
    // 'i' witch come from loop to 0 until last node selected
    // when i< initialNodeSelected, 'i' is the line, otherwise 'i' is column.
    var i = parameters.i();
    // the first node selected to be crossed.
    var initialNodeSelected = parameters.initialNodeSelected();

    var step = 0;
    for (int w = matrixIndexOne; w <= matrixIndexTwo; w++) {
      var oldLink = s.variables().get(w);
      var newLink = p.variables().get(w);
      s.variables().set(w, newLink);
      // it can be a disconnection, a connection a technology exchange or a replacement of 0 for 0.
      // if it is a connection or a disconnection update de node degree.
      var disconnection = isDisconnection(newLink, oldLink);
      var connection = isConnection(newLink, oldLink);
      if (i < initialNodeSelected) {
        if (connection) {
          s.degrees[i] += 1;
          s.degrees[initialNodeSelected + step] += 1;
        }
        if (disconnection) {
          s.degrees[i] -= 1;
          s.degrees[initialNodeSelected + step] -= 1;
        }
        crossed.add(new Pair<>(i, initialNodeSelected + step));
      } else {
        if (connection) {
          s.degrees[initialNodeSelected] += 1;
          s.degrees[i + step] += 1;
        }
        if (disconnection) {
          s.degrees[initialNodeSelected] -= 1;
          s.degrees[i + step] -= 1;
        }
        crossed.add(new Pair<>(initialNodeSelected, i + step));
      }
      step += 1;
    }
  }

  private void test(IntegerSolution s1, IntegerSolution s2) {
    var beginNodes = s1.variables().size() - (numNodes + 1);
    for (int i = 0; i < numNodes; i++) {
      s1.variables().set(beginNodes + i, 1);
      s2.variables().set(beginNodes + i, 3);
      for (int j = i + 1; j < numNodes; j++) {

        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        for (int w = 0; w < setSize; w++) {
          s1.variables().set(index, 1);
          s2.variables().set(index, 3);

        }
      }
    }
  }


}
