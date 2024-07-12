package br.cns24.experiments;

import br.cns24.metrics.AlgebraicConnectivity;
import br.cns24.metrics.DoubleFailureImpact;
import br.cns24.metrics.SingleFailureImpact;
import br.cns24.metrics.ZeroReturnCount;
import br.cns24.metrics.ZeroReturnCountFilter;
import br.cns24.models.Barabasi;
import br.cns24.models.BarabasiDensity;
import br.cns24.models.ErdosRenyiM;
import br.cns24.models.ErdosRenyiP;

public class FailureConsoleExperiment {

	private static void testRings() {
		Integer[][] net = null;
		int minSize = 20;
		int maxSize = 200;
		int passo = 10;

		System.out.println("Redes em Anel");
		printHeader();
		for (int size = minSize; size <= maxSize; size += passo) {
			net = new Integer[size][size];
			for (int i = 0; i < net.length; i++) {
				if (i == 0) {
					net[i][net.length - 1] = 1;
				} else {
					net[i][i - 1] = 1;
				}
				if (i == net.length - 1) {
					net[i][0] = 1;
				} else {
					net[i][i + 1] = 1;
				}
			}
			showLine(net, size);
		}
	}

	private static void testStar() {
		Integer[][] net = null;
		int minSize = 20;
		int maxSize = 200;
		int passo = 10;

		System.out.println("Redes Estrela");
		printHeader();

		for (int size = minSize; size <= maxSize; size += passo) {
			net = new Integer[size][size];
			for (int i = 0; i < net.length; i++) {
				net[0][i] = 1;
				net[i][0] = 1;
			}
			showLine(net, size);
		}
	}

	private static void testBarabasiFixedDensity(double density) {
		Integer[][] net = null;
		int minSize = 20;
		int maxSize = 200;
		int passo = 10;
		BarabasiDensity b = null;
		System.out.printf("Redes Barabasi com densidade %.2f\n", density);
		printHeader();

		for (int size = minSize; size <= maxSize; size += passo) {
			b = new BarabasiDensity(density);
			net = b.grow(null, size);
			showLine(net, size);
		}
	}

	private static void testErdosVarDensity() {
		Integer[][] net = null;
		int minSize = 20;
		int maxSize = 200;
		int passo = 10;
		ErdosRenyiM b = null;
		System.out.printf("Redes Erdos com densidade 1/(n-1)\n");
		printHeader();

		for (int size = minSize; size <= maxSize; size += passo) {
			b = new ErdosRenyiM(1.0 / (size - 1), size);
			net = new Integer[size][size];
			net = b.transform(net);
			showLine(net, size);
		}
	}

	private static void testErdosDensity(double density) {
		Integer[][] net = null;
		int minSize = 20;
		int maxSize = 200;
		int passo = 10;
		ErdosRenyiM b = null;
		System.out.printf("Redes Erdos com densidade 1/(n-1)\n");
		printHeader();

		for (int size = minSize; size <= maxSize; size += passo) {
			b = new ErdosRenyiM(density, size);
			net = new Integer[size][size];
			net = b.transform(net);
			showLine(net, size);
		}
	}

	private static void testBarabasiVarDensity() {
		Integer[][] net = null;
		int minSize = 20;
		int maxSize = 200;
		int passo = 10;
		BarabasiDensity b = null;
		System.out.printf("Redes Barabasi com densidade 1/(n-1)\n");
		printHeader();

		for (int size = minSize; size <= maxSize; size += passo) {
			b = new BarabasiDensity(1.0 / (size - 1));
			net = b.grow(null, size);
			showLine(net, size);
		}
	}

	private static void printHeader() {
		System.out.println("ID\tSFI\tDFI\tZRC\tZRCF\tAC\tTempo (s)");
	}

