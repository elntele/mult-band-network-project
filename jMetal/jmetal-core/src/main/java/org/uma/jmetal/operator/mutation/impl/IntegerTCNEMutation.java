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
  private List<Integer>[][] connectionsMatrix;

  public IntegerTCNEMutation(
      double mutationProbability,
      Random randomGenerator,
      int numNodes,
      int setSize,
      int mixedDistribution
  ) {
    if (mutationProbability < 0) {
      throw new JMetalException("Mutation probability is negative: " + mutationProbability);
    }
    this.mutationProbability = mutationProbability;
    this.randomGenerator = randomGenerator;
    this.numNodes = numNodes;
    this.setSize = setSize;
    this.mixedDistribution = mixedDistribution;
    connectionsMatrix = new List[numNodes][numNodes];
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
    print(solution, List.of(), List.of(), "original");
    buildMatrix(solution);
    List<Integer> iMuted = new ArrayList<>();
    List<Integer> jMuted = new ArrayList<>();

    for (int j = 0; j < numNodes; j++) {
      var random = (randomGenerator.nextInt(100));
      if (random <= mutationProbability) {
        jMuted.add(j);
        mutation(((DefaultIntegerSolution) solution), j, iMuted);
      }
    }


    //parte colocada pra fazer o debug
    System.out.println("Operador de mutação");
    print(solution, iMuted, jMuted, "mudada");
  }

  private void mutation(DefaultIntegerSolution solution, int j, List<Integer> iMuted) {
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
      var linkIndex = Equipments.getLinkPosition(i, j, numNodes, setSize);
      var node = solution.variables().get(nodePartBegin + i);
      if (i != j) {
        if (LevelNode.thisNodeAddressThisLink(node, maxBand)) {
          connectionsMatrix[i][j] = Arrays.asList(newSet);
          connectionsMatrix[j][i] = Arrays.asList(newSet);
          for (int y = 0; y < setSize; y++) {
            solution.variables().set(linkIndex + y, newSet[y]);
          }
          iMuted.add(i);
        }
      }
    }

  }

  private void print(Solution s1, List<Integer> iCrossed, List<Integer> jCrossed, String tipo) {

    System.out.println("solução " + tipo);
    var constraint1 = s1.constraints()[0];
    var constraint2 = s1.constraints()[1];
    PrintPopulation.printMatrixFull(s1.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), iCrossed, jCrossed, setSize);
  }

  private void buildMatrix(Solution s) {
    var connections = s.variables().subList(0, s.variables().size() - (numNodes + 1));
    for (int i = 0; i < numNodes; i++) {
      for (int j = i + 1; j < numNodes; j++) {
        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        ArrayList setConnection = new ArrayList<>();
        for (int w = 0; w < setSize; w++) {
          setConnection.add(connections.get(index + w));
        }
        connectionsMatrix[i][j] = setConnection;
        connectionsMatrix[j][i] = setConnection;
      }
    }
  /*  System.out.println("matrix de conexão smart");
    for (int i =0; i<numNodes; i++){
      for (int j=-0;j<numNodes; j++){
        System.out.print(connectionsMatrix[i][j]+ " ");
      }
      System.out.println();
    }*/

  }

}

