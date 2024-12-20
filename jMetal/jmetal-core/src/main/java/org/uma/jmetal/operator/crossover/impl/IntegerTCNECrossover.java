package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
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
  private Random randomGenerator;
  private Map<String, List<List<String>>> edgeEquivalences = new HashMap<>();
  private int numNodes;
  private int setPosition;
  private int setSize;

  /**
   * Constructor
   */
  public IntegerTCNECrossover(
      double crossoverProbability,
     Random randomGenerator,
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

    return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
  }

  /**
   * doCrossover method
   */
  public List<IntegerSolution> doCrossover(
      double probability, IntegerSolution parent1, IntegerSolution parent2) {
    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    var solutionSize = parent1.variables().size();
    var nodePartBegin = solutionSize - (numNodes + 1);
    var nodesParent1 = parent1.variables().subList(nodePartBegin, solutionSize - 1);
    var nodesParent2 = parent2.variables().subList(nodePartBegin, solutionSize - 1);

    offspring.add((IntegerSolution) parent1.copy());
    offspring.add((IntegerSolution) parent2.copy());
    var variableSize= offspring.get(0).variables().size();
    for (int i = 0; i < numNodes-2; i++) {
        for (int j = i + 1; j < numNodes; j++) {
          if (randomGenerator.nextInt(100) <= probability) {
            if(j==1){
             var tail=  variableSize-1;
              int lastAleloOne= offspring.getFirst().variables().get(tail);
              int lastAleloTwo= offspring.get(1).variables().get(tail);
              offspring.getFirst().variables().set(tail,lastAleloTwo);
              offspring.get(1).variables().set(tail,lastAleloOne);
            }

            var NodeBeginPart = variableSize - (numNodes + 1);
            var NodeI_S0 = nodesParent1.get(i);
            var NodeI_S1 = nodesParent2.get(i);
            var NodeJ_S0 = nodesParent1.get(j);
            var NodeJ_S1 = nodesParent2.get(j);
            var initialIndex = Equipments.getLinkPosition(i, j, numNodes, setSize);
            var connectionSetAsAListS0 = getConnectionSetAsAList(offspring.get(0), initialIndex);
            var connectionSetAsAListS1 = getConnectionSetAsAList(offspring.get(1), initialIndex);
            // Node's switch crossover
            offspring.get(0).variables().set((i + NodeBeginPart), NodeI_S1);
            offspring.get(1).variables().set((i + NodeBeginPart), NodeI_S0);
            offspring.get(0).variables().set((j + NodeBeginPart), NodeJ_S1);
            offspring.get(1).variables().set((j + NodeBeginPart), NodeJ_S0);
            var linksInSetS0 = 0;
            var linksInSetS1 = 0;
            //crossover of set
            for (int l = 0; l < setSize; l++) {
              if (connectionSetAsAListS0[l] > 0) linksInSetS0 += 1;
              if (connectionSetAsAListS1[l] > 0) linksInSetS1 += 1;
              offspring.get(0).variables().set(initialIndex + l, connectionSetAsAListS1[l]);
              offspring.get(1).variables().set(initialIndex + l, connectionSetAsAListS0[l]);
            }
            // maintenance of files:
            // if set connections in the other solution is full zero
            // remove reference in this solution file because it
            // was crossed.
            if (linksInSetS0 == 0) {
              ((DefaultIntegerSolution) offspring.get(1)).file.get(i).remove(j);
              ((DefaultIntegerSolution) offspring.get(1)).file.get(j).remove(i);
            } else {
              // if set connections in the other solution is not zero
              // add a reference in this solution file because it
              // was crossed. If it is already exists no problem, because set
              // don't add duplicate registers.
              ((DefaultIntegerSolution) offspring.get(1)).file.get(i).add(j);
              ((DefaultIntegerSolution) offspring.get(1)).file.get(j).add(i);
            }
            if (linksInSetS1 == 0) {
              ((DefaultIntegerSolution) offspring.get(0)).file.get(i).remove(j);
              ((DefaultIntegerSolution) offspring.get(0)).file.get(j).remove(i);
            } else {
              ((DefaultIntegerSolution) offspring.get(0)).file.get(i).add(j);
              ((DefaultIntegerSolution) offspring.get(0)).file.get(j).add(i);
            }
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
    return 2;
  }

  @Override
  public int numberOfGeneratedChildren() {
    return 2;
  }

  /**
   * this method returns a connection set
   * as a list of integers
   *
   * @param parent
   * @param initialIndex
   */
  private Integer[] getConnectionSetAsAList( IntegerSolution parent, int initialIndex) {
    Integer[] set = new Integer[setSize];
    for (int w = 0; w < setSize; w++) {
      set[w] = parent.variables().get(initialIndex + w);
    }
    this.setPosition = initialIndex;
    return set;
  }


}
