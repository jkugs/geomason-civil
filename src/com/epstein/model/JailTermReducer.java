package com.epstein.model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class JailTermReducer implements Steppable {
	private static final long serialVersionUID = 1L;

	public void step(SimState state) {
		EpsteinGeo epState = (EpsteinGeo)state;
		
		Bag persons = epState.persons.getGeometries();
		
		for(Object person : persons){
			Person p = (Person)((MasonGeometry) person).getUserData();
			
			if(p.jailTerm > 0){
				p.jailTerm--;
				
				if(p.jailTerm == 0){
					epState.jailCount--;
					epState.quietCount++;
				}
			}
		}
	}

}
