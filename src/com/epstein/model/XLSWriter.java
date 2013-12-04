package com.epstein.model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import jxl.write.*;
import jxl.write.Number;

public class XLSWriter implements Steppable{
	private static final long serialVersionUID = 1L;
	
	public XLSWriter(){}
	
	int curStep = 1;
	public void step(SimState state) {	
		EpsteinGeo epState = (EpsteinGeo)state;	
		
		double grievanceAvg = 0;
		double legitimacyAvg = 0;
		double arrestProbAvg = 0;
		int numActivatedOnStep = 0;
		int numArrestsOnStep = 0;
		double jailTermAvgOnStep = 0;
		
		Bag persons = epState.persons.getGeometries();
		Bag cops = epState.cops.getGeometries();
		
		for(Object o : persons){
			Person p = (Person)((MasonGeometry) o).getUserData();
			
			grievanceAvg += p.getGrievance();
			legitimacyAvg += p.getGovtLegitimacy();
			arrestProbAvg += p.arrestProbability;
			
			if(p.activated){
				numActivatedOnStep++;
				p.activated = false;
			}
		}
		grievanceAvg = grievanceAvg/persons.size();	
		legitimacyAvg = legitimacyAvg/persons.size();
		arrestProbAvg = arrestProbAvg/persons.size();
		
		for(Object o : cops){
			Cop c = (Cop)((MasonGeometry) o).getUserData();
			
			if(c.madeArrest){
				numArrestsOnStep++;
				jailTermAvgOnStep += c.givenJailTerm;
				
				c.madeArrest = false;
				c.givenJailTerm = 0;
			}
		}		
		if(numArrestsOnStep > 0){
			jailTermAvgOnStep = jailTermAvgOnStep/numArrestsOnStep;
		}
		
		Number active = new Number(0, curStep, epState.activeCount);
		Number jailed = new Number(1, curStep, epState.jailCount);
		Number quiet = new Number(2, curStep, epState.quietCount);
		Number avgGrievance = new Number(3, curStep, grievanceAvg);
		Number avgLegitimacy = new Number(4, curStep, legitimacyAvg);
		Number numArrests = new Number(5, curStep, numArrestsOnStep);
		Number avgJailTerm = new Number(6, curStep, jailTermAvgOnStep);
		Number avgArrestProb = new Number(7, curStep, arrestProbAvg);
		Number numActive = new Number(8, curStep, numActivatedOnStep);
		try {
			epState.sheet.addCell(active);
			epState.sheet.addCell(jailed);
			epState.sheet.addCell(quiet);
			epState.sheet.addCell(avgGrievance);
			epState.sheet.addCell(avgLegitimacy);
			epState.sheet.addCell(numArrests);
			epState.sheet.addCell(avgJailTerm);
			epState.sheet.addCell(avgArrestProb);
			epState.sheet.addCell(numActive);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	
		curStep++;
	}

}
