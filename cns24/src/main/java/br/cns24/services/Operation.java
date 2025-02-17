package br.cns24.services;

public enum Operation {
  DECREASE(-1),
  REPLACEMENT(0),
  INCREASE(1);

  Operation(Integer action) {
  }

  /**
   * returns true if it don't cause
   * a reinforcement to constraint.
   * is good if returns true.
   *
   * @param destineNodeDegree
   * @param graphDensity
   * @param operation
   * @param numNode
   */
  public static boolean notCauseADegreeFarAway(int destineNodeDegree, Double graphDensity, Operation operation,
      int numNode) {
    var nodeDegreeTarget = graphDensity * (numNode - 1);
    switch (operation) {
      case DECREASE:
        return destineNodeDegree > nodeDegreeTarget;
      case REPLACEMENT:
        return true;
      case INCREASE:
        return destineNodeDegree < nodeDegreeTarget;
    }
    return false;
  }

  /**
   * return an operation
   * decrease; replacement;
   * increase accord to old
   * and new link
   *
   * @param oldLink
   * @param newLink
   */

  public static Operation getOpeOperation(int oldLink, int newLink) {
    if (oldLink > 0 && newLink == 0) return DECREASE;
    if (oldLink == 0 && newLink == 0) return REPLACEMENT;
    if (oldLink > 0 && newLink > 0) return REPLACEMENT;
    if (oldLink == 0 && newLink > 0) return INCREASE;
    return REPLACEMENT;
  }

}
