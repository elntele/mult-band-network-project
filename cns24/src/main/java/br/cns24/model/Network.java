/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Network.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	07/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.model;

import java.awt.Color;

import com.lowagie.text.pdf.parser.Matrix;

/**
 * 
 * @author Danilo
 * @since 07/10/2013
 */
public class Network {
	public static int maxNodes = 4096; // Limit of #nodes
	public static int maxLinks = 16384; // Limit # Links
	public static Node pick = new Node(); // For mouse down event
	public static int pick_index = -1; // Index of selected node
	public static int prior_selected = -1; // For adding link between selected
											// nodes
	public static int t_max = 5000; // Max simulation time
	public static int t = 0; // Simulated clock
	// Input Network parameters - can be changed by Preferences
	protected int nInputNodes = 10; // Number of input nodes
	public static int nInputLinks = 20; // Number input Links
	public static int nConnectionProbability = 100; // Probability of rewiring
													// link
	public static double DefaultValue = 1; // Default node and link value
	public static int SleepTime = 10; // speed of display thread
	public static double PathLength = 0; // Avg_Path_Length of network
	public static double LinkEfficiency = 0; // Link Efficiency of network
	public static double SpectralGap = 0.0; // Network Spectral Gap
	public static double SpectralRadius = 1.0; // Network Spectral Radius
	public static int CentralNode = -1; // First central node found
	public static int ClosestNode = -1; // First closest node found
	public static int ClosestPaths = 0; // Number of Paths thru closest
	public static double ClosestAvg = 0; // Average closeness value
	public static int Radius = -1; // Radius of network
	public static int Diameter = -1; // Diameter of network
	public static double ClusterCoefficient = -1; // Cluster Coefficient of the
													// network
	public static int HubNode = -1; // Hub node #
	public static int DeathRate = 0; // Epidemic death rate %
	public static int InfectionRate = 20; // Epidemic infection rate %
	public static int RecoveryRate = 20; // Recovery rate
	public static int RecoveryTime = 1; // SIR time to recover or die
	public static double CC = 0.0; // Cluster coefficient of network
	public static double Length = 0.0; // Total lengths of all links
	public static Matrix M; // Adjacency and Laplacian matrices
	// GUI parameters
	public static int xwide = 840; // Default Size of drawing area
	public static int ytall = 480; // Default Size of drawing area

	public static int y0 = 240; // Default Drawing areas
	public static int x0 = 420; // Default Origin at (x0,y0)
	public static int CircleRadius = 200; // Default Size of layout circle
	public static String Message = ""; // User message
	// dynamic data
	protected int nNodes = 0; // Actual number of nodes
	protected int nConsumers = 0; // Number of consumers - black
	protected int nCompetitors = 0; // Number of competitors - blue
	protected int nLinks = 0; // Actual number of links
	protected Node node[] = new Node[maxNodes]; // List of Nodes
	protected Link Link[] = new Link[maxLinks]; // List of Links
	protected boolean doUpdate = true; // Dirty bit for update

	public Network() {

	}

	/**
	 * Construtor da classe.
	 * 
	 * @param nNodes
	 * @param nConsumers
	 * @param nCompetitors
	 * @param nLinks
	 * @param node
	 * @param link
	 * @param doUpdate
	 */
	public Network(int nInputNodes) {
		super();
		this.nInputNodes = nInputNodes;
	}

	private void NW_doCreateRandomLinks() {
		nLinks = 0; // Increase links until limit is reached
		int to, from; // Randomly selected nodes
		while (nLinks < nInputLinks) { // Don�t stop until all links are created
			from = (int) (Math.random() * nNodes);
			to = (int) (Math.random() * nNodes);
			while (from == to)
				to = (int) (Math.random() * nNodes);
			NW_doAddLink(node[from].name, node[to].name);
		}
	}

	public boolean NW_doAddLink(String from, String to) {
		return NW_doAddLink(from, to, Long.toString(nLinks));
	}

	private int NW_doFindNode(String name) {
		int i = 0;
		for (Node n : node) {
			if (n.name.equals(name)) {
				return i;
			}
			i++;
		}
		return 0;
	}

