package br.bm.model;

public interface INetworkEvaluator<N, I> {
	public I evaluate(N network);
}