	private static void showLine(Integer[][] net, int idRede) {
		long time = System.currentTimeMillis();
		SingleFailureImpact sfi = SingleFailureImpact.getInstance();
		DoubleFailureImpact dfi = DoubleFailureImpact.getInstance();
		ZeroReturnCount zrc = ZeroReturnCount.getInstance();
		ZeroReturnCountFilter zrcf = ZeroReturnCountFilter.getInstance();
		AlgebraicConnectivity ac = AlgebraicConnectivity.getInstance();
		System.out.printf("%d\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%d\n", idRede, sfi.calculate(net), dfi.calculate(net),
				zrc.calculate(net), zrcf.calculate(net), ac.calculate(net), (System.currentTimeMillis() - time) / 1000);
	}

	public static void main(String[] args) {
		// testRings();
		// testStar();
		testBarabasiFixedDensity(0.03, 100);
		testErdosDensity(0.03, 100);
		// testComplexNetworks();
	}

	private static void testBarabasiFixedDensity(double density, int size) {
		Integer[][] net = null;
		BarabasiDensity b = null;
		System.out.printf("Redes Barabasi com densidade %.2f\n", density);
		printHeader();

		for (int i = 0; i < 30; i++) {
			b = new BarabasiDensity(density);
			net = b.grow(null, size);
			// System.out.println(ArrayUtils.getIntegerinstance().print(ArrayUtils.getIntegerinstance().box(net)));
			showLine(net, i);
		}
	}

	private static void testErdosDensity(double density, int size) {
		Integer[][] net = null;
		ErdosRenyiM b = null;
		System.out.printf("Redes Erdos com densidade 1/(n-1)\n");
		printHeader();

		for (int i = 0; i < 30; i++) {
			b = new ErdosRenyiM(density, size);
			net = new Integer[size][size];
			net = b.transform(net);
			// System.out.println(ArrayUtils.getIntegerinstance().print(ArrayUtils.getIntegerinstance().box(net)));
			showLine(net, i);
		}
	}

	private static void testComplexNetworks() {
		int size = 200;
		int numNets = 10;
		Integer[][] adjacencyMatrix = null;
		SingleFailureImpact sfi = SingleFailureImpact.getInstance();
		DoubleFailureImpact dfi = DoubleFailureImpact.getInstance();
		ZeroReturnCount zrc = ZeroReturnCount.getInstance();
		ZeroReturnCountFilter zrcf = ZeroReturnCountFilter.getInstance();
		Barabasi barabasi = new Barabasi(size);
		// NewmanWatts other = new NewmanWatts(0.2);
		ErdosRenyiP other = new ErdosRenyiP(0.2);

		System.out.println("Redes Barabási");
		printHeader();
		// System.out.println("ID\tZRC\tZRCF");
		int sizeBarabasi = size / numNets;
		for (int i = 0; i < numNets; i++) {
			barabasi = new Barabasi(sizeBarabasi);
			sizeBarabasi += size / numNets;
			adjacencyMatrix = barabasi.grow(null, size);
			System.out.printf("%d\t%.2f\t%.2f\t%.2f\t%.2f\n", i + 1, sfi.calculate(adjacencyMatrix),
					dfi.calculate(adjacencyMatrix), zrc.calculate(adjacencyMatrix), zrcf.calculate(adjacencyMatrix));
			// System.out.printf("%d\t%.2f\t%.2f\n", i + 1,
			// zrc.calculate(adjacencyMatrix), zrcf.calculate(adjacencyMatrix));
		}

		System.out.println("Redes Erdos-Renyi");
		printHeader();
		// System.out.println("ID\tZRC\tZRCF");
		for (int i = 0; i < numNets; i++) {
			adjacencyMatrix = other.transform(adjacencyMatrix);
			// System.out.printf("%d\t%.2f\t%.2f\n", i + 1,
			// zrc.calculate(adjacencyMatrix), zrcf.calculate(adjacencyMatrix));
			System.out.printf("%d\t%.2f\t%.2f\t%.2f\t%.2f\n", i + 1, sfi.calculate(adjacencyMatrix),
					dfi.calculate(adjacencyMatrix), zrc.calculate(adjacencyMatrix), zrcf.calculate(adjacencyMatrix));
		}
	}
}
