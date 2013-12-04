package com.epstein.ui;
import java.awt.Color;
import java.awt.Graphics2D;

import com.epstein.model.District;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.geo.MasonGeometry;

public class DistrictPortrayal extends GeomPortrayal{
	private static final long serialVersionUID = 1L;
	
	public DistrictPortrayal(){
		super(Color.LIGHT_GRAY, false);
	}
	
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
    	District d = (District)((MasonGeometry) object).getUserData();
    	
    	if(d.filled){
    		filled = true;
    	}
    	else{
    		filled = false;
    	}
    	
        super.draw(object, graphics, info);    
    }
}
