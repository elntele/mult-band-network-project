package br.mnc;

import static org.uma.jmetal.util.AbstractAlgorithmRunner.printFinalSolutionSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;

import br.cns24.model.GmlData;
import br.cns24.persistence.GmlDao;

public class ResultsMetricsDao {

  public static void saveMetrics(List<IntegerSolution> population, MetricsHolder metricsHolder) {
    String path = "src/result/print.txt";
    String gmlPath = "src/result/gml/ResultadoGML/";
    new File(gmlPath).mkdirs();


    FileWriter arq = null;
    PrintWriter gravarArq = null;
    var problem = metricsHolder.externalNetworkEvaluatorSettings();
    var algorithmRunner = metricsHolder.algorithmRunner();
    var w=1;
    try {
      arq = new FileWriter(path);
      gravarArq = new PrintWriter(arq);
      for (IntegerSolution solution : population) {
        String patch = gmlPath  + w + ".gml";
        save(patch, solution, problem);
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
    } catch (IOException e) {
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

  private static void save(String patch, IntegerSolution solution, ExternalNetworkEvaluatorSettings problem ) {


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
}