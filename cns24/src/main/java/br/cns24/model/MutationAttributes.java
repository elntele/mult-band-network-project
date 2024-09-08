package br.cns24.model;

public record MutationAttributes(
    int indexOriginNode,
    int indexDestineNode,
    int originNode,
    int destineNode,
    int compare,
    int nodePartBegin
) {
}
