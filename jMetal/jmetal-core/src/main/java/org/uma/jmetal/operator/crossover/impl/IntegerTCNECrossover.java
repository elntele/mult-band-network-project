package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.errorchecking.exception.InvalidProbabilityValueException;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import br.cns24.services.Equipments;
import br.cns24.services.PrintPopulation;

/**
 * This class allows to apply a Technological Control of Nodes and Edges crossover operator using two
 * parent solutions (Integer encoding)
 */
public class IntegerTCNECrossover implements CrossoverOperator<IntegerSolution> {
  private Integer crossoverProbability;
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
      int crossoverProbability,
     Random randomGenerator,
    /*  Integer[] possibleEdgeTypes,
      Map<String, List<List<String>>> edgeEquivalences,*/
      int numNodes,
      int setSize
  ) {
    //Check.probabilityIsValid(crossoverProbability);
    if ((crossoverProbability < 0.0) || (crossoverProbability > 100)) {
      throw new InvalidProbabilityValueException(crossoverProbability) ;
    }

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
    System.out.println("Operador de cruzamento");
    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    var solutionSize = parent1.variables().size();
    var nodePartBegin = solutionSize - (numNodes + 1);
    var nodesParent1 = parent1.variables().subList(nodePartBegin, solutionSize - 1);
    var nodesParent2 = parent2.variables().subList(nodePartBegin, solutionSize - 1);

    offspring.add((IntegerSolution) parent1.copy());
    offspring.add((IntegerSolution) parent2.copy());
    var variableSize= offspring.get(0).variables().size();

    //parte colocada pra fazer o debug
    print(parent1, parent2, List.of(), List.of(), "pai");
    List<Integer> iCrossed = new ArrayList<>();
    List<Integer> jCrossed = new ArrayList<>();

    for (int i = 0; i < numNodes-2; i++) {
      for (int j = i + 1; j < numNodes; j++) {
          if (randomGenerator.nextInt(100) <= probability) {
            iCrossed.add(i);
            jCrossed.add(j);
            // cruzamento do w
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
            var connectionSetAsAListS0 = getConnectionSetAsAList(parent1, initialIndex);
            var connectionSetAsAListS1 = getConnectionSetAsAList(parent2, initialIndex);
            // Node's switch crossover
            offspring.get(0).variables().set((i + NodeBeginPart), NodeI_S1);
            offspring.get(1).variables().set((i + NodeBeginPart), NodeI_S0);
            offspring.get(0).variables().set((j + NodeBeginPart), NodeJ_S1);
            offspring.get(1).variables().set((j + NodeBeginPart), NodeJ_S0);
            //crossover of set
            for (int l = 0; l < setSize; l++) {
              offspring.get(0).variables().set(initialIndex + l, connectionSetAsAListS1[l]);
              offspring.get(1).variables().set(initialIndex + l, connectionSetAsAListS0[l]);
            }

          }
      }

    }
    //parte colocada pra fazer o debug
    print(offspring.get(0), offspring.get(1), iCrossed, jCrossed, "filho");

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

  private void print(Solution s1, Solution s2, List<Integer> iCrossed, List<Integer>jCrossed, String familiar){

    System.out.println("solução "+familiar+" 1");
    var constraint1 = s1.constraints()[0];
    var constraint2 = s1.constraints()[1];
    PrintPopulation.printMatrixFull(s1.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), iCrossed, jCrossed, setSize);
    System.out.println("solução "+familiar+" 2");
    constraint1 = s2.constraints()[0];
    constraint2 = s2.constraints()[1];
    PrintPopulation.printMatrixFull(s2.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), iCrossed,jCrossed, setSize);
  }


}
