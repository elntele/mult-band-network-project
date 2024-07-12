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
    private List<GmlEdge> set = new ArrayList<>();

    public EdgeSet() {
    }

    public List<GmlEdge> getSet() {
        return set;
    }

    public void setSet(List<GmlEdge> set) {
        this.set = set;
    }
}
