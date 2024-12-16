package br.mnc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

public record MetricsHolder(
    ExternalNetworkEvaluatorSettings externalNetworkEvaluatorSettings,
    AlgorithmRunner algorithmRunner,
    Map<Integer, List<ArrayList<IntegerSolution>>> mapFronts

) {
}
