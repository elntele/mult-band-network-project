package org.uma.jmetal.util;

import static org.uma.jmetal.util.SolutionListUtils.getMatrixWithObjectiveValues;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.uma.jmetal.qualityindicator.QualityIndicatorUtils;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * Abstract class for Runner classes
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public abstract class AbstractAlgorithmRunner {
  /**
   * Write the population into two files and prints some data on screen
   *
   * @param population
   */
  public static void printFinalSolutionSet(
      List<? extends Solution<?>> population) {
    // Chama o método novo com os valores padrão
    printFinalSolutionSet(population, "VAR.csv", "FUN.csv");
  }

  /**
   * owner, jorge candeias:
   * write population int two files giving names to files
   *
   * @param population
   * @param nameVar
   * @param nameFun
   */

  public static void printFinalSolutionSet(
      List<? extends Solution<?>> population,
      String nameVar,
      String nameFun) {

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext(nameVar, ","))
        .setFunFileOutputContext(new DefaultFileOutputContext(nameFun, ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file " + nameFun);
    JMetalLogger.logger.info("Variables values have been written to file " + nameVar);
  }


  /**
   * Print all the available quality indicators
   *
   * @param population
   * @param paretoFrontFile
   * @throws FileNotFoundException
   */
  @Deprecated
  public static <S extends Solution<?>> void printQualityIndicators(
      List<S> population, String paretoFrontFile) {

    try {
      QualityIndicatorUtils.printQualityIndicators(
          getMatrixWithObjectiveValues(population), VectorUtils.readVectors(paretoFrontFile, ","));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
