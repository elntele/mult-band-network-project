package br.bm.rwa;

import static br.bm.core.Call.BIDIRECIONAL;
import static br.bm.core.SimonUtil.B0;
import static br.bm.core.SimonUtil.INF;
import static br.bm.core.SimonUtil.PLANCK;
import static br.bm.rwa.Dijkstra.dijkstra_fnb;
import static br.bm.rwa.Funcoes.calculoPmd_fnb;
import static br.bm.rwa.Funcoes.calculoRd_fnb;
import static br.bm.rwa.Funcoes.somatorioPotSwitch;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import br.bm.core.Call;
import br.bm.core.CallList;
import br.bm.core.CallScheduler;
import br.bm.core.CallSchedulerNonUniformHub;
import br.bm.core.Fiber;
import br.bm.core.INetwork;
import br.bm.core.Link;
import br.bm.core.MultiBandNetWorkProfile;
import br.bm.core.NetworkProfile;

public class MultiBandDijkstraSimulator extends OpticalNetworkSimulatorAbstract {
  public static final double D_PMD = 0.05e-12 / sqrt(1000); // 0.04

  public static final double D_PMD_D_CF = 0;

  public static final double DELTA = 10;

  public static final double TIME_PULSE_BROADENING_PMD_P1 = (0.01) * (1 / TAXA_BITS);

  public static final double DELTA_PMD_P1 = (100) * (TAXA_BITS);

  private static final boolean ignorePhysicalImpairments = false;

  @Override
  public void simulate(NetworkProfile network, CallScheduler scheduler, int minCalls) {

  }


  public void simulateMultiBand(INetwork net, CallScheduler genericScheduler, int minCalls) {
    MultiBandNetWorkProfile network = (MultiBandNetWorkProfile) net;
    CallSchedulerNonUniformHub scheduler = (CallSchedulerNonUniformHub) genericScheduler;
    int x = 0, y = 0;
    int nLambdaMax = 0, usarLambda = 0;
    int numCallsBlockedLackOfWaveLenght = 0;
    int numCallsBlockedUnacceptableBer = 0;
    int numCallsBlockedDispersion = 0;
    Vector<Link> rotaUplink = new Vector<Link>(), rotaDownlink = new Vector<Link>();
    Vector<Fiber> fibersUpLink = new Vector<Fiber>(), fibersDownLink = new Vector<Fiber>();
    double numOfCalls = network.getNumOfCalls();
    CallList listOfCalls = new CallList();
    Call tempCall = new Call();

    // of minumum acceptatle snr from dB to linear
    nLambdaMax = 0;
    HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
    // Procura qual a fibra que possui mais comprimentos de onda
    for (int k = 0; k < network.getNodes().size(); k++) {
      for (int i = 0; i < network.getNodes().size(); i++) {
        Vector<Link> rota = new Vector<Link>();
        // here is inside a 'double loop for' where the first for iterate with k from 0 until
        // numNodes-1 and the second with i from 0 until numNodes-1. The dijkstra_fnb()
        // method fulfill the Vector<Link> rota with the path of Links from the origin k node
        // until the last i node.
        // Then, each iterate catch the path from k node until i node, being k de begin node
        // and i the last node, and this path is stored in Vector<Link> rota and rota is
        // stored in HashMap<Integer, List<Link>> cacheRotas
        dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes(), ignorePhysicalImpairments);
        cacheRotas.put(k * 1000 + i, rota);
        //TODO, ATUALIZAÇÂO: ja teve alreração
        // antes era getLambda() e não tinha o for, era só a fibra zero e agora ta getTotalLambda()
        // e tem o for pra pegar todas as fibras
        for (int j = 0; j < network.getLinks()[k][i].getNumFibers(); j++) {
          if (nLambdaMax < network.getLinks()[k][i].getFiber(j).getTotalLambda())
            nLambdaMax = network.getLinks()[k][i].getFiber(j).getTotalLambda();
        }
      }
    }
    network.setnLambdaMax(nLambdaMax);

