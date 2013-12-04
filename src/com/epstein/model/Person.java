package com.epstein.model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class Person extends Agent implements Steppable {
	private static final long serialVersionUID = 1L;
	
	public double govtLegitimacy = 0;
	public double grievance = 0;	
	public double riskAversion = 0;
	public double perceivedHardship = 0;
	public double arrestProbability = 0;
	public boolean active = false;
	public int jailTerm = 0;
	public boolean activated = false;
	
	public Person(double riskAversion, double perceivedHardship, double govtLegitimacy){
		this.riskAversion = riskAversion;
		this.perceivedHardship = perceivedHardship;
		this.govtLegitimacy = govtLegitimacy;
	}
	
	public void step(SimState state){
    	EpsteinGeo epState = (EpsteinGeo)state; 
    	
    	if(this.jailTerm == 0){
	        move(epState);
	        epState.persons.setGeometryLocation(getGeometry(), pointMoveTo);
	        setActive(epState);
    	}
    }

	private void setActive(EpsteinGeo epState){
		boolean alreadyActive = false;
		
		if(this.active)
			alreadyActive = true;
		
		int numActive = 0;
		
		//Loop through all the agents within vision and count the number of active civilians
    	MasonGeometry buffer = new MasonGeometry(this.location.buffer(epState.personVision), this);	
    	Bag persons = epState.persons.getCoveredObjects(buffer);
    	Bag cops = epState.cops.getCoveredObjects(buffer);
		for(Object person : persons){
			Person p = (Person)((MasonGeometry) person).getUserData();
			
			if(p.active){
				numActive++;
			}
		}
		
		/*Calculations for going active*/
		arrestProbability = 1 - Math.exp(-2.3*((cops.size()/(numActive+1))));
		grievance = perceivedHardship * (1 - govtLegitimacy);
		this.active = (grievance - (riskAversion * arrestProbability)) > epState.threshold;
		
		if(!alreadyActive && this.active){
			epState.activeCount++;
			epState.quietCount--;			
			activated = true;
		}
		else if(alreadyActive && !this.active){
			epState.activeCount--;
			epState.quietCount++;
		}
	}
	
	public double getGrievance(){return perceivedHardship * (1 - govtLegitimacy);}
	public double getRiskAversion(){return riskAversion;}
	public double getPerceivedHardship(){return perceivedHardship;}	
	public double getGovtLegitimacy(){return govtLegitimacy;}
}
