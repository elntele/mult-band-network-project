package br.cns24.services;

public enum Operation {
  DECREASE(-1),
  REPLACEMENT(0),
  INCREASE(1);

  Operation(Integer action) {
  }

  public static boolean CauseADegreeFarAway(int destineNodeDegree, Double graphDensity, Operation operation,
      int numNode) {
    var nodeDegreeTarget = graphDensity * (numNode - 1);
    switch (operation) {
      case DECREASE -> {
        if (destineNodeDegree < nodeDegreeTarget) {
          return true;
        }
      }
      case REPLACEMENT -> {
        return false;
      }
      case INCREASE -> {
        if (destineNodeDegree > nodeDegreeTarget) {
          return true;
        }
      }
    }
    return false;
  }

  public static Operation getOpeOperation(int oldLink, int newLink){
    if(oldLink>0 && newLink==0) return DECREASE;
    if(oldLink==0 && newLink==0) return REPLACEMENT;
    if(oldLink>0 && newLink>0) return REPLACEMENT;
    if(oldLink==0 && newLink>0) return INCREASE;
    return REPLACEMENT;
  }

}
