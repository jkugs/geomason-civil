package com.epstein.model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;


public class DistrictCalculator implements Steppable{
	private static final long serialVersionUID = 1L;

	public void step(SimState state) {
		EpsteinGeo epState = (EpsteinGeo)state;
		
		if(!epState.spike){
			Bag districts = epState.districts.getGeometries();
			
			for(Object o : districts){
				MasonGeometry mg = (MasonGeometry)o;
				Bag people = epState.persons.getCoveredObjects(mg);
				District d = (District)((MasonGeometry) o).getUserData();						
				
				if(!d.spike){
					for(Object ob : people){
						Person p = (Person)((MasonGeometry) ob).getUserData();
						if(p.govtLegitimacy < d.govtLegitimacy){
							p.govtLegitimacy = p.govtLegitimacy + d.increaseAmount;
							
							if(p.govtLegitimacy > d.govtLegitimacy){
								p.govtLegitimacy = d.govtLegitimacy;
							}
						}
						else if(p.govtLegitimacy > d.govtLegitimacy){
							p.govtLegitimacy = p.govtLegitimacy - d.decreseAmount;
							
							if(p.govtLegitimacy < d.govtLegitimacy){
								p.govtLegitimacy = d.govtLegitimacy;
							}
						}
					}
				}
			}
		}
	}

}
