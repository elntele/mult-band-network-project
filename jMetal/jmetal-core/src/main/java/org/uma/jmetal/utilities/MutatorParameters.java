package org.uma.jmetal.utilities;

import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

public record MutatorParameters(
    DefaultIntegerSolution solution,
    int index,
    Integer[] newSet,
    int i,
    int j,
    List<Pair<Integer, Integer>> muted) {
}
