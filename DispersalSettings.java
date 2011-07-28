//  DIM SUM Version 0.9 -- Demography and Individual Migration Simulated Using a Markov chain
//  Copyright (C) 2009 Jeremy M. Brown and Kevin Savidge

//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.

//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.

//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

//  If you have questions or comments, please email JMB at jeremymbrown@gmail.com


import java.io.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

class DispersalSettings {
    public Timer pAmTTimer = new Timer("populateAndMigrateThreaded");
    public Timer pAmTTimer2 = new Timer("migrate");
    public Timer cCCTimer = new Timer("checkCarryingCapacity");
    public Timer outputTimer = new Timer("Output");
    public Timer outputTimer2 = new Timer("Output2/Display",false);

    public Timer pruneTimer = new Timer ("prune",false);
    public Timer simulateTimer = new Timer("Simulate");
    public GPU cuda = new GPU(); 

	
	Document document;
	ArrayList<XYFunction> carryingcapacity, hardborders, softborders;
	ArrayList<PFunction> noffspring, dispersalradius;
	ArrayList<Node> initialpopulation;
	int seed; // Added by JMB -- 4.5.10
	int visualfreq; // Added by JMB -- 4.8.10
	
	public ArrayList<Double> samplerects;
	public ArrayList<Integer> n;
	
	public String simulationname = "";
	
	int _nGenerations;
	double _minLat;
	double _maxLat;
	double _minLon;
	double _maxLon;
	
	public double getMinLat()  {
		return _minLat;
	}

	public double getMaxLat()  {
		return _maxLat;
	}

	public double getMinLon()  {
		return _minLon;
	}

	public double getMaxLon()  {
		return _maxLon; 
	}
	
	public boolean visualoutput = false;
	public ArrayList<OutputFunction> outputfunctors = new ArrayList<OutputFunction>();
	