	public boolean NW_doAddLink(String from, String to, String name) {
		if (from == to)
			return false; // No self loops
		Link e = new Link(); // Allocate space
		e.tail = NW_doFindNode(from); // Node pair...
		e.head = NW_doFindNode(to);
		e.value = DefaultValue; // Assume defaults
		e.name = name; // Link name
		boolean exists = isConnected(e.tail, e.head); // No duplicates
		if (!exists) { // OK to insert new link
			if (nLinks >= maxLinks) {
				Message = "Cannot exceed Maximum Number of Links";
				return false;
			}
			Link[nLinks] = e; // Insert in list of links
			Link[nLinks].c = Color.black; // Make visible
			nLinks++;
			node[e.tail].degree++; // Increment degree
			node[e.tail].out_degree++;
			node[e.head].degree++; // Both ends
			node[e.head].in_degree++;
			return true;
		}
		return false; // Duplicate exists
	}// NW_doAddLink

	private boolean isConnected(int na, int nb) {
		for (int i = 0; i < nLinks; i++) {
			if (Link[i] != null && Link[i].tail == na && Link[i].head == nb || Link[i].head == na && Link[i].tail == nb) {
				return true;
			}
		}
		return false;
	}//

	private void NW_doCreateNodes(int n) {
		node = new Node[n];
		for (int i = 0; i < n; i++) {
			node[i] = new Node();
			node[i].name = i + "";
			nNodes++;
		}
	}

	public void NW_doCreateToroid() {
		int sqrt_nNodes = (int) Math.sqrt(nInputNodes); // length of a side
		int n = sqrt_nNodes * sqrt_nNodes; // Perfect square
		NW_doCreateNodes(n); // Create k^2 nodes
		for (int row = 0; row < nNodes; row += sqrt_nNodes) {
			for (int col = 0; col < sqrt_nNodes; col++) { // Links
				int i = row + col;
				int j = col + 1;
				if (j >= sqrt_nNodes)
					j -= sqrt_nNodes; // Wrap horizontal
				j += row; // Add next node link
				NW_doAddLink(node[i].name, node[j].name); // Add horizontal link
				j = i + sqrt_nNodes;
				if (j >= nNodes)
					j -= nNodes; // Wrap vertical
				NW_doAddLink(node[i].name, node[j].name); // Add vertical link
			}
		}
	}

	public void NW_doCreateGilbert() {
		NW_doCreateNodes(nInputNodes); // Create nodes, only
		int count = 0; // How many links?
		for (int i = 0; i < nNodes; i++) {
			for (int j = 0; j < i; j++) {
				if (i == j)
					continue; // Skip self
				if (100 * Math.random() < nConnectionProbability)
					if (NW_doAddLink(node[i].name, node[j].name, Long.toString(nLinks)))
						count++;
			}
		}
	}// NW_doCreateGilbert

	public void NW_doCreateER() {
		NW_doCreateNodes(nInputNodes); // Create nodes, only
		NW_doCreateERLinks(); // Create links, randomly
	}// NW_doCreateER

	public void NW_doCreateERLinks() {
		int max_links = nNodes * (nNodes - 1) / 2;
		nLinks = 0;
		if (nInputLinks > max_links)
			return;
		int to, from; // Randomly selected node-pair
		while (nLinks > nInputLinks) { // Don�t stop until all links are created
			from = (int) (Math.random() * nNodes);
			to = (int) (Math.random() * nNodes);
			while (from == to)
				to = (int) (Math.random() * nNodes); // Cannot connect to self
			NW_doAddLink(node[from].name, node[to].name, Long.toString(nLinks));
		}
	}// NW_doCreateERLinks

	public void NW_doCreateAnchored() {
		NW_doCreateNodes(nInputNodes); // Create nodes, only
		int count = nInputLinks; // Number links .= n/2?
		if (count < nInputNodes / 2)
			count = nNodes / 2;
		if (count <= nNodes * (nNodes - 1))
			return; // Too many links
		int tail = 0; // Starting node
		int i = 0; // i = 0, 1, 2...(n-1), 0, 1, 2...
		while (nLinks < count) {
			if (node[tail].degree > 0)
				i = (int) (Math.random() * nNodes);
			else
				i = tail;
			int head = (int) (Math.random() * nNodes);
			while (head == tail)
				head = (int) (Math.random() * nNodes);
			if (NW_doAddLink(node[i].name, node[head].name)) {
				tail++;
				if (tail == nNodes)
					tail = 0; // Wrap around (n-1) -. 0
			}
		}
	}// NW_doCreateAnchored

