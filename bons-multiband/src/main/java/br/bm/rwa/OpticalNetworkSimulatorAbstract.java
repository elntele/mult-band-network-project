package br.bm.rwa;

import static java.lang.Math.pow;

import br.bm.core.CallScheduler;
import br.bm.core.CallSchedulerUniform;
import br.bm.core.NetworkProfile;

public abstract class OpticalNetworkSimulatorAbstract {
	public static final int BLOQ_WAVELENGTH = -1;
	public static final int BLOQ_BER = -2;
	public static final int BLOQ_DISPERSION = -3;

	public static final int FIBRAS = 1;
	public static final double MAX_TIME = 1e5;

//	public static final double SNR_BLOCK = 19.8;
	
	public static final double SNR_BLOCK = 17.0;


	public static final double SNR_THRESHOLD = pow(10, SNR_BLOCK / 10);

	// TODO
	public static final double MEAN_RATE_CALLS_DUR = 0.01;

	public static final double MEAN_RATE_CALLS_DUR_PARETO = 0.01;

	public static final double LARGURA_LINHA = 0.013e-9;

	public static final double TAXA_BITS = 10e9;

	public static final double GAMA = 1;

	public abstract void simulate(NetworkProfile network, CallScheduler callScheduler, int minCalls);

	public void simulate(NetworkProfile network, CallScheduler callScheduler) {
		simulate(network, callScheduler, 500);
	}
	
	public void simulate(NetworkProfile network) {
		simulate(network, 500);
	}
	
	public void simulate(NetworkProfile network, int numMinCalls) {
		simulate(network, new CallSchedulerUniform(network.getMeanRateBetweenCalls(),
				OpticalNetworkSimulatorAbstract.MEAN_RATE_CALLS_DUR, network.getNodes().size()), numMinCalls);
	}
}
