package br.mnc;

public record ConstrainsMetrics(
    Integer iteration,
    Integer numberOfSolutionWithCAInZero,
    Integer numberOfSolutionWithInadequateEquipment,
    Double meanRateInadequateEquipment,
    Double standardDeviationInadequateEquipment
) {
}
