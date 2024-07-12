package br.cns24.experiments.setup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SomSetup {
	private int taxaAprendizado;
	
	private int raioVizinhanca;
	
	private int numIteracoes;
	
	private int tamanhoGrid;
	
	public SomSetup(){
		this.taxaAprendizado = 50;
		this.raioVizinhanca = 50;
		this.numIteracoes = 500;
		this.tamanhoGrid = 200;
	}

	public SomSetup(int taxaAprendizado, int raioVizinhanca,
			int numIteracoes, int tamanhoGrid) {
		super();
		this.taxaAprendizado = taxaAprendizado;
		this.raioVizinhanca = raioVizinhanca;
		this.numIteracoes = numIteracoes;
		this.tamanhoGrid = tamanhoGrid;
	}

	public int getTaxaAprendizado() {
		return taxaAprendizado;
	}

	@XmlElement
	public void setTaxaAprendizado(int taxaAprendizado) {
		this.taxaAprendizado = taxaAprendizado;
	}

	public int getRaioVizinhanca() {
		return raioVizinhanca;
	}

	@XmlElement
	public void setRaioVizinhanca(int raioVizinhanca) {
		this.raioVizinhanca = raioVizinhanca;
	}

	public int getNumIteracoes() {
		return numIteracoes;
	}

	@XmlElement
	public void setNumIteracoes(int numIteracoes) {
		this.numIteracoes = numIteracoes;
	}

	public int getTamanhoGrid() {
		return tamanhoGrid;
	}

	@XmlElement
	public void setTamanhoGrid(int tamanhoGrid) {
		this.tamanhoGrid = tamanhoGrid;
	}
}
