package br.cns24.models;

public enum TModel {
	K_REGULAR("Rede k-regular"),
	GILBERT("Gilbert"), 
	ERDOS_RENYI_N_M("Erdos-Renyi G(n,m)"),
	ANCHORED_ERDOS_RENYI_N_M("Anchored Erdos-Renyi G(n,m)"),
	ERDOS_RENYI_N_P("Erdos-Renyi G(n,p)"), 
	WATTS_STROGATZ("Watts-Strogatz"), 
	WATTS_STROGATZ_DENSITY("Watts-Strogatz density"), 
	NEWMAN_WATTS("Newman-Watts"),
	BARABASI("Barabasi"),
	BARABASI_DENSITY("Barabasi por densidade"),
	GENERALIZED_SCALE_FREE("Generalized scale-free"),
	POWER_LAW_BY_DISTANCE("Power law by distance"),
	CUSTOM_POWER_LAW("Power law customizado"),
	CUSTOM("Definida pelo usu�rio"),
	CUSTOM_PROBABILITY("Matriz de Probabilidades"),
	K_STAR("Rede k-star"),
	NSF_NET("NsfNet"),
	FINLAND("Finland"),
	PACIFIC_BELL("Pacific Bell"),
	TOROID("Toroid");
	
	private String description;
	
	private TModel(String description){
		this.description = description;
	}
	
	public int getId(){
		for (int i = 0; i < values().length; i++){
			if (values()[i] == this){
				return i;
			}
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return description;
	}
}
