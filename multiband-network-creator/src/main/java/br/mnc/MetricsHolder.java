package br.mnc;

import org.uma.jmetal.algorithm.examples.AlgorithmRunner;

public record MetricsHolder(
    ExternalNetworkEvaluatorSettings externalNetworkEvaluatorSettings,
    AlgorithmRunner algorithmRunner

) {
}
