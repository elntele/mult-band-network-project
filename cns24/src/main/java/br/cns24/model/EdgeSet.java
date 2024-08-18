package br.cns24.model;

import java.util.ArrayList;
import java.util.List;

/**
 * by Jorge Candeias.
 * Its is set concept what is
 * used to hold a set of fibers
 * or edges.
 */

public class EdgeSet {
    private List<GmlEdge> edges = new ArrayList<>();

    public EdgeSet() {
    }

    public List<GmlEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<GmlEdge> edges) {
        this.edges = edges;
    }
}
