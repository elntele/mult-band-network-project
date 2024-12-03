package br.mnc;

import static org.uma.jmetal.util.AbstractAlgorithmRunner.printFinalSolutionSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.JMetalLogger;

public class ResultsMetricsDao {

  public static void saveMetrics(List<IntegerSolution> population, MetricsHolder metricsHolder) {
    String path = "src/result/print.txt";
    new File("src/result/").mkdir();
    new File("src/result/gml").mkdirs();
    String gmlpath = "src/result/gml";
    FileWriter arq = null;
    PrintWriter gravarArq = null;
    var problem = metricsHolder.externalNetworkEvaluatorSettings();
    var algorithmRunner = metricsHolder.algorithmRunner();
    var w=1;
    try {
      arq = new FileWriter(path);
      gravarArq = new PrintWriter(arq);
      for (IntegerSolution solution : population) {
        String patch = gmlpath + "/ResultadoGML/" + w + ".gml";
       // save(patch, solution);
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
}
