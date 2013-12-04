package com.epstein.model;
import sim.util.geo.MasonGeometry;

import com.vividsolutions.jts.geom.Geometry;

public class District{
	public double govtLegitimacy = 0.82;
	public double increaseAmount = 0.01;
	public double decreseAmount = 0.01;
	public boolean filled = false;
	public boolean results = false;
	public String regionName = "Region";
	public double riseRate = 0;
	public boolean spike = false;
	public int duration = 0;
	public double legitimacySpike = 0;
	public int spikeTime = 0;	
	public double decayedLegitimacy = 0;

	public Geometry geometry = null;
	public MasonGeometry masonGeometry = null;

	public District(Geometry geometry, double govtLegitimacy, double initIncrease, double initDecrease, boolean results){
		this.geometry = geometry;
		this.govtLegitimacy = govtLegitimacy;
		this.increaseAmount = initIncrease;
		this.decreseAmount = initDecrease;
		this.results = results;
		
		String[] attributes = geometry.getUserData().toString().split("NAME_1 Value: ");
		String[] name = attributes[1].split(" Field");
		regionName = name[0];
	}
	
	public double getGovtLegitimacy(){return govtLegitimacy;}
	public void setGovtLegitimacy(double govtLegitimacy){this.govtLegitimacy = govtLegitimacy;}	
	public double getIncreaseAmount() {return increaseAmount;	}
	public void setIncreaseAmount(double increaseAmount) {this.increaseAmount = increaseAmount;}
	public double getDecreseAmount() {return decreseAmount;}
	public void setDecreseAmount(double decreseAmount) {this.decreseAmount = decreseAmount;}
	public Geometry getGeometry() {return geometry;}
	public boolean isFilled() {return filled;}
	public void setFilled(boolean filled) {this.filled = filled;}
	public boolean isResults() {return results;}
	public void setResults(boolean results) {this.results = results;}
	public String getRegionName() {return regionName;}
	public void setRegionName(String regionName) {this.regionName = regionName;}

	public int getDurationOfSpike() {
		return duration;
	}

	public void setDurationOfSpike(int duration) {
		this.duration = duration;
	}

	public double getLegitimacySpike() {
		return legitimacySpike;
	}

	public void setLegitimacySpike(double legitimacySpike) {
		this.legitimacySpike = legitimacySpike;
	}

	public int getSpikeTime() {
		return spikeTime;
	}

	public void setSpikeTime(int spikeTime) {
		this.spikeTime = spikeTime;
	}
}
