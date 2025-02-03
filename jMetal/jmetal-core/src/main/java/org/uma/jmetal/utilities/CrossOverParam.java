package org.uma.jmetal.utilities;

import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

public record CrossOverParam(
    DefaultIntegerSolution san,
    DefaultIntegerSolution parent,
    int matrixIndexOne,
    int matrixIndexTwo,
    List<Pair<Integer, Integer>> crossed,
    int i,
    int initialNodeSelected
) {

}
