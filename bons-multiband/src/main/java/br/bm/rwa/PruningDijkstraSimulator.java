package br.bm.rwa;


import br.bm.core.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static br.bm.core.Call.BIDIRECIONAL;
import static br.bm.rwa.Dijkstra.dijkstra_fnb;


public class PruningDijkstraSimulator extends SimpleDijkstraSimulator {

	private boolean regRealizada = false;

	
	public void pruning(NetworkProfile network, boolean[] impossiblePositions) {
		//int x = 0, y = 0, 
		int tempFiberAvailble = 0;
		int nLambdaMax = 0;
		int usarLambda = 0;
		int numCallsBlockedLackOfWaveLenght = 0;
		int numCallsBlockedUnacceptableBer = 0;
		int numCallsBlockedDispersion = 0;
		Vector<Link> rotaUplink = new Vector<Link>(), rotaDownlink = new Vector<Link>();
		Vector<Fiber> fibersUpLink = new Vector<Fiber>(), fibersDownLink = new Vector<Fiber>();
		double numOfCalls = network.getNumOfCalls();
		CallList listOfCalls = new CallList();
		CallList tempListOfCalls = new CallList();  //creates a vector of call to represent current processing call
		Call tempCall = new Call();
		long qtdeTempoZerado = 0;
		boolean caminhoTotalmenteOptico = false;

		int contConv = 0;
		int contReg = 0;
		int regenerou = 0;

		BlockUtil block = BlockUtil.getInstance();

		// of minumum acceptatle snr from dB to linear
		nLambdaMax = 0;
		HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
		// Procura qual a fibra que possui mais comprimentos de onda
		for (int k = 0; k < network.getNodes().size(); k++) {
			for (int i = 0; i < network.getNodes().size(); i++) {	
				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes());
				cacheRotas.put(k * 1000 + i, rota);
				if (nLambdaMax < network.getLinks()[k][i].getFiber(0).getLambda())
					nLambdaMax = network.getLinks()[k][i].getFiber(0).getLambda();
			}

		}
		network.setnLambdaMax(nLambdaMax);
		CallSchedulerUniform scheduler = new CallSchedulerUniform(network.getMeanRateBetweenCalls(),
				MEAN_RATE_CALLS_DUR, network.getNodes().size());

		int indexVariaveis = -1;
		
		for(int i=0; i<impossiblePositions.length; i++){
			impossiblePositions[i] = true;
		}

		for(int x=0; x<network.getNodes().size(); x++){
			for(int y=x+1; y<network.getNodes().size(); y++){

				indexVariaveis++;
				impossiblePositions[indexVariaveis] = true;
				
				for (int i = 1; i <= 40; i++) {
					if (scheduler.getCurrentTime() >= MAX_TIME) {
						listOfCalls.zerachamadas_mpu(scheduler.getCurrentTime());
						scheduler.resetTime_mpu();
						qtdeTempoZerado++;
					}
					scheduler.generateCallRequisition();


					// remove ended calls
					listOfCalls.retirarChamada(scheduler.getCurrentTime(), network.getNodes());
					tempCall = new Call();
					tempCall.setup(x, y, scheduler.getCurrentTime() + scheduler.getDuration(), scheduler.getDuration(),
							BIDIRECIONAL);

					tempListOfCalls.getListaChamadas().clear();
					tempListOfCalls.addChamada(tempCall);	// this is required due to possible use of regenerators

					int[] lambdaEncontrado_loc = {0};

					usarLambda = super.getStatusRota(network, x, y, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado_loc);

					boolean entrouConv = false;
					boolean entrouReg = false;

					if(usarLambda >= 0){
						caminhoTotalmenteOptico = true;
					}else{

						Vector<Integer> regeneratorLocations_loc = new Vector<Integer>();
						regNodes_fnb( rotaUplink, network.getNodes(), regeneratorLocations_loc );

						//se houver regenerador disponivel no caminho, entre em REGENERADOR_QoS ou REGENERADOR_LAMBDA.
						//sen�o, passe direto, i�, bloqueie de acordo com status_loc, calculado anteriormente.
						if(regeneratorLocations_loc.size()>2){

							if(usarLambda == BLOQ_WAVELENGTH){  //se bloquear por comprimento de onda, tente regenerar
								caminhoTotalmenteOptico = false;

								usarLambda = statusRotaConv_fnb(network, x, y, rotaUplink, 
										rotaDownlink, cacheRotas, regeneratorLocations_loc,tempListOfCalls);

								entrouConv = true;	
								contConv++;

								if(this.regRealizada){
									usarLambda = 1000;
								}
								//POR ENQUANTO, BLOQUEIE AS CHAMADAS N�O ESTABELECIDAS DEVIDO A BLOQ_WAVELENGTH.

								//obs: AO sair deste trecho do c�digo, fazer usarLambda_loc um n�mero positivo qualquer;

								//usarLambda_loc=BLOQ_WAVELENGTH;
							}

							if( usarLambda == BLOQ_BER || usarLambda == BLOQ_DISPERSION){ //se bloquear por QoS tente regenerar
								caminhoTotalmenteOptico = false;

								//obs: AO sair deste trecho do c�digo, fazer usarLambda_loc um n�mero positivo qualquer;
								//TODO: resolver problema do usarLambda.
								usarLambda = statusRotaReg_fnb(network, x, y, rotaUplink, 
										rotaDownlink, cacheRotas, regeneratorLocations_loc, tempListOfCalls, lambdaEncontrado_loc[0]);


								entrouReg = true;
								contReg++;

								if(this.regRealizada){
									usarLambda = 1000;
								}
							}
						}
					}


					// ve se realmente existe uma rota
					if (usarLambda == BLOQ_WAVELENGTH) {
						numCallsBlockedLackOfWaveLenght++;
					} else if (usarLambda == BLOQ_BER) {
						numCallsBlockedUnacceptableBer++;
						impossiblePositions[indexVariaveis] = false;
						break;
					} else if (usarLambda == BLOQ_DISPERSION) {
						numCallsBlockedDispersion++;
						impossiblePositions[indexVariaveis] = false;
						break;
					} else { // the call has been accepted no regenerators were used
						if (caminhoTotalmenteOptico){
							tempCall.setWavelengthUp(usarLambda);
							tempCall.setWavelengthDown(usarLambda);

							fibersUpLink.clear();
							fibersDownLink.clear();

							for(int j = 0; j < rotaUplink.size(); j++){	  //scanning all links in in found rote
								tempFiberAvailble = rotaUplink.get(j).getAvailableFiber(usarLambda);  //ja � implementa��o p multi-fibra
								fibersUpLink.add(rotaUplink.get(j).getFiber(tempFiberAvailble));

								tempFiberAvailble = rotaUplink.get(j).getAvailableFiber(usarLambda);  //ja � implementa��o p multi-fibra
								fibersDownLink.add(rotaDownlink.get(j).getFiber(tempFiberAvailble));
							}

							tempCall.alloc(fibersUpLink,fibersDownLink,network.getNodes());
							listOfCalls.addChamada(tempCall); //add actual tempCall to the active list of Calls
						}
						else {        //the call has been accepted  regenerators were used

							if(!entrouConv && !entrouReg){
								System.out.println("Entrou!");
							}

							regenerou++;
							for (int k = 0;k<tempListOfCalls.getListaChamadas().size();k++) {
								tempListOfCalls.getListaChamadas().get(k).alloc(network.getNodes());
								listOfCalls.addChamada(tempListOfCalls.getListaChamadas().get(k));
							}
						}
					}
				}

			}

		}
		network.getBp().setBer(numCallsBlockedUnacceptableBer / numOfCalls);
		network.getBp().setLambda(numCallsBlockedLackOfWaveLenght / numOfCalls);
		network.getBp().setDispersion(numCallsBlockedDispersion / numOfCalls);
		network.getBp().setMeanDist(listOfCalls.getDistanciaMedia());
		network.getBp().setTotal(
				(numCallsBlockedLackOfWaveLenght + numCallsBlockedUnacceptableBer + numCallsBlockedDispersion)
				/ numOfCalls);

