package br.mnc;

import static org.uma.jmetal.util.AbstractAlgorithmRunner.printFinalSolutionSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;

import br.cns24.model.GmlData;
import br.cns24.persistence.GmlDao;

public class ResultsMetricsDao {

  public static void saveMetrics(List<IntegerSolution> population, MetricsHolder metricsHolder, int execution,
      boolean printGML) {
    String path = String.format("src/result/execution%d/printExecution%d.txt", execution, execution);
    String gmlPath = String.format("src/result/execution%d/gml/ResultadoGML/", execution);
    if (printGML){
      new File(gmlPath).mkdirs();
    }else{
       gmlPath = String.format("src/result/execution%d/", execution);
      new File(gmlPath).mkdirs();

    }


    FileWriter arq = null;
    PrintWriter gravarArq = null;
    var problem = metricsHolder.externalNetworkEvaluatorSettings();
    var algorithmRunner = metricsHolder.algorithmRunner();
    var w = 1;
    try {
      arq = new FileWriter(path);
      gravarArq = new PrintWriter(arq);
      for (IntegerSolution solution : population) {
        String patch = gmlPath + w + ".gml";
        if (printGML) {

          save(patch, solution, problem);
        } else {
          System.out.println("attention: .GML is not being saved, to save it set printGML as true in Main Class." );
        }
        w += 1;
      }

      System.out.println("fitness evaluation number" + (problem).contEvaluate);
      gravarArq.printf("fitness evaluation number" + (problem).contEvaluate + '\n');
      System.out.println("database saved in GML format");
      long computingTime = algorithmRunner.getComputingTime();
      JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
      gravarArq.printf("Total execution time: " + computingTime + "ms" + '\n');

      saveConstraintsMetrics(metricsHolder, gravarArq);
      saveParetoMetrics(metricsHolder.mapFronts(), gravarArq);
      saveCountConstraintXNode(metricsHolder.externalNetworkEvaluatorSettings().getSolutionsXNodeConstraint(), gravarArq);
      printFinalSolutionSet(population);

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {

      try {
        if (gravarArq != null) {
          gravarArq.close();
        }
        if (arq != null) {
          arq.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void save(String patch, IntegerSolution solution, ExternalNetworkEvaluatorSettings problem) {


    Integer[] vars = new Integer[solution.variables().size()];
    for (int i = 0; i < vars.length; i++) {
      vars[i] = solution.variables().get(i);
    }
    Map<String, String> informations = new HashMap();
    informations.put("Country", "Brazil");
    informations.put("PB", Double.toString(solution.objectives()[0]));
    informations.put("Capex", Double.toString(solution.objectives()[1]));
    GmlDao gmlDao = new GmlDao();
    GmlData gmlData = new GmlData();
    var nodes = problem.getGml().getNodes();
    gmlData.setNodes(nodes);
    gmlData.setEdgesLosingLinkInformation(vars, nodes);
    gmlData.setInformations(informations);
    gmlData.createComplexNetwork();
    gmlDao.save(gmlData, patch);


  }

  private static void saveConstraintsMetrics(MetricsHolder metricsHolder,
      PrintWriter gravarArq) throws Exception {
    var problem = metricsHolder.externalNetworkEvaluatorSettings();
    Map<Integer, ConstrainsMetrics> mapConstraints = metricsHolder.externalNetworkEvaluatorSettings().getConstraintsStatistics();


    mapConstraints.entrySet().stream()
        .forEach(entry -> {
          ConstrainsMetrics value = entry.getValue();
          ConstrainsMetrics metrics = value;
          // Escrevendo no arquivo no formato linha separada por ponto e vírgula
          gravarArq.printf(
              "iteration: %d; ca0Rest1: %d; " +
                  "eqInadRest2: %d; " +
                  "medianrest2: %.2f; " +
                  "StdRest2: %.2f%n",
              metrics.iteration(),
              metrics.numberOfSolutionWithCAInZero(),
              metrics.numberOfSolutionWithInadequateEquipment(),
              metrics.meanRateInadequateEquipment(),
              metrics.standardDeviationInadequateEquipment()
          );


        });


    System.out
        .println("fitness evaluation number" + ((ExternalNetworkEvaluatorSettings) problem).contEvaluate);


  }

  private static void saveParetoMetrics(Map<Integer, List<ArrayList<IntegerSolution>>> mapFronts,
      PrintWriter gravarArq) throws Exception {
    mapFronts.entrySet().stream()
        .forEach(entry -> {
          Integer iteration = entry.getKey();
          List<ArrayList<IntegerSolution>> paretos = entry.getValue();
          int numberOfSolutionsInFirstPareto = (paretos.isEmpty()) ? 0 : paretos.get(0).size();
          int numberOfParetoSets = paretos.size();
          gravarArq.printf(
              "interação: %d; numero de soluções no primeiro pareto: %d; numero de paretos: %d%n",
              iteration,
              numberOfSolutionsInFirstPareto,
              numberOfParetoSets
          );
        });

    System.out.println("Pareto metrics saved.");
  }


  private static void saveCountConstraintXNode(Map<Integer, ArrayList<Integer>> mapNode,
      PrintWriter gravarArq) throws Exception {
    mapNode.entrySet().stream()
        .forEach(entry -> {
          Integer iteration = entry.getKey();
         List<Integer> node = entry.getValue();
          gravarArq.printf(
              "interação: %d; Nós versus qtd de soluções com restrição: %s%n",
              iteration,
              node.toString()
          );
        });

    System.out.println("histogram nodes x constraints saved.");
  }


}
