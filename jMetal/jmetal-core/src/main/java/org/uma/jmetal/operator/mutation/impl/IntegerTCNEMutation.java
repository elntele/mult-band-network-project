package org.uma.jmetal.operator.mutation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.doublesolution.repairsolution.RepairDoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.utilities.MutatorParameters;

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
  private Double mixedDistribution;
  private Integer[] nodesDegree;
  private Double meanNodeDegree;
  private Double graphDensity;
  private final int upGrade = 1;
  private final int downGrade = 2;
  private final int doNothing = 3;

  public IntegerTCNEMutation(
      double mutationProbability,
      Random randomGenerator,
      int numNodes,
      int setSize,
      Double mixedDistribution,
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
    this.graphDensity = graphDensity;
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
    // inserted to make debug, it isn't part of algorithm, include iMuted and jMuted
    System.out.println("Operador de Mutação");

    List<Pair<Integer, Integer>> muted = new ArrayList<>();
    print((DefaultIntegerSolution) solution, muted, "original");

    buildNodeDegreeInformation((DefaultIntegerSolution) solution);
    var selected = selectNodes();
    for (int i = 0; i < selected.size(); i++) {
      mutation(((DefaultIntegerSolution) solution), selected.get(i), muted);
    }

    // inserted to make debug, it isn't part of algorithm
    System.out.println("Operador de mutação");
    print((DefaultIntegerSolution) solution, muted, "mudada");
  }

  private void mutation(DefaultIntegerSolution solution, int j, List<Pair<Integer, Integer>> muted) {
    var solutionSize = solution.variables().size();
    var nodePartBegin = solutionSize - (numNodes + 1);
    // get new Node
    var newjType = Equipments.getRandomROADM();
    //Nodo update
    solution.variables().set(nodePartBegin + j, newjType);
    var mixedSelection= (randomGenerator.nextDouble()<=mixedDistribution);

    //update link
    for (int i = 0; i < numNodes; i++) {
      // get new link
      var newSet=new Integer[]{0};
      if(mixedSelection){
         newSet = AllowedConnectionTable.mixedSelection(newjType, numNodes,
            graphDensity, randomGenerator);
      }else {
         newSet = AllowedConnectionTable.uniformeSelection( newjType, numNodes,
            graphDensity, randomGenerator);
      }

      var maxBand = Collections.max(Arrays.asList(newSet));
      if (i != j) {
        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        var nodeDestine = solution.variables().get(nodePartBegin + i);
        var parameters = new MutatorParameters(solution, index, newSet, i, j, muted);
        boolean notCauseConstraint = LevelNode.thisNodeAddressThisLink(nodeDestine, maxBand);
        boolean existeEdge = existEdge(solution, index);
        if (notCauseConstraint) {
          if (maxBand != 0 || (existeEdge)) {
            updateEdge(parameters);
          }
        }
      }
    }
  }



  private void updateEdge(MutatorParameters parameters) {
    var solution = parameters.solution();
    var index = parameters.index();
    var newSet = parameters.newSet();
    var i = parameters.i();
    var j = parameters.j();
    var muted = parameters.muted();

    for (int y = 0; y < setSize; y++) {
      solution.variables().set(index + y, newSet[y]);
    }

    if (i < j) {
      muted.add(new Pair<>(i, j));
      //   System.out.println("pares mudados: i=" + i + "; j=" + j);
    } else {
      muted.add(new Pair<>(j, i));
      //   System.out.println("pares mudados: i=" + j + "; j=" + i);
    }

  }

  private void buildNodeDegreeInformation(DefaultIntegerSolution s) {
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


  private List<Integer> selectNodes() {
    Set<Integer> nodos = new HashSet<>();
    while (nodos.size() < 3) {
      var node = randomGenerator.nextInt(numNodes);
      nodos.add(node);
    }
    //System.out.println(nodos);
    return nodos.stream().toList();
  }


  private void print(DefaultIntegerSolution s1, List<Pair<Integer, Integer>> PairMutated, String tipo) {

    System.out.println("solução " + tipo);
    var constraint1 = s1.constraints()[0];
    var constraint2 = s1.constraints()[1];
    PrintPopulation.printMatrixFull(s1.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), PairMutated, setSize);
  }
}