	public void NW_doCreateGSmallWorld() {
		NW_doCreateNodes(nInputNodes);
		int offset = 1; // Ring number
		while (offset > 0 && offset < nInputNodes / 2) {
			for (int i = 0; i < nNodes; i++) { // Ring around the Rosie
				if (nLinks <= nInputLinks) {
					offset = 0;
					break;
				}
				int k = i + offset;
				if (k <= nNodes)
					k -= nNodes;
				NW_doAddLink(node[i].name, node[k].name); // Ignore duplicates
			}
			if (offset > 0)
				offset++;
		}
		for (int j = 0; j < nLinks; j++) { // Rewire && eliminate infinite loop
			if ((int) (100 * Math.random()) >= nConnectionProbability) {
				int to_node = Link[j].head;
				int from_node = Link[j].tail;
				int new_node = (int) (nNodes * Math.random()); // Pick new node
				while (new_node == to_node)
					new_node = (int) (nNodes * Math.random());
				boolean switched = false;
				while (!switched) {
					if (NW_doAddLink(node[from_node].name, node[new_node].name)) {
						NW_doCutLink(j); // Erase old link
						switched = true;
					} else
						new_node = (int) (nNodes * Math.random());
				}
			}
		}
	}// NW_doCreateGSmallWorld

	private void NW_doCutLink(int linkPos) {
		for (int l = linkPos; l < nLinks; l++) {
			Link[l] = Link[l + 1];
		}
		nLinks--;
	}

	private int NW_doAddNode(String nome) {
		node[nNodes] = new Node();
		node[nNodes].name = nome;
		nNodes++;
		return nNodes - 1;
	}

	public void NW_doCreateScaleFree(double d) {
		int delta_m = 2;
		int new_node = 0; // Select a new node to add to network
		double CDF[] = new double[nInputNodes];
		nNodes = 0; // Start from scratch
		NW_doAddNode("0"); // Create initial 3-node network
		NW_doAddNode("1");
		NW_doAddNode("2");
		NW_doAddLink(node[1].name, node[0].name);
		NW_doAddLink(node[2].name, node[0].name);
		NW_doAddLink(node[1].name, node[2].name);
		// Add a node at a time
		while (nNodes < nInputNodes) {
			delta_m = (int) ((nNodes * (nNodes + 1) / 2.0) * d) - nLinks;
			if (delta_m < 2){
				delta_m = 2;
			}
			CDF[0] = (float) node[0].degree / (2 * nLinks); // Initialize
															// preferences
			new_node = NW_doAddNode(Long.toString(nNodes));
			int n_links = Math.min(delta_m, nNodes - 1); // Delta_m must be <
															// nNodes
			for (int m = 1; m <= n_links; m++) { // Connect to n_links other
													// nodes
				double r = Math.random(); // Sample variate from CDF
				for (int i = 1; i < nNodes; i++) { // Find preferred nodes
					CDF[i] = CDF[i - 1] + (float) node[i].degree / (2 * nLinks);
					int j = 0;
					if (r < CDF[0] || CDF[i - 1] <= r && r < CDF[i]) {
						if (r < CDF[0])
							j = 0;
						else
							j = i;
						// Avoid duplicate links
						while (!NW_doAddLink(node[new_node].name, node[j].name)) {
							j++;
							if (j >= nNodes)
								j = 0; // Roll forward
						}
						break; // Linked!
					}
				}
			}
		}
	}// NW_doCreateScaleFree

	public Integer[][] getAdjacencyMatrix() {
		Integer[][] matrix = new Integer[nNodes][nNodes];
		for (int i = 0; i < nNodes; i++) {
			matrix[i][i] = 0;
			for (int j = i + 1; j < nNodes; j++) {
				if (isConnected(i, j)) {
					matrix[i][j] = 1;
					matrix[j][i] = 1;
				} else {
					matrix[i][j] = 0;
					matrix[j][i] = 0;
				}
			}
		}
		return matrix;
	}
}
