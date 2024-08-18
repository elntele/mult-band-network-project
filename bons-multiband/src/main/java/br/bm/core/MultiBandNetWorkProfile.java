package br.bm.core;

import java.util.List;
import java.util.Vector;

import br.cns24.experiments.ComplexNetwork;

public class MultiBandNetWorkProfile implements INetwork{

  private Integer[] rawData;
  private Link[][] links;
  private Vector<Node> nodes;
  private double meanRateBetweenCalls;
  private double meanRateOfCallsDuration;
  private double numOfCalls;
  private double snrThresholddB;
  private boolean amplifiers;
  private boolean fwm;
  private boolean crTalk;
  private boolean pmd;
  private boolean pmdDcf;
  private boolean rd;
  private double taxaBits;
  private double larguraDeLinha;
  private double gama;
  private double dPmd;
  private double dPmdDcf;
  private double delta;
  private List<Double> epsilon;
  private int codEscolhaLambda;
  private int codAlgorRotmto;
  private boolean bloqCamadaRede;
  private int nLambdaMax;
  private NetworkBP bp;
  private NetworkBP minimumBp;
  private NetworkCost cost;
  private int networkLoad;
  private double oxcIsolationFactor;
  private Double[][] completeDistances;
  private ComplexNetwork cn;

  public MultiBandNetWorkProfile(){
    super();
  }

  public MultiBandNetWorkProfile(Link[][] links, Vector<Node> nodes, int networkLoad,
      double meanRateOfCallsDuration, double numOfCalls, double snrThresholddB, boolean amplifiers, boolean fwm,
      boolean crTalk, boolean pmd, boolean pmdDcf, boolean rd, double taxaBits, double larguraDeLinha,
      double gama, double dPmd, double dPmdDcf, double delta, int codEscolhaLambda,
      int codAlgorRotmto, boolean bloqCamadaRede, int nLambdaMax) {
    super();
    this.links = links;
    this.nodes = nodes;
    this.networkLoad = networkLoad;
    this.meanRateBetweenCalls = networkLoad/0.01;
    this.meanRateOfCallsDuration = meanRateOfCallsDuration;
    this.numOfCalls = numOfCalls;
    this.snrThresholddB = snrThresholddB;
    this.amplifiers = amplifiers;
    this.fwm = fwm;
    this.crTalk = crTalk;
    this.pmd = pmd;
    this.pmdDcf = pmdDcf;
    this.rd = rd;
    this.taxaBits = taxaBits;
    this.larguraDeLinha = larguraDeLinha;
    this.gama = gama;
    this.dPmd = dPmd;
    this.dPmdDcf = dPmdDcf;
    this.delta = delta;
    this.codEscolhaLambda = codEscolhaLambda;
    this.codAlgorRotmto = codAlgorRotmto;
    this.bloqCamadaRede = bloqCamadaRede;
    this.nLambdaMax = nLambdaMax;
    this.bp = new NetworkBP();
    this.minimumBp = new NetworkBP();
    this.cost = new NetworkCost();
  }

  public void cleanBp(){
    bp.clean();
  }

  /**
   * Método acessor para obter o valor do atributo links.
   *
   * @return O valor de links
   */
  public Link[][] getLinks() {
    return links;
  }

  /**
   * Metodo acessor para alterar o valor do atributo links.
   *
   * @param links
   *            O novo valor de links
   */
  public void setLinks(Link[][] links) {
    this.links = links;
  }

  /**
   * Metodo acessor para obter o valor do atributo nodes.
   *
   * @return O valor de nodes
   */
  public Vector<Node> getNodes() {
    return nodes;
  }

  /**
   * Metodo acessor para alterar o valor do atributo nodes.
   *
   * @param nodes
   *            O novo valor de nodes
   */
  public void setNodes(Vector<Node> nodes) {
    this.nodes = nodes;
  }

  /**
   * Metodo acessor para obter o valor do atributo meanRateBetweenCalls.
   *
   * @return O valor de meanRateBetweenCalls
   */
  public double getMeanRateBetweenCalls() {
    return meanRateBetweenCalls;
  }

