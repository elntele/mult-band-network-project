package org.uma.jmetal.operator.mutation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.repairsolution.RepairDoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import br.cns24.services.AllowedConnectionTable;
import br.cns24.services.Equipments;
import br.cns24.services.LevelNode;
import br.cns24.services.PrintPopulation;


/**
 * This class allows to apply a Technological Control of Nodes and Edges mutation operator using two
 * parent solutions (Integer encoding)
 */
public class IntegerTCNEMutation implements MutationOperator<IntegerSolution> {

  private double mutationProbability;
  private RepairDoubleSolution solutionRepair;

  private Random randomGenerator;
  private int numNodes;
  private int setSize;
  private int mixedDistribution;
  private Integer[] nodesDegree;
  private Double meanNodeDegree;
  private Double graphDensity;

  public IntegerTCNEMutation(
      double mutationProbability,
      Random randomGenerator,
      int numNodes,
      int setSize,
      int mixedDistribution,
      Double graphDensity
  ) {
    if (mutationProbability < 0) {
      throw new JMetalException("Mutation probability is negative: " + mutationProbability);
    }
    this.mutationProbability = mutationProbability;
    this.randomGenerator = randomGenerator;
    this.numNodes = numNodes;
    this.setSize = setSize;
    this.mixedDistribution = mixedDistribution;
    nodesDegree = new Integer[numNodes];
    Arrays.fill(nodesDegree, 0);
    this.meanNodeDegree = Math.ceil(graphDensity * (numNodes - 1));
    this.graphDensity=graphDensity;
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
    //parte colocada pra fazer o debug
    System.out.println("Operador de Mutação");
    print((DefaultIntegerSolution) solution, List.of(), List.of(), "original");
    buildMatrix((DefaultIntegerSolution) solution);
    List<Integer> iMuted = new ArrayList<>();
    List<Integer> jMuted = new ArrayList<>();

    for (int j = 0; j < numNodes; j++) {
      var random = (randomGenerator.nextInt(1001));
      if (random <= mutationProbability) {
        // jMuted.add(j);
        mutation(((DefaultIntegerSolution) solution), j, iMuted, jMuted);
      }
    }


    //parte colocada pra fazer o debug
    System.out.println("Operador de mutação");
    print((DefaultIntegerSolution) solution, iMuted, jMuted, "mudada");
  }

  private void mutation(DefaultIntegerSolution solution, int j, List<Integer> iMuted, List<Integer> jMuted) {
    var solutionSize = solution.variables().size();
    var nodePartBegin = solutionSize - (numNodes + 1);
    // get new Node
    var newJ = Equipments.getRandomROADM();
    //Nodo update
    solution.variables().set(nodePartBegin + j, newJ);
    // get new link
    var newSet = AllowedConnectionTable.randomChooseConnection(mixedDistribution, setSize, newJ);
    var maxBand = Collections.max(Arrays.asList(newSet));
    //update link
    for (int i = 0; i < numNodes; i++) {
      var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
      var node = solution.variables().get(nodePartBegin + i);
      if (i != j) {
        if (LevelNode.thisNodeAddressThisLink(node, maxBand)) {
          var highProbability = randomGenerator.nextInt(1001) <= highestProbability();
          var lowerPobability = randomGenerator.nextInt(1001) <= lowerProbability();
          if (isNotADisconnection(newSet)) {
            if (existEdge(solution, index)) {
              if (highProbability) {
                for (int y = 0; y < setSize; y++) {
                  solution.variables().set(index + y, newSet[y]);
                }
              }
            } else {
              if (nodesDegree[i] < meanNodeDegree) {
                  if (highProbability){
                    for (int y = 0; y < setSize; y++) {
                      solution.variables().set(index + y, newSet[y]);
                    }
                    nodesDegree[i] += 1;
                  }
              }else{
                if(lowerPobability){
                  for (int y = 0; y < setSize; y++) {
                    solution.variables().set(index + y, newSet[y]);
                  }
                  nodesDegree[i] += 1;
                }

              }
            }
          } else {
            if (existEdge(solution, index)){
              if(nodesDegree[i]< meanNodeDegree){
                continue;
              }else{
                if (highProbability){
                  for (int y = 0; y < setSize; y++) {
                    solution.variables().set(index + y, newSet[y]);
                  }
                  nodesDegree[i] -= 1;
                }
              }
            }
          }
          //print part
          if (i < j) {
            iMuted.add(i);
            jMuted.add(j);
          } else {
            iMuted.add(j);
            jMuted.add(i);
          }
        }
      }
    }

  }

  private void print(DefaultIntegerSolution s1, List<Integer> iCrossed, List<Integer> jCrossed, String tipo) {

    System.out.println("solução " + tipo);
    var constraint1 = s1.constraints()[0];
    var constraint2 = s1.constraints()[1];
    PrintPopulation.printMatrixFull(s1.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), iCrossed, jCrossed, setSize);
  }

  private void buildMatrix(DefaultIntegerSolution s) {
    List<Integer> connections = s.variables().subList(0, s.variables().size() - (numNodes + 1));
    for (int i = 0; i < numNodes; i++) {
      for (int j = i + 1; j < numNodes; j++) {
        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        nodesDegree[i] += 1;
        for (int w = 0; w < setSize; w++) {
          if (connections.get(index + w) > 0) {
            nodesDegree[i] += 1;
          }
        }
      }
    }
  }

  private boolean isNotADisconnection(Integer[] newSet) {
    var sun = Arrays.stream(Arrays.stream(newSet)
        .mapToInt(Integer::intValue)
        .toArray()).sum();
    return sun > 0;
  }

  private boolean existEdge(DefaultIntegerSolution s, int index) {
    for (int i = 0; i < setSize; i++) {
      if (s.variables().get(index + i) > 0) return true;
    }
    return false;
  }

  private int highestProbability() {
    var probabilityOne= (int) Math.round(1.0-graphDensity *100);
    var probabilityTwo= (int) Math.round(graphDensity *100);
    return Math.max(probabilityOne, probabilityTwo);
  }

  private int lowerProbability(){
    var probabilityOne= (int) Math.round(1.0-graphDensity *100);
    var probabilityTwo= (int) Math.round(graphDensity *100);
    return Math.min(probabilityOne, probabilityTwo);
  }

}

