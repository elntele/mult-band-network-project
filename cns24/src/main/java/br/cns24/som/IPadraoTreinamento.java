/**
 * 
 */
package br.cns24.som;

import java.util.List;

/**
 * .
 * 
 * @author Danilo Ara�jo
 */
public class IPadraoTreinamento implements Comparable<IPadraoTreinamento> {
	private String rotulo;

	private double posX;

	private double posY;

	private List<Integer> caracteristicas;

	public IPadraoTreinamento() {
		super();
	}

	@Override
	public String toString() {
		return rotulo;
	}

	public IPadraoTreinamento(String rotulo, List<Integer> caracteristicas) {
		super();
		this.rotulo = rotulo;
		this.caracteristicas = caracteristicas;
	}

	/**
	 * M�todo acessor para obter o valor de rotulo
	 * 
	 * @return O valor de rotulo
	 */
	public String getRotulo() {
		return rotulo;
	}

	/**
	 * M�todo acessor para modificar o valor de rotulo
	 * 
	 * @param rotulo
	 *            O novo valor de rotulo
	 */
	public void setRotulo(String rotulo) {
		this.rotulo = rotulo;
	}

	/**
	 * M�todo acessor para obter o valor de caracteristicas
	 * 
	 * @return O valor de caracteristicas
	 */
	public List<Integer> getCaracteristicas() {
		return caracteristicas;
	}

	public Integer[] getCaracteristicasArray() {
		return (Integer[]) caracteristicas.toArray(new Integer[0]);
	}

	/**
	 * M�todo acessor para modificar o valor de caracteristicas
	 * 
	 * @param caracteristicas
	 *            O novo valor de caracteristicas
	 */
	public void setCaracteristicas(List<Integer> caracteristicas) {
		this.caracteristicas = caracteristicas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rotulo == null) ? 0 : rotulo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IPadraoTreinamento other = (IPadraoTreinamento) obj;
		if (rotulo == null) {
			if (other.rotulo != null)
				return false;
		} else if (!rotulo.equals(other.rotulo))
			return false;
		return true;
	}

	/**
	 * M�todo acessor para obter o valor de posX
	 * 
	 * @return O valor de posX
	 */
	public double getPosX() {
		return posX;
	}

	/**
	 * M�todo acessor para modificar o valor de posX
	 * 
	 * @param posX
	 *            O novo valor de posX
	 */
	public void setPosX(double posX) {
		this.posX = posX;
	}

	/**
	 * M�todo acessor para obter o valor de posY
	 * 
	 * @return O valor de posY
	 */
	public double getPosY() {
		return posY;
	}

	/**
	 * M�todo acessor para modificar o valor de posY
	 * 
	 * @param posY
	 *            O novo valor de posY
	 */
	public void setPosY(double posY) {
		this.posY = posY;
	}

	@Override
	public int compareTo(IPadraoTreinamento padrao) {
		return this.getRotulo().compareTo(padrao.getRotulo());
	}
}
