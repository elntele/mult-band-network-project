package org.uma.jmetal.operator.mutation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.doublesolution.repairsolution.RepairDoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.utilities.MutatorParameters;

import com.sun.xml.bind.v2.runtime.output.MTOMXmlOutput;

import br.cns24.services.AllowedConnectionTable;
import br.cns24.services.Bands;
import br.cns24.services.Equipments;
import br.cns24.services.LevelNode;
import br.cns24.services.Operation;
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
    //var selected = selectNodes((DefaultIntegerSolution)solution);
    var selected = selectNodesUniformly();
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
    var mixedSelection = (randomGenerator.nextDouble() <= mixedDistribution);

    //update link
    for (int i = 0; i < numNodes; i++) {
      // get new link
      var newSet = new Integer[]{ 0 };
      if (mixedSelection) {
        newSet = AllowedConnectionTable.mixedSelection(newjType, numNodes,
            graphDensity, randomGenerator);
      } else {
        newSet = AllowedConnectionTable.uniformeSelection(newjType, numNodes,
            graphDensity, randomGenerator);
      }

      var maxBand = Collections.max(Arrays.asList(newSet));
      if (i != j) {
        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        var roadmNodeDestine = solution.variables().get(nodePartBegin + i);
        var parameters = new MutatorParameters(solution, index, newSet, i, j, muted);
        var oldLink =   solution.variables().get(index);
        var destine = Math.max(i,j);
        var destineDegree=solution.degrees[destine];
        var operation = Operation.getOpeOperation(oldLink,newSet[0]);
        boolean localCA= Operation.CauseADegreeFarAway(destineDegree,graphDensity,operation, numNodes);
        boolean localEquipInadequate = LevelNode.thisNodeAddressThisLink(roadmNodeDestine, maxBand);
        var increaseConstraint= (localCA||!localEquipInadequate);
        boolean existeEdge = existEdge(solution, index);
        if (!  increaseConstraint) {
          if (maxBand != 0 || (existeEdge)) {
            updateEdge(parameters);
          }
        }
      }
    }

    if (randomGenerator.nextDouble() < mutationProbability) {
      var size = solution.variables().size() - 1;
      var newW = randomGenerator.nextInt(100 - 10 + 1) + 10;
      solution.variables().set(size, newW);
    }
  }


  private void updateEdge(MutatorParameters parameters) {
    var solution = parameters.solution();
    var index = parameters.index();
    var newLink = parameters.newSet();
    var i = parameters.i();
    var j = parameters.j();
    var muted = parameters.muted();
    var oldLink =   solution.variables().get(index);

    var disconnection = Bands.isDisconnection(newLink[0], oldLink);
    var connection= Bands.isConnection(newLink[0], oldLink);

    for (int y = 0; y < setSize; y++) {
      solution.variables().set(index + y, newLink[y]);
    }

    if (disconnection) {
      solution.degrees[i] -= 1;
      solution.degrees[j] -= 1;
    }

    if(connection) {
      solution.degrees[i] += 1;
      solution.degrees[j] += 1;
    }

    if (i < j) {
      muted.add(new Pair<>(i, j));
    } else {
      muted.add(new Pair<>(j, i));
    }

  }



  private boolean existEdge(DefaultIntegerSolution s, int index) {
    for (int i = 0; i < setSize; i++) {
      if (s.variables().get(index + i) > 0) return true;
    }
    return false;
  }

  /**
   * select nodes with
   * @param solution
   * @return
   */
  private List<Integer> selectNodes(DefaultIntegerSolution solution) {
    List<Integer> weights = new ArrayList<>();
    int weight = 0;
    for (int i = 0; i < numNodes; i++) {
      var reference = (int) Math.ceil(graphDensity * (numNodes - 1));
      var distance = solution.degrees[i] - reference;
      if (distance == -1 || distance == 1 || distance==0) {
        weight = 1;
      } else if (distance < 0) {
        weight = Math.abs(distance) * 3;
      } else {
        weight = Math.abs(distance) * 2;
      }
      weights.add(weight);
    }
    List<Integer> ranges = new ArrayList<>();
    int totalWeights = 0;
    for (Integer w : weights) {
      totalWeights += w;
      ranges.add(totalWeights);
    }

    Set<Integer> selectedNodes = new HashSet<>();
      for (int w = 0; w<numNodes; w++){
        if (randomGenerator.nextDouble()< mutationProbability){
          var randomValue = randomGenerator.nextInt(totalWeights);
          for (int i = 0; i < ranges.size(); i++) {
            if (randomValue < ranges.get(i)) {
              selectedNodes.add(i);
              break;
            }
          }
        }
      }
    return selectedNodes.stream().toList();
  }


  private List<Integer> selectNodesUniformly() {
    Set<Integer> nodos = new HashSet<>();
    for (int i=0; i<numNodes; i++){
      if (randomGenerator.nextDouble()<mutationProbability){
        nodos.add(i);
      }
    }
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

