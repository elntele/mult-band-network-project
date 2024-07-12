package br.bm.core;



import java.util.List;
import java.util.Vector;

import static br.bm.core.SimonUtil.getRandomExp;

/**
 * @author drba
 * 
 */
public class Call {
	
	public static final int BIDIRECIONAL = 1000;
	public static final int UNIDIRECIONAL_REGENERADA = 2000;
	public static final int UNIDIRECIONAL = 3000;

	private int wavelengthUp;
	private int wavelengthDown;
	private int source;
	private int destination;
	private List<Fiber> fibersUpLink = new Vector<Fiber>();
	private List<Fiber> fibersDownLink = new Vector<Fiber>();
	private double fallTime;
	private double duration;
	private int callType;
	
	public Call() {
		super();
		callType = BIDIRECIONAL;
		source = -1;
		destination = -1;
	}

	public Call(int wavelengthUp, int wavelengthDown, int source, int destination, List<Fiber> fibersUpLink,
			List<Fiber> fibersDownLink, double fallTime, double duration, int callType) {
		super();
		this.wavelengthUp = wavelengthUp;
		this.wavelengthDown = wavelengthDown;
		this.source = source;
		this.destination = destination;
		this.fibersUpLink = fibersUpLink;
		this.fibersDownLink = fibersDownLink;
		this.fallTime = fallTime;
		this.duration = duration;
		this.callType = callType;
	}

	public void setup(int source, int destination, double fallTime, double duration, int callType) {
		this.source = source;
		this.destination = destination;
		this.fallTime = fallTime;
		this.duration = duration;
		this.callType = callType;
	}

	public double getLinkLength() {
		double comp = 0;
		// sets the fiber used in this call
		for (int i_loc = 0; i_loc < fibersUpLink.size(); i_loc++) {
			comp += fibersUpLink.get(i_loc).getLength();
		}
		return comp;

	}

	public void setTime(double time, double meanRateOfCallDuration) {
		fallTime = time + getRandomExp(meanRateOfCallDuration);
		duration = fallTime - time;
		System.out.println("fallTime = "  + fallTime);
		System.out.println("duration = "  + duration);
	}

	public void alloc(Vector<Node> listOfNodes_par) {

		// sets the fiber used in this call
		for (int i_loc = 0; i_loc < fibersUpLink.size(); i_loc++) {
			fibersUpLink.get(i_loc).addTime(duration);
		}

		fibersUpLink.get(0).allocate(wavelengthUp, listOfNodes_par.get(source).getLaserPower());
		for (int i_loc = 1; i_loc < (fibersUpLink.size()); i_loc++) {
			int sourceNode_loc = fibersUpLink.get(i_loc).getSourceNode();
			double potOut_loc = (fibersUpLink.get(i_loc - 1).getPowerF(wavelengthUp))
					* (listOfNodes_par.get(sourceNode_loc).getSwitchAtenuation());
			fibersUpLink.get(i_loc).allocate(wavelengthUp, potOut_loc);
		}
	}

	/**
	 * Stores the fibers used by the call in the Vector
	 * 
	 * @param fibersInUpLinkPath
	 *            a Vector of fibers in up link
	 * @param fibersInDownLinkPath
	 *            a Vector of fibers in down link
	 * @param listOfNodes_par
	 *            Vector of nodes
	 */
	public void alloc(Vector<Fiber> fibersInUpLinkPath, Vector<Fiber> fibersInDownLinkPath, Vector<Node> listOfNodes_par) {
		if (fibersInUpLinkPath.isEmpty()) {
			return;
		}
		fibersUpLink.clear();
		fibersDownLink.clear();
		// sets the fiber used in this call
		for (int i_loc = 0; i_loc < fibersInUpLinkPath.size(); i_loc++) {
			fibersUpLink.add(fibersInUpLinkPath.get(i_loc));
			fibersInUpLinkPath.get(i_loc).addTime(duration);
		}

		fibersUpLink.get(0).allocate(wavelengthUp, listOfNodes_par.get(source).getLaserPower());
		for (int i_loc = 1; i_loc < (fibersUpLink.size()); i_loc++) {
			int sourceNode_loc = fibersUpLink.get(i_loc).getSourceNode();
			double potOut_loc = fibersUpLink.get(i_loc).getPowerF(wavelengthUp)
					* (listOfNodes_par.get(sourceNode_loc).getSwitchAtenuation());
			fibersUpLink.get(i_loc).allocate(wavelengthUp, potOut_loc);

		}
		// Alocar a volta da chamada
		if (callType == BIDIRECIONAL) {
			for (int i_loc = 0; i_loc < fibersInDownLinkPath.size(); i_loc++) {
				// sets the fiber used in this call
				fibersDownLink.add(fibersInDownLinkPath.get(i_loc));
				fibersInDownLinkPath.get(i_loc).addTime(duration);
			}

			fibersDownLink.get(0).allocate(wavelengthUp, listOfNodes_par.get(destination).getLaserPower());

			for (int i_loc = 1; i_loc < (fibersDownLink.size()); i_loc++) {
				int sourceNode_loc = fibersDownLink.get(i_loc).getSourceNode();
				double potOut_loc = (fibersDownLink.get(i_loc - 1).getPowerF(wavelengthUp))
						* (listOfNodes_par.get(sourceNode_loc).getSwitchAtenuation());
				fibersDownLink.get(i_loc).allocate(wavelengthUp, potOut_loc);

			}
		}
	}