	public DispersalSettings(String filename) throws Exception {
		SAXBuilder builder = new SAXBuilder();
        document = builder.build(new File(filename));
        try {
        	_nGenerations = this.getNGenerations();
        	_minLat = document.getRootElement().getChild("latitude").getAttribute("minimum").getDoubleValue();
        	_maxLat = document.getRootElement().getChild("latitude").getAttribute("maximum").getDoubleValue();
        	_minLon = document.getRootElement().getChild("longitude").getAttribute("minimum").getDoubleValue();
        	_maxLon = document.getRootElement().getChild("longitude").getAttribute("maximum").getDoubleValue();
        }
        catch( Exception e ) {
        	System.out.println("Either unable to load config file, or tags are missing (generations, latitude, or longitude).");
        	throw e;
        }
        
        carryingcapacity = new ArrayList<XYFunction>();
        hardborders = new ArrayList<XYFunction>();
        softborders = new ArrayList<XYFunction>();
        noffspring = new ArrayList<PFunction>();
        dispersalradius = new ArrayList<PFunction>();
        initialpopulation = new ArrayList<Node>();
        samplerects = new ArrayList<Double>();
        n = new ArrayList<Integer>();
		seed = -1;  // Added by JMB - 4.5.10
		visualfreq = 1; // Added by JMB -- 4.8.10
        
        try {
        	simulationname = document.getRootElement().getAttribute("name").getValue();
        }
        catch( Exception e ) {
			simulationname = "Default Simulation";
        }

		try{
			seed = Integer.parseInt(document.getRootElement().getChildTextTrim("seed"));	// Added by JMB -- 4.5.10
		}
		catch( Exception e)
		{
			seed = -1;
		}
		
		try{
			visualfreq = document.getRootElement().getChild("output").getChild("visual").getAttribute("output_every").getIntValue();
		}catch(Exception e){
			visualfreq=1;
		}
		
		  java.util.List dataelem = document.getRootElement().getChild("carryingcapacity").getChildren("data");
          for(int i=0; i<dataelem.size(); i++) {
                  Element e = (Element)dataelem.get(i);
                  if( e.getAttribute("type").getValue().equalsIgnoreCase("image") ) {
                          try {
                                  Image image = new ImageIcon( e.getText() ).getImage();
                                  XYFunction xyf = new XYFunction(image,
                                                                                                  e.getAttribute("color").getValue(),
                                                                                                  e.getAttribute("maxvalue").getDoubleValue(), -1.0 );
                                  if( e.getAttribute("startgeneration") != null && e.getAttribute("startgeneration") != null ) {
                                          xyf.startgeneration = e.getAttribute("startgeneration").getIntValue();
                                          xyf.endgeneration = e.getAttribute("endgeneration").getIntValue();
                                  }
                                  carryingcapacity.add( xyf );
                          }
                          catch( Exception e2 ) {
                                  System.out.println("Fatal error while loading carrying capacity map.");
                                  throw e2;
                          }
                  }
                  else if( e.getAttribute("type").getValue().equalsIgnoreCase("file") ) {
                          try {
                                  XYFunction xyf = new XYFunction( e.getText(), -1.0 );
                                  if( e.getAttribute("startgeneration") != null && e.getAttribute("startgeneration") != null ) {
                                          xyf.startgeneration = e.getAttribute("startgeneration").getIntValue();
                                          xyf.endgeneration = e.getAttribute("endgeneration").getIntValue();
                                  }
                                  carryingcapacity.add( xyf );
                          }
                          catch( Exception e2 ) {
                                  System.out.println("Fatal error while loading carrying capacity map.");
                                  throw e2;
                          }
                  }
          }
          
          
          
          dataelem = document.getRootElement().getChild("hardborders").getChildren("data");
          for(int i=0; i<dataelem.size(); i++) {
                  Element e = (Element)dataelem.get(i);
                  if( e.getAttribute("type").getValue().equalsIgnoreCase("image") ) {
                          try {
                                  Image image = new ImageIcon( e.getText() ).getImage();
                                  XYFunction xyf = new XYFunction(image,
                                                                                                  e.getAttribute("color").getValue(),
                                                                                                  e.getAttribute("maxvalue").getDoubleValue(), (isEdgeDeadly())?1.0:-1.0 );
                                  if( e.getAttribute("startgeneration") != null && e.getAttribute("startgeneration") != null ) {
                                          xyf.startgeneration = e.getAttribute("startgeneration").getIntValue();
                                          xyf.endgeneration = e.getAttribute("endgeneration").getIntValue();
                                  }
                                  hardborders.add( xyf );
                          }
                          catch( Exception e2 ) {
                                  System.out.println("Fatal error while loading hard borders map.");
                                  throw e2;
                          }
                  }
                  else if( e.getAttribute("type").getValue().equalsIgnoreCase("file") ) {
                          try {
                                  XYFunction xyf = new XYFunction( e.getText(), (isEdgeDeadly())?1.0:-1.0 );
                                  if( e.getAttribute("startgeneration") != null && e.getAttribute("startgeneration") != null ) {
                                          xyf.startgeneration = e.getAttribute("startgeneration").getIntValue();
                                          xyf.endgeneration = e.getAttribute("endgeneration").getIntValue();
                                  }
                                  hardborders.add( xyf );
                          }
                          catch( Exception e2 ) {
                                  System.out.println("Fatal error while loading hard borders map.");
                                  throw e2;
                          }
                  }
          }
          
          dataelem = document.getRootElement().getChild("softborders").getChildren("data");
          for(int i=0; i<dataelem.size(); i++) {
                  Element e = (Element)dataelem.get(i);
                  if( e.getAttribute("type").getValue().equalsIgnoreCase("image") ) {
                          try {
                                  Image image = new ImageIcon( e.getText() ).getImage();
                                  XYFunction xyf = new XYFunction(image,
                                                                                                  e.getAttribute("color").getValue(),
                                                                                                  e.getAttribute("maxvalue").getDoubleValue(), (isEdgeDeadly())?-1.0:1.0 );
                                  if( e.getAttribute("startgeneration") != null && e.getAttribute("startgeneration") != null ) {
                                          xyf.startgeneration = e.getAttribute("startgeneration").getIntValue();
                                          xyf.endgeneration = e.getAttribute("endgeneration").getIntValue();
                                  }
                                  softborders.add( xyf );
                          }
                          catch( Exception e2 ) {
                                  System.out.println("Fatal error while loading soft borders capacity map.");
                                  throw e2;
                          }
                  }
                  else if( e.getAttribute("type").getValue().equalsIgnoreCase("file") ) {
                          try {
                                  XYFunction xyf = new XYFunction( e.getText(), (isEdgeDeadly())?-1.0:1.0 );
                                  if( e.getAttribute("startgeneration") != null && e.getAttribute("startgeneration") != null ) {
                                          xyf.startgeneration = e.getAttribute("startgeneration").getIntValue();
                                          xyf.endgeneration = e.getAttribute("endgeneration").getIntValue();
                                  }
                                  softborders.add( xyf );
                          }
                          catch( Exception e2 ) {
                                  System.out.println("Fatal error while loading soft borders map.");
                                  throw e2;
                          }
                  }
          }

		
        dataelem = document.getRootElement().getChild("reproductiveability").getChildren("distribution");
        for(int i=0; i<dataelem.size(); i++) {
                Element e = (Element)dataelem.get(i);
                String x = "";
                String p = "";
                if( e.getAttribute("offspring") != null && e.getAttribute("p") != null ) {
                        x = e.getAttribute("offspring").getValue();
                        p = e.getAttribute("p").getValue();
                }
                
                PFunction pf = new PFunction(x,p);
                
                if( e.getAttribute("startgeneration") != null && e.getAttribute("startgeneration") != null ) {
                        pf.startgeneration = e.getAttribute("startgeneration").getIntValue();
                        pf.endgeneration = e.getAttribute("endgeneration").getIntValue();
                }
                noffspring.add( pf );
        }
        
        dataelem = document.getRootElement().getChild("dispersalradius").getChildren("distribution");
        for(int i=0; i<dataelem.size(); i++) {
                Element e = (Element)dataelem.get(i);
                String x = "";
                String p = "";
                if( e.getAttribute("radii") != null && e.getAttribute("p") != null ) {
                        x = e.getAttribute("radii").getValue();
                        p = e.getAttribute("p").getValue();
                }
                
                PFunction pf = new PFunction(x,p);
                
                if( e.getAttribute("startgeneration") != null && e.getAttribute("startgeneration") != null ) {
                        pf.startgeneration = e.getAttribute("startgeneration").getIntValue();
                        pf.endgeneration = e.getAttribute("endgeneration").getIntValue();
                }
                dispersalradius.add( pf );
        }

		dataelem = document.getRootElement().getChild("initialpopulation").getChildren("node");
		for(int i=0; i<dataelem.size(); i++) {
			Element e = (Element)dataelem.get(i);
			int n = 1;
			if( e.getAttribute("n") != null ) n = e.getAttribute("n").getIntValue();
			for(int j=0; j<n; j++)
				if( e.getAttribute("lat").getDoubleValue() <= getMaxLat() && e.getAttribute("lat").getDoubleValue() >= getMinLat() && 
				    e.getAttribute("lon").getDoubleValue() <= getMaxLon() && e.getAttribute("lon").getDoubleValue() >= getMinLon() ) {
						// initialpopulation.add( new Node(e.getAttribute("lat").getDoubleValue(), e.getAttribute("lon").getDoubleValue(), e.getAttribute("r").getIntValue(), e.getAttribute("g").getIntValue(), e.getAttribute("b").getIntValue() ) );
						initialpopulation.add( new Node( getMinLat()+(getMaxLat()-e.getAttribute("lat").getDoubleValue())  , e.getAttribute("lon").getDoubleValue(), e.getAttribute("r").getIntValue(), e.getAttribute("g").getIntValue(), e.getAttribute("b").getIntValue() ) ); // Since the internal coordinate system flips latitudes, this
				} else {																																																														  // flips the latitudes of the starting population to match up.
					throw new Exception("Initial Population outside of bounds!");
				}
		}
		
		Element eoutput = document.getRootElement().getChild("output");
		if( eoutput == null ) {
			System.out.println("no output; nothing to do!");
			throw new Exception("ImBoredException");	
		}
		dataelem = document.getRootElement().getChild("output").getChildren();
		for(int i=0; i<dataelem.size(); i++) {
			Element outputi = (Element)dataelem.get(i);
			if( outputi.getName().equalsIgnoreCase("visual") )
				visualoutput = true;
			else {
				//  Create the output functor
				OutputFunction of = new OutputFunction(this,outputi);
				outputfunctors.add(of);
			}
		}
	}
	