  /**
   * Metodo acessor para alterar o valor do atributo meanRateBetweenCalls.
   *
   * @param meanRateBetweenCalls
   *            O novo valor de meanRateBetweenCalls
   */
  public void setMeanRateBetweenCalls(double meanRateBetweenCalls) {
    this.meanRateBetweenCalls = meanRateBetweenCalls;
  }

  /**
   * Método acessor para obter o valor do atributo meanRateOfCallsDuration.
   *
   * @return O valor de meanRateOfCallsDuration
   */
  public double getMeanRateOfCallsDuration() {
    return meanRateOfCallsDuration;
  }

  /**
   * Metodo acessor para alterar o valor do atributo meanRateOfCallsDuration.
   *
   * @param meanRateOfCallsDuration
   *            O novo valor de meanRateOfCallsDuration
   */
  public void setMeanRateOfCallsDuration(double meanRateOfCallsDuration) {
    this.meanRateOfCallsDuration = meanRateOfCallsDuration;
  }

  /**
   * Metodo acessor para obter o valor do atributo numOfCalls.
   *
   * @return O valor de numOfCalls
   */
  public double getNumOfCalls() {
    return numOfCalls;
  }

  /**
   * Metodo acessor para alterar o valor do atributo numOfCalls.
   *
   * @param numOfCalls
   *            O novo valor de numOfCalls
   */
  public void setNumOfCalls(double numOfCalls) {
    this.numOfCalls = numOfCalls;
  }

  /**
   * Método acessor para obter o valor do atributo snrThresholddB.
   *
   * @return O valor de snrThresholddB
   */
  public double getSnrThresholddB() {
    return snrThresholddB;
  }

  /**
   * Metodo acessor para alterar o valor do atributo snrThresholddB.
   *
   * @param snrThresholddB
   *            O novo valor de snrThresholddB
   */
  public void setSnrThresholddB(double snrThresholddB) {
    this.snrThresholddB = snrThresholddB;
  }

  /**
   * Método acessor para obter o valor do atributo amplifiers.
   *
   * @return O valor de amplifiers
   */
  public boolean isAmplifiers() {
    return amplifiers;
  }

  /**
   * Metodo acessor para alterar o valor do atributo amplifiers.
   *
   * @param amplifiers
   *            O novo valor de amplifiers
   */
  public void setAmplifiers(boolean amplifiers) {
    this.amplifiers = amplifiers;
  }

  /**
   * Método acessor para obter o valor do atributo fwm.
   *
   * @return O valor de fwm
   */
  public boolean isFwm() {
    return fwm;
  }

  /**
   * Metodo acessor para alterar o valor do atributo fwm.
   *
   * @param fwm
   *            O novo valor de fwm
   */
  public void setFwm(boolean fwm) {
    this.fwm = fwm;
  }

  /**
   * Método acessor para obter o valor do atributo crTalk.
   *
   * @return O valor de crTalk
   */
  public boolean isCrTalk() {
    return crTalk;
  }

  /**
   * Metodo acessor para alterar o valor do atributo crTalk.
   *
   * @param crTalk
   *            O novo valor de crTalk
   */
  public void setCrTalk(boolean crTalk) {
    this.crTalk = crTalk;
  }

  /**
   * Método acessor para obter o valor do atributo pmd.
   *
   * @return O valor de pmd
   */
  public boolean isPmd() {
    return pmd;
  }

  /**
   * Metodo acessor para alterar o valor do atributo pmd.
   *
   * @param pmd
   *            O novo valor de pmd
   */
  public void setPmd(boolean pmd) {
    this.pmd = pmd;
  }

  /**
   * Método acessor para obter o valor do atributo pmdDcf.
   *
   * @return O valor de pmdDcf
   */
  public boolean isPmdDcf() {
    return pmdDcf;
  }

