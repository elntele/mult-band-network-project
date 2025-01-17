package br.cns24.services;

import java.util.ArrayList;
import java.util.Arrays;
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
      Map<Integer, Set<Integer>> file, int setSize) {
    System.out.println("Constraints: " + constraintOne + ", " + constraintTwo);
    int index = 0;
    var beginNodePart = solution.size() - (numNodes + 1);
    System.out.println("comutadores");
    for (int i = beginNodePart; i < solution.size() - 1; i++) {
      System.out.print(" " + solution.get(i) + " ");
    }
    System.out.println("\n");
    System.out.println("file: " + file);
    System.out.println("\n");
    // Espaço para alinhar com os rótulos das linhas
    System.out.print("  ");
    // Imprime os cabeçalhos das colunas (nós)
    for (int i = 0; i < numNodes; i++) {
      if (i < 10) {
        System.out.printf("   %d ", i);
      } else {
        System.out.printf("  %d ", i);
      }
    }
    System.out.println(); // Quebra de linha após os cabeçalhos

    // Percorre a lista e imprime os valores como uma matriz
    for (int i = 0; i < numNodes; i++) {
      // Imprime o rótulo da linha
      if (i < 10) {
        System.out.printf("%d  ", i);
      } else {
        System.out.printf("%d ", i);
      }

      for (int j = 0; j < numNodes; j++) {
        if (i == j) {
          System.out.print("  x  "); // Diagonal principal (sem conexão)
        } else if (j > i) {
          // Imprime três valores em sequência da lista
          for (int w = 0; w < setSize; w++) {
            var alelo = solution.get(index + w);
            if (alelo.toString().length() > 1) {
              System.out.printf("(%d) ", alelo);
            } else {
              System.out.printf("( %d) ", alelo);
            }
          }
          index += setSize;
        } else {
          // Espaço para elementos abaixo da diagonal
          System.out.print("     ");
        }
      }
      System.out.println(); // Quebra de linha após cada linha da matriz
    }
    System.out.println("\n\n");
  }


  public static void printMatrixFull(List<Integer> solution, int numNodes, String constraintOne, String constraintTwo,
      List<Integer> lI, List<Integer> lJ, int setSize) {
    List<Integer> iS = new ArrayList<>(lI);
    List<Integer> jS = new ArrayList<>(lJ);
    System.out.println("Constraints: " + constraintOne + ", " + constraintTwo);
    int index = 0;
    var beginNodePart = solution.size() - (numNodes + 1);
    System.out.println(
        "Branco: não tem restrição e não foi alterado por operador; Verde: foi alterado por operador; " +
            "Vermelho: tem restrição; Amarelo: tem restrição e foi alterado por operador");
    System.out.println("N: Número do nó; R: Tipo de ROADMs");

    // Imprime N mais seta pra direita
    System.out.print("  N  \u27F6");
    // imprime o número dos nós
    for (int i = 0; i < numNodes; i++) {
      if (i == 0) {
        System.out.printf("   %d  ", i);
      } else {
        if (i < 10) {
          System.out.printf("  %d  ", i);
        } else {
          System.out.printf(" %d  ", i);
        }
      }
    }
    //quebra de linha
    System.out.print("\n");
    // imprime seta para baixo
    System.out.print("  \u2193");
    // imprime R mais seta para baixo mais seta pra direita
    System.out.print(" R\u2193 \u2192");
    // imprime os ROADMs
    for (int i = 0; i < numNodes; i++) {
      if (i == 0) {
        System.out.printf(" %d    ", solution.get(beginNodePart + i));
      } else {
        System.out.printf("%d    ", solution.get(beginNodePart + i));
      }
    }
    // Quebra de linha após os cabeçalhos
    System.out.println();
    // Percorre a lista e imprime os valores como uma matriz
    for (int i = 0; i < numNodes; i++) {
      // Imprime o rótulo da linha
      if (i < 10) {
        System.out.printf("  %d %d   ", i, solution.get(beginNodePart + i));
      } else {
        System.out.printf(" %d %d   ", i, solution.get(beginNodePart + i));
      }

      for (int j = 0; j < numNodes; j++) {
        if (i == j) {
          System.out.print(" x  "); // Diagonal principal (sem conexão)
        } else if (j > i) {
          // Imprime três valores em sequência da lista
          var set = "";
          for (int w = 0; w < setSize; w++) {
            set += solution.get(index + w).toString();
          }
          if (iS.contains(i) && jS.contains(j)) {
            if (set.length() > 1) {
              if (hasConstraint(solution.get(beginNodePart + i), solution.get(beginNodePart + j), set)) {
                System.out.printf("\033[0;33m" + "(" + set + ") " + "\033[0m");
              } else {
                System.out.printf("\033[0;32m" + "(" + set + ") " + "\033[0m");
              }
            } else {
              if (hasConstraint(solution.get(beginNodePart + i), solution.get(beginNodePart + j), set)) {
                System.out.printf("\033[0;33m" + "( " + set + ") " + "\033[0m");
              } else {
                System.out.printf("\033[0;32m" + "( " + set + ") " + "\033[0m");
              }

            }

            iS.remove(Integer.valueOf(i));
            jS.remove(Integer.valueOf(j));
          } else {
            if (set.length() > 1) {
              if (hasConstraint(solution.get(beginNodePart + i), solution.get(beginNodePart + j), set)) {
                System.out.printf("\033[0;31m" + "(" + set + ") " + "\033[0m");
              } else {
                System.out.printf("(" + set + ") ");
              }

            } else {
              if (hasConstraint(solution.get(beginNodePart + i), solution.get(beginNodePart + j), set)) {
                System.out.printf("\033[0;31m" + "( " + set + ") " + "\033[0m");
              } else {
                System.out.printf("( " + set + ") ");
              }

            }
          }
          index += setSize;
        } else {
          // Espaço para elementos abaixo da diagonal
          System.out.print("     ");
        }

      }

      System.out.println(); // Quebra de linha após cada linha da matriz
    }
    System.out.println("\n\n");
  }


  private static boolean hasConstraint(int i, int j, String s) {

    if (!LevelNode.thisNodeAddressThisLink(i, Integer.parseInt(s))){
        return true;
    }
    if (!LevelNode.thisNodeAddressThisLink(j, Integer.parseInt(s))){
      return true;
    }
    return false;
  }

}