	public String getSimName() {
		return simulationname;
	}
	
	public int getNGenerations() throws Exception {
		return Integer.parseInt( document.getRootElement().getChild("generations").getText() );
	}
	
	public int getNThreads() throws Exception {
		if( document.getRootElement().getChild("threads") != null ) 
			return Integer.parseInt( document.getRootElement().getChild("threads").getText() );
		return 1;
	}
	

	
	public boolean isEdgeDeadly() throws Exception {
		return document.getRootElement().getChild("edges").getAttribute("type").getValue().equalsIgnoreCase("deadly");
	}
	
    public int getNOffspring(int generation, java.util.Random rand) {
        PFunction thefunc=null;
        for(int i=0;i<noffspring.size(); i++)
                if( noffspring.get(i).startgeneration <= generation && noffspring.get(i).endgeneration >= generation )  // MODIFIED BY JMB -- 10.20.09 -- CHANGED FINAL > TO >=
                        thefunc = noffspring.get(i);
        if( thefunc == null ) {
                for(int i=0;i<noffspring.size();i++)
                        if( noffspring.get(i).startgeneration == -1 && noffspring.get(i).endgeneration == -1 ) {
                                thefunc = noffspring.get(i);
                                break;
                        }
        }
        return (int)thefunc.draw(rand);
}

