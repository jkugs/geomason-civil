package com.epstein.model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;


public class DistrictSpiker implements Steppable{
	private static final long serialVersionUID = 1L;
	
	private int time = 0;
	private District d;
	
	public DistrictSpiker(District d){
		this.d = d;
	}
	
	public void step(SimState state) {
		EpsteinGeo epState = (EpsteinGeo)state;
		

		if(epState.schedule.getTime() == d.spikeTime){
			d.spike = true;
		}
		
		if(d.spike == true){	
			Bag people = epState.persons.getCoveredObjects(d.masonGeometry);			
			
			d.decayedLegitimacy += d.riseRate;
			for(Object ob : people){
				Person p = (Person)((MasonGeometry) ob).getUserData();	
				
				if(time == 0){
					d.riseRate = (d.govtLegitimacy - d.legitimacySpike)/(d.duration);
					p.govtLegitimacy = d.legitimacySpike;
					d.decayedLegitimacy = d.legitimacySpike;
				}
				else{
					p.govtLegitimacy = d.decayedLegitimacy;
					
					if(time == d.duration){
						p.govtLegitimacy = d.govtLegitimacy;
					}
				}
			}
			
			time++;
			if(time == d.duration + 1){
				d.spike = false;
				time = 0;
			}
		}
	}


}
