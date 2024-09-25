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

  private Random randomGenerator;
  private Integer[] possibleEdgeTypes;
  private Map<String, List<List<String>>> edgeEquivalences = new HashMap<>();
  private int numNodes;
  private int setPosition;
  private int setSize;

  public IntegerTCNEMutation(
      double mutationProbability,
      Random randomGenerator,
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
      var random = (randomGenerator.nextInt(100));
      //var random=0.95;
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
   * that, an approach to make mutation is chosen accord the
   * percent tax
   */

  private void selectMutationApproach(DefaultIntegerSolution solution, int indexOriginNode, double percent) {

    var mAttrs = getAttributes(solution, indexOriginNode);
    var neighborhood = solution.file.get(indexOriginNode);
    if (neighborhood.isEmpty()) {
      doBirth(solution, mAttrs);
    } else {
      if (percent <= 40) {
        downGrade(((DefaultIntegerSolution) solution), mAttrs);
      } else if (percent > 40 && percent <= 80) {
        upGrade(((DefaultIntegerSolution) solution), mAttrs);
      } else if (percent > 80 && percent <= 90) {
        doBirth(solution, mAttrs);
      } else {
        doExtinction(solution, mAttrs);
      }
    }
    correction(solution);
  }

  /**
   * This method make upgrade in a pair of nodes.
   * This receives a solution and some attributes of nodes
   * por what will be mutated. This method randomly choose a
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
    if (mAttrs.compare() < 0) { //wssOriginNode is minor then destine node
      // wssOriginNode receive upGrade to even level of wssDestineNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
      var nextLevel = LevelNode.updateForThisLevel(mAttrs.wssDestineNode());
      solution.variables().set(indexInChromosome, nextLevel);
      newLevel = nextLevel;
    } else if (mAttrs.compare() > 0) { // destine node is minor origin node
      //  wssDestineNode  receive upGrade to even level of wssOriginNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var nextLevel = LevelNode.updateForThisLevel(mAttrs.wssOriginNode());
      solution.variables().set(indexInChromosome, nextLevel);
      newLevel = nextLevel;
    } else {// two node is in the even level
      var indexOriginNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
      var indexDestineNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var nextLevelNodeOrigin = LevelNode.nexLevel(mAttrs.wssOriginNode());
      var nextLevelNodeDestine = LevelNode.nexLevel(mAttrs.wssDestineNode());

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
    if (mAttrs.compare() < 0) { //wssOriginNode is minor than destine node
      // wssDestineNode receive downgrade to even level of wssOriginNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var belowLevelNode = LevelNode.updateForThisLevel(mAttrs.wssOriginNode());
      // here chance destine node
      solution.variables().set(indexInChromosome, belowLevelNode);
      newLevel = belowLevelNode;

    } else if (mAttrs.compare() > 0) {// destine node is minor than origin node
      // wssOriginNode receive downgrade to even level of wssDestineNode
      var indexInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
      var belowLevelNode = LevelNode.updateForThisLevel(mAttrs.wssDestineNode());
      // here chance oringin node
      solution.variables().set(indexInChromosome, belowLevelNode);
      newLevel = belowLevelNode;
    } else { // two node is in the even level
      var indexOriginNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
      var indexDestineNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexDestineNode();
      var belowLevelNodeOrigin = LevelNode.belowLevel(mAttrs.wssOriginNode());
      var belowLevelNodeDestine = LevelNode.belowLevel(mAttrs.wssDestineNode());
      // here chance both origin and destine node
      solution.variables().set(indexOriginNodeInChromosome, belowLevelNodeOrigin);
      solution.variables().set(indexDestineNodeInChromosome, belowLevelNodeDestine);
      newLevel = belowLevelNodeOrigin;
    }
    //link adjust
    var index = Equipments.getLinkPosition(mAttrs.indexOriginNode(), mAttrs.indexDestineNode(), numNodes, setSize);
    for (int i = 0; i < setSize; i++) {
      //different of zero ,or, it is birth and
      //there is had a birth procedure
      try {
        if (solution.variables().get(index + i) != 0) {
          var link = Bands.getBandForThisNode(newLevel);
          solution.variables().set(index + i, link);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * this method link two nodes.
   * the destine node is update
   * to be adjusted to origin node.
   * after that, a link is created
   * between the two.
   *
   * @param solution
   * @param mAttrs
   */

  private void doBirth(DefaultIntegerSolution solution, MutationAttributes mAttrs) {
    var indexOriginNodeInChromosome = mAttrs.nodePartBegin() + mAttrs.indexOriginNode();
    var mathLevelNode = LevelNode.updateForThisLevel(mAttrs.wssDestineNode());
    solution.variables().set(indexOriginNodeInChromosome, mathLevelNode);
    var index = Equipments.getLinkPosition(mAttrs.indexOriginNode(), mAttrs.indexDestineNode(), numNodes, setSize);
    for (int i = 0; i < setSize; i++) {
      var link = Bands.getBandForThisNode(mathLevelNode);
      solution.variables().set(index + i, link);
    }
    solution.file.get(mAttrs.indexOriginNode()).add(mAttrs.indexDestineNode());
    solution.file.get(mAttrs.indexDestineNode()).add(mAttrs.indexOriginNode());
  }


  private void doExtinction(DefaultIntegerSolution solution, MutationAttributes mAttrs) {
    var neighborhoodA = solution.file.get(mAttrs.indexOriginNode());
    var neighborhoodB = solution.file.get(mAttrs.indexDestineNode());
    // major than 1, or, it will cause a node isolation
    if (neighborhoodA.size() > 1 && neighborhoodB.size() > 1) {
      var index = Equipments.getLinkPosition(mAttrs.indexOriginNode(), mAttrs.indexDestineNode(), numNodes, setSize);
      for (int i = 0; i < setSize; i++) {
        solution.variables().set(index + i, 0);
      }
      solution.file.get(mAttrs.indexOriginNode()).remove(mAttrs.indexDestineNode());
      solution.file.get(mAttrs.indexDestineNode()).remove(mAttrs.indexOriginNode());
    }
  }


  /**
   * this method make correction in solution.
   * this go through all nodes and upgrade/downgrade
   * node accord his connections or create a connection
   * if node is isolated.
   *
   * @param solution
   */
  private void correction(DefaultIntegerSolution solution) {
    var solutionSize = solution.variables().size();
    var nodePartBegin = solutionSize - (numNodes + 1);
    var wssNodes = solution.variables().subList(nodePartBegin, (solutionSize - 1));
    // go through all nodes
    for (int i = 0; i < numNodes; i++) {
      var neighborhood = solution.file.get(i);
      final int indexNodeOrigin = i;
      List<Bands> bandsList = new ArrayList<>();
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
      if (bandsList.isEmpty()) {
        var mAttrs = getAttributes(solution, i);
        doBirth(solution, mAttrs);
      } else {
        var linkReference = bandsList.getLast();
        var wssInThisNode = wssNodes.get(i);
        Random random = new Random();
        var isToDoNodeAdjust = random.nextBoolean();

        if (isToDoNodeAdjust) {//adjust wss in Node
          // On the next line, there is a note about the largest link retrieved
          // from bandsList.getLast().
          // It will change the node to one that meets the largest link in the set
          // if the node already meets it, it promotes horizontal excitation.
          var betterNode = LevelNode.howIsTheNodeForThisBand(linkReference);
          solution.variables().set(nodePartBegin + i, betterNode);
        } else { //adjust link
          // na proxima linha recupera-se um link apropriado para o nó.
          // ele vai alterar o link e retorna exatamente o link apropriado
          // não cabe excitações horizontais
          final int betterlink = LevelNode.howIsTheBandForThisNode(wssInThisNode);
          var neighborhoodList = new ArrayList<>(neighborhood);
          neighborhoodList.stream().forEach(j -> {
            var linkBeginPosition = Equipments.getLinkPosition(indexNodeOrigin, j, numNodes, setSize);
            //go through all links  of one wssInThisNode
            var linkEqualZero = 0;
            for (int w = 0; w < setSize; w++) {
              if (solution.variables().get(linkBeginPosition + w) > 0) {
                var link = LevelNode.chosenLevelLink(betterlink);
                solution.variables().set(linkBeginPosition + w, link);
                //link can return 0, so if it happens, signaling
                if (link == 0) {
                  linkEqualZero += 1;
                }
              } else {
                linkEqualZero += 1;
              }
              if (linkEqualZero == setSize) {
                solution.file.get(indexNodeOrigin).remove(j);
                solution.file.get(j).remove(indexNodeOrigin);
              }
              var link = solution.variables().get(linkBeginPosition + w);
              var linkBand = Bands.getBand(link);
              bandsList.add(linkBand);
            }
          });
        }
      }
    }
  }

  private MutationAttributes getAttributes(DefaultIntegerSolution solution, int indexOriginNode) {
    var neighborhood = solution.file.get(indexOriginNode);
    var solutionSize = solution.variables().size();
    var nodePartBegin = solutionSize - (numNodes + 1);
    var wssInNodes = solution.variables().subList(nodePartBegin, (solutionSize - 1));
    Random random = new Random();
    var indexDestineNode = 0;
    try {
      var listNeighborhood = new ArrayList<Integer>(neighborhood);
      Collections.shuffle(listNeighborhood);
      indexDestineNode = listNeighborhood.get(0);
    } catch (Exception e) {
      indexDestineNode = random.nextInt(numNodes);
      while (indexDestineNode == indexOriginNode) {
        indexDestineNode = random.nextInt(numNodes);
      }

    }

    var originNode = wssInNodes.get(indexOriginNode);
    var destineNode = wssInNodes.get(indexDestineNode);
    // compareTo return -1; 0; 1 respectively: origin< destine; origin=destine; origin>destine
    var compare = LevelNode.getLevel(originNode).compareTo(LevelNode.getLevel(destineNode));
    return new MutationAttributes(
        indexOriginNode,
        indexDestineNode,
        originNode,
        destineNode,
        compare,
        nodePartBegin);
  }

}