    for (int i = 1; i <= numOfCalls; i++) {
      if (scheduler.getCurrentTime() >= MAX_TIME) {
        listOfCalls.zerachamadas_mpu(scheduler.getCurrentTime());
        scheduler.resetTime_mpu();
      }
      //this method gerate source node, destine node
      // call time ??? Ask to danilo
      scheduler.generateCallRequisition();
      x = scheduler.getNextSourceNode();
      y = scheduler.getNextDestinationNode();
      // remove ended calls
      listOfCalls.retirarChamada(scheduler.getCurrentTime(), network.getNodes());

      tempCall = new Call();
      tempCall.setup(x, y, scheduler.getCurrentTime() + scheduler.getDuration(), scheduler.getDuration(),
          BIDIRECIONAL);

      CallList tempListOfCalls = new CallList(); // creates a vector of
      // call to represent
      // current processing
      // call
      tempListOfCalls.addChamada(tempCall); // this is required due
      // to
      // possible use of
      // regenerators

      int[] lambdaEncontrado_loc = { 0 };
      //TODO, jorge, acho que tem que se concentrar aqui pra estabelecer se houve um bloqueio
      // porque um caminho de luz que se iniciou na banda c encntrou um estágio que só tinha l e s
      // no caminho. Se isso acontecer é BLOQ_WAVELENGTH
      usarLambda = getStatusMultiBandRota(network, x, y, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado_loc);
      // ve se realmente existe uma rota
      if (usarLambda == BLOQ_WAVELENGTH) {
        numCallsBlockedLackOfWaveLenght++;
      } else if (usarLambda == BLOQ_BER) {
        numCallsBlockedUnacceptableBer++;
      } else if (usarLambda == BLOQ_DISPERSION) {
        numCallsBlockedDispersion++;
      } else { // the call has been accepted no regenerators were used
        //TODO jorge, acho que aqui é aquele lance que carmelo falou
        // de totalmente transparente em que escolhe um canal e ocupa ele.

        tempCall.setWavelengthUp(usarLambda);
        tempCall.setWavelengthDown(usarLambda);

        fibersUpLink.clear();
        fibersDownLink.clear();

        for (int j = 0; j < rotaUplink.size(); j++) {
          // scanning all links in found rote ja �
          // implementa��o p
          // multi-fibra
          fibersUpLink.add(rotaUplink.get(j).getFiber(rotaUplink.get(j).getAvailableFiber(usarLambda)));
          fibersDownLink.add(rotaDownlink.get(j).getFiber(rotaDownlink.get(j).getAvailableFiber(usarLambda)));
        }

        tempCall.alloc(fibersUpLink, fibersDownLink, network.getNodes());
        listOfCalls.addChamada(tempCall); // add actual tempCall
        // to the
        // active list of Calls
      }
      if ((numCallsBlockedLackOfWaveLenght + numCallsBlockedUnacceptableBer
          + numCallsBlockedDispersion) >= minCalls) {
        numOfCalls = i;
        // System.out.println("Stopping simulation with " + numOfCalls +
        // " calls...");
        break;
      }
    }
    // System.out.println("numCallsBlockedUnacceptableBer=" +
    // numCallsBlockedUnacceptableBer);
    // System.out.println("numCallsBlockedLackOfWaveLenght=" +
    // numCallsBlockedLackOfWaveLenght);
    // System.out.println("numCallsBlockedDispersion=" +
    // numCallsBlockedDispersion);
    network.getBp().setBer(numCallsBlockedUnacceptableBer / numOfCalls);
    network.getBp().setLambda(numCallsBlockedLackOfWaveLenght / numOfCalls);
    network.getBp().setDispersion(numCallsBlockedDispersion / numOfCalls);
    network.getBp().setMeanDist(listOfCalls.getDistanciaMedia());
    network.getBp()
        .setTotal((numCallsBlockedLackOfWaveLenght + numCallsBlockedUnacceptableBer + numCallsBlockedDispersion)
            / numOfCalls);
  }


  /**
   * Retorna um lambda para uso ou um codigo de erro.
   *
   * @param network
   * @param origem
   * @param destino
   * @param rotaUplink
   * @param rotaDownlink
   * @param cacheRotas
   * @param lambdaEncontrado
   */
  protected int getStatusMultiBandRota(
      MultiBandNetWorkProfile network,
      int origem, int destino,
      Vector<Link> rotaUplink,
      Vector<Link> rotaDownlink,
      Map<Integer,
          List<Link>> cacheRotas,
      int[] lambdaEncontrado) {
    int retorno = -1;
    rotaUplink.clear(); // limpa a vari�vel q calcula a rota direta
    rotaDownlink.clear(); // limpa a vari�vel q calcula a rota de volta
    // here is selected the route between origin node 'origem' and destine node 'destino'
    rotaUplink.addAll(cacheRotas.get(origem * 1000 + destino));
    // encontra o comprimento de onda por First Fit para os casos de
    // caminhos de menor distancia (dijkstra) reverte a rota encontrada
    // here rotaDownlink which is the opposite list to rotaUplink is filled
    for (int j = rotaUplink.size() - 1; j >= 0; j--) {
      int destination_loc = rotaUplink.get(j).getSource();
      int source_loc = rotaUplink.get(j).getDestination();
      rotaDownlink.add(network.getLinks()[source_loc][destination_loc]);
    }
    // procura para cada lambda

    retorno = firstFit(network, rotaUplink, rotaDownlink, lambdaEncontrado, retorno);

    if (retorno == -1) // nao h� lambda disponivel
    {
      return BLOQ_WAVELENGTH;
    }
    //TODO JORGE, até aqui ja está pronto, já conseguimos ver se ha ou não canal disponível
    // de ponta a ponta.

    //TODO jorge, aqui vem a questão dos impedimentos da camada física como
    // alargamento temporal, de certeza tem que alterar de acordo com a banda.
    // ATENÇÃO. ATENÇÃO ATENÇÃO, ATENÇÃO, ATENÇÃO: VOU COLOCAR ignorePhysicalImpairments como true
    // tem que desfazer pra poder contar com os impediments da camada física.

    if (/*!ignorePhysicalImpairments*/false) {
      // calcula o alargamento temporal devido � PMD da fibra de
      // transmissao
      double timePulseBroadeningPmd_loc = TIME_PULSE_BROADENING_PMD_P1
          * calculoPmd_fnb(rotaUplink, TAXA_BITS, D_PMD);
      // calcula o alargamento temporal resultante devido a PMD
      double pulseBroadeningPmd_loc = sqrt(timePulseBroadeningPmd_loc * timePulseBroadeningPmd_loc);
      // calcula o delta t(%) resultante
      double totalDeltaPulseBroadening = abs(DELTA_PMD_P1 * pulseBroadeningPmd_loc
          + calculoRd_fnb(rotaUplink, TAXA_BITS, LARGURA_LINHA, GAMA, retorno));

      if (totalDeltaPulseBroadening > DELTA) {
        return BLOQ_DISPERSION;
      }
      // a qualidade do servico e insuficiente, retorna valor
      // apropriado a
      // ser
      // tratado
      if (getSNR(network, rotaUplink, retorno) < SNR_THRESHOLD
          || getSNR(network, rotaDownlink, retorno) < SNR_THRESHOLD) {
        return BLOQ_BER;
      }
    }
    //TODO jorge, essas latências talvez devam virar parametros e serem escolhidas
    // conforme a banda.
    double latency = 0;
    double latencyAux = 0;
    // TODO: gravar dados de lat�ncia do trasmissor
    // delay de interfaces de rede
    latencyAux = 2 * 51.2e-9;
    latency += latencyAux;
    // delay de transponders
    latencyAux = 2 * 10e-6;
    latency += latencyAux;
    // delay de booster e pre
    latencyAux = 2 * 0.15e-6;
    latency += latencyAux;
    for (int j = rotaUplink.size() - 1; j >= 0; j--) {
      // TODO: gravar dados de latencia da rota
      latencyAux = 4.9e-6 * rotaUplink.get(j).getLength();
      latency += latencyAux;
      // delay de compensadores de dispers�o
      latencyAux = latencyAux * 0.25;
      latency += latencyAux;

    }
    // TODO: gravar dados de latencia no receptor
//		System.out.printf("Lat�ncia = %.4f\n", latency * 1e6);

    return retorno;
  }

  /**
   * Retorna um lambda para uso ou um c�digo de erro.
   *
   * @param network
   * @param origem
   * @param destino
   * @param rotaUplink
   * @param rotaDownlink
   * @param "cacheRotas"
   * @param lambdaEncontrado
   */
  protected int getStatusRota(NetworkProfile network, int origem, int destino, Vector<Link> rotaUplink,
      Vector<Link> rotaDownlink, int[] lambdaEncontrado) {
    int retorno = -1;
    // encontra o comprimento de onda por First Fit para os casos de minhops
    // e menor distancia (dijkstra) reverte a rota encontrada
    for (int j = rotaUplink.size() - 1; j >= 0; j--) {
      int destination_loc = rotaUplink.get(j).getSource();
      int source_loc = rotaUplink.get(j).getDestination();
      rotaDownlink.add(network.getLinks()[source_loc][destination_loc]);
    }
    // procura para cada lambda
    // retorno = firstFit(network, rotaUplink, rotaDownlink, lambdaEncontrado, retorno);
    // retorno = newMostUsed(network, rotaUplink, rotaDownlink,
    // lambdaEncontrado, retorno, mapa);

    if (retorno == -1) // nao ha lambda disponivel
    {
      return BLOQ_WAVELENGTH;
    }

    if (!ignorePhysicalImpairments) {
      // calcula o alargamento temporal devido a PMD da fibra de
      // transmissao
      double timePulseBroadeningPmd_loc = TIME_PULSE_BROADENING_PMD_P1
          * calculoPmd_fnb(rotaUplink, TAXA_BITS, D_PMD);
      // calcula o alargamento temporal resultante devido � PMD
      double pulseBroadeningPmd_loc = sqrt(timePulseBroadeningPmd_loc * timePulseBroadeningPmd_loc);
      // calcula o delta t(%) resultante
      double totalDeltaPulseBroadening = abs(DELTA_PMD_P1 * pulseBroadeningPmd_loc
          + calculoRd_fnb(rotaUplink, TAXA_BITS, LARGURA_LINHA, GAMA, retorno));

      if (totalDeltaPulseBroadening > DELTA) {
        return BLOQ_DISPERSION;
      }
      // a qualidade do servico e insuficiente, retorna valor
      // apropriado a
      // ser
      // tratado
     /* if (getSNR(network, rotaUplink, retorno) < SNR_THRESHOLD
          || getSNR(network, rotaDownlink, retorno) < SNR_THRESHOLD) {
        return BLOQ_BER;
      }*/
    }

    return retorno;
  }

  /**
   * this method receives networkProfile, where is get
   * the maxLambda in network which is being evaluated.
   * receives two lists rotaUplink and rotaDownlink which
   * contains the links between a node origin and a destiny
   * node. This aiming investigate if the existe a w wavelenth path
   * between two node which too are give.
   * So the main loop go through the all lambdas and the internal loop
   * go through link by link between two nodes.
   * if there is no lambda, so lambdaEncontrado don't get a new add lambda,
   * and it returns -1.
   *
   * @param network
   * @param rotaUplink
   * @param rotaDownlink
   * @param lambdaEncontrado
   * @param retorno
   */
  public int firstFit(MultiBandNetWorkProfile network, Vector<Link> rotaUplink, Vector<Link> rotaDownlink,
      int[] lambdaEncontrado, int retorno) {
    boolean lambdaDisponivel_loc;

    for (int nLambda_loc = 0; (nLambda_loc < network.getnLambdaMax()); nLambda_loc++) {
      lambdaDisponivel_loc = true;
      // this loop look all links between a pair of nodes origin destine, including all node in path
      // and appoint if a lambda is free for all path.
      for (int i = 0; i < rotaUplink.size(); i++) {
        // se lambda nao disponível
        // in this if verify route: first in up link, after in down link.
        var linkUp = rotaUplink.get(i);
        var linkDown = rotaDownlink.get(i);
        var fibersUp = linkUp.getFibers();
        var fibersDown = linkDown.getFibers();
        boolean fibersUpIsNotEmpty = !fibersUp.isEmpty();
        boolean fibersDownIsNotEmpty = !fibersDown.isEmpty();
        boolean lambdaUpIsNotAvailable = !linkUp.lambdaIsAvaliable(nLambda_loc);
        boolean lambdaDownIsNotAvailable = !linkDown.lambdaIsAvaliable(nLambda_loc);
        boolean isNotFreeToGo = fibersUpIsNotEmpty && lambdaUpIsNotAvailable;
        boolean isNotFreeToBack = fibersDownIsNotEmpty && lambdaDownIsNotAvailable;
        if (isNotFreeToGo || isNotFreeToBack) {
          lambdaDisponivel_loc = false;
          break;
        }
      }

      if (lambdaDisponivel_loc) {
        retorno = nLambda_loc;
        lambdaEncontrado[0] = retorno;
        break;
      }
    }
    return retorno;
  }

  public static double getSNR(MultiBandNetWorkProfile network, Vector<Link> path, int lambda) {
    int source, destino;
    double sIn, nIn, G1G2, LMux2Exp;
    double sInPart1, sInPart2, nInPart1, nInPart2, nInPart3, nInPartFwm, nInPartCrTalk;
    double potFwm;
    double somatorioPotencias = 0.0, epsilon_loc;
    double swAtenuation, muxDemuxGain, fiberGain;

    if (path.isEmpty()) {
      return INF;
    }
    source = path.get(0).getSource();
    //TODO atenção aqui jorge tem que ver se funciona pegar pelo indice da lista de
    // características do wss, anteriormente esse parametro era uma double e
    // agora é uma lista, com a minha modificaçõa foi para uma lista e agora tem
    // o problema de que tem que multiplicar pelo epsilon de cada nó e não por
    // um epsilon único, como era no início
    // Obs: epsilon é o fator de isolamento do WSS.
    epsilon_loc = network.getEpsilon().get(source);
    //TODO epsilon loq tenha que ir como parametro do método somatorioPotSwitch(),
    // ou o método somatorioPotSwitch() tenha que ter acesso aos tipos de wss do cromossomo
    // para cunsultar direto na classe Equipaments.
    somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, source, lambda);
    //TODO , jorge aqui por exemoplo: no todo anterior epsilon_loc pega o fator de isolamento
    // do wss do primeiro nó, mas na nova abordagem o fator de isolamento do primeiro nó pode não ser
    // o mesmo dos demais nós. então tem que estudar essa parte e modificar se necessário.
    somatorioPotencias *= epsilon_loc;
    // nIn deve ser noiseIn
    nIn = (network.getNodes().get(source).getLaserPower() / network.getNodes().get(source).getLaserSNR())
        + somatorioPotencias;
    //sIn deve ser signalIn
    sIn = network.getNodes().get(source).getLaserPower();

    for (int j = 0; j < path.size(); j++) {
      nInPartFwm = 0.0;
      nInPartCrTalk = 0.0;
      nInPart3 = 0.0;
      source = path.get(j).getSource();
      destino = path.get(j).getDestination();

      swAtenuation = network.getNodes().get(destino).getSwitchAtenuation();
      //TODO, jorge, aqui em muxDemuxGain, fiberGain, LMux2Exp, G1G2 e nInPart3 vai depender
      // da banda que a fibra opera, eu acredito, isso provavelmente vai depender
      // de estudos para ser elucidado
      muxDemuxGain = path.get(j).getFiber(0).getMuxDemuxGain();
      fiberGain = path.get(j).getFiber(0).getGain();
      LMux2Exp = muxDemuxGain * muxDemuxGain * path.get(j).getFiber(0).getGain();

      G1G2 = path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
          * path.get(j).getFiber(0).getG0PreAmp(path.get(j).getFiber(0).getSumPowerD());

      sInPart1 = (sIn * G1G2);
      sInPart2 = LMux2Exp * swAtenuation;

      sIn = sInPart1 * sInPart2;

      nInPart1 = G1G2 * LMux2Exp * swAtenuation;
      nInPart2 = nIn;
      nInPart3 = ((PLANCK * path.get(j).getFiber(0).getFrequency(lambda) * B0) / (2 * muxDemuxGain))
          * (path.get(j)
          .getFiber(
              0)
          .getBoosterF(path.get(j).getFiber(0).getSumPowerB())
          + (path.get(j).getFiber(0).getPreF(path.get(j).getFiber(0).getSumPowerD())
          / (path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
          * fiberGain)));

      // desconsiderando FWM por enquanto
      // potFwm = path.get(j).getFiber(0).getSumPowerFWM(lambda,
      // network.getNodes());
      // nInPartFwm = potFwm
      // / (muxDemuxGain * fiberGain * path.get(j).getFiber(0)
      // .getG0Booster(path.get(j).getFiber(0).getSumPowerB()));

      somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, destino, lambda);
      nInPartCrTalk = (somatorioPotencias * epsilon_loc)
          / (muxDemuxGain * muxDemuxGain * swAtenuation * fiberGain * G1G2);

      nIn = nInPart1 * (nInPart2 + nInPartFwm + nInPart3 + nInPartCrTalk);
    }

    return (sIn / nIn);
  }

  public static double getSNRAse(NetworkProfile network, Vector<Link> path, int lambda) {
    int source, destino;
    double sIn, nIn, G1G2, LMux2Exp;
    double sInPart1, sInPart2, nInPart1, nInPart2, nInPart3;
    double somatorioPotencias = 0.0, epsilon_loc;
    double swAtenuation, muxDemuxGain, fiberGain;

    if (path.isEmpty()) {
      return INF;
    }
    source = path.get(0).getSource();
    epsilon_loc = network.getEpsilon();

    somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, source, lambda);
    somatorioPotencias *= epsilon_loc;
    nIn = (network.getNodes().get(source).getLaserPower() / network.getNodes().get(source).getLaserSNR())
        + somatorioPotencias;
    sIn = network.getNodes().get(source).getLaserPower();

    for (int j = 0; j < path.size(); j++) {
      nInPart3 = 0.0;
      source = path.get(j).getSource();
      destino = path.get(j).getDestination();

      swAtenuation = network.getNodes().get(destino).getSwitchAtenuation();
      muxDemuxGain = path.get(j).getFiber(0).getMuxDemuxGain();
      fiberGain = path.get(j).getFiber(0).getGain();
      LMux2Exp = muxDemuxGain * muxDemuxGain * path.get(j).getFiber(0).getGain();

      G1G2 = path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
          * path.get(j).getFiber(0).getG0PreAmp(path.get(j).getFiber(0).getSumPowerD());

      sInPart1 = (sIn * G1G2);
      sInPart2 = LMux2Exp * swAtenuation;

      sIn = sInPart1 * sInPart2;

      nInPart1 = G1G2 * LMux2Exp * swAtenuation;
      nInPart2 = nIn;
      nInPart3 = ((PLANCK * path.get(j).getFiber(0).getFrequency(lambda) * B0) / (2 * muxDemuxGain))
          * (path.get(j)
          .getFiber(
              0)
          .getBoosterF(path.get(j).getFiber(0).getSumPowerB())
          + (path.get(j).getFiber(0).getPreF(path.get(j).getFiber(0).getSumPowerD())
          / (path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
          * fiberGain)));

      somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, destino, lambda);

      nIn = nInPart1 * (nInPart2 + nInPart3);
    }

    return (sIn / nIn);
  }
}
