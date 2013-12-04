package com.epstein.ui;
import com.epstein.model.EpsteinGeo;
import com.vividsolutions.jts.io.ParseException;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.jfree.data.xy.XYSeries;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.media.chart.TimeSeriesChartGenerator;

public class EpsteinGeoUI extends GUIState{
	private Display2D display;
    private JFrame displayFrame;

    private GeomVectorFieldPortrayal roadsPortrayal = new GeomVectorFieldPortrayal(true);
    private GeomVectorFieldPortrayal districtsPortrayal = new GeomVectorFieldPortrayal();
    private GeomVectorFieldPortrayal personPortrayal = new GeomVectorFieldPortrayal();
    private GeomVectorFieldPortrayal copPortrayal = new GeomVectorFieldPortrayal();
    
	private TimeSeriesChartGenerator chart;
	private XYSeries active, jailed, quiet;
    
    public Object getSimulationInspectedObject(){
        return state;
    }
    
    public EpsteinGeoUI(SimState state){
        super(state);
    }

    public EpsteinGeoUI() throws ParseException{
        super(new EpsteinGeo(System.currentTimeMillis()));
    }
    
    public void quit(){
		super.quit();
		if(displayFrame!=null) displayFrame.dispose();
		displayFrame = null; //let gc
		display = null; //let gc
	}

	public void init(Controller controller){
        super.init(controller);

        display = new Display2D(EpsteinGeo.WIDTH, EpsteinGeo.HEIGHT, this);
        display.attach(districtsPortrayal, "Political Districts");
        display.attach(roadsPortrayal, "Road Network");
        display.attach(personPortrayal, "People");
        display.attach(copPortrayal, "Cops");

        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        
		chart = new TimeSeriesChartGenerator();
		chart.setTitle("Regional Epstein Model");
		chart.setYAxisLabel("Agents");
		chart.setXAxisLabel("Time");
		JFrame frame = chart.createFrame();
		frame.setVisible(true);
		frame.pack();
		controller.registerFrame(frame);
    }

	public void start(){
        super.start();
        setupPortrayals();
        
        chart.removeAllSeries();
		
		active = new XYSeries("Active Count");
		jailed = new XYSeries("Jail Count");
		quiet = new XYSeries("Quiet Count");
		
		chart.addSeries(active, null);
		chart.addSeries(jailed, null);
		chart.addSeries(quiet, null);
		
		scheduleRepeatingImmediatelyAfter(new Steppable(){
			private static final long serialVersionUID = 1L;

			public void step(final SimState state){
				EpsteinGeo epGeo = (EpsteinGeo)state;
           
				double x = state.schedule.getTime(); 
				int activeY = epGeo.activeCount;
				int jailedY = epGeo.jailCount;
				int quietY = epGeo.quietCount;
           
				// now add the data
				if (x >= Schedule.EPOCH && x < Schedule.AFTER_SIMULATION){
					active.add(x, activeY, true);
					jailed.add(x, jailedY, true);
					quiet.add(x, quietY, true);
				}
			}
       });
    }

    private void setupPortrayals(){
        EpsteinGeo epState = (EpsteinGeo)state;

        districtsPortrayal.setField(epState.districts);
        districtsPortrayal.setPortrayalForAll(new DistrictPortrayal());
        
        roadsPortrayal.setField(epState.roads);
        roadsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.pink, true));

        personPortrayal.setField(epState.persons);
        personPortrayal.setPortrayalForAll(new PersonPortrayal(epState.portrayalSize));
        
        copPortrayal.setField(epState.cops);
        copPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLUE, epState.portrayalSize, true));
        
        display.reset();
        display.setBackdrop(Color.WHITE);
        display.repaint();
    }

    public static void main(String[] args){
        EpsteinGeoUI worldGUI = null;

        try{
            worldGUI = new EpsteinGeoUI();
        }
        catch (ParseException ex){
            Logger.getLogger(EpsteinGeoUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        Console console = new Console(worldGUI);
        console.setVisible(true);
    }
}