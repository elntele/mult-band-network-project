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
    this.mixedDistribution=mixedDistribution;
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
    System.out.println("solução original");
    var constraint1 =  solution.constraints()[0];
    var constraint2 = solution.constraints()[1];
    PrintPopulation.printMatrixFull(solution.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), List.of(), List.of(), setSize);

    List<Integer> iMuted = new ArrayList<>();
    List<Integer> jMuted = new ArrayList<>();
    for (int i = 0; i < numNodes; i++) {

      for (int j = i+1; j < numNodes; j++) {
        var random = (randomGenerator.nextInt(100));
        if (random <= mutationProbability) {
          iMuted.add(i);
          jMuted.add(j);
          mutation(((DefaultIntegerSolution) solution), i, j);
        }
      }

    }
    //parte colocada pra fazer o debug
    System.out.println("Operador de mutação");
    System.out.println("solução mudada");
    PrintPopulation.printMatrixFull(solution.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), iMuted, jMuted, setSize);
  }

  private void mutation(DefaultIntegerSolution solution, int i, int j) {
    var solutionSize=solution.variables().size();
    var nodePartBegin = solutionSize - (numNodes + 1);
    var linkIndex = Equipments.getLinkPosition(i, j, numNodes, setSize);
    List<Integer> possibleLink =null;
    var random = randomGenerator.nextInt(101);
    if (random<=mixedDistribution){
      possibleLink = Arrays.asList(AllowedConnectionTable.getUniformConnectionSet());
    }else{
      possibleLink = Arrays.asList(AllowedConnectionTable.getPossibleConnection());
    }
    List<Integer> choicedList = new ArrayList<>();
    for (int w = 0; w < setSize; w++) {
      Collections.shuffle(possibleLink);
      choicedList.add(possibleLink.getFirst());
    }
    Collections.sort(choicedList);
    var wssIndicator = choicedList.getLast();
    var newI= Equipments.getMatchWss(wssIndicator);
    var newJ= Equipments.getMatchWss(wssIndicator);
    for (int y=0; y<choicedList.size(); y++ ){
      solution.variables().set(linkIndex+y, choicedList.get(y));
    }
    solution.variables().set(nodePartBegin+i, newI);
    solution.variables().set(nodePartBegin+j, newJ);

  }


}

