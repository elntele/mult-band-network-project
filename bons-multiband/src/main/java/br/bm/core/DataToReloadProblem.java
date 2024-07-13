package br.bm.core;

import java.util.List;
import java.util.Map;

public record DataToReloadProblem(
    Integer numberOfVariables,
    Integer numberOfObjectives,
    List<Integer> variable,
    Integer[] lowerBounds,
    Integer[] upperBounds
) {
}
