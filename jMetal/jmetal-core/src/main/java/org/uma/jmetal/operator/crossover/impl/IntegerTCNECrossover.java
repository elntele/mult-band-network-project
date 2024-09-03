package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

/**
 * This class allows to apply a Technological Control of Nodes and Edges crossover operator using two
 * parent solutions (Integer encoding)
 */
public class IntegerTCNECrossover implements CrossoverOperator<IntegerSolution> {
  private double distributionIndex;
  private double crossoverProbability;
  private Integer[] possibleEdgeTypes;

  private RandomGenerator<Double> randomGenerator;

  /**
   * Constructor
   */
  public IntegerTCNECrossover(double crossoverProbability, double distributionIndex,
      RandomGenerator<Double> randomGenerator, Integer[] possibleEdgeTypes) {
    Check.probabilityIsValid(crossoverProbability);
    Check.valueIsNotNegative(distributionIndex);
    this.crossoverProbability = crossoverProbability;
    this.distributionIndex = distributionIndex;
    this.randomGenerator = randomGenerator;
    this.possibleEdgeTypes=possibleEdgeTypes;
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

    return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
  }

  /**
   * doCrossover method
   */
  public List<IntegerSolution> doCrossover(
      double probability, IntegerSolution parent1, IntegerSolution parent2) {
    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);

    offspring.add((IntegerSolution) parent1.copy());
    offspring.add((IntegerSolution) parent2.copy());
    return offspring;
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


}
