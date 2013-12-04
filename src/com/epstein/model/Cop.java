package com.epstein.model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;


public class Cop extends Agent implements Steppable {
	private static final long serialVersionUID = 1L;
	
	public boolean madeArrest = false;
	public int givenJailTerm = 0;
	
    public void step(SimState state){
    	EpsteinGeo epState = (EpsteinGeo)state; 
    	
        move(epState);
        epState.cops.setGeometryLocation(getGeometry(), pointMoveTo);
        
        arrestSuspect(epState);
    }
    
	private void arrestSuspect(EpsteinGeo epState) {	
		Bag suspects = new Bag();
		
		//Loop through all agents within vision
		MasonGeometry buffer = new MasonGeometry(this.location.buffer(epState.copVision), this);    	
    	Bag persons = epState.persons.getCoveredObjects(buffer);		
    	for(Object person : persons){
			Person p = (Person)((MasonGeometry) person).getUserData();
			
			if(p.active && p.jailTerm == 0){
				suspects.add(p);
			}
    	}
		
		/*If any active agents have been added to the bag of suspects,
		one is selected randomly and jailed for a random period of time (0 - maxJailTerm).  
		The cop then moves to the location of the suspect.*/    	
    	if(suspects.size() > 0){
			Person person = (Person) suspects.get(epState.random.nextInt(suspects.size()));
			
			person.active = false;
			givenJailTerm = epState.random.nextInt(epState.maxJailTerm);
			person.jailTerm = givenJailTerm;
			
			//move to position of jailed suspect
			moveTo(person.currentPos);
			segment = person.segment;
			currentIndex = person.currentIndex;
			startIndex = person.startIndex;
			endIndex = person.endIndex;
			epState.cops.setGeometryLocation(getGeometry(), pointMoveTo);
			
			epState.activeCount--;
			
			//if the random jail term is 0, only increase quiet count.
			if(person.jailTerm > 0){
				epState.jailCount++;
			}
			else{
				epState.quietCount++;
			}
			
			madeArrest = true;
		}
			
		
	}
}
