package br.bm.core;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static br.bm.core.Util.LOGGER;

public class BlockUtil {
	
	private static BlockUtil instance;
	private  ArrayList<Double> lambda = new ArrayList<Double>();
	private  ArrayList<Double> ber = new ArrayList<Double>();
	private  ArrayList<Double> dispersion = new ArrayList<Double>();
	private ArrayList<Integer> contConv = new ArrayList<Integer>();
	private ArrayList<Integer> contReg = new ArrayList<Integer>();
	private ArrayList<Integer> regenerou = new ArrayList<Integer>();
	public  String path;
		
	public static BlockUtil getInstance(){
		if(instance == null){
			instance = new BlockUtil();
		}
		
		return instance;
	}
	
	
	public void addBlockLambda(double blocksLambda){
		//block = {lambda, ber, dispersion}
		
		lambda.add(blocksLambda);
	}
	
	public void addBlockBer(double blocksBer){
		//block = {lambda, ber, dispersion}
		
		ber.add(blocksBer);
	}
	
	public void addBlockDispersion(double blocksDispersion){
		//block = {lambda, ber, dispersion}
		
		dispersion.add(blocksDispersion);
	}
	
	public void addConv(int conv){
		contConv.add(conv);
	}
	
	public void addReg(int reg){
		contReg.add(reg);
	}
	
	public void addRegenerou(int regenerou){
		this.regenerou.add(regenerou);
	}
	
	public void printBlocks(double geracao) {
		try {
			FileOutputStream fos = new FileOutputStream(path + "_BLOCKS_"+geracao+".txt");
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);
			
			bw.write("Bloq. Lambda");
			bw.write("\tBloq. BER");
			bw.write("\tBloq. Dispersion");
			bw.write("\tBloq. Total");
			bw.write("\t\tCont Conv");
			bw.write("\tCont Reg");
			bw.write("\tCont Regeneracoes");
			bw.newLine();
			
			for(int i=0; i<lambda.size(); i++){
				bw.write("" + lambda.get(i));
				bw.write("\t" + ber.get(i));
				bw.write("\t" + dispersion.get(i));
				bw.write("\t" + (lambda.get(i)+ber.get(i)+dispersion.get(i)));
				
				if(i<contConv.size()){
					bw.write("\t\t" + contConv.get(i));
					bw.write("\t" + contReg.get(i));
					bw.write("\t" + regenerou.get(i));
				}
				
				bw.newLine();
			}
			
			bw.close();
			lambda.clear();
			ber.clear();
			dispersion.clear();
			contConv.clear();
			contReg.clear();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
		}
	} 
	
}
