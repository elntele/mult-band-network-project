package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.selection.impl.DifferentialEvolutionSelection;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

/**
 * Abstract class representing differential evolution (DE) algorithms
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
public abstract class AbstractDifferentialEvolution<Result> extends AbstractEvolutionaryAlgorithm<DoubleSolution, Result> {
  public abstract DifferentialEvolutionCrossover getCrossoverOperator() ;
  public abstract DifferentialEvolutionSelection getSelectionOperator() ;
}
