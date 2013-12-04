package com.epstein.model;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

public class EpsteinGeo extends SimState{
	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH = 600; 
    public static final int HEIGHT = 500; 
    
    /** How many agents in the simulation */ 
	public int numPersons = 700;
	public int numCops = 40;
	
	public double globalLegitimacy = 0.82;
	public double threshold = 0.1;
	
	public int activeCount = 0;
	public int quietCount = 0;
	public int jailCount = 0;
	
	public int maxJailTerm = 30;
	
	public double personVision = 3.0;
	public double copVision = 3.0;
	public double basemoveRate = 0.1;
	
	public double initIncrease = 0.01;
	public double initDecrease = 0.01;
	
	public double portrayalSize = 0.05;
	
	public WritableWorkbook workbook;
	public WritableSheet sheet;
	public boolean allResults = false;
	public String results = "results.xls";
	
	public boolean spike = false;
	public ArrayList<Integer> durations = new ArrayList<Integer>();
	public int duration = 0;
	public ArrayList<Integer> spikes = new ArrayList<Integer>();
	public double legitimacySpike = 0;
	public ArrayList<Integer> spikeTimes = new ArrayList<Integer>();
	public int spikeTime = 0;
	
	/** Fields to hold the associated GIS information */ 
    public GeomVectorField roads = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField initDistricts= new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField districts = new GeomVectorField(WIDTH, HEIGHT);
    // where all the agents live
    public GeomVectorField persons = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField cops = new GeomVectorField(WIDTH, HEIGHT);

    // The Importer is responsible for reading in the GIS files.  If you have installed either 
    // GeoTools and/or OGR on your system, you can use those importers.  The ShapeFileImporter does 
    // not rely on external libraries.  
    //OGRImporter importer = new OGRImporter();
    //GeoToolsImporter importer = new GeoToolsImporter();
    ShapeFileImporter importer = new ShapeFileImporter();


    // Stores the walkway network connections.  We represent the walkways as a PlanarGraph, which allows 
    // easy selection of new waypoints for the agents.  
    public GeomPlanarGraph network = new GeomPlanarGraph();
    public GeomVectorField junctions = new GeomVectorField(WIDTH, HEIGHT); // nodes for intersections
    
    //from GridLock.java
    HashMap<Integer, GeomPlanarGraphEdge> idsToEdges = new HashMap<Integer, GeomPlanarGraphEdge>();
    HashMap<GeomPlanarGraphEdge, ArrayList<Person>> edgeTraffic = new HashMap<GeomPlanarGraphEdge, ArrayList<Person>>();
    //ArrayList<person> agentList = new ArrayList<person>();

    public EpsteinGeo(long seed) { super (seed); }
    
    /** adds nodes corresponding to road intersections to GeomVectorField
    *
    * @param nodeIterator Points to first node
    * @param intersections GeomVectorField containing intersection geometry
    *
    * Nodes will belong to a planar graph populated from LineString network.
    */
    private void addIntersectionNodes(@SuppressWarnings("rawtypes") Iterator nodeIterator, GeomVectorField intersections){
    	GeometryFactory fact = new GeometryFactory();
    	Coordinate coord = null;
    	Point point = null;

    	while (nodeIterator.hasNext()){
    		Node node = (Node) nodeIterator.next();
    		coord = node.getCoordinate();
    		point = fact.createPoint(coord);
    		//System.out.println("NodeCoord -- " + coord);
    		//System.out.println("NodePoint -- " + point);

    		junctions.addGeometry(new MasonGeometry(point));
    	}
    }
   
