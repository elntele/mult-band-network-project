package br.cns24.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.DocFlavor;

public class PrintPopulation {
  // Método estático que imprime uma lista de cromossomos 1 a 1
  public static void print(List<List<Integer>> population, int numNodes) {
    for (int chromIndex = 0; chromIndex < population.size(); chromIndex++) {
      List<Integer> chromosome = population.get(chromIndex);
      System.out.println("Cromossomo " + (chromIndex + 1) + ":");
      printChromosomeMatrix(chromosome, numNodes);
      System.out.println();
    }
  }

  // Método estático auxiliar para imprimir o cromossomo como uma matriz de conexão
  private static void printChromosomeMatrix(List<Integer> chromosome, int numNodes) {
    int index = 0;

    // Imprime os cabeçalhos das colunas (nós)
    System.out.print("     "); // Espaço para alinhar com os rótulos das linhas
    for (int i = 1; i <= numNodes; i++) {
      System.out.printf("  %d   ", i);
    }
    System.out.println(); // Quebra de linha após os cabeçalhos

    // Percorre a matriz de conexão (cromossomo) com rótulos de linhas
    for (int i = 0; i < numNodes; i++) {
      // Imprime o rótulo da linha
      System.out.printf("%d  ", i + 1);

      for (int j = 0; j < numNodes; j++) {
        if (i == j) {
          System.out.print("  x  "); // Diagonal principal (sem conexão)
        } else if (j > i) {
          // Imprime os três valores do cromossomo em sequência
          System.out.printf("(%d%d%d) ", chromosome.get(index), chromosome.get(index + 1), chromosome.get(index + 2));
          index += 3;
        } else {
          System.out.print("      "); // Espaço para elementos abaixo da diagonal
        }
      }
      System.out.println(); // Quebra de linha após cada linha da matriz
    }
  }

  public static void printMatrix(List<Integer> solution, int numNodes, String constraintOne, String constraintTwo,
      String constraintTree, Map<Integer, Set<Integer>> file) {
    System.out.println("Constraints: " + constraintOne + ", " + constraintTwo + ", " + constraintTree);
    int index = 0;
    var beginNodePart = solution.size() - (numNodes + 1);
    System.out.println("comutadores");
    for (int i = beginNodePart; i < solution.size() - 1; i++) {
      System.out.print(" " + solution.get(i) + " ");
    }
    System.out.println("\n");
    System.out.println("file: "+file);
    System.out.println("\n");
    // Imprime os cabeçalhos das colunas (nós)
    System.out.print("  "); // Espaço para alinhar com os rótulos das linhas
    for (int i = 0; i < numNodes; i++) {
      System.out.printf("  %d   ", i);
    }
    System.out.println(); // Quebra de linha após os cabeçalhos

    // Percorre a lista e imprime os valores como uma matriz
    for (int i = 0; i < numNodes; i++) {
      // Imprime o rótulo da linha
      System.out.printf("%d  ", i);

      for (int j = 0; j < numNodes; j++) {
        if (i == j) {
          System.out.print("  x  "); // Diagonal principal (sem conexão)
        } else if (j > i) {
          // Imprime três valores em sequência da lista
          System.out.printf("(%d%d%d) ", solution.get(index), solution.get(index + 1), solution.get(index + 2));
          index += 3;
        } else {
          System.out.print("      "); // Espaço para elementos abaixo da diagonal
        }
      }
      System.out.println(); // Quebra de linha após cada linha da matriz
    }
    System.out.println("\n\n");
  }

}
