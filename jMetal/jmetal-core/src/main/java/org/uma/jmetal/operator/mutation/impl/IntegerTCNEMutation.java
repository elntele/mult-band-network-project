package org.uma.jmetal.operator.mutation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import org.uma.jmetal.operator.mutation.MutationOperator;
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
public class IntegerTCNEMutation implements MutationOperator<IntegerSolution> {

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
      RandomGenerator<Double> randomGenerator,
     /* Integer[] possibleEdgeTypes,
      Map<String, List<List<String>>> edgeEquivalences,*/
      int numNodes,
      int setSize
  ) {
    if (mutationProbability < 0) {
      throw new JMetalException("Mutation probability is negative: " + mutationProbability);
    }
    this.mutationProbability = mutationProbability;
    this.randomGenerator = randomGenerator;
    this.possibleEdgeTypes = possibleEdgeTypes;
    this.edgeEquivalences = edgeEquivalences;
    this.numNodes = numNodes;
    this.setSize = setSize;
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

  @Override
  public double mutationProbability() {
    return 0;
  }

  /**
   * This method make upgrade in a pair of nodes.
   * This receives a solution and some attributes of nodes
   * por how will be mutated. The first part is a term of
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
   */
  private void doMutation(IntegerSolution solution) {
    for (int i = 0; i < numNodes; i++) {
      var random = randomGenerator.getRandomValue();
      if (random <= mutationProbability) {
        selectMutationApproach(((DefaultIntegerSolution) solution), i, random);
      }

    }
  }



  /**
   * This method select the mutation technic to do mutation
   * in an exactly one pair of nodes.
   * This receives a solution and an index origin node and the
   * percent tax randomly choose for before method. the destine
   * index node is randomly choose to make a node pair. after
   * that, an approach to make mutation is chose accord the
   * percent tax
   */

  private void selectMutationApproach(DefaultIntegerSolution solution, int indexOriginNode, double percent) {
    var neighborhood = solution.file.get(indexOriginNode);
    var solutionSize = solution.variables().size();
    var nodePartBegin = solutionSize - numNodes + 1;
    var nodesPart = solution.variables().subList(nodePartBegin, solutionSize);
    Random random = new Random();
    var indexDestineNode = random.nextInt(0, neighborhood.size());
    while (indexDestineNode == indexOriginNode && !neighborhood.isEmpty()) {
      indexDestineNode = random.nextInt(0, neighborhood.size());
    }
    var originNode = nodesPart.get(indexOriginNode);
    var destineNode = nodesPart.get(indexDestineNode);
    // compareTo return -1; 0; 1 respectively: origin< destine; origin=destine; origin>destine
    var compare = LevelNode.getLevel(originNode).compareTo(LevelNode.getLevel(destineNode));
    var mAttrs = new MutationAttributes(
        indexOriginNode,
        indexDestineNode,
        originNode,
        destineNode,
        compare,
        nodePartBegin);

    if (neighborhood.isEmpty()) {
      doBirth(solution, mAttrs);
    }
    if (percent <= 0.4) {
      downGrade(((DefaultIntegerSolution) solution), mAttrs);
    } else if (percent > 0.4 && percent <= 0.8) {
      upGrade(((DefaultIntegerSolution) solution), mAttrs);
    } else if (percent > 0.8 && percent <= 0.9) {
      doBirth(solution, mAttrs);
    } else {
      doExtinction(solution, mAttrs);
    }

    correction(((DefaultIntegerSolution) solution));

  }

  /**
   * This method make upgrade in a pair of nodes.
   * This receives a solution and some attributes of nodes
   * por how will be mutated. This method randomly choose a
   * neighbor in neighborhood and equalize the level of nodes
   * for the higher them. If there are no node higher this
   * upgrade the two nodes. ofter that, this method upgrade the
   * link with 20% of chance tobe in the below level technological
   * in relation of nodes.
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
      newLevel = nextLevel;
    } else if (mAttrs.compare() > 0) { // destine node is minor origin node
      //  destineNode  receive upGrade to even level of originNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var nextLevel = LevelNode.updateForThisLevel(mAttrs.originNode());
      solution.variables().set(indexInChromosome, nextLevel);
      newLevel = nextLevel;
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

  /**
   * This method make downgrade in a pair of nodes.
   * This receives a solution and some attributes of nodes
   * por how will be mutated. This method randomly choose a
   * neighbor in neighborhood and equalize the level of nodes
   * for the lower them. If there are no node lower this
   * downgrade the two nodes. ofter that, this method downgrade the
   * link with 20% of chance tobe in the below level technological
   * in relation of nodes.
   *
   * @param solution
   * @param mAttrs
   */

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

  private void doBirth(DefaultIntegerSolution solution, MutationAttributes mAttrs) {
    var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
    var mathLevelNode = LevelNode.updateForThisLevel(mAttrs.destineNode());
    solution.variables().set(indexInChromosome, mathLevelNode);
    if (mAttrs.destineNode() == 0) {

    }
    var index = Equipments.getLinkPosition(mAttrs.indexOriginNode(), mAttrs.indexDestineNode(), numNodes, setSize);
    for (int i = 0; i < setSize; i++) {
      var link = Bands.getBandForThisNode(mathLevelNode);
      solution.variables().set(index + i, link);
    }
    solution.file.get(mAttrs.originNode()).add(mAttrs.destineNode());
    solution.file.get(mAttrs.destineNode()).add(mAttrs.originNode());
  }


  private void doExtinction(DefaultIntegerSolution solution, MutationAttributes mAttrs) {
    var neighborhoodA = solution.file.get(mAttrs.originNode());
    var neighborhoodB = solution.file.get(mAttrs.destineNode());
    if (neighborhoodA.size() > 1 && neighborhoodB.size() > 1) {
      var index = Equipments.getLinkPosition(mAttrs.indexOriginNode(), mAttrs.indexDestineNode(), numNodes, setSize);
      for (int i = 0; i < setSize; i++) {
        if (solution.variables().get(index + i) != 0) {
          solution.variables().set(index + i, 0);
        }
      }
      solution.file.get(mAttrs.originNode()).remove(mAttrs.destineNode());
      solution.file.get(mAttrs.destineNode()).remove(mAttrs.originNode());
    }
  }

  //voltar aqui jorge
  private void correction(DefaultIntegerSolution solution) {
    var solutionSize = solution.variables().size();
    var nodePartBegin = solutionSize - numNodes + 1;
    var nodesPart = solution.variables().subList(nodePartBegin, solutionSize);
    // go through all nodes
    for (int i = 0; i < numNodes; i++) {
      var neighborhood = solution.file.get(i);
      final int indexNodeOrigin = i;
      List<Bands> bandsList = new ArrayList<>();
      List<Integer> linksPositions = new ArrayList<>();
      // go through all neighbor of one node
      neighborhood.stream().forEach(j -> {
        var linkBeginPosition = Equipments.getLinkPosition(indexNodeOrigin, j, numNodes, setSize);
        //go through all links  of one node
        for (int w = 0; w < setSize; w++) {
          var link = solution.variables().get(linkBeginPosition + w);
          var linkBand = Bands.getBand(link);
          bandsList.add(linkBand);
        }
      });
      Collections.sort(bandsList);

      var linkReference = bandsList.getLast();
      var node = nodesPart.get(i);
      Random random = new Random();
      var percent = random.nextBoolean();

      if (percent) {//adjust node
        var betterNode = LevelNode.howIsTheNodeForThisBand(linkReference);
        solution.variables().set(nodePartBegin + i, betterNode);
      } else { //adjust link
        final int betterlink = LevelNode.howIsTheBandForThisNode(node);
        solution.variables().set(nodePartBegin + i, betterlink);
        neighborhood.stream().forEach(j -> {
          var linkBeginPosition = Equipments.getLinkPosition(indexNodeOrigin, j, numNodes, setSize);
          //go through all links  of one node
          for (int w = 0; w < setSize; w++) {
            if (solution.variables().get(linkBeginPosition + w) > 0) {
              solution.variables().set(linkBeginPosition + w, betterlink);
            }
            var link = solution.variables().get(linkBeginPosition + w);
            var linkBand = Bands.getBand(link);
            bandsList.add(linkBand);
          }
        });
      }
      LevelNode.howIsTheBandForThisNode(node);
    }
  }

}

