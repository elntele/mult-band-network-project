package org.uma.jmetal.operator.mutation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.doublesolution.repairsolution.RepairDoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import br.cns24.services.Bands;
import br.cns24.services.Equipments;
import br.cns24.services.LevelNode;


/**
 * This class allows to apply a Technological Control of Nodes and Edges mutation operator using two
 * parent solutions (Integer encoding)
 */
public class IntegerTCNEMutation implements CrossoverOperator<IntegerSolution> {

  private double mutationProbability;
  private RepairDoubleSolution solutionRepair;

  private RandomGenerator<Double> randomGenerator;
  private Integer[] possibleEdgeTypes;
  private Map<String, List<List<String>>> edgeEquivalences = new HashMap<>();
  private int numNodes;
  private int setPosition;
  private int setSize;

  public IntegerTCNEMutation(
      double mutationProbability,
      RepairDoubleSolution solutionRepair,
      RandomGenerator<Double> randomGenerator,
      Integer[] possibleEdgeTypes,
      Map<String, List<List<String>>> edgeEquivalences,
      int numNodes,
      int setSize
  ) {
    if (mutationProbability < 0) {
      throw new JMetalException("Mutation probability is negative: " + mutationProbability);
    }
    this.mutationProbability = mutationProbability;
    this.solutionRepair = solutionRepair;
    this.randomGenerator = randomGenerator;
    this.possibleEdgeTypes = possibleEdgeTypes;
    this.edgeEquivalences = edgeEquivalences;
    this.numNodes = numNodes;
    this.setSize = setSize;
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

  /**
   * Execute() method
   */
  public IntegerSolution execute(IntegerSolution solution) throws JMetalException {
    if (null == solution) {
      throw new JMetalException("Null parameter");
    }

    doMutation(solution);
    return solution;
  }

  /**
   * Perform the mutation operation
   */
  private void doMutation(IntegerSolution solution) {
    double y = 0.0, yl = 0.0, yu = 0.0;


    for (int i = 0; i < numNodes; i++) {
      var random = randomGenerator.getRandomValue();

      if (randomGenerator.getRandomValue() <= mutationProbability) {

        y = solutionRepair.repairSolutionVariableValue(y, yl, yu);
      }
      solution.variables().set(i, (int) y);
    }
  }

  @Override
  public List<IntegerSolution> execute(List<IntegerSolution> integerSolutions) {
    return List.of();
  }

  private void upGrade(DefaultIntegerSolution solution, int indexOriginNode) {
    var neighborhood = solution.file.get(indexOriginNode);
    if (neighborhood.isEmpty()) {
      birth(solution, indexOriginNode);
    }
    var solutionSize = solution.variables().size();
    var nodePartBegin = solutionSize - numNodes + 1;
    var nodesPart = solution.variables().subList(nodePartBegin, solutionSize);
    Random random = new Random();
    var indexNextNode = random.nextInt(0, neighborhood.size());
    var originNode = nodesPart.get(indexOriginNode);
    var destineNode = nodesPart.get(indexNextNode);
    // return -1; 0; 1 respectively: origin< destine; origin=destine; origin>destine
    var compare = LevelNode.getLevel(originNode).compareTo(LevelNode.getLevel(destineNode));
    //node's upgrade
    if (compare < 0) {
      // originNode receive update to even
      originNode = LevelNode.updateForThisLevel(destineNode);
      solution.variables().set(nodePartBegin + indexOriginNode, originNode);

    } else if (compare > 0) {
      destineNode = LevelNode.updateForThisLevel(originNode);
      solution.variables().set(nodePartBegin + indexNextNode, destineNode);
    } else {
      var nextLevel = LevelNode.nexLevel(originNode);
      solution.variables().set(nodePartBegin + indexOriginNode, nextLevel);
      solution.variables().set(nodePartBegin + indexNextNode, nextLevel);
    }
    //link upgrade
    var index= Equipments.getLinkPosition(indexOriginNode, indexNextNode,numNodes,setSize);
    for (int i =0; i<setSize; i++){
      //different of zero ,or, it is birth and
      //there is had a birth procedure
      if (solution.variables().get(index+i)!=0){
        var link = Bands.getBandForTheNode(nodePartBegin);
        solution.variables().set(index+i, link );
      }


    }
  }

  private void downGrade() {

  }

  private void birth(DefaultIntegerSolution solution, int node) {
    Random random = new Random();
    var nextNode = random.nextInt(0, numNodes);
    while (nextNode == node) {
      nextNode = random.nextInt(0, numNodes);
    }
  }
}

