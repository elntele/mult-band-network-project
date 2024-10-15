package br.bm.model.performance;

import br.bm.core.CallSchedulerNonUniformHub;
import br.bm.core.INetwork;
import br.bm.core.MultiBandNetWorkProfile;
import br.bm.core.NetworkProfile;
import br.bm.model.INetworkEvaluator;
import br.bm.model.NumericalResult;
import br.bm.model.TNetworkIndicator;
import br.bm.rwa.MultiBandDijkstraSimulator;
import br.bm.rwa.SimpleDijkstraSimulator;

public class BlockingProbabilityEstimator implements INetworkEvaluator<INetwork, NumericalResult> {
	private static final double MIN_BP = 0.000001;

	//private SimpleDijkstraSimulator simulator;
	private MultiBandDijkstraSimulator simulator;

	private int networkLoad;

	public BlockingProbabilityEstimator() {
		this(100);
	}

	public BlockingProbabilityEstimator(int networkLoad) {
		this.networkLoad = networkLoad;
		simulator = new MultiBandDijkstraSimulator();
	}

	@Override
	public NumericalResult evaluate(INetwork net) {
	//	NetworkProfile profile = (NetworkProfile) net;
		MultiBandNetWorkProfile profile = (MultiBandNetWorkProfile) net;

		simulator.simulateMultiBand(profile, new CallSchedulerNonUniformHub(profile.getNodes().size(), networkLoad), 1000);

		return new NumericalResult(TNetworkIndicator.BLOCKING_PROBABILITY,
				profile.getBp().getTotal() < MIN_BP ? MIN_BP : profile.getBp().getTotal());
	}

}
