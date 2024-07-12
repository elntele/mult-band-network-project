package br.cns24.experiments;

public enum MenuActions {
	NEW("Nova rede"), 
	NEW_SIMULATION("Nova simula��o"), 
	OPEN("Abrir rede..."),
	IMPORT("Importar rede..."),
	SAVE("Salvar rede..."), 
	PRINT("Imprimir..."), 
	CONFIG("Configura��es..."), 
	EXIT("Sair"), 
	EXPORT("Exportar imagem para PNG..."),
	ABOUT("Sobre o Complex Network Simulator"), 
	EXPORT_TABLE_TXT("Exportar dados tabulados para TXT..."), 
	EXPORT_TABLE_XML("Exportar dados tabulados para XML..."), 
	SOM_EXPERIMENT("Analisar modelos usando Mapas Auto-Organiz�veis...");

	private String description;

	private MenuActions(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
