package br.bm.model.robustness;

import br.bm.core.INetwork;
import br.bm.core.NetworkProfile;
import br.bm.model.INetworkEvaluator;
import br.bm.model.NumericalResult;
import br.bm.model.TNetworkIndicator;
import br.cns24.TMetric;

public class AlgebraicConnectivityEvaluator implements INetworkEvaluator<INetwork, NumericalResult> {

	@Override
	public NumericalResult evaluate(INetwork network) {
		return new NumericalResult(TNetworkIndicator.ALGEBRAIC_CONNECTIVITY,
				((NetworkProfile) network).getCn().getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY));
	}

}
