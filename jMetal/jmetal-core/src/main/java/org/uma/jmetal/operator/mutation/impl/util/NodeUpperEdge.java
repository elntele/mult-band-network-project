package org.uma.jmetal.operator.mutation.impl.util;

public class NodeUpperEdge {

  private int node;
  private int upperEdge;
  private boolean visited;

  public NodeUpperEdge(int node) {
    this.node = node;
  }

  public int getNode() {
    return node;
  }

  public void setNode(int node) {
    this.node = node;
  }

  public int getUpperEdge() {
    return upperEdge;
  }

  public void setUpperEdge(int upperEdge) {
    this.upperEdge = upperEdge;
  }

  public boolean isVisited() {
    return visited;
  }

  public void setVisited(boolean visited) {
    this.visited = visited;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    NodeUpperEdge that = (NodeUpperEdge) obj;
    return this.node == that.node;
  }
}