    public double getDispersalRadius(int generation, java.util.Random rand) {
        PFunction thefunc=null;
        for(int i=0;i<dispersalradius.size(); i++)
                if( dispersalradius.get(i).startgeneration <= generation && dispersalradius.get(i).endgeneration >= generation )        // MODIFIED BY JMB -- 10.20.09 -- CHANGED FINAL > TO >=
                        thefunc = dispersalradius.get(i);
        if( thefunc == null ) {
                for(int i=0;i<dispersalradius.size();i++)
                        if( dispersalradius.get(i).startgeneration == -1 && dispersalradius.get(i).endgeneration == -1 ) {
                                thefunc = dispersalradius.get(i);
                                break;
                        }
        }
        return thefunc.draw(rand);
}
    
    
    public XYFunction getCarryingCapacity(int generation) {
            XYFunction thefunc=null;
            for(int i=0;i<carryingcapacity.size(); i++)
                    if( carryingcapacity.get(i).startgeneration <= generation && carryingcapacity.get(i).endgeneration >= generation )
                            thefunc = carryingcapacity.get(i);
            if( thefunc == null ) {
                    for(int i=0;i<carryingcapacity.size();i++)
                            if( carryingcapacity.get(i).startgeneration == -1 && carryingcapacity.get(i).endgeneration == -1 ) {
                                    thefunc = carryingcapacity.get(i);
                                    break;
                            }
            }
            return thefunc;
    }
    
    public XYFunction getHardBorders(int generation) {
            XYFunction thefunc=null;
            for(int i=0;i<hardborders.size(); i++)
                    if( hardborders.get(i).startgeneration <= generation && hardborders.get(i).endgeneration >= generation )
                            thefunc = hardborders.get(i);
            if( thefunc == null ) {
                    for(int i=0;i<hardborders.size();i++)
                            if( hardborders.get(i).startgeneration == -1 && hardborders.get(i).endgeneration == -1 ) {
                                    thefunc = hardborders.get(i);
                                    break;
                            }
            }
            return thefunc;
    }
    
    public XYFunction getSoftBorders(int generation) {
            XYFunction thefunc=null;
            for(int i=0;i<softborders.size(); i++)
                    if( softborders.get(i).startgeneration <= generation && softborders.get(i).endgeneration >= generation ) // MODIFIED BY JMB -- 10.20.09 -- CHANGED FINAL > TO >=
                            thefunc = softborders.get(i);
            if( thefunc == null ) {
                    for(int i=0;i<softborders.size();i++)
                            if( softborders.get(i).startgeneration == -1 && softborders.get(i).endgeneration == -1 ) {
                                    thefunc = softborders.get(i);
                                    break;
                            }
            }
            return thefunc;
    }
    

	
	public ArrayList<Node> getInitialPopulation() {
		return initialpopulation;
	}
	
	public void doOutput(ArrayList<Node> generation,int gen_num, java.util.Random rand) throws Exception {
		if( gen_num == -1 ) {
			for(int i=0; i<outputfunctors.size(); i++) {
				outputfunctors.get(i).execute(generation,gen_num,rand);
			}	
		}
		for(int i=0; i<outputfunctors.size(); i++) {
			if( outputfunctors.get(i).output_every_n_generations != -1 && (((gen_num % outputfunctors.get(i).output_every_n_generations) == 0) || gen_num == 1 ) )
				outputfunctors.get(i).execute(generation,gen_num,rand);
		}
	}
}