		network.getBp().setContConv(contConv);
		network.getBp().setContReg(contReg);
		network.getBp().setContRegenerou(regenerou);

		System.out.println("Conv = " + contConv + "  Reg = " + contReg 
				+ "   Regenerou = " + regenerou);

	}


	/******************************************************************
	 *****  FUNCTION....: statusRotaReg_fnb
	 *****  DESCRIPTION.: choose the nodes on which the call will be regenerated.
	 *****  PARAMETERS.:  (4 next) check if these effects must, or not, be considered,
	 *****                bits rate(PMD), PMD coefficient(PMD), delta(PMD)
	 *******************************************************************/
	//====================================================================================//
	//=====================Novo c�digo de regenerados para QoS============================//
	//==========Regenera no n� mais distante partindo do mais pr�ximo da origem===========//
	//====================================================================================//
	private int statusRotaReg_fnb(NetworkProfile network, int origem,
			int destino, Vector<Link> rotaUplink, Vector<Link> rotaDownlink,
			Map<Integer, List<Link>> cacheRotas,
			Vector<Integer> regeneratorLocationsLoc,
			CallList listOfCalls, int lambdaEncontrado) {

		Call tempCall = new Call();
		NetworkBP bp = new NetworkBP();

		//seta informa��es a partir da primeira chamada do vetor
		double tempodequeda_loc = listOfCalls.getListaChamadas().get(0).getFallTime();
		double duracao_loc = listOfCalls.getListaChamadas().get(0).getDuration();

		listOfCalls.getListaChamadas().clear();

		//determina o caminho atual
		Vector<Link> trechoEmAnalise_uplink, trechoEmAnalise_downlink, trechoEmCalculo_uplink;

		//Armazena a atual cadeia de fibras do caminho q esta sendo montado
		Vector<Fiber> fibersInUpLink = new Vector<Fiber>();

		for(int i=0; i<network.getNodes().size(); i++){
			network.getNodes().get(i).resetMarkedRegenerators();
		}

		this.regRealizada = false;		
		double[] outputPulseBroadening_uplink_loc={0};
		double nIn_uplink_loc=0, sIn_uplink_loc = 0,sOut_uplink_loc = 0,nOut_uplink_loc=0;
		double currentTotalPulseBroadening_uplink_loc = 0.0;
		double[] outputPulseBroadening_downlink_loc= {0};
		double sIn_downlink_loc=0,nIn_downlink_loc=0,nOut_downlink_loc=0,sOut_downlink_loc=0;
		double currentTotalPulseBroadening_downlink_loc = 0.0;
		double fatorDeRuidoDownlink_loc=0, fatorDeRuidoUplink_loc=0;

		double somatorioPotencias = 0.0;
		if(network.isCrTalk()){
			somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), network.getNodes(), rotaUplink,
					regeneratorLocationsLoc.get(0), lambdaEncontrado);
			somatorioPotencias *= network.getEpsilon();
		}

		nIn_downlink_loc = ( network.getNodes().get(regeneratorLocationsLoc.get(0)).getLaserPower() /
				network.getNodes().get(regeneratorLocationsLoc.get(0)).getLaserSNR())+somatorioPotencias;

		sIn_downlink_loc = network.getNodes().get(regeneratorLocationsLoc.get(0)).getLaserPower();

		//============== CALCULO DA QOS TRECHO A TRECHO ===========================

		int tempOrig_index, tempDest_index;
		tempOrig_index = 0;
		tempDest_index = 1;
		trechoEmCalculo_uplink = cutPath_fnb(rotaUplink, regeneratorLocationsLoc.get(tempOrig_index), 
				regeneratorLocationsLoc.get(tempDest_index));

		while(true){

			//corta o trecho em analise
			trechoEmAnalise_uplink = cutPath_fnb(rotaUplink, regeneratorLocationsLoc.get(tempOrig_index), 
					regeneratorLocationsLoc.get(tempDest_index));

			boolean testeQos_uplink, testeQos_downlink;
			testeQos_downlink = true;  testeQos_uplink = true;

			testeQos_uplink = Funcoes.checkPhysImpairm_fnb(network, trechoEmCalculo_uplink, 
					lambdaEncontrado, sIn_uplink_loc, nIn_uplink_loc, sOut_uplink_loc, nOut_uplink_loc, outputPulseBroadening_uplink_loc,
					currentTotalPulseBroadening_uplink_loc, bp);

			if(testeQos_uplink){
				//corta o trecho em Analise
				trechoEmAnalise_downlink = cutPath_fnb(rotaDownlink, regeneratorLocationsLoc.get(tempDest_index), 
						regeneratorLocationsLoc.get(tempOrig_index));

				//calcula a potencia de entrada
				somatorioPotencias = 0.0;

				if(network.isCrTalk()){
					somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), network.getNodes(), 
							trechoEmAnalise_downlink, tempDest_index, lambdaEncontrado);
					somatorioPotencias *= network.getEpsilon();
				}

				nIn_downlink_loc = ( network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserPower() /
						network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserSNR())+somatorioPotencias;

				sIn_downlink_loc = network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserPower();

				testeQos_downlink = Funcoes.checkPhysImpairm_fnb(network, trechoEmAnalise_downlink, lambdaEncontrado, 
						sIn_downlink_loc, nIn_downlink_loc, sOut_downlink_loc, nOut_downlink_loc, 
						outputPulseBroadening_downlink_loc, currentTotalPulseBroadening_downlink_loc, bp);

				if(testeQos_downlink){ //QoS downlink satisfeito?

					if( tempDest_index < (regeneratorLocationsLoc.size() - 2) ){ // ainda h� reg. adiante? SIM
						sIn_uplink_loc = sOut_uplink_loc;
						nIn_uplink_loc = nOut_uplink_loc;

						currentTotalPulseBroadening_uplink_loc = outputPulseBroadening_uplink_loc[0];
						trechoEmCalculo_uplink = cutPath_fnb(rotaUplink, regeneratorLocationsLoc.get(tempDest_index), 
								regeneratorLocationsLoc.get(tempDest_index+1));

						tempDest_index++;
						continue;
					}
					else if( tempDest_index == (regeneratorLocationsLoc.size()-1) ){
						//Aloca fibras
						for(int m=0; m<trechoEmAnalise_uplink.size(); m++){
							fibersInUpLink.add(trechoEmAnalise_uplink.get(m).getFiber(0));
						}

						//Estabelece a chamada
						tempCall.setWavelengthUp(lambdaEncontrado);
						tempCall.setFibersUpLink(fibersInUpLink);
						tempCall.setSource(regeneratorLocationsLoc.get(tempOrig_index));
						tempCall.setDestination(regeneratorLocationsLoc.get(tempDest_index));
						tempCall.setFallTime(tempodequeda_loc);
						tempCall.setDuration(duracao_loc);
						tempCall.setCallType(Call.UNIDIRECIONAL_REGENERADA);

						listOfCalls.addChamada(tempCall);

						fibersInUpLink.clear();

						for(int m=0; m<trechoEmAnalise_downlink.size(); m++){
							fibersInUpLink.add(trechoEmAnalise_downlink.get(m).getFiber(0));
						}

						//Estabeleces a chamada de  volta
						tempCall.setWavelengthUp(lambdaEncontrado);
						tempCall.setFibersUpLink(fibersInUpLink);
						tempCall.setSource(regeneratorLocationsLoc.get(tempDest_index));
						tempCall.setDestination(regeneratorLocationsLoc.get(tempOrig_index));
						tempCall.setFallTime(tempodequeda_loc);
						tempCall.setDuration(duracao_loc);
						tempCall.setCallType(Call.UNIDIRECIONAL);

						listOfCalls.addChamada(tempCall);

						fibersInUpLink.clear();
						this.regRealizada = true;
						break;
					}//  else if (tempDest_index == (regeneratorLocations_par.size()-1)){

					////////////////////////////////////////////////////////////
					//ETAPA 2 --- IN�CIO ///////////////////////////////////////
					////////////////////////////////////////////////////////////

					else{ //Ainda h� reg. adiante? NAO

						if( tempOrig_index == 0 ){

							//REGENERA O SINAL --- IN�CIO //
							//calcula a potencia de entrada
							somatorioPotencias = 0.0;

							if(network.isCrTalk()){
								somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), network.getNodes(), 
										trechoEmAnalise_uplink, regeneratorLocationsLoc.get(tempDest_index), lambdaEncontrado);
								somatorioPotencias *= network.getEpsilon();
							}

							nIn_uplink_loc = ( network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserPower()/
									network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserSNR())+somatorioPotencias;

							sIn_downlink_loc = network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserPower();

							currentTotalPulseBroadening_uplink_loc = 0.0;

							//REGENERA O SINAL --- FIM    //

							// Decrementar a qtd de regeneradores livres no n�;
							network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).decNumFreeRegenerators();
							network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).markRegenerators();

							//Aloca fibras ida
							for(int m=0; m<trechoEmAnalise_uplink.size(); m++){
								fibersInUpLink.add(trechoEmAnalise_uplink.get(m).getFiber(0));
							}

							//Estabelece a chamada
							tempCall.setWavelengthUp(lambdaEncontrado);
							tempCall.setFibersUpLink(fibersInUpLink);
							tempCall.setSource(regeneratorLocationsLoc.get(tempOrig_index));
							tempCall.setDestination(regeneratorLocationsLoc.get(tempDest_index));
							tempCall.setFallTime(tempodequeda_loc);
							tempCall.setDuration(duracao_loc);
							tempCall.setCallType(Call.UNIDIRECIONAL_REGENERADA);

							if(tempOrig_index == 0){
								tempCall.setCallType(Call.UNIDIRECIONAL);
							}

							listOfCalls.addChamada(tempCall);

							fibersInUpLink.clear();

							for(int m=0; m<trechoEmAnalise_downlink.size(); m++){
								fibersInUpLink.add(trechoEmAnalise_downlink.get(m).getFiber(0));
							}

							tempCall.setWavelengthUp(lambdaEncontrado);
							tempCall.setFibersUpLink(fibersInUpLink);
							tempCall.setSource(regeneratorLocationsLoc.get(tempDest_index));
							tempCall.setDestination(regeneratorLocationsLoc.get(tempOrig_index));
							tempCall.setFallTime(tempodequeda_loc);
							tempCall.setDuration(duracao_loc);
							tempCall.setCallType(Call.UNIDIRECIONAL);

							listOfCalls.addChamada(tempCall);

							fibersInUpLink.clear();

							//N� regenerador analisado � assumido como n� fonte intermedi�rio (NOVO PSEUDOC�DIGO)

							tempOrig_index = tempDest_index;
							tempDest_index = regeneratorLocationsLoc.size() - 1;
							trechoEmCalculo_uplink = cutPath_fnb(rotaUplink, regeneratorLocationsLoc.get(tempOrig_index), 
									regeneratorLocationsLoc.get(tempDest_index));
							continue;
						}
						else{
							sIn_uplink_loc = sOut_uplink_loc;
							nIn_uplink_loc = nOut_uplink_loc;
							currentTotalPulseBroadening_uplink_loc = outputPulseBroadening_uplink_loc[0];
							trechoEmCalculo_uplink = cutPath_fnb(rotaUplink, regeneratorLocationsLoc.get(tempDest_index), 
									regeneratorLocationsLoc.get(tempDest_index+1));
							tempDest_index++;
							continue;
						}
					}//  chave do  else { //Ainda h� reg. adiante? NAO (ETAPA 2)

				}//         if (testeQoS_downlink_loc==true)

			}//         if (testeQoS_uplink_loc==true)

			if(!testeQos_uplink || !testeQos_downlink){
				if((tempDest_index - tempOrig_index) == 1 ){
					for(int u=0; u<network.getNodes().size(); u++){
						network.getNodes().get(u).rescueMarkedRegenerators();
						network.getNodes().get(u).resetMarkedRegenerators();
					}
					if(bp.getDispersion() > 0){
						return BLOQ_DISPERSION;
					}
					if(bp.getBer() > 0){
						return BLOQ_BER;
					}
				}else{
					tempDest_index--;

					//REGENERA O SINAL --- IN�CIO //
					//calcula a potencia de entrada
					somatorioPotencias = 0.0;
					trechoEmAnalise_uplink = cutPath_fnb(rotaUplink, regeneratorLocationsLoc.get(tempOrig_index), 
							regeneratorLocationsLoc.get(tempDest_index));

					if(network.isCrTalk()){
						somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), network.getNodes(), 
								trechoEmAnalise_uplink, regeneratorLocationsLoc.get(tempDest_index), lambdaEncontrado);

						somatorioPotencias *= network.getEpsilon();
					}

					nIn_uplink_loc = ( network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserPower()/
							network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserSNR())+somatorioPotencias;

					sIn_uplink_loc = network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).getLaserPower();

					currentTotalPulseBroadening_uplink_loc = 0.0;
					//REGENERA O SINAL --- FIM    //

					//Decrementar a qtd de regeneradores livres no n�;
					network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).decNumFreeRegenerators();
					network.getNodes().get(regeneratorLocationsLoc.get(tempDest_index)).markRegenerators();

					//Aloca fibras ida
					for(int m=0; m<trechoEmAnalise_uplink.size(); m++){
						fibersInUpLink.add(trechoEmAnalise_uplink.get(m).getFiber(0));
					}

					//Estabelece a chamada
					tempCall.setWavelengthUp(lambdaEncontrado);
					tempCall.setFibersUpLink(fibersInUpLink);
					tempCall.setSource(regeneratorLocationsLoc.get(tempOrig_index));
					tempCall.setDestination(regeneratorLocationsLoc.get(tempDest_index));
					tempCall.setFallTime(tempodequeda_loc);
					tempCall.setDuration(duracao_loc);
					tempCall.setCallType(Call.UNIDIRECIONAL_REGENERADA);

					if(tempOrig_index == 0){
						tempCall.setCallType(Call.UNIDIRECIONAL);
					}

					listOfCalls.addChamada(tempCall);

					fibersInUpLink.clear();

					trechoEmAnalise_downlink = cutPath_fnb(rotaDownlink, regeneratorLocationsLoc.get(tempDest_index), 
							regeneratorLocationsLoc.get(tempOrig_index));

					//Aloca fibras volta
					for(int m=0; m<trechoEmAnalise_downlink.size(); m++){
						fibersInUpLink.add(trechoEmAnalise_downlink.get(m).getFiber(0));
					}

					//Estabelecer a chamada
					tempCall.setWavelengthUp(lambdaEncontrado);
					tempCall.setFibersUpLink(fibersInUpLink);
					tempCall.setSource(regeneratorLocationsLoc.get(tempDest_index));
					tempCall.setDestination(regeneratorLocationsLoc.get(tempOrig_index));
					tempCall.setFallTime(tempodequeda_loc);
					tempCall.setDuration(duracao_loc);
					tempCall.setCallType(Call.UNIDIRECIONAL);

					listOfCalls.addChamada(tempCall);

					fibersInUpLink.clear();

					tempOrig_index = tempDest_index;
					tempDest_index++;
					trechoEmCalculo_uplink = cutPath_fnb(rotaUplink, regeneratorLocationsLoc.get(tempOrig_index), 
							regeneratorLocationsLoc.get(tempDest_index));
					continue;
				}

			}

		}//=======FIM----CALCULO DA QOS TRECHO A TRECHO ===========================

		return 0;
	}// chave do in�cio da fun��o

	/******************************************************************
	 *****  FUNCTION....: statusRotaConv_fnb
	 *****  DESCRIPTION.: choose the nodes on which the call will be regenerated.
	 *****  PARAMETERS.:  (4 next) check if these effects must, or not, be considered,
	 *****                bits rate(PMD), PMD coefficient(PMD), delta(PMD)
	 *******************************************************************/
	//====================================================================================//
	//=====================Novo c�digo de regenerados para QoS============================//
	//Converte no n� mais distante partindo do mais pr�ximo da origem e tamb�m regenera por QoS//
	//====================================================================================//
	private int statusRotaConv_fnb(NetworkProfile network, int origem,
			int destino, Vector<Link> rotaUplink, Vector<Link> rotaDownlink,
			Map<Integer, List<Link>> cacheRotas, Vector<Integer> regeneratorLocations,
			CallList listOfCalls) {

		Call tempCall = new Call();
		NetworkBP bp = new NetworkBP();

		//seta informa�oes apartir da primeira chamada do vetor
		double tempodequeda_loc  =  listOfCalls.getListaChamadas().get(0).getFallTime();
		double duracao_loc       =  listOfCalls.getListaChamadas().get(0).getDuration();

		listOfCalls.getListaChamadas().clear();

		//determina o caminho atual
		Vector<Link> trechoEmAnalise_uplink_loc,trechoEmAnalise_downlink_loc,trechoEmCalculo_uplink_loc;

		//Armazena a atual cadeia de fibras do caminho q esta sendo montado
		Vector<Fiber> fibersInUpLink_loc = new Vector<Fiber>();

		for(int i=0; i<network.getNodes().size(); i++){
			network.getNodes().get(i).resetMarkedRegenerators();
		}

		boolean primeiro_lambda_loc;
		this.regRealizada = false;
		double[] outputPulseBroadening_uplink_loc = {0};
		double nIn_uplink_loc, sIn_uplink_loc,sOut_uplink_loc = 0,nOut_uplink_loc = 0;
		double currentTotalPulseBroadening_uplink_loc = 0.0;
		double[] outputPulseBroadening_downlink_loc = {0};
		double sIn_downlink_loc,nIn_downlink_loc,nOut_downlink_loc = 0,sOut_downlink_loc = 0;
		double currentTotalPulseBroadening_downlink_loc = 0.0;
		double fatorDeRuidoDownlink_loc, fatorDeRuidoUplink_loc;
		int lambda_trecho_Atual_loc, lambda_Anterior_loc = 0;

		//Antes de calcular a pot�ncia de entrada, deve fazer o FF da origem at� o primeiro n� com regenerador
		trechoEmAnalise_uplink_loc=cutPath_fnb(rotaUplink,regeneratorLocations.get(0),regeneratorLocations.get(1));
		trechoEmAnalise_downlink_loc=cutPath_fnb(rotaDownlink,regeneratorLocations.get(1),regeneratorLocations.get(0));

		//fun��o first-fit
		lambda_trecho_Atual_loc=firstFit_fnb(trechoEmAnalise_uplink_loc, trechoEmAnalise_downlink_loc,
				network.getnLambdaMax());

		if (lambda_trecho_Atual_loc == BLOQ_WAVELENGTH)
			return lambda_trecho_Atual_loc;

		primeiro_lambda_loc = true;

		//=============IN�CIO BUSCA LAMBDA TRECHO A TRECHO===========================

		int tempOrig_index, tempDest_index;
		tempOrig_index=0;
		tempDest_index=1;

		while(true){

			//corta o trecho em analise
			trechoEmAnalise_uplink_loc=cutPath_fnb(rotaUplink,regeneratorLocations.get(tempOrig_index),
					regeneratorLocations.get(tempDest_index));
			trechoEmAnalise_downlink_loc=cutPath_fnb(rotaDownlink,regeneratorLocations.get(tempDest_index),
					regeneratorLocations.get(tempOrig_index));

			if (primeiro_lambda_loc == false){
				//fun��o first-fit
				lambda_trecho_Atual_loc=firstFit_fnb(trechoEmAnalise_uplink_loc, trechoEmAnalise_downlink_loc,
						network.getnLambdaMax());
			}
			primeiro_lambda_loc = false;

			if(lambda_trecho_Atual_loc == BLOQ_WAVELENGTH){
				if ((tempDest_index-tempOrig_index)==1){
					for(int u=0;u<network.getNodes().size();u++){
						network.getNodes().get(u).rescueMarkedRegenerators();
						network.getNodes().get(u).resetMarkedRegenerators();
					}
					return BLOQ_WAVELENGTH;
				}else{
					tempDest_index--;

					//estabelece trecho anterior
					//REGENERA O SINAL --- IN�CIO //
					//calcula a potencia de entrada
					double somatorioPotencias =0.0;
					trechoEmAnalise_uplink_loc=cutPath_fnb(rotaUplink,regeneratorLocations.get(tempOrig_index),
							regeneratorLocations.get(tempDest_index));
					if (network.isCrTalk()){
						somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), network.getNodes(), 
								trechoEmAnalise_uplink_loc/* path_par_uplink*/, regeneratorLocations.get(tempDest_index), 
								lambda_Anterior_loc);
						somatorioPotencias *= network.getEpsilon();
					} 

					nIn_uplink_loc = (network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserPower()/
							network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserSNR())+somatorioPotencias;

					sIn_uplink_loc =  network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserPower();

					currentTotalPulseBroadening_uplink_loc = 0.0;
					//REGENERA O SINAL --- FIM    //

					//Decrementar a qtd de regeneradores livres no n�;
					network.getNodes().get(regeneratorLocations.get(tempDest_index)).decNumFreeRegenerators();
					network.getNodes().get(regeneratorLocations.get(tempDest_index)).markRegenerators();

					//Aloca fibras ida
					for(int m=0; m<trechoEmAnalise_uplink_loc.size();m++){
						fibersInUpLink_loc.add(trechoEmAnalise_uplink_loc.get(m).getFiber(0));
					}

					//Estabelece a chamada (tempCall_loc)
					tempCall.setWavelengthUp(lambda_Anterior_loc);
					tempCall.setFibersUpLink(fibersInUpLink_loc);
					tempCall.setSource(regeneratorLocations.get(tempOrig_index));
					tempCall.setDestination(regeneratorLocations.get(tempDest_index));
					tempCall.setFallTime(tempodequeda_loc);
					tempCall.setDuration(duracao_loc);
					tempCall.setCallType(Call.UNIDIRECIONAL_REGENERADA);

					if(tempOrig_index==0){
						tempCall.setCallType(Call.UNIDIRECIONAL);
					}
					listOfCalls.addChamada(tempCall);

					fibersInUpLink_loc.clear();

					trechoEmAnalise_downlink_loc = cutPath_fnb(rotaDownlink, regeneratorLocations.get(tempDest_index),
							regeneratorLocations.get(tempOrig_index));

					for(int m=0; m<trechoEmAnalise_downlink_loc.size(); m++){
						fibersInUpLink_loc.add(trechoEmAnalise_downlink_loc.get(m).getFiber(0));
					}

					//estabelecer a chamada
					tempCall.setWavelengthUp(lambda_Anterior_loc);
					tempCall.setFibersUpLink(fibersInUpLink_loc);
					tempCall.setSource(regeneratorLocations.get(tempDest_index));
					tempCall.setDestination(regeneratorLocations.get(tempOrig_index));
					tempCall.setFallTime(tempodequeda_loc);
					tempCall.setDuration(duracao_loc);
					tempCall.setCallType(Call.UNIDIRECIONAL);

					listOfCalls.addChamada(tempCall);

					fibersInUpLink_loc.clear();

					tempOrig_index = tempDest_index;
					tempDest_index++;
					continue;
				} // chave else do if ((tempDest_index-tempOrig_index)==1)
			} // chave do  if (lambda_trecho_Atual_loc == BLOQ_WAVELENGTH)
			else{  // else do if (lambda_trecho_Atual_loc == BLOQ_WAVELENGTH)
				boolean testeQoS_uplink_loc, testeQoS_downlink_loc;

				testeQoS_uplink_loc=true;
				testeQoS_downlink_loc=true;

				//calcula a potencia de entrada
				double somatorioPotencias = 0.0;
				if(network.isCrTalk()){
					somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), network.getNodes(), 
							trechoEmAnalise_uplink_loc,regeneratorLocations.get(tempOrig_index), lambda_trecho_Atual_loc);
					somatorioPotencias *= network.getEpsilon();
				}

				nIn_uplink_loc = ( network.getNodes().get(regeneratorLocations.get(tempOrig_index)).getLaserPower() 
						/ network.getNodes().get(regeneratorLocations.get(tempOrig_index)).getLaserSNR() )+ somatorioPotencias;

				sIn_uplink_loc = network.getNodes().get(regeneratorLocations.get(tempOrig_index)).getLaserPower();

				testeQoS_uplink_loc = Funcoes.checkPhysImpairm_fnb(network, trechoEmAnalise_uplink_loc,
						lambda_trecho_Atual_loc, sIn_uplink_loc, nIn_uplink_loc,
						sOut_uplink_loc, nOut_uplink_loc, outputPulseBroadening_uplink_loc, 
						currentTotalPulseBroadening_uplink_loc, bp );

				if(testeQoS_uplink_loc){
					somatorioPotencias = 0.0;

					if(network.isCrTalk()){
						somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), network.getNodes(), 
								trechoEmAnalise_downlink_loc, tempDest_index, lambda_trecho_Atual_loc);
						somatorioPotencias *= network.getEpsilon();
					}

					nIn_downlink_loc = (network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserPower()/
							network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserSNR()) + somatorioPotencias;
					sIn_downlink_loc = network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserPower();

					testeQoS_downlink_loc = Funcoes.checkPhysImpairm_fnb(network, trechoEmAnalise_downlink_loc,
							lambda_trecho_Atual_loc, sIn_downlink_loc, nIn_downlink_loc,
							sOut_downlink_loc, nOut_downlink_loc, outputPulseBroadening_downlink_loc, 
							currentTotalPulseBroadening_downlink_loc, bp );

					if(testeQoS_downlink_loc){ //QoS satisfeito?

							if(tempDest_index < (regeneratorLocations.size()-2)){ //ainda h� reg. adiante? SIM
									lambda_Anterior_loc = lambda_trecho_Atual_loc;
							++tempDest_index;
							continue;
							}else if(tempDest_index == (regeneratorLocations.size()-1)){
								//Aloca fibras
								for(int m=0; m<trechoEmAnalise_uplink_loc.size(); m++){
									fibersInUpLink_loc.add(trechoEmAnalise_uplink_loc.get(m).getFiber(0));
								}

								//Estabelece a chamada
								tempCall.setWavelengthUp(lambda_trecho_Atual_loc);
								tempCall.setFibersUpLink(fibersInUpLink_loc);
								tempCall.setSource(regeneratorLocations.get(tempOrig_index));
								tempCall.setDestination(regeneratorLocations.get(tempDest_index));
								tempCall.setFallTime(tempodequeda_loc);
								tempCall.setDuration(duracao_loc);
								tempCall.setCallType(Call.UNIDIRECIONAL_REGENERADA); //inicia com uma chamada unidirecional
								//Chegando nesse trecho, a chamada ser� sempre UNIDIRECIONAL_REGENERADA

								listOfCalls.addChamada(tempCall);

								fibersInUpLink_loc.clear();

								for(int m=0; m<trechoEmAnalise_downlink_loc.size(); m++){
									fibersInUpLink_loc.add(trechoEmAnalise_downlink_loc.get(m).getFiber(0));
								}

								tempCall.setWavelengthUp(lambda_trecho_Atual_loc);
								tempCall.setFibersUpLink(fibersInUpLink_loc);
								tempCall.setSource(regeneratorLocations.get(tempDest_index));
								tempCall.setDestination(regeneratorLocations.get(tempOrig_index));
								tempCall.setFallTime(tempDest_index);
								tempCall.setDuration(duracao_loc);
								tempCall.setCallType(Call.UNIDIRECIONAL);	//inicia com uma chamada unidirecional
								//Chegando nesse trecho, a chamada sera sempre UNIDIRECIONAL

								listOfCalls.addChamada(tempCall);

								fibersInUpLink_loc.clear();
								this.regRealizada = true;
								break;
							}//  else if (tempDest_index == (regeneratorLocations_par.size()-1)){

							////////////////////////////////////////////////////////////
							//ETAPA 2 --- INiCIO ///////////////////////////////////////
							////////////////////////////////////////////////////////////

							else{ //Ainda h� reg. adiante? NAO 
								if(tempOrig_index == 0){
									//REGENERA O SINAL --- IN�CIO //

									//calcula a potencia de entrada	        					 
									somatorioPotencias = 0.0;
									if(network.isCrTalk()){
										somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), network.getNodes(), 
												trechoEmAnalise_uplink_loc, regeneratorLocations.get(tempDest_index), 
												lambda_trecho_Atual_loc);
										somatorioPotencias *= network.getEpsilon();	        					 
									}

									nIn_uplink_loc = ( network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserPower() / 
											network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserSNR()) + 
											somatorioPotencias; 
									sIn_uplink_loc = network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserPower();

									currentTotalPulseBroadening_uplink_loc = 0.0;
									//REGENERA O SINAL --- FIM    //

									// Decrementar a qtd de regeneradores livres no n�;
									network.getNodes().get(regeneratorLocations.get(tempDest_index)).decNumFreeRegenerators();
									network.getNodes().get(regeneratorLocations.get(tempDest_index)).markRegenerators();

									for(int m=0; m<trechoEmAnalise_uplink_loc.size(); m++){
										fibersInUpLink_loc.add(trechoEmAnalise_uplink_loc.get(m).getFiber(0));
									}

									//Estabelece a chamada
									tempCall.setWavelengthUp(lambda_trecho_Atual_loc);
									tempCall.setFibersUpLink(fibersInUpLink_loc);
									tempCall.setSource(regeneratorLocations.get(tempOrig_index));
									tempCall.setDestination(regeneratorLocations.get(tempDest_index));
									tempCall.setFallTime(tempodequeda_loc);
									tempCall.setDuration(duracao_loc);
									tempCall.setCallType(Call.UNIDIRECIONAL_REGENERADA);

									if(tempOrig_index == 0){
										tempCall.setCallType(Call.UNIDIRECIONAL);
									}

									listOfCalls.addChamada(tempCall);

									fibersInUpLink_loc.clear();

									//Aloca fibras volta
									for(int m=0; m<trechoEmAnalise_downlink_loc.size(); m++){
										fibersInUpLink_loc.add(trechoEmAnalise_downlink_loc.get(m).getFiber(0));
									}

									//Estabelecer chamada
									tempCall.setWavelengthUp(lambda_trecho_Atual_loc);
									tempCall.setFibersUpLink(fibersInUpLink_loc);
									tempCall.setSource(regeneratorLocations.get(tempDest_index));
									tempCall.setDestination(regeneratorLocations.get(tempOrig_index));
									tempCall.setFallTime(tempodequeda_loc);
									tempCall.setDuration(duracao_loc);
									tempCall.setCallType(Call.UNIDIRECIONAL);

									listOfCalls.addChamada(tempCall);

									fibersInUpLink_loc.clear();

									//N� regenerador analisado � assumido como n� fonte intermedi�rio (NOVO PSEUDOC�DIGO)
									tempOrig_index = tempDest_index;
									tempDest_index = regeneratorLocations.size()-1;
									continue;
								}//chave do if (tempOrig_index==0)

								else{
									tempDest_index++;
									continue;
								}
							}//chave do else
					}// fimif(testeQoS_downlink_loc)
				}//fimif(testeQos_uplink_loc)

				if(!testeQoS_uplink_loc || !testeQoS_downlink_loc){
					if((tempDest_index-tempOrig_index) == 1){
						for(int u=0; u<network.getNodes().size(); u++){
							network.getNodes().get(u).rescueMarkedRegenerators();
							network.getNodes().get(u).resetMarkedRegenerators();
						}

						if(bp.getDispersion() > 0){
							return BLOQ_DISPERSION;
						}
						if(bp.getBer() > 0){
							return BLOQ_BER;
						}
					}else{
						tempDest_index--;

						//REGENERA O SINAL --- IN�CIO //
						//calcula a potencia de entrada
						somatorioPotencias =0.0;
						trechoEmAnalise_uplink_loc=cutPath_fnb(rotaUplink,
								regeneratorLocations.get(tempOrig_index),regeneratorLocations.get(tempDest_index));

						if(network.isCrTalk()){
							somatorioPotencias = Funcoes.somatorioPotSwitch(network.getLinks(), 
									network.getNodes(), trechoEmAnalise_uplink_loc, regeneratorLocations.get(tempDest_index),
									lambda_trecho_Atual_loc);
							somatorioPotencias *= network.getEpsilon();
						}

						nIn_uplink_loc = ( network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserPower() /
								network.getNodes().get(regeneratorLocations.get(tempOrig_index)).getLaserSNR()) + 
								somatorioPotencias;

						sIn_uplink_loc = network.getNodes().get(regeneratorLocations.get(tempDest_index)).getLaserPower();

						currentTotalPulseBroadening_uplink_loc = 0.0;
						// REGENERA P SINAL --- FIM //

						//Decrementa a qtd de regeneradores livres no n�;
						network.getNodes().get(regeneratorLocations.get(tempDest_index)).decNumFreeRegenerators();
						network.getNodes().get(regeneratorLocations.get(tempDest_index)).markRegenerators();

						//Aloca fibras ida
						for(int m=0; m<trechoEmAnalise_uplink_loc.size(); m++){
							fibersInUpLink_loc.add(trechoEmAnalise_uplink_loc.get(m).getFiber(0));
						}

						//Estabelece a chamada
						tempCall.setWavelengthDown(lambda_trecho_Atual_loc);
						tempCall.setFibersUpLink(fibersInUpLink_loc);
						tempCall.setSource(regeneratorLocations.get(tempOrig_index));
						tempCall.setDestination(regeneratorLocations.get(tempDest_index));
						tempCall.setFallTime(tempodequeda_loc);
						tempCall.setCallType(Call.UNIDIRECIONAL_REGENERADA);

						if(tempOrig_index == 0){
							tempCall.setCallType(Call.UNIDIRECIONAL);
						}

						listOfCalls.addChamada(tempCall);

						fibersInUpLink_loc.clear();

						trechoEmAnalise_downlink_loc = cutPath_fnb(rotaDownlink, regeneratorLocations.get(tempDest_index), 
								regeneratorLocations.get(tempOrig_index));

						//Aloca fibras volta
						for(int m=0; m<trechoEmAnalise_downlink_loc.size(); m++){
							fibersInUpLink_loc.add(trechoEmAnalise_downlink_loc.get(m).getFiber(0));
						}

						//Estabelecer a chamada
						tempCall.setWavelengthUp(lambda_trecho_Atual_loc);
						tempCall.setFibersUpLink(fibersInUpLink_loc);
						tempCall.setSource(regeneratorLocations.get(tempDest_index));
						tempCall.setDestination(regeneratorLocations.get(tempOrig_index));
						tempCall.setFallTime(tempodequeda_loc);
						tempCall.setCallType(Call.UNIDIRECIONAL);

						listOfCalls.addChamada(tempCall);

						fibersInUpLink_loc.clear();

						tempOrig_index = tempDest_index;
						tempDest_index++;
						continue;
					}//chave do else do if
				}// chave do if (testeQoS_uplink_loc==false || testeQoS_downlink_loc==false){
			}	// chave do else do if (lambda_trecho_Atual_loc == BLOQ_WAVELENGTH){

		}//=======FIM----BUSCA LAMBDA TRECHO A TRECHO ===========================  for (;;)

		return 0;

	}//fim da funcao


	/******************************************************************
	 *****  FUNCTION....: firstFit_fnb
	 *****  DESCRIPTION.: organiza o vetor com regeneradoes disponiveis (regeneratorLocations).
	 *****                este tambem contem a origem e o destino da chamada.
	 *****
	 *****  PARAMETERS.:  path,vectorOfNodes, regeneratorLocations
	 *******************************************************************/
	private int firstFit_fnb(Vector<Link> trechoEmAnaliseUplinkLoc,
			Vector<Link> trechoEmAnaliseDownlinkLoc, int nLambdaMax_par) {

		int lambdaEncontrado = -1;

		for(int nLambda_loc=0; (nLambda_loc < nLambdaMax_par); nLambda_loc++) //procura para cada lambda
		{
			boolean lambdaDisponivel_loc = true;
			// Procura em cada enlace o comprimento de onda dispon�vel de mesmo �ndice
			for(int i=0; i<trechoEmAnaliseUplinkLoc.size(); i++){	
				//se lambda nao disponivel
				if( !(trechoEmAnaliseUplinkLoc.get(i).getFiber(0).isLambdaAvailable(nLambda_loc)) 
						|| !(trechoEmAnaliseDownlinkLoc.get(i).getFiber(0).isLambdaAvailable(nLambda_loc))  ){
					lambdaDisponivel_loc = false;
					break;
				}
			}
			if(lambdaDisponivel_loc){
				lambdaEncontrado = nLambda_loc;
				break;
			}
		}

		if(lambdaEncontrado == -1){	//nao h� lambda disponivel
			return BLOQ_WAVELENGTH;
		}
		return 0;
	}

	/******************************************************************
	 *****  FUNCTION....: cutPath_fnb
	 *****  DESCRIPTION.: parte um caminho em caminhos menores
	 *****
	 *****  PARAMETERS.:  matrixOfLinks,vectorOfNodes, meanRateBetweenCalls, meanRateodCallsDuration,
	 *****                number of calls and snrThreshould
	 *******************************************************************/
	private Vector<Link> cutPath_fnb(Vector<Link> rotaUplink, Integer beginNode,
			Integer endNode) {

		Vector<Link> tempPath = new Vector<Link>();
		for( int i=0; i<rotaUplink.size();i++)
			if (rotaUplink.get(i).getSource()==beginNode){             //modif.regeneradores
				for(int j=i; j<rotaUplink.size();j++){
					tempPath.add(rotaUplink.get(j));
					if(rotaUplink.get(j).getDestination()==endNode)
						break;
				}
				break;
			}

		return tempPath;
	}

	/******************************************************************
	 *****  FUNCTION....: regNodes_fnb
	 *****  DESCRIPTION.: organiza o vetor com regeneradoes disponiveis (regeneratorLocations).
	 *****                este tambem contem a origem e o destino da chamada.
	 *****
	 *****  PARAMETERS.:  path,vectorOfNodes, regeneratorLocations
	 *******************************************************************/
	private void regNodes_fnb(Vector<Link> rotaUplink, Vector<Node> vectorNodes,
			Vector<Integer> regeneratorLocationsLoc) {
		regeneratorLocationsLoc.clear();
		regeneratorLocationsLoc.add(rotaUplink.get(0).getSource());
		//encontra os nos com regeneradores livres
		for(int i=1; i<rotaUplink.size(); i++){
			int source_loc = rotaUplink.get(i).getSource();
			if(vectorNodes.get(source_loc).getNumFreeRegenerators() > 0){
				regeneratorLocationsLoc.add(source_loc);
			}
		}
		//adiciona o destino da chamada
		regeneratorLocationsLoc.add(rotaUplink.get(rotaUplink.size()-1).getDestination());
	}
}
