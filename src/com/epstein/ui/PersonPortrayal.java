package com.epstein.ui;
import java.awt.Color;
import java.awt.Graphics2D;

import com.epstein.model.Person;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.geo.MasonGeometry;

public class PersonPortrayal extends GeomPortrayal{
	private static final long serialVersionUID = 1L;
	
	public PersonPortrayal(double portrayalSize){
		super(Color.green, portrayalSize, true);
	}
	
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
    	Person p = (Person)((MasonGeometry) object).getUserData();
    	
    	if(!p.active && p.jailTerm == 0){
    		paint = Color.green;
    	}
    	
    	if(p.active){
    		paint = Color.red;
    	}
    	
    	if(p.jailTerm > 0){
    		paint = Color.gray;
    	}
    	
        super.draw(object, graphics, info);    
    }
}
