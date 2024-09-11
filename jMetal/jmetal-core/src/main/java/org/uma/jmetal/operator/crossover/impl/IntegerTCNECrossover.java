package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import br.cns24.services.Equipments;

/**
 * This class allows to apply a Technological Control of Nodes and Edges crossover operator using two
 * parent solutions (Integer encoding)
 */
public class IntegerTCNECrossover implements CrossoverOperator<IntegerSolution> {
  private double crossoverProbability;
  private Integer[] possibleEdgeTypes;
  private RandomGenerator<Double> randomGenerator;
  private Map<String, List<List<String>>> edgeEquivalences = new HashMap<>();
  private int numNodes;
  private  int setPosition;
  private int setSize;

  /**
   * Constructor
   */
  public IntegerTCNECrossover(
      double crossoverProbability,
      RandomGenerator<Double> randomGenerator,
    /*  Integer[] possibleEdgeTypes,
      Map<String, List<List<String>>> edgeEquivalences,*/
      int numNodes,
      int setSize
  ) {
    Check.probabilityIsValid(crossoverProbability);
    this.crossoverProbability = crossoverProbability;
    this.randomGenerator = randomGenerator;
    /*this.possibleEdgeTypes = possibleEdgeTypes;
    this.edgeEquivalences = edgeEquivalences;*/
    this.numNodes=numNodes;
    this.setSize=setSize;
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

    return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
  }

  /**
   * doCrossover method
   */
  public List<IntegerSolution> doCrossover(
      double probability, IntegerSolution parent1, IntegerSolution parent2) {
    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    var solutionSize = parent1.variables().size();
    var nodePartBegin = solutionSize - numNodes + 1;
    var nodesParent1 = parent1.variables().subList(nodePartBegin, solutionSize);
    var nodesParent2 = parent2.variables().subList(nodePartBegin, solutionSize);

    offspring.add((IntegerSolution) parent1.copy());
    offspring.add((IntegerSolution) parent2.copy());
    Random random = new Random();

    for (int i = 0; i < numNodes-2; i++) {
      if (randomGenerator.getRandomValue() <= probability) {
        var j = random.nextInt( numNodes-1)+i;
        while (j == i) {
          j = random.nextInt( numNodes-1)+i;
        }
        var NodeIP1 = nodesParent1.get(i);
        var NodeIP2 = nodesParent2.get(i);
        var NodeJP1 = nodesParent1.get(j);
        var NodeJP2 = nodesParent2.get(j);

        var setP1 = getSetConnection(i, j, 3, offspring.get(0));
        var setP2 = getSetConnection(i, j, 3, offspring.get(1));
        // Node's switch crossover
        offspring.get(0).variables().set(i, NodeIP2);
        offspring.get(1).variables().set(i, NodeIP1);
        offspring.get(0).variables().set(j, NodeJP2);
        offspring.get(1).variables().set(i, NodeJP1);

        //set crossover
        for(int l =0; l<setP1.length; l++){
          offspring.get(0).variables().set(l, setP2[l]);
          offspring.get(1).variables().set(l, setP1[l]);
        }
      }
    }
    return offspring;
  }

  @Override
  public double crossoverProbability() {
    return 0;
  }

  @Override
  public int numberOfRequiredParents() {
    return 0;
  }

  @Override
  public int numberOfGeneratedChildren() {
    return 0;
  }

  private Integer[] getSetConnection(int i, int j, int setSize, IntegerSolution parent) {
    Integer[] set = new Integer[setSize];
    var initialIndex = Equipments.getLinkPosition(i, j, numNodes, setSize);
    for (int w = 0; w < setSize; w++) {
      set[w] = parent.variables().get(initialIndex + w);
    }
    this.setPosition=initialIndex;
    return set;
  }


}