    public void finish(){
    	super.finish();
    	
    	if(schedule.getTime() > 0){
			try {
				workbook.write();
				workbook.close();
			} catch (WriteException | IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	spike = false;
    }
   
    /** Add agents to the simulation and to the agent GeomVectorField.  Note that each agent does not have
    * any attributes.   */
    private void addPeople(){
    	double riskAversion = 0;
    	double perceivedHardship = 0;
    	//double govtLegitimacy = 0;
	   
    	for (int i = 0; i < numPersons; i++){
    		riskAversion = random.nextDouble();
	   	   	perceivedHardship = random.nextDouble();
	   	   	
	   	   	Person p = new Person(riskAversion, perceivedHardship, globalLegitimacy);
	   	   	MasonGeometry mgPerson = new MasonGeometry(p.getGeometry(), p);
	   	   	mgPerson.isMovable = true; 
	   	   	persons.addGeometry(mgPerson);
	   	   	p.start(this);
	   	   	schedule.scheduleRepeating(0, 2, p);
    	}
    	//System.out.println("MBR People = " + persons.getMBR().getMinX() + "," + persons.getMBR().getMaxX() + " - " + persons.getMBR().getMinY() + "," + persons.getMBR().getMaxY());   	
    }
   
   
    private void addCops(){
    	for (int i = 0; i < numCops; i++){
    		Cop c = new Cop();
    		MasonGeometry mgCop = new MasonGeometry(c.getGeometry(), c);
    		mgCop.isMovable = true;
    		cops.addGeometry(mgCop);
    		c.start(this);
    		schedule.scheduleRepeating(0, 2, c);
    	}
    }
    
    private void addDistricts(){
	   	Bag districtsB = initDistricts.getGeometries();
	   	for(Object o : districtsB){   		
	   		MasonGeometry mgDistrict = (MasonGeometry)o;
	   		District d = new District(mgDistrict.geometry, globalLegitimacy, initIncrease, initDecrease, allResults);
	   		MasonGeometry mg = new MasonGeometry(d.getGeometry(), d);
	   		d.masonGeometry = mg;
	   		districts.addGeometry(mg);
	   	}
	   	
	   	
    }
   
    public void start(){    	
    	activeCount = 0;
    	jailCount = 0;
    	quietCount = numPersons;
    	
    	super.start();
    	try{
	   	   	// We want to save the MBR so that we can ensure that all GeomFields
    		// cover identical area.
    	   	//System.out.println("reading districts layer");
  	   	
    	   	initDistricts.clear();
    	   	//URL districtsURL = new URL("data/iran/IRN_adm1.shp");
    	   	//ShapeFileImporter.read(districtsURL, initDistricts);
    	   	importer.ingest("data/iran/IRN_adm1", initDistricts, null);
    		//importer.ingest("data/iraq/IRQ_adm1", initDistricts, null);
    	   	districts.clear();
    	   	addDistricts();
 
    	 	Envelope MBR = districts.getMBR();
            //MBR.expandToInclude(districts2.getMBR());
           
    	 	//System.out.println("reading roads layer");
    	 	
    	 	//URL roadsURL = new URL("data/iran/IRN_roads.shp");
    	   	//ShapeFileImporter.read(roadsURL, roads);
    	 	importer.ingest("data/iran/IRN_roads", roads, null);
    	 	//importer.ingest("data/iraq/IRQ_roads", roads, null);
           
    	 	//Envelope MBR = roads.getMBR();
    	 	MBR.expandToInclude(roads.getMBR());
           
            //System.out.println("Done reading data");
           
            // Now synchronize the MBR for all GeomFields to ensure they cover the same area
            districts.setMBR(MBR);
            roads.setMBR(MBR);
           
            //createNetwork(); //from GridLock.java

            network.createFromGeomField(roads);
            addIntersectionNodes(network.nodeIterator(), junctions);           
            
            persons.clear(); // clear any existing agents from previous runs
            cops.clear();           
            addPeople();
            addCops(); 
           
            //System.out.println("MBR PreStart = " + persons.getMBR().getMinX() + "," + persons.getMBR().getMaxX() + " - " + persons.getMBR().getMinY() + "," + persons.getMBR().getMaxY());
            persons.setMBR(MBR);
            //System.out.println("MBR PostStart = " + persons.getMBR().getMinX() + "," + persons.getMBR().getMaxX() + " - " + persons.getMBR().getMinY() + "," + persons.getMBR().getMaxY());
            cops.setMBR(MBR);       
            //System.out.println("Cop = " + cops.getMBR().getMinX() + "," + cops.getMBR().getMaxX() + "," + cops.getMBR().getMinY() + "," + cops.getMBR().getMaxY());
            
            Initializer li = new Initializer();
            schedule.scheduleOnce(0, 0, li);      
                        
    	}
    	catch (FileNotFoundException ex){
    		Logger.getLogger(EpsteinGeo.class.getName()).log(Level.SEVERE, null, ex);
    	}
    }
   
    public static void main(String[] args){
    	doLoop(EpsteinGeo.class, args);
    	System.exit(0);
    }
   
    
   	public int getNumPersons() { return numPersons; } 
   	public void setNumPersons(int numPersons1) {this.numPersons = numPersons1; }
    public double getInitGovtLegitimacy(){return globalLegitimacy;}
    public void setInitGovtLegitimacy(double initGovtLegitimacy){this.globalLegitimacy = initGovtLegitimacy;}
  	public int getNumCops() {return numCops;}
   	public void setNumCops(int n) {numCops = n;}  
  	public double getThreshold() {return threshold;}
  	public void setThreshold(double threshold) {this.threshold = threshold;}
  	public int getMaxJailTerm() {return maxJailTerm;}
  	public void setMaxJailTerm(int maxJailTerm) {this.maxJailTerm = maxJailTerm;}
	public int getActiveCount() {return activeCount;}
	public void setActiveCount(int activeCount) {this.activeCount = activeCount;}
	public int getQuietCount() {return quietCount;}
	public void setQuietCount(int quietCount) {this.quietCount = quietCount;}
	public int getJailCount() {return jailCount;}
	public void setJailCount(int jailCount) {this.jailCount = jailCount;}
	public double getPersonVision() {return personVision;}
	public void setPersonVision(double personVision) {this.personVision = personVision;}
	public double getCopVision() {return copVision;}
	public void setCopVision(double copVision) {this.copVision = copVision;}
	public double getBasemoveRate() {return basemoveRate;}
	public void setBasemoveRate(double basemoveRate) {this.basemoveRate = basemoveRate;}
	public double getInitDecrease() {return initDecrease;}
	public void setInitDecrease(double initDecrease) {this.initDecrease = initDecrease;}
	public double getInitIncrease() {	return initIncrease;}
	public void setInitIncrease(double initIncrease) {this.initIncrease = initIncrease;}
	public double getPortrayalSize() {return portrayalSize;}
	public void setPortrayalSize(double portrayalSize) {this.portrayalSize = portrayalSize;}
	public String getResults() {return results;}
	public void setResults(String results) {this.results = results;}
	public boolean isAllResults() {return allResults;}
	public void setAllResults(boolean allResults) {this.allResults = allResults;}		
	public int getDurationOfSpike() {return duration;}
	public void setDurationOfSpike(int duration) {this.duration = duration;}
	public double getLegitimacySpike() {return legitimacySpike;}
	public void setLegitimacySpike(double legitimacySpike) {this.legitimacySpike = legitimacySpike;}
	public int getSpikeTime() {return spikeTime;}
	public void setSpikeTime(int spikeTime) {this.spikeTime = spikeTime;}
}