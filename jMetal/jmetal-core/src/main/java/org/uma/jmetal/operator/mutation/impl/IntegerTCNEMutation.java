package org.uma.jmetal.operator.mutation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.doublesolution.repairsolution.RepairDoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import br.cns24.model.MutationAttributes;
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

  private void upgradeDownGrade(DefaultIntegerSolution solution, int indexOriginNode, double percent) {
    var neighborhood = solution.file.get(indexOriginNode);
    if (neighborhood.isEmpty()) {
      birth(solution, indexOriginNode);
    }
    var solutionSize = solution.variables().size();
    var nodePartBegin = solutionSize - numNodes + 1;
    var nodesPart = solution.variables().subList(nodePartBegin, solutionSize);
    Random random = new Random();
    var indexDestineNode = random.nextInt(0, neighborhood.size());
    var originNode = nodesPart.get(indexOriginNode);
    var destineNode = nodesPart.get(indexDestineNode);
    // return -1; 0; 1 respectively: origin< destine; origin=destine; origin>destine
    var compare = LevelNode.getLevel(originNode).compareTo(LevelNode.getLevel(destineNode));
    var mAttrs = new MutationAttributes(
        indexOriginNode,
        indexDestineNode,
        originNode,
        destineNode,
        compare,
        nodePartBegin);
    if (percent <= 0.4) {
      downGrade(((DefaultIntegerSolution) solution), mAttrs);
    } else {
      upGrade(((DefaultIntegerSolution) solution), mAttrs);
    }

  }

  /**
   * This method make upgrade in a pair of nodes.
   * This receives a solution and an index of nodes
   * how will be mutated. The first part is a term of
   * correction because if the node has no neighbor, so
   * it is a solution with isolated node and this method
   * send it to birth method were a connection will birth.
   * this method randomly choose a neighbor in neighborhood
   * and equalize the level of node for the higher them. If
   * there are no node higher this upgrade the two. ofter
   * that, this upgrade the link with 20% of chance tobe in
   * the below level technological.
   * o upgrade de link ta errado, lembrar que tem que seguir
   * tabela de equivaências, mas pode atracar com o level dos
   * nós
   *
   * @param solution
   * @param mAttrs
   */

  private void upGrade(DefaultIntegerSolution solution, MutationAttributes mAttrs) {
    var newLevel = 0;
    if (mAttrs.compare() < 0) { //originNode is minor destine node
      // originNode receive upGrade to even level of destineNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
      var nextLevel = LevelNode.updateForThisLevel(mAttrs.destineNode());
      solution.variables().set(indexInChromosome, nextLevel);
      newLevel=nextLevel;
    } else if (mAttrs.compare() > 0) { // destine node is minor origin node
      //  destineNode  receive upGrade to even level of originNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var nextLevel = LevelNode.updateForThisLevel(mAttrs.originNode());
      solution.variables().set(indexInChromosome, nextLevel);
      newLevel=nextLevel;
    } else {// two node is in the even level
      var indexOriginNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
      var indexDestineNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var nextLevelNodeOrigin = LevelNode.nexLevel(mAttrs.originNode());
      var nextLevelNodeDestine = LevelNode.nexLevel(mAttrs.destineNode());

      solution.variables().set(indexOriginNodeInChromosome, nextLevelNodeOrigin);
      solution.variables().set(indexDestineNodeInChromosome, nextLevelNodeDestine);
      newLevel = nextLevelNodeOrigin;

    }
    //link adjust
    var index = Equipments.getLinkPosition(mAttrs.indexOriginNode(), mAttrs.indexDestineNode(), numNodes, setSize);
    for (int i = 0; i < setSize; i++) {
      //different of zero ,or, it is birth and
      //there is had a birth procedure
      if (solution.variables().get(index + i) != 0) {
        var link = Bands.getBandForThisNode(newLevel);
        solution.variables().set(index + i, link);
      }


    }
  }

  private void downGrade(DefaultIntegerSolution solution, MutationAttributes mAttrs) {
    //node's upgrade
    var newLevel = 0;
    if (mAttrs.compare() < 0) { //originNode is minor destine node
      // destineNode receive downgrade to even level of originNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var belowLevelNode = LevelNode.updateForThisLevel(mAttrs.originNode());
      solution.variables().set(indexInChromosome, belowLevelNode);
      newLevel = belowLevelNode;

    } else if (mAttrs.compare() > 0) {// destine node is minor origin node
      // originNode receive downgrade to even level of destineNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
      var belowLevelNode = LevelNode.updateForThisLevel(mAttrs.destineNode());
      solution.variables().set(indexInChromosome, belowLevelNode);
      newLevel = belowLevelNode;
    } else { // two node is in the even level
      var indexOriginNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
      var indexDestineNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var belowLevelNodeOrigin = LevelNode.belowLevel(mAttrs.originNode());
      var belowLevelNodeDestine = LevelNode.belowLevel(mAttrs.destineNode());
      solution.variables().set(indexOriginNodeInChromosome, belowLevelNodeOrigin);
      solution.variables().set(indexDestineNodeInChromosome, belowLevelNodeDestine);
      newLevel = belowLevelNodeOrigin;
    }
    //link adjust
    var index = Equipments.getLinkPosition(mAttrs.indexOriginNode(), mAttrs.indexDestineNode(), numNodes, setSize);
    for (int i = 0; i < setSize; i++) {
      //different of zero ,or, it is birth and
      //there is had a birth procedure
      if (solution.variables().get(index + i) != 0) {
        var link = Bands.getBandForThisNode(newLevel);
        solution.variables().set(index + i, link);
      }
    }
  }

  private void birth(DefaultIntegerSolution solution, int node) {
    Random random = new Random();
    var nextNode = random.nextInt(0, numNodes);
    while (nextNode == node) {
      nextNode = random.nextInt(0, numNodes);
    }
  }
}

