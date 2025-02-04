package br.mnc;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.crossover.impl.IntegerTCNECrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerTCNEMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.constraintcomparator.impl.OverallConstraintViolationDegreeComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import static org.uma.jmetal.util.AbstractAlgorithmRunner.printFinalSolutionSet;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
  public static void main(String[] args) {

    ExternalNetworkEvaluatorSettings problem; // do Jmetal
    Algorithm<List<IntegerSolution>> algorithm; // do Jmetal
    CrossoverOperator<IntegerSolution> crossover; // do Jmetal
    MutationOperator<IntegerSolution> mutation; // do Jmetal
    SelectionOperator<List<IntegerSolution>, IntegerSolution> selection; // do
    String path = "./selectedCityInPernabucoState.gml";
    //var path = "./teste2.gml";
    var populationSize = 100;
    var maxEvaluations = 100000;
    var iterationsToPrint = 40;
    var setSize = 1;
    var load = 1500;
    var numNodes = 26;
    var numberOfExecutions = 1;
    var printGML=false;
    Double mixedDistribution=0.0;
    for (int i = 1; i <= numberOfExecutions; i++) {
      problem = new ExternalNetworkEvaluatorSettings(setSize, populationSize, path, iterationsToPrint, i, load);
      // ****************************
      // it compares with a Random chosen between 0 and 1
      Double crossoverProbability = 0.30;
      double crossoverDistributionIndex = 20.0;
      //crossover = new IntegerSBXCrossover(crossoverProbability, crossoverDistributionIndex);
      crossover = new IntegerTCNECrossover(crossoverProbability, new Random(), numNodes, setSize);
      double mutationProbability = 1.0 / problem.numberOfVariables();
      double mutationDistributionIndex = 20.0;
      // mutation = new IntegerPolynomialMutation(mutationProbability, mutationDistributionIndex);
      mutation = new IntegerTCNEMutation(0.12, new Random(), numNodes, setSize, mixedDistribution, 0.16);

      // new: create a comparator of constraint violation
      OverallConstraintViolationDegreeComparator<IntegerSolution> constraintComparator = new OverallConstraintViolationDegreeComparator<>();
      // new the constraint comparator now is passed as a parameter
      selection = new BinaryTournamentSelection<IntegerSolution>(constraintComparator);

      algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, populationSize, iterationsToPrint,
          numNodes).setSelectionOperator(
          selection).setMaxEvaluations(maxEvaluations).build();


      AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
      List<IntegerSolution> population;
      population = algorithm.result();

      NSGAII<IntegerSolution> nsgaiiAlgorithm = (NSGAII<IntegerSolution>) algorithm;
      Map<Integer, List<ArrayList<IntegerSolution>>> mapFronts = nsgaiiAlgorithm.getMapFronts();

      var metrics = new MetricsHolder(problem, algorithmRunner, mapFronts);
      ResultsMetricsDao.saveMetrics(population, metrics, i, printGML);
    }

  }


}