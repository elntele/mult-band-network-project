package org.uma.jmetal.operator.mutation.impl.util;

public class NodeUpperEdge {

  private int node;
  private int majorEdge;
  private boolean visited;

  public NodeUpperEdge(int node) {
    this.node = node;
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