  /**
   * Metodo acessor para alterar o valor do atributo pmdDcf.
   *
   * @param pmdDcf
   *            O novo valor de pmdDcf
   */
  public void setPmdDcf(boolean pmdDcf) {
    this.pmdDcf = pmdDcf;
  }

  /**
   * Método acessor para obter o valor do atributo rd.
   *
   * @return O valor de rd
   */
  public boolean isRd() {
    return rd;
  }

  /**
   * Metodo acessor para alterar o valor do atributo rd.
   *
   * @param rd
   *            O novo valor de rd
   */
  public void setRd(boolean rd) {
    this.rd = rd;
  }

  /**
   * Método acessor para obter o valor do atributo taxaBits.
   *
   * @return O valor de taxaBits
   */
  public double getTaxaBits() {
    return taxaBits;
  }

  /**
   * Metodo acessor para alterar o valor do atributo taxaBits.
   *
   * @param taxaBits
   *            O novo valor de taxaBits
   */
  public void setTaxaBits(double taxaBits) {
    this.taxaBits = taxaBits;
  }

  /**
   * Método acessor para obter o valor do atributo larguraDeLinha.
   *
   * @return O valor de larguraDeLinha
   */
  public double getLarguraDeLinha() {
    return larguraDeLinha;
  }

  /**
   * Metodo acessor para alterar o valor do atributo larguraDeLinha.
   *
   * @param larguraDeLinha
   *            O novo valor de larguraDeLinha
   */
  public void setLarguraDeLinha(double larguraDeLinha) {
    this.larguraDeLinha = larguraDeLinha;
  }

  /**
   * Método acessor para obter o valor do atributo gama.
   *
   * @return O valor de gama
   */
  public double getGama() {
    return gama;
  }

  /**
   * Metodo acessor para alterar o valor do atributo gama.
   *
   * @param gama
   *            O novo valor de gama
   */
  public void setGama(double gama) {
    this.gama = gama;
  }

  /**
   * Método acessor para obter o valor do atributo dPmd.
   *
   * @return O valor de dPmd
   */
  public double getdPmd() {
    return dPmd;
  }

  /**
   * Metodo acessor para alterar o valor do atributo dPmd.
   *
   * @param dPmd
   *            O novo valor de dPmd
   */
  public void setdPmd(double dPmd) {
    this.dPmd = dPmd;
  }

  /**
   * Método acessor para obter o valor do atributo dPmdDcf.
   *
   * @return O valor de dPmdDcf
   */
  public double getdPmdDcf() {
    return dPmdDcf;
  }

  /**
   * Metodo acessor para alterar o valor do atributo dPmdDcf.
   *
   * @param dPmdDcf
   *            O novo valor de dPmdDcf
   */
  public void setdPmdDcf(double dPmdDcf) {
    this.dPmdDcf = dPmdDcf;
  }

  /**
   * Método acessor para obter o valor do atributo delta.
   *
   * @return O valor de delta
   */
  public double getDelta() {
    return delta;
  }

  /**
   * Metodo acessor para alterar o valor do atributo delta.
   *
   * @param delta
   *            O novo valor de delta
   */
  public void setDelta(double delta) {
    this.delta = delta;
  }

  /**
   * Método acessor para obter o valor do atributo epsilon.
   *
   * @return O valor de epsilon
   */
  public List<Double> getEpsilon() {
    return epsilon;
  }

  /**
   * Metodo acessor para alterar o valor do atributo epsilon.
   *
   * @param epsilon
   *            O novo valor de epsilon
   */
  public void setEpsilon(List<Double> epsilon) {
    this.epsilon = epsilon;
  }

  /**
   * Método acessor para obter o valor do atributo codEscolhaLambda.
   *
   * @return O valor de codEscolhaLambda
   */
  public int getCodEscolhaLambda() {
    return codEscolhaLambda;
  }

  /**
   * Metodo acessor para alterar o valor do atributo codEscolhaLambda.
   *
   * @param codEscolhaLambda
   *            O novo valor de codEscolhaLambda
   */
  public void setCodEscolhaLambda(int codEscolhaLambda) {
    this.codEscolhaLambda = codEscolhaLambda;
  }

