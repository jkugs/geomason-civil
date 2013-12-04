package com.epstein.model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import jxl.write.*;
import jxl.write.Number;

public class DistrictXLSWriter implements Steppable{
	private static final long serialVersionUID = 1L;
	
	private District district;
	private WritableSheet sheet;
	
	public DistrictXLSWriter(District district, WritableSheet sheet){
		this.district = district;
		this.sheet = sheet;
	}
	
	int curStep = 1;
	public void step(SimState state) {
		EpsteinGeo epState = (EpsteinGeo)state;	
		
		int activeCount = 0;
		int jailCount = 0;
		int quietCount = 0;
		double grievanceAvg = 0;
		double legitimacyAvg = 0;
		double arrestProbAvg = 0;
		int numActivatedOnStep = 0;
		int numArrestsOnStep = 0;
		double jailTermAvgOnStep = 0;
		
		Bag persons = epState.persons.getCoveredObjects(district.masonGeometry);
		Bag cops = epState.cops.getCoveredObjects(district.masonGeometry);
		
		for(Object o : persons){
			Person p = (Person)((MasonGeometry) o).getUserData();
			
			grievanceAvg += p.getGrievance();
			legitimacyAvg += p.getGovtLegitimacy();
			arrestProbAvg += p.arrestProbability;
			
			if(p.active){
				activeCount++;
			}
			else if(p.jailTerm > 0){
				jailCount++;
			}
			else{
				quietCount++;
			}
			
			if(p.activated){
				numActivatedOnStep++;
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
			}
		}		
		if(numArrestsOnStep > 0){
			jailTermAvgOnStep = jailTermAvgOnStep/numArrestsOnStep;
		}
		
		Number active = new Number(0, curStep, activeCount);
		Number jailed = new Number(1, curStep, jailCount);
		Number quiet = new Number(2, curStep, quietCount);
		Number avgGrievance = new Number(3, curStep, grievanceAvg);
		Number avgLegitimacy = new Number(4, curStep, legitimacyAvg);
		Number numArrests = new Number(5, curStep, numArrestsOnStep);
		Number avgJailTerm = new Number(6, curStep, jailTermAvgOnStep);
		Number avgArrestProb = new Number(7, curStep, arrestProbAvg);
		Number numActive = new Number(8, curStep, numActivatedOnStep);
		Number numCops = new Number(9, curStep, cops.size());
		try {
			sheet.addCell(active);
			sheet.addCell(jailed);
			sheet.addCell(quiet);
			sheet.addCell(avgGrievance);
			sheet.addCell(avgLegitimacy);
			sheet.addCell(numArrests);
			sheet.addCell(avgJailTerm);
			sheet.addCell(avgArrestProb);
			sheet.addCell(numActive);
			sheet.addCell(numCops);
		} catch (WriteException e) {
			e.printStackTrace();
		}
		
		curStep++;
	}

}
