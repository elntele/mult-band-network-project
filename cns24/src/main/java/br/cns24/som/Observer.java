package br.cns24.som;

import java.util.List;


/**
 * @author Danilo
 *
 */
public interface Observer {
	public void atualizarResultados(List<PadraoTreinamento> padroes, double progresso);
}
