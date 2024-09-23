package br.cns24.model;

public record MutationAttributes(
    int indexOriginNode,
    int indexDestineNode,
    int wssOriginNode,
    int wssDestineNode,
    int compare,
    int nodePartBegin
) {
}
