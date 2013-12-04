package com.epstein.model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;


public class LegitimacySpiker implements Steppable{
	private static final long serialVersionUID = 1L;
	
	private int time = 0;
	
	public void step(SimState state) {
		EpsteinGeo epState = (EpsteinGeo)state;
		
		if(epState.schedule.getTime() == epState.spikeTime){
			epState.spike = true;
		}
		
		if(epState.spike == true){
			Bag districts = epState.districts.getGeometries();
			
			
			for(Object o : districts){
				MasonGeometry mg = (MasonGeometry)o;
				Bag people = epState.persons.getCoveredObjects(mg);
				District d = (District)((MasonGeometry) o).getUserData();
			
				for(Object ob : people){
					Person p = (Person)((MasonGeometry) ob).getUserData();	
					
					if(time == 0){
						d.riseRate = (d.govtLegitimacy - epState.legitimacySpike)/(epState.duration);
						p.govtLegitimacy = epState.legitimacySpike;
					}
					else{
						p.govtLegitimacy += d.riseRate;
						
						if(time == epState.duration){
							p.govtLegitimacy = d.govtLegitimacy;
						}
					}
				}
			}

			time++;
			if(time == epState.duration + 1){
				epState.spike = false;
				time = 0;
			}
		}
	}

}
