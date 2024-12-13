package br.mnc;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

public class StatisticsConstraints {


  public static ConstrainsMetrics getMetrics(List<DefaultIntegerSolution> pop, int iteration) {
    Integer numberOfSolutionWithCAInZero = 0;
    Integer numberOfSolutionWithInadequateEquipment = 0;
    Double sunInadequateEquipment = 0.0;
    Double majorEquipmentInadequateConstraint = Double.MIN_VALUE;

    // Use DescriptiveStatistics to calculate standard deviation
    DescriptiveStatistics stats = new DescriptiveStatistics();

    for (DefaultIntegerSolution s : pop) {
      if (s.constraints()[0] == 1) {
        numberOfSolutionWithCAInZero += 1;
      }
      if (s.constraints()[1] > 0) {
        numberOfSolutionWithInadequateEquipment += 1;
        sunInadequateEquipment += s.constraints()[1];
        stats.addValue(s.constraints()[1]);
        if (s.constraints()[1]> majorEquipmentInadequateConstraint){
          majorEquipmentInadequateConstraint=s.constraints()[1];
        }
      }
    }

    Double averageRestrictionRateForInadequateEquipment = numberOfSolutionWithInadequateEquipment > 0
        ? sunInadequateEquipment / numberOfSolutionWithInadequateEquipment
        : 0.0;

    Double standardDeviationInadequateEquipment = stats.getStandardDeviation();

    return new ConstrainsMetrics(iteration,
        numberOfSolutionWithCAInZero,
        numberOfSolutionWithInadequateEquipment,
        averageRestrictionRateForInadequateEquipment,
        standardDeviationInadequateEquipment);
  }



}
