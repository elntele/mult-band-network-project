package br.cns24.experiments;

import static br.cns24.models.TModel.BARABASI;
import static br.cns24.models.TModel.BARABASI_DENSITY;
import static br.cns24.models.TModel.CUSTOM;
import static br.cns24.models.TModel.CUSTOM_PROBABILITY;
import static br.cns24.models.TModel.ERDOS_RENYI_N_M;
import static br.cns24.models.TModel.ERDOS_RENYI_N_P;
import static br.cns24.models.TModel.GILBERT;
import static br.cns24.models.TModel.K_REGULAR;
import static br.cns24.models.TModel.NEWMAN_WATTS;
import static br.cns24.models.TModel.TOROID;
import static br.cns24.models.TModel.WATTS_STROGATZ;
import static br.cns24.models.TModel.WATTS_STROGATZ_DENSITY;
import br.cns24.models.TModel;

public enum TParameterModel {
	DENSITY_ERDOS("Densidade", ERDOS_RENYI_N_M), 
	PROBABILITY_ERDOS("Probabilidade", ERDOS_RENYI_N_P), 
	PROBABILITY_GILBERT("Probabilidade", GILBERT), 
	K("Parâmetro k", K_REGULAR), 
	WS_PROBABILITY("Probabilidade", WATTS_STROGATZ),
	WSD_DENSITY("Densidade", WATTS_STROGATZ_DENSITY),
	WSD_PROBABILITY("Probabilidade", WATTS_STROGATZ_DENSITY),
	WS_DENSITY("Densidade", WATTS_STROGATZ),
	K_WS("Parâmetro k", WATTS_STROGATZ),
	NW_PROBABILITY("Probabilidade", NEWMAN_WATTS), 
	NUM_NODES_BARABASI("Número de nós", BARABASI), 
	M_LINKS_BARABASI("Número de links para novos nós", BARABASI),
	DENSITY_BARABASI("Densidade da rede Barabási", BARABASI_DENSITY),
	NUM_NODES_CUSTOM("Número de nós", CUSTOM), 
	NUM_NODES_CUSTOM_PROBABILITY("Número de nós", CUSTOM_PROBABILITY),
	TOROID_DENSITY("Densidade", TOROID);

	private String description;

	private TModel model;

	public static TParameterModel getType(String model, String description) {
		for (TParameterModel parameterModel : values()) {
			if (parameterModel.model.toString().equals(model) && parameterModel.description.equals(description)) {
				return parameterModel;
			}
		}
		return null;
	}

	private TParameterModel(String description, TModel model) {
		this.description = description;
		this.model = model;
	}

	public String getDescription() {
		return description;
	}

	public TModel getModel() {
		return model;
	}
}
