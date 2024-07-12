package br.cns24.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import br.cns24.experiments.ComplexNetwork;
import br.cns24.model.GmlData;
import br.cns24.model.GmlEdge;
import br.cns24.model.GmlNode;

public class GmlDao implements IGmlDao {
	private static final String TAB_SYMBOL = "    ";

	@Override
	public void save(ComplexNetwork network, String path) {
	}

	public void save(GmlData gml, String path) {
		try {
			FileWriter fw = new FileWriter(new File(path));
			fw.write(createFileContent(gml));
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param gml
	 * @return
	 */
	public String createFileContent(GmlData gml) {
		StringBuffer content = new StringBuffer();

		content.append("graph [\n");

		for (String key : gml.getInformations().keySet()) {
			content.append(TAB_SYMBOL + key + " \"" + gml.getInformations().get(key).toString() + "\"\n");
		}

		List<GmlNode> orderedNodes = new Vector<>();
		Comparator<GmlNode> c = new Comparator<GmlNode>() {
			@Override
			public int compare(GmlNode n1, GmlNode n2) {
				return n1.getId() > n2.getId() ? -1 : 1;
			}
		};
		orderedNodes.addAll(gml.getNodes());
		Collections.sort(orderedNodes, c);

		for (GmlNode node : orderedNodes) {
			content.append(TAB_SYMBOL + "node [\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "id " + node.getId() + "\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "label \"" + node.getLabel() + "\"\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "Longitude " + node.getLongitude() + "\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "Latitude " + node.getLatitude() + "\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "Country \"" + node.getCountry() + "\"\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "Internal " + node.getInternal() + "\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "Population " + node.getPopulation() + "\n");

			for (String key : node.getInformations().keySet()) {
				content.append(TAB_SYMBOL + TAB_SYMBOL + key + " \"" + node.getInformations().get(key) + "\"\n");
			}

			content.append(TAB_SYMBOL + "]\n");
		}

		for (GmlEdge edge : gml.getEdges()) {
			content.append(TAB_SYMBOL + "edge[\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "source " + edge.getSource().getId() + "\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "target " + edge.getTarget().getId() + "\n");
			content.append(TAB_SYMBOL + TAB_SYMBOL + "LinkLabel \"" + edge.getLabel() + "\"\n");

			if (edge.getInformations() != null) {
				for (String key : edge.getInformations().keySet()) {
					content.append(TAB_SYMBOL + TAB_SYMBOL + key + " \"" + edge.getInformations().get(key) + "\"\n");
				}
			}

			content.append(TAB_SYMBOL + "]\n");
		}

		content.append("]\n");
		return content.toString();
	}

	@Override
	public ComplexNetwork load(String path) {
		GmlData data = loadGmlData(path);
		return data.createComplexNetwork();
	}

	private String[] split(String text) {
		String[] parts = new String[2];
		text = text.trim();
		if (text.startsWith("\"")) {
			parts[0] = text.substring(1, text.indexOf(" ")).trim();
		} else {
			try {
				parts[0] = text.substring(0, text.indexOf(" ")).trim();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (text.endsWith("\"")) {
			parts[1] = text.substring(text.indexOf(" "), text.length() - 1).trim();
		} else {
			parts[1] = text.substring(text.indexOf(" "), text.length()).trim();
		}
		if (parts[1].startsWith("\"")) {
			parts[1] = parts[1].substring(1);
		}

		return parts;
	}

	public GmlData loadGmlData(String path) {
		return loadGmlData(path, true);// precisa colocar pra false se for pra
										// remover nós isolados do gml
	}

	public GmlData loadGmlData(String path, boolean allowIslands) {
		GmlData data = new GmlData();
		int state = 0;

		try {
			FileReader fr = new FileReader(new File(path));
			LineNumberReader lnr = new LineNumberReader(fr);
			String line = lnr.readLine();
			String lowerLine = null;
			String[] parts = null;
			GmlNode node = new GmlNode(-1);
			GmlEdge edge = new GmlEdge();

			data.setMaxLatitude(Double.NEGATIVE_INFINITY);
			data.setMinLatitude(Double.POSITIVE_INFINITY);
			data.setMinLongitude(Double.POSITIVE_INFINITY);
			data.setMaxLongitude(Double.NEGATIVE_INFINITY);

			while (line != null) {
				lowerLine = line.trim().toLowerCase();
				if (line.trim().equals("")) {
					line = lnr.readLine();
					continue;
				}
				if (lowerLine.startsWith("node")) {
					state = 1;
				} else if (lowerLine.startsWith("edge")) {
					state = 2;
				}
				switch (state) {
				case 0:
					if (!(lowerLine.startsWith("graph") || lowerLine.startsWith("]"))) {
						parts = split(line);
						data.addInformation(parts[0], parts[1]);
					}
					break;
				case 1:
					if (lowerLine.startsWith("]") && node.getId() != -1) {
						HashMap<String, String> infos = new HashMap<String, String>();
						for (String i : node.getInformations().keySet()) {
							infos.put(i, node.getInformations().get(i));
						}
						boolean added = data.addNode(node.getId(), node.getLabel(), node.getCountry(),
								node.getLongitude(), node.getLatitude(), node.getInternal(), infos,
								node.getPopulation());
						if (added) {
							if (node.getLongitude() > data.getMaxLongitude()) {
								data.setMaxLongitude(node.getLongitude());
							}
							if (node.getLongitude() < data.getMinLongitude()) {
								data.setMinLongitude(node.getLongitude());
							}
							if (node.getLatitude() > data.getMaxLatitude()) {
								data.setMaxLatitude(node.getLatitude());
							}
							if (node.getLatitude() < data.getMinLatitude()) {
								data.setMinLatitude(node.getLatitude());
							}
						}
						node = new GmlNode(-1);
					} else if (!lowerLine.startsWith("node") && !lowerLine.trim().equals("]")) {
						parts = split(line);
						if (parts[0].equals("id")) {
							node.setId(Integer.parseInt(parts[1]));
						} else if (parts[0].equals("label")) {
							node.setLabel(parts[1]);
						} else if (parts[0].equals("Country")) {
							node.setCountry(parts[1]);
						} else if (parts[0].equals("Longitude")) {
							node.setLongitude(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("Internal")) {
							node.setInternal(Integer.parseInt(parts[1]));
						} else if (parts[0].equals("Latitude")) {
							node.setLatitude(Double.parseDouble(parts[1]));
						} else if (parts[0].equalsIgnoreCase("Population")) {
							node.setPopulation(Long.parseLong(parts[1]));
						} else if (!parts[0].trim().equals("")) {
							node.getInformations().put(parts[0], parts[1]);
						}
					}
					break;
				case 2:
					if (lowerLine.startsWith("]")) {
						HashMap<String, String> infos = new HashMap<String, String>();
						for (String i : edge.getInformations().keySet()) {
							infos.put(i, edge.getInformations().get(i));
						}
						if (!data.getEdges().contains(edge) && edge.getSource().getId() != -1) {
							data.addEdge(edge.getSource().getId(), edge.getTarget().getId(), edge.getLabel(), infos);
						}
						edge = new GmlEdge();
					} else if (!lowerLine.startsWith("edge")) {
						parts = split(line);
						if (parts[0].equals("source")) {
							edge.getSource().setId(Integer.parseInt(parts[1]));
						} else if (parts[0].equals("target")) {
							edge.getTarget().setId(Integer.parseInt(parts[1]));
						} else if (parts[0].equals("LinkLabel")) {
							edge.setLabel(parts[1]);
						} else if (!parts[0].trim().equals("")) {
							edge.getInformations().put(parts[0], parts[1]);
						}
					}
					break;
				}

				line = lnr.readLine();
			}

			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		data.setName((data.getInformations().get("GeoLocation") + " " + data.getInformations().get("Network")).trim());

		boolean achou = false;
		List<GmlNode> isolados = new Vector<>();
		for (GmlNode node : data.getNodes()) {
			achou = false;
			for (GmlEdge edge : data.getEdges()) {
				if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
					achou = true;
					break;
				}
			}
			if (!achou) {
				isolados.add(node);
			}
		}
		if (!allowIslands) {
			data.getNodes().removeAll(isolados);
		}
		// colocar novos ids para os nós
		int newId = 0;
		Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();
		for (GmlNode dn : data.getNodes()) {
			idMap.put(dn.getId(), newId);
			newId++;
		}

		for (GmlNode node : data.getNodes()) {
			for (int idOldNode : idMap.keySet()) {
				if (node.getId() == idOldNode) {
					node.setId(idMap.get(idOldNode));
					break;
				}
			}
		}
		return data;
	}
	

	/**
	 * metodo copiado do de loadGmlData exatamente igual, porem sem a 
	 * parte que muda os ids autor jorge candeias
	 * @param path
	 * @param allowIslands
	 * @return
	 */
	
	
	public GmlData loadGmlDataWithTheSamId(String path, boolean allowIslands) {
		GmlData data = new GmlData();
		int state = 0;

		try {
			FileReader fr = new FileReader(new File(path));
			LineNumberReader lnr = new LineNumberReader(fr);
			String line = lnr.readLine();
			String lowerLine = null;
			String[] parts = null;
			GmlNode node = new GmlNode(-1);
			GmlEdge edge = new GmlEdge();

			data.setMaxLatitude(Double.NEGATIVE_INFINITY);
			data.setMinLatitude(Double.POSITIVE_INFINITY);
			data.setMinLongitude(Double.POSITIVE_INFINITY);
			data.setMaxLongitude(Double.NEGATIVE_INFINITY);

			while (line != null) {
				lowerLine = line.trim().toLowerCase();
				if (line.trim().equals("")) {
					line = lnr.readLine();
					continue;
				}
				if (lowerLine.startsWith("node")) {
					state = 1;
				} else if (lowerLine.startsWith("edge")) {
					state = 2;
				}
				switch (state) {
				case 0:
					if (!(lowerLine.startsWith("graph") || lowerLine.startsWith("]"))) {
						parts = split(line);
						data.addInformation(parts[0], parts[1]);
					}
					break;
				case 1:
					if (lowerLine.startsWith("]") && node.getId() != -1) {
						HashMap<String, String> infos = new HashMap<String, String>();
						for (String i : node.getInformations().keySet()) {
							infos.put(i, node.getInformations().get(i));
						}
						boolean added = data.addNode(node.getId(), node.getLabel(), node.getCountry(),
								node.getLongitude(), node.getLatitude(), node.getInternal(), infos,
								node.getPopulation());
						if (added) {
							if (node.getLongitude() > data.getMaxLongitude()) {
								data.setMaxLongitude(node.getLongitude());
							}
							if (node.getLongitude() < data.getMinLongitude()) {
								data.setMinLongitude(node.getLongitude());
							}
							if (node.getLatitude() > data.getMaxLatitude()) {
								data.setMaxLatitude(node.getLatitude());
							}
							if (node.getLatitude() < data.getMinLatitude()) {
								data.setMinLatitude(node.getLatitude());
							}
						}
						node = new GmlNode(-1);
					} else if (!lowerLine.startsWith("node") && !lowerLine.trim().equals("]")) {
						parts = split(line);
						if (parts[0].equals("id")) {
							node.setId(Integer.parseInt(parts[1]));
						} else if (parts[0].equals("label")) {
							node.setLabel(parts[1]);
						} else if (parts[0].equals("Country")) {
							node.setCountry(parts[1]);
						} else if (parts[0].equals("Longitude")) {
							node.setLongitude(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("Internal")) {
							node.setInternal(Integer.parseInt(parts[1]));
						} else if (parts[0].equals("Latitude")) {
							node.setLatitude(Double.parseDouble(parts[1]));
						} else if (parts[0].equalsIgnoreCase("Population")) {
							node.setPopulation(Long.parseLong(parts[1]));
						} else if (!parts[0].trim().equals("")) {
							node.getInformations().put(parts[0], parts[1]);
						}
					}
					break;
				case 2:
					if (lowerLine.startsWith("]")) {
						HashMap<String, String> infos = new HashMap<String, String>();
						for (String i : edge.getInformations().keySet()) {
							infos.put(i, edge.getInformations().get(i));
						}
						if (!data.getEdges().contains(edge) && edge.getSource().getId() != -1) {
							data.addEdge(edge.getSource().getId(), edge.getTarget().getId(), edge.getLabel(), infos);
						}
						edge = new GmlEdge();
					} else if (!lowerLine.startsWith("edge")) {
						parts = split(line);
						if (parts[0].equals("source")) {
							edge.getSource().setId(Integer.parseInt(parts[1]));
						} else if (parts[0].equals("target")) {
							edge.getTarget().setId(Integer.parseInt(parts[1]));
						} else if (parts[0].equals("LinkLabel")) {
							edge.setLabel(parts[1]);
						} else if (!parts[0].trim().equals("")) {
							edge.getInformations().put(parts[0], parts[1]);
						}
					}
					break;
				}

				line = lnr.readLine();
			}

			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		data.setName((data.getInformations().get("GeoLocation") + " " + data.getInformations().get("Network")).trim());

		boolean achou = false;
		List<GmlNode> isolados = new Vector<>();
		for (GmlNode node : data.getNodes()) {
			achou = false;
			for (GmlEdge edge : data.getEdges()) {
				if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
					achou = true;
					break;
				}
			}
			if (!achou) {
				isolados.add(node);
			}
		}
		if (!allowIslands) {
			data.getNodes().removeAll(isolados);
		}

		return data;
	}
	
	
	


	public GmlData loadGmlDataFromContent(String content) {
		GmlData data = new GmlData();
		int state = 0;

		String lines[] = content.split("\n");
		String lowerLine = null;
		String[] parts = null;
		GmlNode node = new GmlNode(-1);
		GmlEdge edge = new GmlEdge();

		data.setMaxLatitude(Double.NEGATIVE_INFINITY);
		data.setMinLatitude(Double.POSITIVE_INFINITY);
		data.setMinLongitude(Double.POSITIVE_INFINITY);
		data.setMaxLongitude(Double.NEGATIVE_INFINITY);

		for (String line : lines) {
			lowerLine = line.trim().toLowerCase();
			if (lowerLine.startsWith("node")) {
				state = 1;
			} else if (lowerLine.startsWith("edge")) {
				state = 2;
			}
			switch (state) {
			case 0:
				if (!(lowerLine.startsWith("graph") || lowerLine.startsWith("]"))) {
					parts = split(line);
					data.addInformation(parts[0], parts[1]);
				}
				break;
			case 1:
				if (lowerLine.startsWith("]") && node.getId() != -1) {
					HashMap<String, String> infos = new HashMap<String, String>();
					for (String i : node.getInformations().keySet()) {
						infos.put(i, node.getInformations().get(i));
					}
					boolean added = data.addNode(node.getId(), node.getLabel(), node.getCountry(), node.getLongitude(),
							node.getLatitude(), node.getInternal(), infos, node.getPopulation());
					if (added) {
						if (node.getLongitude() > data.getMaxLongitude()) {
							data.setMaxLongitude(node.getLongitude());
						}
						if (node.getLongitude() < data.getMinLongitude()) {
							data.setMinLongitude(node.getLongitude());
						}
						if (node.getLatitude() > data.getMaxLatitude()) {
							data.setMaxLatitude(node.getLatitude());
						}
						if (node.getLatitude() < data.getMinLatitude()) {
							data.setMinLatitude(node.getLatitude());
						}
					}
					node = new GmlNode(-1);
				} else if (!lowerLine.startsWith("node")) {
					parts = split(line);
					if (parts[0].equals("id")) {
						node.setId(Integer.parseInt(parts[1]));
					} else if (parts[0].equals("label")) {
						node.setLabel(parts[1]);
					} else if (parts[0].equals("Country")) {
						node.setCountry(parts[1]);
					} else if (parts[0].equals("Longitude")) {
						node.setLongitude(Double.parseDouble(parts[1]));
					} else if (parts[0].equals("Internal")) {
						node.setInternal(Integer.parseInt(parts[1]));
					} else if (parts[0].equals("Latitude")) {
						node.setLatitude(Double.parseDouble(parts[1]));
					} else if (!parts[0].trim().equals("")) {
						node.getInformations().put(parts[0], parts[1]);
					} else if (parts[0].equals("IDH")) {
						node.setIdh(Double.parseDouble(parts[1]));
					}else if (parts[0].equals("GINI")) {
						node.setGine(Double.parseDouble(parts[1]));
					}else if (parts[0].equals("PIB")) {
						node.setPib(Double.parseDouble(parts[1]));
					}
				}
				break;
			case 2:
				if (lowerLine.startsWith("]")) {
					HashMap<String, String> infos = new HashMap<String, String>();
					for (String i : edge.getInformations().keySet()) {
						infos.put(i, edge.getInformations().get(i));
					}
					if (!data.getEdges().contains(edge) && edge.getSource().getId() != -1) {
						data.addEdge(edge.getSource().getId(), edge.getTarget().getId(), edge.getLabel(), infos);
					}
					edge = new GmlEdge();
				} else if (!lowerLine.startsWith("edge")) {
					parts = split(line);
					if (parts[0].equals("source")) {
						edge.getSource().setId(Integer.parseInt(parts[1]));
					} else if (parts[0].equals("target")) {
						edge.getTarget().setId(Integer.parseInt(parts[1]));
					} else if (parts[0].equals("LinkLabel")) {
						edge.setLabel(parts[1]);
					} else if (!parts[0].trim().equals("")) {
						edge.getInformations().put(parts[0], parts[1]);
					}
				}
				break;
			}
		}

		data.setName((data.getInformations().get("GeoLocation") + " " + data.getInformations().get("Network")).trim());

		boolean achou = false;
		List<GmlNode> isolados = new Vector<>();
		for (GmlNode nodeAux : data.getNodes()) {
			achou = false;
			for (GmlEdge edgeAux : data.getEdges()) {
				if (edgeAux.getSource().equals(nodeAux) || edgeAux.getTarget().equals(nodeAux)) {
					achou = true;
					break;
				}
			}
			if (!achou) {
				isolados.add(nodeAux);
			}
		}
	//	data.getNodes().removeAll(isolados);// jorge removeu pra que n�o remova os nós isolados.
		// colocar novos ids para os nós
		int newId = 0;
		Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();
		for (GmlNode dn : data.getNodes()) {
			idMap.put(dn.getId(), newId);
			newId++;
		}

		// for (GmlEdge edge : data.getEdges()) {
		// boolean c1 = false;
		// boolean c2 = false;
		// for (int idOldNode : idMap.keySet()) {
		// if (edge.getSource().getId() == idOldNode && !c1) {
		// edge.getSource().setId(idMap.get(idOldNode));
		// c1 = true;
		// }
		// if (edge.getTarget().getId() == idOldNode && !c2) {
		// edge.getTarget().setId(idMap.get(idOldNode));
		// c2 = true;
		// }
		// if (c1 && c2) {
		// break;
		// }
		// }
		// }
		for (GmlNode nodeAux : data.getNodes()) {
			for (int idOldNode : idMap.keySet()) {
				if (nodeAux.getId() == idOldNode) {
					nodeAux.setId(idMap.get(idOldNode));
					break;
				}
			}
		}
		return data;
	}

	public static void main(String[] args) {
		GmlDao dao = new GmlDao();
		// GmlData data = dao.loadGmlData("C:\\doutorado\\datasets\\internet
		// topology\\Abilene.gml");
		GmlData data = dao
				.loadGmlData("C:/Users/jorge/workspace/ClusterPe/src/MunicipiosDePernambucoTec.RedesFinalizado.gml");
		// GmlData data =
		// dao.loadGmlData("C:/Users/jorge/workspace/ClusterPe/src/graph2.gml");
		System.out.println(data.getInformations().get("Network"));
		System.out.println(data.getNodes().size());
		System.out.println(data.getEdges().size());
	}

}
