package org.uma.jmetal.operator.selection.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.DominanceWithConstraintsComparator;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

/**
 * This class implements a selection for selecting a number of solutions from
 * a solution list. The solutions are taken by mean of its ranking and
 * crowding distance values.
 *
 * @author Antonio J. Nebro, Juan J. Durillo
 */
@SuppressWarnings("serial")
public class RankingAndCrowdingSelection<S extends Solution<?>>
    implements SelectionOperator<List<S>,List<S>> {
  private final int solutionsToSelect ;
  private Comparator<S> dominanceComparator ;
  private List<ArrayList<S>> rankedSubPopulations;

  /** Constructor */
  public RankingAndCrowdingSelection(int solutionsToSelect, Comparator<S> dominanceComparator) {
    this.dominanceComparator = dominanceComparator ;
    this.solutionsToSelect = solutionsToSelect ;
  }

  /** Constructor */
  public RankingAndCrowdingSelection(int solutionsToSelect) {
    this(solutionsToSelect, new DominanceWithConstraintsComparator<S>()) ;
  }

  /* Getter */
  public int numberOfSolutionsToSelect() {
    return solutionsToSelect;
  }

  /** Execute() method */
  public List<S> execute(List<S> solutionList) throws JMetalException {
    Check.notNull(solutionList);
    Check.collectionIsNotEmpty(solutionList);
    Check.that(solutionList.size() >= solutionsToSelect, "The population size ("+solutionList.size()+") is smaller than" +
        "the solutions to selected ("+solutionsToSelect+")");

    Ranking<S> ranking = new FastNonDominatedSortRanking<>(dominanceComparator);
    ranking.compute(solutionList) ;

    return crowdingDistanceSelection(ranking);
  }

  protected List<S> crowdingDistanceSelection(Ranking<S> ranking) {
    CrowdingDistanceDensityEstimator<S> crowdingDistance = new CrowdingDistanceDensityEstimator<>() ;
    List<S> population = new ArrayList<>(solutionsToSelect) ;
    int rankingIndex = 0;
    while (population.size() < solutionsToSelect) {
      if (subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
        crowdingDistance.compute(ranking.getSubFront(rankingIndex));
        addRankedSolutionsToPopulation(ranking, rankingIndex, population);
        rankingIndex++;
      } else {
        crowdingDistance.compute(ranking.getSubFront(rankingIndex));
        addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
      }
    }

    if (ranking instanceof FastNonDominatedSortRanking) {
      FastNonDominatedSortRanking<S> fastRanking = (FastNonDominatedSortRanking<S>) ranking;
      this.rankedSubPopulations = fastRanking.getRankedSubPopulations();
    } else {
      System.err.println(" in RankingAndCrowdingSelection class, crowdingDistanceSelectionm ethod" +
          " Ranking isn't a FastNonDominatedSortRanking instance");
    }


    return population ;
  }

  protected boolean subfrontFillsIntoThePopulation(Ranking<S> ranking, int rank, List<S> population) {
    return ranking.getSubFront(rank).size() < (solutionsToSelect - population.size()) ;
  }

  protected void addRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
    List<S> front ;

    front = ranking.getSubFront(rank);

    population.addAll(front);
  }

  protected void addLastRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S>population) {
    List<S> currentRankedFront = ranking.getSubFront(rank) ;

    currentRankedFront.sort(new CrowdingDistanceDensityEstimator<>().comparator());

    int i = 0 ;
    while (population.size() < solutionsToSelect) {
      population.add(currentRankedFront.get(i)) ;
      i++ ;
    }
  }

  public List<ArrayList<S>> getRankedSubPopulations() {
    return rankedSubPopulations;
  }
}
