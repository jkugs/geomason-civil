package com.epstein.model;
import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class Initializer implements Steppable{
	private static final long serialVersionUID = 1L;

	public void step(SimState state) {
		initializeLegitimacies(state);
	}
	
	public void initializeLegitimacies(SimState state){
		EpsteinGeo epState = (EpsteinGeo)state;
		Bag districts = epState.districts.getGeometries();
		
		JailTermReducer jailTermReducer = new JailTermReducer();
        epState.schedule.scheduleRepeating(0, 4, jailTermReducer);
        
        DistrictCalculator lg = new DistrictCalculator();
        epState.schedule.scheduleRepeating(0, 3, lg);
        
        if(epState.spikeTime > 0){
        	LegitimacySpiker spiker = new LegitimacySpiker();
        	epState.schedule.scheduleRepeating(0, 1, spiker);
        }
		
		try {
        	epState.workbook = Workbook.createWorkbook(new File(epState.results)); 
        	epState.sheet = epState.workbook.createSheet("Country", 0);
        	
        	WritableCellFormat format = new WritableCellFormat();
        	format.setVerticalAlignment(VerticalAlignment.JUSTIFY);
        	
        	Label activeCount = new Label(0, 0, "Active Count", format);
        	epState.sheet.addCell(activeCount);
        	
        	Label jailedCount = new Label(1, 0, "Jailed Count", format);
        	epState.sheet.addCell(jailedCount);
        	
        	Label quietCount = new Label(2, 0, "Quiet Count", format);
        	epState.sheet.addCell(quietCount);
        	
        	Label grievance = new Label(3, 0, "Grievance Average", format);
        	epState.sheet.addCell(grievance);

        	Label legitimacy = new Label(4, 0, "Legitimacy Average", format);
        	epState.sheet.addCell(legitimacy);
        	
        	Label numArrests = new Label(5, 0, "Arrested", format);
        	epState.sheet.addCell(numArrests);
        	
        	Label jailTerm = new Label(6, 0, "Jail Term Average", format);
        	epState.sheet.addCell(jailTerm);
        	
        	Label arrestProb = new Label(7, 0, "Arrest Probability Average", format);
        	epState.sheet.addCell(arrestProb);
        	
        	Label numActive = new Label(8, 0, "Activated", format);
        	epState.sheet.addCell(numActive);
        	
			XLSWriter xlsWriter = new XLSWriter();
			epState.schedule.scheduleRepeating(0, 6, xlsWriter);
		
		} catch (IOException | WriteException e) {
			e.printStackTrace();
		}
		
		
		for(Object o : districts){
			MasonGeometry mg = (MasonGeometry)o;
			Bag people = epState.persons.getCoveredObjects(mg);
			District d = (District)((MasonGeometry) o).getUserData();						
			
			DistrictSpiker districtSpiker = new DistrictSpiker(d);
			epState.schedule.scheduleRepeating(0, 1, districtSpiker);
			
			for(Object ob : people){
				Person p = (Person)((MasonGeometry) ob).getUserData();
				p.govtLegitimacy = d.govtLegitimacy;
			}
			
			
			if(d.results){
				try{
					WritableSheet sheet = epState.workbook.createSheet(d.regionName, 1);
					
					WritableCellFormat format = new WritableCellFormat();
	            	format.setVerticalAlignment(VerticalAlignment.JUSTIFY);
	            	
	            	Label activeCount = new Label(0, 0, "Active Count", format);
	            	sheet.addCell(activeCount);
	            	
	            	Label jailedCount = new Label(1, 0, "Jailed Count", format);
	            	sheet.addCell(jailedCount);
	            	
	            	Label quietCount = new Label(2, 0, "Quiet Count", format);
	            	sheet.addCell(quietCount);
	            	
	            	Label grievance = new Label(3, 0, "Grievance Average", format);
	            	sheet.addCell(grievance);
	
	            	Label legitimacy = new Label(4, 0, "Legitimacy Average", format);
	            	sheet.addCell(legitimacy);
	            	
	            	Label numArrests = new Label(5, 0, "Arrested", format);
	            	sheet.addCell(numArrests);
	            	
	            	Label jailTerm = new Label(6, 0, "Jail Term Average", format);
	            	sheet.addCell(jailTerm);
	            	
	            	Label arrestProb = new Label(7, 0, "Arrest Probability Average", format);
	            	sheet.addCell(arrestProb);
	            	
	            	Label numActive = new Label(8, 0, "Activated", format);
	            	sheet.addCell(numActive);
	            	
	            	Label numCops = new Label(9, 0, "Cops", format);
	            	sheet.addCell(numCops);
	            	
					DistrictXLSWriter writer = new DistrictXLSWriter(d, sheet);
					epState.schedule.scheduleRepeating(0, 5, writer);
				} catch (WriteException e) {
					e.printStackTrace();
				}						
			}
		}
	}
}
