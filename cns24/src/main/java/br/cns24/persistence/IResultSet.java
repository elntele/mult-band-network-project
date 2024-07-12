package br.cns24.persistence;

import java.io.IOException;

import br.cns24.experiments.ResultSet;

public interface IResultSet {
	/**
	 * Salva o objeto passado em um arquivo xml
	 * 
	 * @param xml
	 *            Path do arquivo xml a ser salvo
	 * @param object
	 *            Objeto referente � rede complexa
	 * @throws IOException
	 *             Caso ocorra algum erro no processo de serializa��o
	 */
	public void save(String xml, ResultSet object) throws IOException;

	/**
	 * Carrega um objeto da classe especificada.
	 * 
	 * @param xml
	 *            Path do arquivo xml a ser salvo
	 * @return Objeto correspondente ao XML.
	 * @throws IOException
	 *             Caso ocorra algum erro no processo de deserializa��o
	 */
	public ResultSet load(String xml) throws IOException;
}
