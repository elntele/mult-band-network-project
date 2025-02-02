package org.uma.jmetal.operator.crossover.impl;

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
import org.uma.jmetal.utilities.CrossOverParameters;

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
      double probability, DefaultIntegerSolution parent1, DefaultIntegerSolution parent2) {
    //test(parent1, parent2);
    var beginNodes = parent1.variables().size() - (numNodes + 1);
    List<Pair<Integer, Integer>> crossed1 = new ArrayList<>();
    List<Pair<Integer, Integer>> crossed2 = new ArrayList<>();
    System.out.println("Operador de cruzamento");
    print(parent1, parent2, crossed1, crossed1, "pai", 0, 0);

    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    var san1 = parent1.copy();
    var san2 = parent2.copy();
    var temp1 = randomGenerator.nextInt(numNodes);
    int temp2 = temp1 + 4;
    if (temp2 >= numNodes - 1) temp2 = numNodes - 1;

    var initialNodeSelected = Math.min(temp1, temp2);
    initialNodeSelected += 1;
    var finalNodeSelected = Math.max(temp1, temp2);
    finalNodeSelected -= 1;
    var offset = 0;
    if (randomGenerator.nextDouble() < probability) {
      System.out.println("degrees originais no cruzamento");
      System.out.printf(" de %d até %d%n", initialNodeSelected, finalNodeSelected);
      for (int w = 0; w < numNodes; w++) {
        System.out.print(String.format("%02d", w) + ";");
      }
      System.out.println();
      Arrays.stream(san1.degrees).forEach(value -> System.out.print(String.format("%02d", value) + ";"));
      System.out.println("original");
      Arrays.stream(san2.degrees).forEach(value -> System.out.print(String.format("%02d", value) + ";"));
      System.out.println("original");

      //node crossOver and node degree
      for (int i = initialNodeSelected; i <= finalNodeSelected; i++) {
        san1.variables().set(beginNodes + i, parent2.variables().get(beginNodes + i));
        san2.variables().set(beginNodes + i, parent1.variables().get(beginNodes + i));
        san1.degrees[i] = parent2.degrees[i];
        san2.degrees[i] = parent1.degrees[i];
      }
      Arrays.stream(san1.degrees).forEach(value -> System.out.print(String.format("%02d", value) + ";"));
      System.out.println("mudado");
      Arrays.stream(san2.degrees).forEach(value -> System.out.print(String.format("%02d", value) + ";"));
      System.out.println("mudado");
      System.out.println();

      //parte colocada pra fazer o debug

      for (int i = 0; i <= finalNodeSelected + 1; i++) {
        if (i != initialNodeSelected) {
          if (i < initialNodeSelected) {
            var matrixIndexOne = Equipments.getLinkPosition(i, initialNodeSelected, numNodes, setSize);
            var matrixIndexTwo = Equipments.getLinkPosition(i, finalNodeSelected, numNodes, setSize);
            CrossOverParameters parametersOne = new CrossOverParameters(san1, parent2, matrixIndexOne, matrixIndexTwo,
                crossed1, i, initialNodeSelected, finalNodeSelected);
            CrossOverParameters parametersTwo = new CrossOverParameters(san2, parent1, matrixIndexOne, matrixIndexTwo,
                crossed2, i, initialNodeSelected, finalNodeSelected);
            updateEdge(parametersOne);
            updateEdge(parametersTwo);
          } else {
            var matrixIndexOne = Equipments.getLinkPosition(i, initialNodeSelected + offset, numNodes, setSize);
            var matrixIndexTwo = Equipments.getLinkPosition(initialNodeSelected + offset, numNodes - 1, numNodes,
                setSize);
            CrossOverParameters parametersOne = new CrossOverParameters(san1, parent2, matrixIndexOne, matrixIndexTwo,
                crossed1, i, initialNodeSelected + offset, finalNodeSelected);
            CrossOverParameters parametersTwo = new CrossOverParameters(san2, parent1, matrixIndexOne, matrixIndexTwo,
                crossed2, i, initialNodeSelected + offset, finalNodeSelected);
            updateEdge(parametersOne);
            updateEdge(parametersTwo);
            offset += 1;
          }
        }
      }

    }

    if (randomGenerator.nextDouble() < probability) {
      var size = parent1.variables().size() - 1;
      san1.variables().set(size, parent2.variables().get(size));
      san2.variables().set(size, parent1.variables().get(size));

    }

    print(san1, san2, crossed1, crossed2, "filho", initialNodeSelected, finalNodeSelected);
    offspring.add((IntegerSolution) san1);
    offspring.add((IntegerSolution) san2);
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

  private void updateEdge(CrossOverParameters parameters) {
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
    // the ultimate node selected to be crossed.
    var finalNodeSelected = parameters.finalNodeSelected();

    var step = 0;
    for (int w = matrixIndexOne; w <= matrixIndexTwo; w++) {
      var removedEdge = p.variables().get(w);
      var implantedEdge = p.variables().get(w);
      s.variables().set(w, implantedEdge);
      // continuar aqui
      var disconnection= isDisconnection(implantedEdge, removedEdge);
      var connection= isConnection(implantedEdge, removedEdge);
      if (i < initialNodeSelected) {
        if (implantedEdge > 0) {
          s.degrees[i] += 1;
        } else {
          s.degrees[i] -= 1;
        }
        crossed.add(new Pair<>(i, initialNodeSelected + step));
      } else {
        if (implantedEdge > 0) {
          s.degrees[initialNodeSelected] += 1;
        } else {
          s.degrees[initialNodeSelected] -= 1;
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

  private boolean isDisconnection(int newEdge, int oldEdge){
    if (oldEdge!=0 && newEdge==0){
      return true;
    }
    return false;
  }

  private boolean isConnection(int newEdge, int oldEdge){
    if (oldEdge==0 && newEdge!=0){
      return true;
    }
    return false;
  }


}