	/**
	 * Pull away a fiber in the call
	 * 
	 * @param vectorOfNodes_par
	 */
	void deAllocCall(Vector<Node> vectorOfNodes_par) {
		// up link de alocation
		int i_loc;
		for (i_loc = 0; i_loc < fibersUpLink.size(); i_loc++) {
			fibersUpLink.get(i_loc).deallocate(wavelengthUp);
		}

		if (callType == BIDIRECIONAL)
			// down link de alocation
			for (i_loc = 0; i_loc < fibersDownLink.size(); i_loc++)
				fibersDownLink.get(i_loc).deallocate(wavelengthDown);
		else if (callType == UNIDIRECIONAL_REGENERADA) {
			vectorOfNodes_par.get(source).incNumFreeRegenerators();
		}
	}

	/**
	 * Metodo acessor para obter o valor do atributo wavelengthUp.
	 * 
	 * @return O valor de wavelengthUp
	 */
	public int getWavelengthUp() {
		return wavelengthUp;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo wavelengthUp.
	 * 
	 * @param wavelengthUp
	 *            O novo valor de wavelengthUp
	 */
	public void setWavelengthUp(int wavelengthUp) {
		this.wavelengthUp = wavelengthUp;
	}

	/**
	 * Metodo acessor para obter o valor do atributo wavelengthDown.
	 * 
	 * @return O valor de wavelengthDown
	 */
	public int getWavelengthDown() {
		return wavelengthDown;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo wavelengthDown.
	 * 
	 * @param wavelengthDown
	 *            O novo valor de wavelengthDown
	 */
	public void setWavelengthDown(int wavelengthDown) {
		this.wavelengthDown = wavelengthDown;
	}

	/**
	 * Metodo acessor para obter o valor do atributo source.
	 * 
	 * @return O valor de source
	 */
	public int getSource() {
		return source;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo source.
	 * 
	 * @param source
	 *            O novo valor de source
	 */
	public void setSource(int source) {
		this.source = source;
	}

	/**
	 * Metodo acessor para obter o valor do atributo destination.
	 * 
	 * @return O valor de destination
	 */
	public int getDestination() {
		return destination;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo destination.
	 * 
	 * @param destination
	 *            O novo valor de destination
	 */
	public void setDestination(int destination) {
		this.destination = destination;
	}

	/**
	 * Metodo acessor para obter o valor do atributo fibersUpLink.
	 * 
	 * @return O valor de fibersUpLink
	 */
	public List<Fiber> getFibersUpLink() {
		return fibersUpLink;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo fibersUpLink.
	 * 
	 * @param fibersUpLink
	 *            O novo valor de fibersUpLink
	 */
	public void setFibersUpLink(List<Fiber> fibersUpLink) {
		this.fibersUpLink = fibersUpLink;
	}

	/**
	 * Metodo acessor para obter o valor do atributo fibersDownLink.
	 * 
	 * @return O valor de fibersDownLink
	 */
	public List<Fiber> getFibersDownLink() {
		return fibersDownLink;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo fibersDownLink.
	 * 
	 * @param fibersDownLink
	 *            O novo valor de fibersDownLink
	 */
	public void setFibersDownLink(List<Fiber> fibersDownLink) {
		this.fibersDownLink = fibersDownLink;
	}

	/**
	 * Metodo acessor para obter o valor do atributo fallTime.
	 * 
	 * @return O valor de fallTime
	 */
	public double getFallTime() {
		return fallTime;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo fallTime.
	 * 
	 * @param fallTime
	 *            O novo valor de fallTime
	 */
	public void setFallTime(double fallTime) {
		this.fallTime = fallTime;
	}

	/**
	 * Metodo acessor para obter o valor do atributo duration.
	 * 
	 * @return O valor de duration
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo duration.
	 * 
	 * @param duration
	 *            O novo valor de duration
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * Metodo acessor para obter o valor do atributo callType.
	 * 
	 * @return O valor de callType
	 */
	public int getCallType() {
		return callType;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo callType.
	 * 
	 * @param callType
	 *            O novo valor de callType
	 */
	public void setCallType(int callType) {
		this.callType = callType;
	}

	/**
	 * Metodo acessor para obter o valor do atributo bidirecional.
	 * 
	 * @return O valor de bidirecional
	 */
	public static int getBidirecional() {
		return BIDIRECIONAL;
	}

	/**
	 * Metodo acessor para obter o valor do atributo unidirecionalRegenerada.
	 * 
	 * @return O valor de unidirecionalRegenerada
	 */
	public static int getUnidirecionalRegenerada() {
		return UNIDIRECIONAL_REGENERADA;
	}

	/**
	 * Metodo acessor para obter o valor do atributo unidirecional.
	 * 
	 * @return O valor de unidirecional
	 */
	public static int getUnidirecional() {
		return UNIDIRECIONAL;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + destination;
		result = prime * result + ((fibersDownLink == null) ? 0 : fibersDownLink.hashCode());
		result = prime * result + ((fibersUpLink == null) ? 0 : fibersUpLink.hashCode());
		result = prime * result + source;
		result = prime * result + wavelengthDown;
		result = prime * result + wavelengthUp;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Call other = (Call) obj;
		if (destination != other.destination)
			return false;
		if (fibersDownLink == null) {
			if (other.fibersDownLink != null)
				return false;
		} else if (!fibersDownLink.equals(other.fibersDownLink))
			return false;
		if (fibersUpLink == null) {
			if (other.fibersUpLink != null)
				return false;
		} else if (!fibersUpLink.equals(other.fibersUpLink))
			return false;
		if (source != other.source)
			return false;
		if (wavelengthDown != other.wavelengthDown)
			return false;
		if (wavelengthUp != other.wavelengthUp)
			return false;
		return true;
	}
}