  /**
   * Método acessor para obter o valor do atributo codAlgorRotmto.
   *
   * @return O valor de codAlgorRotmto
   */
  public int getCodAlgorRotmto() {
    return codAlgorRotmto;
  }

  /**
   * Metodo acessor para alterar o valor do atributo codAlgorRotmto.
   *
   * @param codAlgorRotmto
   *            O novo valor de codAlgorRotmto
   */
  public void setCodAlgorRotmto(int codAlgorRotmto) {
    this.codAlgorRotmto = codAlgorRotmto;
  }

  /**
   * Método acessor para obter o valor do atributo bloqCamadaRede.
   *
   * @return O valor de bloqCamadaRede
   */
  public boolean isBloqCamadaRede() {
    return bloqCamadaRede;
  }

  /**
   * Metodo acessor para alterar o valor do atributo bloqCamadaRede.
   *
   * @param bloqCamadaRede
   *            O novo valor de bloqCamadaRede
   */
  public void setBloqCamadaRede(boolean bloqCamadaRede) {
    this.bloqCamadaRede = bloqCamadaRede;
  }

  /**
   * Método acessor para obter o valor do atributo bp.
   *
   * @return O valor de bp
   */
  public NetworkBP getBp() {
    return bp;
  }

  /**
   * Metodo acessor para alterar o valor do atributo bp.
   *
   * @param bp
   *            O novo valor de bp
   */
  public void setBp(NetworkBP bp) {
    this.bp = bp;
  }

  /**
   * Método acessor para obter o valor do atributo cost.
   *
   * @return O valor de cost
   */
  public NetworkCost getCost() {
    return cost;
  }

  /**
   * Metodo acessor para alterar o valor do atributo cost.
   *
   * @param cost
   *            O novo valor de cost
   */
  public void setCost(NetworkCost cost) {
    this.cost = cost;
  }

  /**
   * Método acessor para obter o valor do atributo nLambdaMax.
   * @return O valor de nLambdaMax
   */
  public int getnLambdaMax() {
    return nLambdaMax;
  }

  /**
   * Metodo acessor para alterar o valor do atributo nLambdaMax.
   * @param nLambdaMax O novo valor de nLambdaMax
   */
  public void setnLambdaMax(int nLambdaMax) {
    this.nLambdaMax = nLambdaMax;
  }

  /**
   * @return o valor do atributo minimumBp
   */
  public NetworkBP getMinimumBp() {
    return minimumBp;
  }

  /**
   * Altera o valor do atributo minimumBp
   * @param minimumBp O valor para setar em minimumBp
   */
  public void setMinimumBp(NetworkBP minimumBp) {
    this.minimumBp = minimumBp;
  }

  /**
   * @return o valor do atributo oxcIsolationFactor
   */
  public double getOxcIsolationFactor() {
    return oxcIsolationFactor;
  }

  /**
   * Altera o valor do atributo oxcIsolationFactor
   * @param oxcIsolationFactor O valor para setar em oxcIsolationFactor
   */
  public void setOxcIsolationFactor(double oxcIsolationFactor) {
    this.oxcIsolationFactor = oxcIsolationFactor;
  }

  /**
   * @return o valor do atributo completeDistances
   */
  public Double[][] getCompleteDistances() {
    return completeDistances;
  }

  /**
   * Altera o valor do atributo completeDistances
   * @param completeDistances O valor para setar em completeDistances
   */
  public void setCompleteDistances(Double[][] completeDistances) {
    this.completeDistances = completeDistances;
  }

  public ComplexNetwork getCn() {
    return cn;
  }

  public void setCn(ComplexNetwork cn) {
    this.cn = cn;
  }

  public Integer[] getRawData() {
    return rawData;
  }

  public void setRawData(Integer[] rawData) {
    this.rawData = rawData;
  }

  public int getNetworkLoad() {
    return networkLoad;
  }

  public void setNetworkLoad(int networkLoad) {
    this.networkLoad = networkLoad;
  }
}
