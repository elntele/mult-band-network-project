package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.errorchecking.exception.InvalidProbabilityValueException;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;
import org.uma.jmetal.utilities.MutatorParameters;

import com.sun.xml.bind.v2.runtime.reflect.Lister;

import br.cns24.services.Equipments;
import br.cns24.services.PrintPopulation;

/**
 * This class allows to apply a Technological Control of Nodes and Edges crossover operator using two
 * parent solutions (Integer encoding)
 */
public class IntegerTCNECrossover implements CrossoverOperator<IntegerSolution> {
  private Double crossoverProbability;
  private Random randomGenerator;
  private int numNodes;
  private int setSize;

  /**
   * Constructor
   */
  public IntegerTCNECrossover(
      Double crossoverProbability,
      Random randomGenerator,
      int numNodes,
      int setSize
  ) {
    Check.probabilityIsValid(crossoverProbability);
    this.crossoverProbability = crossoverProbability;
    this.randomGenerator = randomGenerator;
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

    return doCrossover(crossoverProbability, (DefaultIntegerSolution) solutions.get(0),
        (DefaultIntegerSolution) solutions.get(1));
  }

  /**
   * doCrossover method
   */
  public List<IntegerSolution> doCrossover(
      double probability, DefaultIntegerSolution parent1, DefaultIntegerSolution parent2) {
    //test(parent1, parent2);
    var beginNodes = parent1.variables().size() - (numNodes + 1);
    List<Pair<Integer, Integer>> crossed1 = new ArrayList<>();
    List<Pair<Integer, Integer>> crossed2 = new ArrayList<>();
    System.out.println("Operador de cruzamento");
    print(parent1, parent2, crossed1, crossed1, "pai", 0, 0);

    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    var san1 = parent1.copy();
    var san2 = parent2.copy();
    var temp1 = randomGenerator.nextInt(numNodes);
    int temp2 = temp1+4;
    if (temp2>=numNodes-1) temp2=numNodes-1;

    var begin = Math.min(temp1, temp2);
    begin+=1;
    var end = Math.max(temp1, temp2);
    end-=1;
    var offset = 0;
    if (randomGenerator.nextDouble() < probability) {
      //node crossOver
      for (int i = begin; i <= end; i++) {
        san1.variables().set(beginNodes + i, parent2.variables().get(beginNodes+i));
        san2.variables().set(beginNodes + i, parent1.variables().get(beginNodes+i));
      }

      //parte colocada pra fazer o debug

      for (int noi = 0; noi <= end+1; noi++) {
        if (noi != begin) {
          if (noi < begin) {
            var point1 = Equipments.getLinkPosition(noi, begin, numNodes, setSize);
            var point2 = Equipments.getLinkPosition(noi, end, numNodes, setSize);
            updateEdge(san1, parent2, point1 * setSize,
                point2 * setSize, crossed1, noi, begin);
            updateEdge(san2, parent1, point1 * setSize,
                point2 * setSize, crossed2, noi, begin);
          } else {
            var point1 = Equipments.getLinkPosition(noi, begin + offset, numNodes, setSize);
            var point2 = Equipments.getLinkPosition(begin + offset, numNodes - 1, numNodes, setSize);
            updateEdge(san1, parent2, point1 * setSize, point2 * setSize, crossed1, noi, begin + offset);
            updateEdge(san2, parent1, point1 * setSize, point2 * setSize, crossed2, noi, begin + offset);
            offset += 1;
          }
        }
      }

    }

    if (randomGenerator.nextDouble() < probability) {
      var size = parent1.variables().size() - 1;
      san1.variables().set(size, parent2.variables().get(size));
      san2.variables().set(size, parent1.variables().get(size));

    }

    print(san1, san2, crossed1, crossed2, "filho", begin, end);
    offspring.add((IntegerSolution) san1);
    offspring.add((IntegerSolution) san2);
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


  private void print(Solution s1, Solution s2, List<Pair<Integer, Integer>> crossed1,
      List<Pair<Integer, Integer>> crossed2, String familiar, int begin, int end) {

    System.out.println("inicio " + begin);
    System.out.println("fim " + end);
    System.out.println("solução " + familiar + " 1");
    var constraint1 = s1.constraints()[0];
    var constraint2 = s1.constraints()[1];
    PrintPopulation.printMatrixFull(s1.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), crossed1, setSize);
    System.out.println("inicio " + begin);
    System.out.println("fim " + end);
    System.out.println("solução " + familiar + " 2");
    constraint1 = s2.constraints()[0];
    constraint2 = s2.constraints()[1];
    PrintPopulation.printMatrixFull(s2.variables(), numNodes, Double.toString(constraint1),
        Double.toString(constraint2), crossed2, setSize);
  }

  private void updateEdge(DefaultIntegerSolution s, DefaultIntegerSolution p, int beginIndex, int endIndex,
      List<Pair<Integer, Integer>> crossed, int i, int begin) {
    var step = 0;
    for (int w = beginIndex; w <= endIndex; w++) {
      for (int y = 0; y < setSize; y++) {
        s.variables().set(w + y, p.variables().get(w + y));
        if (i < begin) {
          crossed.add(new Pair<>(i, begin + step));
        } else {
          crossed.add(new Pair<>(begin, i + step));
        }
      }
      step += 1;
    }
  }

  private void test(IntegerSolution s1, IntegerSolution s2) {
    var beginNodes = s1.variables().size() - (numNodes + 1);
    for (int i = 0; i < numNodes; i++) {
      s1.variables().set(beginNodes + i, 1);
      s2.variables().set(beginNodes + i, 3);
      for (int j = i + 1; j < numNodes; j++) {

        var index = Equipments.getLinkPosition(i, j, numNodes, setSize);
        for (int w = 0; w < setSize; w++) {
          s1.variables().set(index, 1);
          s2.variables().set(index, 3);

        }
      }
    }
  }

}
