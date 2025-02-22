package org.uma.jmetal.solution.integersolution.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.uma.jmetal.solution.AbstractSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;


/**
 * Defines an implementation of the {@link IntegerSolution} interface
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DefaultIntegerSolution extends AbstractSolution<Integer> implements IntegerSolution {
  protected List<Bounds<Integer>> bounds;
  // added by Jorge Candeias for adequate to proposal
  public Map<Integer, Set<Integer>> file = new HashMap<>();
  public Integer[] degrees;

  /**
   * Constructor
   */
  public DefaultIntegerSolution(List<Bounds<Integer>> boundsList, int numberOfObjectives, int numberOfConstraints) {
    super(boundsList.size(), numberOfObjectives, numberOfConstraints);

    this.bounds = boundsList;

    IntStream.range(0, bounds.size()).forEach(i -> variables().set(
        i, JMetalRandom.getInstance().nextInt(this.bounds.get(i).getLowerBound(), this.bounds.get(i).getUpperBound())));
  }

  /**
   * Constructor
   */
  public DefaultIntegerSolution(List<Bounds<Integer>> boundsList, int numberOfObjectives, int numberOfConstraints,
      int nodeNumber) {
    super(boundsList.size(), numberOfObjectives, numberOfConstraints);

    this.bounds = boundsList;
    this.degrees = new Integer[nodeNumber];

    IntStream.range(0, bounds.size()).forEach(i -> variables().set(
        i, JMetalRandom.getInstance().nextInt(this.bounds.get(i).getLowerBound(), this.bounds.get(i).getUpperBound())));
  }

  /**
   * Copy constructor
   */
  public DefaultIntegerSolution(DefaultIntegerSolution solution) {
    super(solution.variables().size(), solution.objectives().length, solution.constraints().length);

    IntStream.range(0, solution.variables().size()).forEach(i -> variables().set(i, solution.variables().get(i)));
    IntStream.range(0, solution.objectives().length).forEach(i -> objectives()[i] = solution.objectives()[i]);
    IntStream.range(0, solution.constraints().length).forEach(i -> constraints()[i] = solution.constraints()[i]);

    bounds = solution.bounds;

    attributes = new HashMap<>(solution.attributes);
    file = solution.file.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> new HashSet<>(entry.getValue())
        ));
    this.degrees=  Arrays.copyOf(solution.degrees, solution.degrees.length);
  }

  @Override
  public Bounds<Integer> getBounds(int index) {
    return this.bounds.get(index);
  }

  @Override
  public DefaultIntegerSolution copy() {
    return new DefaultIntegerSolution(this);
  }


}
