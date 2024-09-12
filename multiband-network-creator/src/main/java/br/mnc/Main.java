package br.mnc;

import br.cns24.model.GmlData;
import br.cns24.persistence.GmlDao;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.crossover.impl.IntegerTCNECrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.mutation.impl.IntegerTCNEMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.uma.jmetal.util.AbstractAlgorithmRunner.printFinalSolutionSet;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Problem<IntegerSolution> problem; // do Jmetal
        Algorithm<List<IntegerSolution>> algorithm; // do Jmetal
        CrossoverOperator<IntegerSolution> crossover; // do Jmetal
        MutationOperator<IntegerSolution> mutation; // do Jmetal
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection; // do
        problem = new ExternalNetworkEvaluatorSettings(3);


        // ****************************
        double crossoverProbability = 1.0;
        double crossoverDistributionIndex = 20.0;
        crossover = new IntegerSBXCrossover(crossoverProbability, crossoverDistributionIndex);
        crossover = new IntegerTCNECrossover(crossoverProbability,() -> JMetalRandom.getInstance().nextDouble(),4, 3);
        double mutationProbability = 1.0 / problem.numberOfVariables();
        double mutationDistributionIndex = 20.0;
       // mutation = new IntegerPolynomialMutation(mutationProbability, mutationDistributionIndex);
        mutation = new IntegerTCNEMutation(mutationProbability,() -> JMetalRandom.getInstance().nextDouble() , 4, 3);
        selection = new BinaryTournamentSelection<IntegerSolution>();

        algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, 100).setSelectionOperator(selection).setMaxEvaluations(10000).build();
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        List<IntegerSolution> population;
        population = algorithm.result();
        int w = 1;

        String path = "src/result/print.txt";
        new File( "src/result/").mkdir();
        new File( "src/result/gml").mkdirs();
        String gmlpath = "src/result/gml";

        FileWriter arq = null;
        try {
            arq = new FileWriter(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrintWriter gravarArq = new PrintWriter(arq);



        for (IntegerSolution solution : population) {
            String patch = gmlpath + "/ResultadoGML/" + w + ".gml";
            save(patch, solution);
            w += 1;
        }


        System.out
                .println("fitness evaluation number" + ((ExternalNetworkEvaluatorSettings) problem).contEvaluate);
        gravarArq.printf(
                "fitness evaluation number" + ((ExternalNetworkEvaluatorSettings) problem).contEvaluate + '\n');
        System.out.println("database saved in GML format");

        long computingTime = algorithmRunner.getComputingTime();
        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
        gravarArq.printf("Total execution time: " + computingTime + "ms" + '\n');
        printFinalSolutionSet(population);

    }

    public static void save(String patch, IntegerSolution solution){

           /* Pattern[] arrayPatterns = solution.getLineColumn();
            Integer[] vars = new Integer[solution.getNumberOfVariables()];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = solution.getVariableValue(i);
            }
            Map<String, String> informations = new HashMap();
            informations.put("Country", "Brazil");
            informations.put("PB", Double.toString(solution.objectives()[0]));
            informations.put("Capex", Double.toString(solution.objectives()[1]));
            GmlDao gmlDao = new GmlDao();
            GmlData gmlData = new GmlData();
            gmlData.setNodes(patternGml(arrayPatterns));
            BooleanAndEdge B = makelink(arrayPatterns, vars);
            gmlData.setEdges(B.getEdges());
            gmlData.setInformations(informations);
            gmlData.createComplexNetwork();
            gmlDao.save(gmlData, patch);*/


    }
}