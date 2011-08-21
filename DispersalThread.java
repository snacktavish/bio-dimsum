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



import java.util.*;
import static java.lang.Math.*;

public class DispersalThread implements Runnable
{
	public ArrayList<Node> thisGeneration = null;
	public ArrayList<Node> nextGeneration = null;

	public DispersalSettings settings;

	public int threadNum =-1;

	public int end_gen = -1;	// Added by JMB
	
	public java.util.Random rand;	// Added by JMB -- 4.5.10
	
	public DispersalThread(DispersalSettings ds, int end_generation, int ran_seed)	// Modified by JMB -- 4.5.10
	{
		settings = ds;
		thisGeneration = new ArrayList<Node>();
		nextGeneration = new ArrayList<Node>();
		end_gen = end_generation;
		rand = new java.util.Random(ran_seed);		// Added by JMB -- 4.5.10
	}

	public void add(Node n)
	{
		thisGeneration.add(n);
	}

	public void pause() throws Exception
	{
		// System.in.read();
		// System.in.read();
	}

	public void dprint(String so)
	{
		// System.out.println(so);
	}

	public void run()
	{
		ArrayList<Node> children = new ArrayList<Node>();
		try {

			for(int i=0; i<thisGeneration.size(); i++) {
				// Decide how many offspring and add to the next generation (uninitialized in lat, lon)
				int numberOfChildren = settings.getNOffspring(Dispersion.generation, rand);
				for(int j=0 ; j < numberOfChildren ; j++ )
				{
					Node child = new Node(Dispersion.generation+1, thisGeneration.get(i)); // generation, parent	JMB COMMENT -- CREATES NEXT GEN NODE BY PASSING NEXT GEN # AND PARENT TO CONSTRUCTOR
					thisGeneration.get(i).children.add(child);
					children.add(child);
				}
			}
			
			double minlat = settings.getMinLat();
			double maxlat = settings.getMaxLat();
			double minlon = settings.getMinLon();
			double maxlon = settings.getMaxLon();

			double step_d = 0.0001;

			boolean debug = false;				// ADDED BY JMB -- Boolean to control the outputting of LOTS of extra information to help debug
			
			Iterator<Node> childItr = children.iterator();	// Added by JMB -- 4.13.10
			
			childrenloop:
				for(int i=0; i<children.size(); i++) {
					// System.out.println("Thread Number "+threadNum);
					// Node n = children.get(i);
					Node n = (Node)childItr.next();		// Added by JMB -- 4.13.10
					
					// this next chunk should be moved...
					double sb_lonspace = 0.0;
					double sb_latspace = 0.0;
					double hb_lonspace = 0.0;
					double hb_latspace = 0.0;
					
					if (n.generation-1 == end_gen)	// To properly get soft borders for the children of individuals in the final generation
					{
						sb_lonspace = (maxlon-minlon)/(settings.getSoftBorders(n.generation-1).getMaxX());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LONGITUDES IN DECIMAL DEGREES (UNIT: DEGREES/PIXEL)
						sb_latspace = (maxlat-minlat)/(settings.getSoftBorders(n.generation-1).getMaxY());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LATITUDES IN DECIMAL DEGREES
						hb_lonspace = (maxlon-minlon)/(settings.getHardBorders(n.generation-1).getMaxX());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LONGITUDES IN DECIMAL DEGREES
						hb_latspace = (maxlat-minlat)/(settings.getHardBorders(n.generation-1).getMaxY());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LATITUDES IN DECIMAL DEGREES												
					}
					else
					{
						sb_lonspace = (maxlon-minlon)/(settings.getSoftBorders(n.generation).getMaxX());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LONGITUDES IN DECIMAL DEGREES (UNIT: DEGREES/PIXEL)
						sb_latspace = (maxlat-minlat)/(settings.getSoftBorders(n.generation).getMaxY());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LATITUDES IN DECIMAL DEGREES
						hb_lonspace = (maxlon-minlon)/(settings.getHardBorders(n.generation).getMaxX());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LONGITUDES IN DECIMAL DEGREES
						hb_latspace = (maxlat-minlat)/(settings.getHardBorders(n.generation).getMaxY());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LATITUDES IN DECIMAL DEGREES						
					}
										
					double d = settings.getDispersalRadius(n.generation,rand);
					double crs = rand.nextDouble() * 2 * Math.PI;	// Modified by JMB -- 4.5.10
					
					double lat1 = n.parent.lat;
					double lon1 = n.parent.lon;

					/*if (n.generation ==  && n.unique == )
					{
						System.out.println(" created");
						System.out.println("d: "+d);
						System.out.println("crs: "+crs);
						System.out.println("lat1: "+lat1);
						System.out.println("lon1: "+lon1);
					}*/					
					
	
					
					lldata lld = new lldata();		// JMB -- lldata CLASS STORES LAT/LON DATA AND DOES CONVERSIONS	

					try {
					travelloop:
						while( d >= 0.000001 ) {
							
							double sb_lat_bt = 0.0;		// JMB -- Using this to keep track of lat/lon value for border reflections and adjusting inexact positions, if necessary
							double sb_lon_bt = 0.0;
							double hb_lat_bt = 0.0;
							double hb_lon_bt = 0.0;
							//double border_buffer = 0.001;
							
							if (debug)
							{
								System.out.println("");
								System.out.println("d: "+d);			// JMB -- FOR DEBUGGING
								System.out.println("crs: "+crs);		// JMB -- FOR DEBUGGING
								System.out.println("lat1: "+lat1);		// JMB -- FOR DEBUGGING
								System.out.println("lon1: "+lon1);		// JMB -- FOR DEBUGGING
							}
			
							
							lld.d = d;					// JMB COMMENT -- SETS D INTERNALLY IN LLD OBJECT
							lld.R = 6371;				// radius of spherical earth in km (TODO: use value(# or units?) from xml)
							lld.setLatSDDeg(lat1);		// JMB COMMENT -- CONVERTS LATITUDE FROM DECIMAL DEGREES TO RADIANS
							lld.setLonSDDeg(lon1);		// JMB COMMENT -- CONVERTS LONGITUDE FROM DECIMAL DEGREES TO RADIANS
							lld.crs = crs;				// JMB COMMENT -- SETS CRS INTERNALLY WITHIN LLD OBJECT
							lld.llffromdcrs();			// JMB COMMENT -- GETS ENDING LAT/LON FROM DISTANCE AND COURSE AND STORES INTERNALLY IN LLD OBJECT

							dprint("initial -- start: lat="+lld.lats()+" lon="+lld.lons());
							dprint("initial --        crs="+lld.crs()+" d="+lld.d);
							dprint("initial --   end: lat="+lld.latf()+" lon="+lld.lonf());

							double hb_dd=100000000,sb_dd=100000000;
							int hb_dx=0,hb_dy=0,sb_dx=0,sb_dy=0;

							if( crs >= 0 && crs <= PI/2 ) {		// JMB COMMENT -- DETERMINES IF COURSE IS NORTHEAST...OR IS THIS SOUTHEAST, SINCE INPUT MAPS ARE FLIPPED??
								// check soft borders first
								double ilat = lat1 - ((lat1-minlat)%sb_latspace) + sb_latspace; // the next crossings of lat,lon lines	
								double ilon = lon1 - ((lon1-minlon)%sb_lonspace) + sb_lonspace; // (-),(+) b/c 1st quadrant	
								//System.out.println("SE ilat: "+ilat); // ADDED BY JMB -- FOR DEBUGGING
								//System.out.println("SE ilon: "+ilon); // ADDED BY JMB -- FOR DEBUGGING
								
								if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
								{					// If so, pushes it barely off in a random direction.
									if (rand.nextDouble() > 0.5)
										lat1 += 0.000000001;
									else
										lat1 -= 0.000000001;
									ilat = lat1 - ((lat1-minlat)%sb_latspace) + sb_latspace;
								}
								
								if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
								{
									if (rand.nextDouble() > 0.5)
										lon1 += 0.000000001;
									else
										lon1 -= 0.000000001;
									ilon = lon1 - ((lon1-minlon)%sb_lonspace) + sb_lonspace;
								}																
								
								sb_lat_bt = ilat;
								sb_lon_bt = ilon;
								
								dprint("Next sb latitude crossing north: "+ilat);
								dprint("Next sb longitude crossing east: "+ilon);

								lldata i1 = lld.londfromlat3(ilat);		// JMB COMMENT -- FINDS COORDINATES FOR NEAREST LAT BORDER CROSSING
								lldata i2 = lld.latdfromlon3(ilon);		// JMB COMMENT -- FINDS COORDINATES FOR NEAREST LON BORDER CROSSING
								if (debug)
								{
									System.out.println("");
									System.out.println("Northeast Soft Border Info");
									System.out.println("d: "+d+" i1.d: "+i1.d+" i1.d: "+i2.d+" ");	// ADDED BY JMB
									System.out.println("sb_i1.d: "+i1.d+"	sb_i1.latf: "+i1.latf()+"	sb_i1.lonf: "+i1.lonf());
									System.out.println("sb_i2.d: "+i2.d+"	sb_i2.latf: "+i2.latf()+"	sb_i2.lonf: "+i2.lonf());
									System.out.println("sb_ilat: "+ilat);
									System.out.println("sb_ilon: "+ilon);
									System.out.println("Starting at lat: "+lld.lats()+" and lon: "+lld.lons());
									System.out.println("Original end at lat: "+lld.latf()+" and lon: "+lld.lonf());
									System.out.println("");
								}
								if( i1.d <= i2.d  && i1.d < d) {		// JMB COMMENT -- TRUE IF NEAREST PIXEL BORDER CROSSING IS A LAT CROSSING
									dprint("sb - lat crossing is closer");
									dprint("start: lat="+i1.lats()+" lon="+i1.lons());
									dprint("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d+" crs="+i1.crs);
									if (debug)
										System.out.println("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d+" crs="+i1.crs);
									// we're now at the point in i1 that puts us one step_d before the pixel crossing.
									sb_dd = i1.d;				// JMB COMMENT -- SETS SB_DD EQUAL TO DISTANCE TO NEAREST LAT CROSSING
									sb_dx = 0; sb_dy = 1;		// JMB COMMENT -- SB_DX AND SB_DY FUNCTION AS INDICATORS FOR CROSSING? DY INDICATES LAT CROSSING?
								}
								else if( i2.d < d ) {				// JMB COMMENT -- TRUE IF NEAREST PIXEL BORDER CROSSING IS A LON CROSSING
									dprint("sb - lon crossing is closer");
									dprint("start: lat="+i2.lats()+" lon="+i2.lons());
									dprint("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d);
									if (debug)
										System.out.println("target (lat crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d+" crs="+i2.crs);
									// we're now at the point in i2 that puts us one step_d before the pixel crossing.
									sb_dd = i2.d;				// JMB COMMENT -- SETS SB_DD EQEUAL TO DISTANCE TO NEAREST LON CROSSING
									sb_dx = 1; sb_dy = 0;		// JMB COMMENT -- SB_DX AND SB_DY FUNCTION AS INDICATORS FOR CROSSING?  DX INDICATES LON CROSSING?
									}


								// check hard border crossing next
								ilat = lat1 - ((lat1-minlat)%hb_latspace) + hb_latspace; // the next crossings of lat,lon lines
								ilon = lon1 - ((lon1-minlon)%hb_lonspace) + hb_lonspace; // (-),(+) b/c 1st quadrant
								dprint("Next hb latitude crossing north: "+ilat);
								dprint("Next hb longitude crossing east: "+ilon);

								if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
								{					// If so, pushes it barely off in a random direction.
									if (rand.nextDouble() > 0.5)
										lat1 += 0.000000001;
									else
										lat1 -= 0.000000001;
									ilat = lat1 - ((lat1-minlat)%sb_latspace) + sb_latspace;
								}
								
								if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
								{
									if (rand.nextDouble() > 0.5)
										lon1 += 0.000000001;
									else
										lon1 -= 0.000000001;
									ilon = lon1 - ((lon1-minlon)%sb_lonspace) + sb_lonspace;
								}																								
								
								hb_lat_bt = ilat;
								hb_lon_bt = ilon;
								
								i1 = lld.londfromlat3(ilat);
								i2 = lld.latdfromlon3(ilon);
								if (debug)
								{
									System.out.println("");
									System.out.println("Northeast Hard Border Info");
									System.out.println(d+" "+i1.d+" "+i2.d+" ");	// ADDED BY JMB
									System.out.println("LLD Start Lat: "+lld.lats());
									System.out.println("LLD End Lat: "+lld.latf());
									System.out.println("LLD Start Lon: "+lld.lons());
									System.out.println("LLD End Lon: "+lld.lonf());
									System.out.println("hb_i1.d: "+i1.d);
									System.out.println("hb_i2.d: "+i2.d);
									System.out.println("hb_ilat: "+ilat);
									System.out.println("hb_ilon: "+ilon);
									System.out.println("");
								}
								if( i1.d <= i2.d && i1.d < d ) {
									dprint("hb - lat crossing is closer");
									dprint("start: lat="+i1.lats()+" lon="+i1.lons());
									dprint("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d);
									// we're now at the point in i1 that puts us one step_d before the pixel crossing.
									hb_dd = i1.d;
									hb_dx = 0; hb_dy = 1;
								}
								else if( i2.d < d ) {
									dprint("hb - lon crossing is closer");
									dprint("start: lat="+i2.lats()+" lon="+i2.lons());
									dprint("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d);
									// we're now at the point in i2 that puts us one step_d before the pixel crossing.
									hb_dd = i2.d;
									hb_dx = 1; hb_dy = 0;
								}
							}
							else if( crs > PI/2 && crs <= PI ) {			// JMB COMMENT -- Determines if course is southeast.  Note that this coordinate system is upside down relative to the standard.
								// check soft borders first					// JMB COMMENT -- 4.14.10 -- COURSE MAY ACTUALLY BE NORTHEAST ON INPUT MAP??
								double ilat = lat1 - ((lat1-minlat)%sb_latspace); // the next crossings of lat,lon lines
								double ilon = lon1 - ((lon1-minlon)%sb_lonspace) + sb_lonspace; 
								// System.out.println("NW ilat: "+ilat);
								// System.out.println("NW ilon: "+ilon);
								dprint("Next sb latitude crossing south: "+ilat);
								dprint("Next sb longitude crossing east: "+ilon);

								if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
								{					// If so, pushes it barely off in a random direction.
									if (rand.nextDouble() > 0.5)
										lat1 += 0.000000001;
									else
										lat1 -= 0.000000001;
									ilat = lat1 - ((lat1-minlat)%sb_latspace);
								}
								
								if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
								{
									if (rand.nextDouble() > 0.5)
										lon1 += 0.000000001;
									else
										lon1 -= 0.000000001;
									ilon = lon1 - ((lon1-minlon)%sb_lonspace) + sb_lonspace; 
								}								
								
								sb_lat_bt = ilat;
								sb_lon_bt = ilon;
								
								lldata i1 = lld.londfromlat3(ilat);
								lldata i2 = lld.latdfromlon3(ilon);
								if (debug)
								{
									System.out.println("");
									System.out.println("Southeast Soft Border Info");
									System.out.println("d: "+d+" i1.d: "+i1.d+" i1.d: "+i2.d);	// ADDED BY JMB
									System.out.println("LLD Start Lat: "+lld.lats());
									System.out.println("LLD End Lat: "+lld.latf());
									System.out.println("LLD Start Lon: "+lld.lons());
									System.out.println("LLD End Lon: "+lld.lonf());
									System.out.println("sb_i1.d: "+i1.d+"	sb_i1.latf: "+i1.latf()+"	sb_i1.lonf: "+i1.lonf());
									System.out.println("sb_i2.d: "+i2.d+"	sb_i2.latf: "+i2.latf()+"	sb_i2.lonf: "+i2.lonf());
									System.out.println("sb_ilat: "+ilat);
									System.out.println("sb_ilon: "+ilon);
									System.out.println("");
								}
								if( i1.d <= i2.d  && i1.d < d) {
									dprint("sb - lat crossing is closer");
									dprint("start: lat="+i1.lats()+" lon="+i1.lons());
									dprint("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d);
									if (debug)
										System.out.println("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d+" crs="+i1.crs);
									sb_dd = i1.d;
									sb_dx = 0; sb_dy = -1;
								}
								else if( i2.d < d ) {
									dprint("sb - lon crossing is closer");
									dprint("start: lat="+i2.lats()+" lon="+i2.lons());
									dprint("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d);
									if (debug)
										System.out.println("target (lat crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d+" crs="+i2.crs);
									sb_dd = i2.d;
									sb_dx = 1; sb_dy = 0;
								}


								// check hard border crossing next
								ilat = lat1 - ((lat1-minlat)%hb_latspace); // the next crossings of lat,lon lines
								ilon = lon1 - ((lon1-minlon)%hb_lonspace) + hb_lonspace; 
								dprint("Next hb latitude crossing south: "+ilat);
								dprint("Next hb longitude crossing east: "+ilon);

								if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
								{					// If so, pushes it barely off in a random direction.
									if (rand.nextDouble() > 0.5)
										lat1 += 0.000000001;
									else
										lat1 -= 0.000000001;
									ilat = lat1 - ((lat1-minlat)%sb_latspace);
								}
								
								if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
								{
									if (rand.nextDouble() > 0.5)
										lon1 += 0.000000001;
									else
										lon1 -= 0.000000001;
									ilon = lon1 - ((lon1-minlon)%sb_lonspace) + sb_lonspace; 
								}																
								
								hb_lat_bt = ilat;
								hb_lon_bt = ilon;
								
								i1 = lld.londfromlat3(ilat);
								i2 = lld.latdfromlon3(ilon);
								if (debug)
								{
									System.out.println("");
									System.out.println("Southeast Hard Border Info");
									System.out.println(d+" "+i1.d+" "+i2.d+" ");  // ADDED BY JMB
									System.out.println("LLD Start Lat: "+lld.lats());
									System.out.println("LLD End Lat: "+lld.latf());
									System.out.println("LLD Start Lon: "+lld.lons());
									System.out.println("LLD End Lon: "+lld.lonf());
									System.out.println("hb_i1.d: "+i1.d);
									System.out.println("hb_i2.d: "+i2.d);
									System.out.println("hb_ilat: "+ilat);
									System.out.println("hb_ilon: "+ilon);
									System.out.println("");
								}
								if( i1.d <= i2.d && i1.d < d ) {
									dprint("hb - lat crossing is closer");
									dprint("start: lat="+i1.lats()+" lon="+i1.lons());
									dprint("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d);
									hb_dd = i1.d;
									hb_dx = 0; hb_dy = -1;
								}
								else if( i2.d < d ) {
									dprint("hb - lon crossing is closer");
									dprint("start: lat="+i2.lats()+" lon="+i2.lons());
									dprint("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d);
									hb_dd = i2.d;
									hb_dx = 1; hb_dy = 0;
								}

							}
							else if( crs > PI && crs <= 3*PI/2 ) {  // JMB COMMENT -- DETERMINES IF COURSE IS SOUTHWEST
								// check soft borders first			// JMB COMMENT -- 4.14.10 -- COURSE MAY ACTUALLY BE NORTHWEST ON INPUT MAP??
								double ilat = lat1 - ((lat1-minlat)%sb_latspace); // the next crossings of lat,lon lines
								double ilon = lon1 - ((lon1-minlon)%sb_lonspace);
								dprint("Next sb latitude crossing south: "+ilat);
								dprint("Next sb longitude crossing west: "+ilon);

								if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
								{					// If so, pushes it barely off in a random direction.
									if (rand.nextDouble() > 0.5)
										lat1 += 0.000000001;
									else
										lat1 -= 0.000000001;
									ilat = lat1 - ((lat1-minlat)%sb_latspace);
								}
								
								if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
								{
									if (rand.nextDouble() > 0.5)
										lon1 += 0.000000001;
									else
										lon1 -= 0.000000001;
									ilon = lon1 - ((lon1-minlon)%sb_lonspace);
								}
								
								
								sb_lat_bt = ilat;
								sb_lon_bt = ilon;
								
								lldata i1 = lld.londfromlat3(ilat);
								lldata i2 = lld.latdfromlon3(ilon);
								if (debug)
								{
									System.out.println("");
									System.out.println("Southwest Soft Border Info");
									System.out.println("d: "+d+" i1.d: "+i1.d+" i2.d: "+i2.d);	// ADDED BY JMB
									System.out.println("LLD Start Lat: "+lld.lats());
									System.out.println("LLD End Lat: "+lld.latf());
									System.out.println("LLD Start Lon: "+lld.lons());
									System.out.println("LLD End Lon: "+lld.lonf());
									System.out.println("sb_i1.d: "+i1.d+"	sb_i1.latf: "+i1.latf()+"	sb_i1.lonf: "+i1.lonf());
									System.out.println("sb_i2.d: "+i2.d+"	sb_i2.latf: "+i2.latf()+"	sb_i2.lonf: "+i2.lonf());
									System.out.println("sb_ilat: "+ilat);
									System.out.println("sb_ilon: "+ilon);
									System.out.println("");
								}
								if( i1.d <= i2.d  && i1.d < d) {
									dprint("sb - lat crossing is closer");
									dprint("start: lat="+i1.lats()+" lon="+i1.lons());
									dprint("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d);
									if (debug)
										System.out.println("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d+" crs="+i1.crs);
									sb_dd = i1.d;
									sb_dx = 0; sb_dy = -1;
								}
								else if( i2.d < d ) {
									dprint("sb - lon crossing is closer");
									dprint("start: lat="+i2.lats()+" lon="+i2.lons());
									dprint("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d);
									if (debug)
										System.out.println("target (lat crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d+" crs="+i2.crs);
									sb_dd = i2.d;
									sb_dx = -1; sb_dy = 0;
								}

								// check hard border crossing next
								ilat = lat1 - ((lat1-minlat)%hb_latspace); // the next crossings of lat,lon lines
								ilon = lon1 - ((lon1-minlon)%hb_lonspace);
								dprint("Next hb latitude crossing south: "+ilat);
								dprint("Next hb longitude crossing west: "+ilon);

								if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
								{					// If so, pushes it barely off in a random direction.
									if (rand.nextDouble() > 0.5)
										lat1 += 0.000000001;
									else
										lat1 -= 0.000000001;
									ilat = lat1 - ((lat1-minlat)%sb_latspace);
								}
								
								if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
								{
									if (rand.nextDouble() > 0.5)
										lon1 += 0.000000001;
									else
										lon1 -= 0.000000001;
									ilon = lon1 - ((lon1-minlon)%sb_lonspace);
								}								
								
								hb_lat_bt = ilat;
								hb_lon_bt = ilon;
								
								i1 = lld.londfromlat3(ilat);
								i2 = lld.latdfromlon3(ilon);
								if (debug)
								{
									System.out.println("");
									System.out.println("Southwest Hard Border Info");
									System.out.println(d+" "+i1.d+" "+i2.d+" ");	// ADDED BY JMB
									System.out.println("LLD Start Lat: "+lld.lats());
									System.out.println("LLD End Lat: "+lld.latf());
									System.out.println("LLD Start Lon: "+lld.lons());
									System.out.println("LLD End Lon: "+lld.lonf());
									System.out.println("hb_i1.d: "+i1.d);
									System.out.println("hb_i2.d: "+i2.d);
									System.out.println("hb_ilat: "+ilat);
									System.out.println("hb_ilon: "+ilon);
									System.out.println("");
								}
								if( i1.d <= i2.d && i1.d < d ) {
									dprint("hb - lat crossing is closer");
									dprint("start: lat="+i1.lats()+" lon="+i1.lons());
									dprint("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d);
									hb_dd = i1.d;
									hb_dx = 0; hb_dy = -1;
								}
								else if( i2.d < d ) {
									dprint("hb - lon crossing is closer");
									dprint("start: lat="+i2.lats()+" lon="+i2.lons());
									dprint("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d);
									hb_dd = i2.d;
									hb_dx = -1; hb_dy = 0;
								}

							}						
							else if( crs > 3*PI/2 && crs <= 2*PI ) {   // JMB COMMENT -- 10.19.09 -- DETERMINES IF COURSE IS NORTHWEST
								// check soft borders first			   // JMB COMMENT -- 4.14.10 -- COURSE MAY ACTUALLY BE SOUTHWEST ON INPUT MAP??
								double ilat = lat1 - ((lat1-minlat)%sb_latspace) + sb_latspace; // the next crossings of lat,lon lines
								double ilon = lon1 - ((lon1-minlon)%sb_lonspace); 
								dprint("Next sb latitude crossing north: "+ilat);
								dprint("Next sb longitude crossing west: "+ilon);

								if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
								{					// If so, pushes it barely off in a random direction.
									if (rand.nextDouble() > 0.5)
										lat1 += 0.000000001;
									else
										lat1 -= 0.000000001;
									ilat = lat1 - ((lat1-minlat)%sb_latspace) + sb_latspace;
								}
								
								if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
								{
									if (rand.nextDouble() > 0.5)
										lon1 += 0.000000001;
									else
										lon1 -= 0.000000001;
									ilon = lon1 - ((lon1-minlon)%sb_lonspace);
								}
								
								sb_lat_bt = ilat;
								sb_lon_bt = ilon;
								
								lldata i1 = lld.londfromlat3(ilat);
								lldata i2 = lld.latdfromlon3(ilon);
								if (debug)
								{
									System.out.println("");
									System.out.println("Northwest Soft Border Info");
									System.out.println(d+" "+i1.d+" "+i2.d+" ");	// ADDED BY JMB
									System.out.println("LLD Start Lat: "+lld.lats());
									System.out.println("LLD End Lat: "+lld.latf());
									System.out.println("LLD Start Lon: "+lld.lons());
									System.out.println("LLD End Lon: "+lld.lonf());
									System.out.println("sb_i1.d: "+i1.d+"	sb_i1.latf: "+i1.latf()+"	sb_i1.lonf: "+i1.lonf());
									System.out.println("sb_i2.d: "+i2.d+"	sb_i2.latf: "+i2.latf()+"	sb_i2.lonf: "+i2.lonf());
									System.out.println("sb_ilat: "+ilat);
									System.out.println("sb_ilon: "+ilon);
									System.out.println("");
								}
								if( i1.d <= i2.d  && i1.d < d) {
									dprint("sb - lat crossing is closer");
									dprint("start: lat="+i1.lats()+" lon="+i1.lons());
									dprint("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d);
									if (debug)
										System.out.println("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d+" crs="+i1.crs);
									sb_dd = i1.d;
									sb_dx = 0; sb_dy = 1;
								}
								else if( i2.d < d ) {
									dprint("sb - lon crossing is closer");
									dprint("start: lat="+i2.lats()+" lon="+i2.lons());
									dprint("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d);
									if (debug)
										System.out.println("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d+" crs="+i2.crs);
									sb_dd = i2.d;
									sb_dx = -1; sb_dy = 0;
								}

								// check hard border crossing next
								ilat = lat1 - ((lat1-minlat)%hb_latspace) + hb_latspace; // the next crossings of lat,lon lines
								ilon = lon1 - ((lon1-minlon)%hb_lonspace); 
								dprint("Next hb latitude crossing north: "+ilat);
								dprint("Next hb longitude crossing west: "+ilon);

								if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
								{					// If so, pushes it barely off in a random direction.
									if (rand.nextDouble() > 0.5)
										lat1 += 0.000000001;
									else
										lat1 -= 0.000000001;
									ilat = lat1 - ((lat1-minlat)%sb_latspace) + sb_latspace;
								}
								
								if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
								{
									if (rand.nextDouble() > 0.5)
										lon1 += 0.000000001;
									else
										lon1 -= 0.000000001;
									ilon = lon1 - ((lon1-minlon)%sb_lonspace);
								}								
								
								hb_lat_bt = ilat;
								hb_lon_bt = ilon;
								
								i1 = lld.londfromlat3(ilat);
								i2 = lld.latdfromlon3(ilon);
								if (debug)
								{
									System.out.println("");
									System.out.println("Northwest Hard Border Info");
									System.out.println("d: "+d+" i1.d: "+i1.d+" i2.d: "+i2.d);	// ADDED BY JMB
									System.out.println("LLD Start Lat: "+lld.lats());
									System.out.println("LLD End Lat: "+lld.latf());
									System.out.println("LLD Start Lon: "+lld.lons());
									System.out.println("LLD End Lon: "+lld.lonf());
									System.out.println("hb_i1.d: "+i1.d);
									System.out.println("hb_i2.d: "+i2.d);
									System.out.println("hb_ilat: "+ilat);
									System.out.println("hb_ilon: "+ilon);
									System.out.println("");
								}
								if( i1.d <= i2.d && i1.d < d ) {
									dprint("hb - lat crossing is closer");
									dprint("start: lat="+i1.lats()+" lon="+i1.lons());
									dprint("target (lat crossing): lat="+i1.latf()+" lon="+i1.lonf()+" d="+i1.d);
									hb_dd = i1.d;
									hb_dx = 0; hb_dy = 1;
								}
								else if( i2.d < d ) {
									dprint("hb - lon crossing is closer");
									dprint("start: lat="+i2.lats()+" lon="+i2.lons());
									dprint("target (lon crossing): lat="+i2.latf()+" lon="+i2.lonf()+" d="+i2.d);
									hb_dd = i2.d;
									hb_dx = -1; hb_dy = 0;
								}

							}



							if( d < min(sb_dd,hb_dd) ) {	// JMB COMMENT -- 10.20.09 -- DISPERSAL OCCURS BEFORE PIXEL BOUNDARY IS CROSSED, 
								dprint("Case 1");			//								SO INDIVIDUAL IS ADDED TO THE NEXT GENERATION
								// System.out.println("Moving done. d: "+d+"  sb_dd: "+sb_dd+"  hb_dd: "+hb_dd);	// ADDED BY JMB
								//for (int z=0; z<4; z++)
									//System.out.println("");	// ADDED BY JMB
								
								lld.d = d;
								lld.llffromdcrs();
								
								//*********************** Fudging to keep poorly estimated positions (due to step_d alterations) from crossing border boundaries inadvertently ***********************
								
								// IS THIS CHECK NECESSARY AT THIS POINT IN THE LOOP?  PERHAPS NOT, BUT SHOULD MAKE SURE BEFORE REMOVING IT.
								
								if ( (lat1 >= sb_lat_bt) && (lon1 >= sb_lon_bt) )		// Approaching borders from the upper right
								{
									n.lat = lld.getLatFDDeg();
									n.lon = lld.getLonFDDeg();
									if (lld.getLatFDDeg() < sb_lat_bt)
									{
										n.lat = sb_lat_bt + 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");
										}
									}
									if (lld.getLonFDDeg() < sb_lon_bt)
									{
										n.lon = sb_lon_bt + 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}
								}
								else if ( (lat1 < sb_lat_bt) && (lon1 < sb_lon_bt) )	// Approaching borders from the bottom left
								{
									n.lat = lld.getLatFDDeg();
									n.lon = lld.getLonFDDeg();
									if (lld.getLatFDDeg() > sb_lat_bt)
									{
										n.lat = sb_lat_bt - 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");	
										}
									}
									if (lld.getLonFDDeg() > sb_lon_bt)
									{
										n.lon = sb_lon_bt - 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");	
										}
									}									
								}
								else if ( (lat1 > sb_lat_bt) && (lon1 < sb_lon_bt) )	// Approaching borders from the upper left
								{
									n.lat = lld.getLatFDDeg();
									n.lon = lld.getLonFDDeg();
									if (lld.getLatFDDeg() < sb_lat_bt)
									{
										n.lat = sb_lat_bt + 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
									if (lld.getLonFDDeg() > sb_lon_bt)
									{
										n.lon = sb_lon_bt - 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
								}
								else if ( (lat1 < sb_lat_bt) && (lon1 > sb_lon_bt) )	// Approaching borders from the bottom right
								{	
									n.lat = lld.getLatFDDeg();
									n.lon = lld.getLonFDDeg();
									if (lld.getLatFDDeg() > sb_lat_bt)
									{
										n.lat = sb_lat_bt - 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");	
										}
									}									
									if (lld.getLonFDDeg() < sb_lon_bt)
									{
										n.lon = sb_lon_bt + 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
								}
								else
								{
									System.out.println("Weird things in soft border buffer for individual being added to next gen. Exiting...");
									System.exit(1);	
								}
								
								if ( (lat1 >= hb_lat_bt) && (lon1 >= hb_lon_bt) )		// Fudging to keep poorly estimated positions from crossing hard border boundaries
								{
									if (n.lat < hb_lat_bt)
									{
										n.lat = hb_lat_bt + 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");
										}
									}									
									if (n.lon < hb_lon_bt)
									{
										n.lon = hb_lon_bt + 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
								}
								else if ( (lat1 < hb_lat_bt) && (lon1 < hb_lon_bt) )
								{
									if (n.lat > hb_lat_bt)
									{
										n.lat = hb_lat_bt - 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
									if (n.lon > hb_lon_bt)
									{
										n.lon = hb_lon_bt - 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
								}
								else if ( (lat1 > hb_lat_bt) && (lon1 < hb_lon_bt) )
								{
									if (n.lat < hb_lat_bt)
									{
										n.lat = hb_lat_bt + 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
									if (n.lon > hb_lon_bt)
									{
										n.lon = hb_lon_bt - 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
								}
								else if ( (lat1 < hb_lat_bt) && (lon1 > hb_lon_bt) )
								{	
									if (n.lat > hb_lat_bt)
									{
										n.lat = hb_lat_bt - 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");																				
										}
									}									
									if (n.lon < hb_lon_bt)
									{
										n.lon = hb_lon_bt + 0.00001;
										if ( debug )
										{
											System.out.println("");
											System.out.println("Border buffer used when adding to next gen!");
											System.out.println("");
										}
									}									
								}
								else
								{
									System.out.println("Weird things in hard border buffer for individual being added to next gen. Exiting...");
									System.exit(1);	
								}
								
								if (debug)
									System.out.println("Just before being added to next gen -- lat: "+n.lat+" lon: "+n.lon);
													
								nextGeneration.add( n );
								d=0;
								
								/*if (n.generation ==  && n.unique == )
									System.out.println(" ADDED TO NEXT GEN");*/
								
								continue childrenloop;
							}
							else if( abs(sb_dd-hb_dd) < step_d ) {	// JMB COMMENT -- 10.20.09 -- BOTH SOFT AND HARD PIXEL BOUNDARIES WILL BE CROSSED 
								//d = 0;
								// both soft & hard must be checked at the same time -- but the order is up to you
								// I arbitrarily chose to check hard borders first
								dprint("*** Checking Hard Border");
																	
								XYFunction hb = null;
								if (n.generation-1 == end_gen)								// JMB -- Added to make sure that children of the final generation get the right hard borders
									hb = settings.getHardBorders(n.generation-1);
								else
									hb = settings.getHardBorders(n.generation);
								
								if( rand.nextDouble() <= hb.f(hb.toX(lon1,minlon,maxlon)+hb_dx,hb.toY(lat1,minlat,maxlat)+hb_dy) ) {	// JMB COMMENT -- FINDS HARD BORDER VALUE FOR NEXT PIXEL WITH RESPECT TO LONGITUDE
									dprint("failed");																				//								AND CHECKS TO SEE IF INDIVIDUAL SURVIVES HARD BORDER CROSSING.
									if (debug)
										System.out.println("Hard border death!");				// ADDED BY JMB -- FOR DEBUGGING
																	
									n.parent.children.remove( n.parent.children.indexOf( n ) ); // Added by JMB -- 4.14.10 -- Child needs to be removed from parent's children vector if it is not added to the next generation.
									
									/*if (n.generation ==  && n.unique == )
										System.out.println(" DIED AT HARD BORDER CHECK 1");*/
									
									continue childrenloop; // this exits the travel loop immediately, so the current child never gets added to the next generation
								}
								else {
									// don't "travel" the distance, as that will be done by soft border check
									//							lld.d = hb_dd+step_d;
									//							d-=(hb_dd+step_d);
									dprint("passed");
								}

								dprint("*** Checking Soft Border");
								
								//ldd.d = sb_dd;
								//lld.llffromdcrs();
								XYFunction sb = null;
								if (n.generation-1 == end_gen)									// JMB -- Added to make sure that children of the final generation get the right soft borders
									sb = settings.getSoftBorders(n.generation-1);
								else
									sb = settings.getSoftBorders(n.generation);
								dprint("I\'m at ("+sb.toX(lon1,minlon,maxlon)+","+sb.toY(lat1,minlat,maxlat)+")");
								dprint("I\'m checking ("+(sb.toX(lon1,minlon,maxlon)+sb_dx)+","+(sb.toY(lat1,minlat,maxlat)+sb_dy)+")");
								if (debug)
								{
									System.out.println("");
									System.out.println("Current Lon (x): "+lon1);
									System.out.println("Current Lat (y): "+lat1);
									System.out.println("crs: "+crs);																						// JMB - Debugging
									System.out.println("d: "+d);																							// JMB - Debugging
									System.out.println("I\'m at ("+sb.toX(lon1,minlon,maxlon)+","+sb.toY(lat1,minlat,maxlat)+")");							// JMB - Debugging
									System.out.println("I\'m checking ("+(sb.toX(lon1,minlon,maxlon)+sb_dx)+","+(sb.toY(lat1,minlat,maxlat)+sb_dy)+")");	// JMB - Debugging
								}
								double ran_num = rand.nextDouble();
								if (debug)
								{
									System.out.println("Random number is: "+ran_num);
									System.out.println("Border value being tested: "+sb.f(sb.toX(lon1,minlon,maxlon)+sb_dx, sb.toY(lat1,minlat,maxlat)+sb_dy));
									System.out.println("sb_dx: "+sb_dx);
									System.out.println("sb_dy: "+sb_dy);
									System.out.println("");
								}
								if( ran_num <= sb.f(sb.toX(lon1,minlon,maxlon)+sb_dx, sb.toY(lat1,minlat,maxlat)+sb_dy) ) {
									// failed the soft border-- stop before border, reflect back, update d, and continue
									dprint("failed");
									lld.d = (sb_dd-step_d);
									d-= (sb_dd-step_d);								
									crs = rand.nextDouble() * 2 * PI;// / 4+3*PI/4;
									lld.llffromdcrs();
									if ( (lat1 > sb_lat_bt) && (lon1 > sb_lon_bt) )		// Fudging to keep poorly estimated positions from crossing boundaries
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if (lld.latf() < sb_lat_bt)
										{
											lat1 = sb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");																				
											}
										}										
										if (lld.lonf() < sb_lon_bt)
										{
											lon1 = sb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");
											}
										}																				
									}
									else if ( (lat1 < sb_lat_bt) && (lon1 < sb_lon_bt) )
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if (lld.latf() > sb_lat_bt)
										{
											lat1 = sb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");																				
											}
										}																				
										if (lld.lonf() > sb_lon_bt)
										{
											lon1 = sb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");																				
											}
										}																				
									}
									else if ( (lat1 > sb_lat_bt) && (lon1 < sb_lon_bt) )
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if (lld.latf() < sb_lat_bt)
										{
											lat1 = sb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");
											}
										}																				
										if (lld.lonf() > sb_lon_bt)
										{
											lon1 = sb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");
											}
										}																				
									}
									else if ( (lat1 < sb_lat_bt) && (lon1 > sb_lon_bt) )
									{	
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if (lld.latf() > sb_lat_bt)
										{
											lat1 = sb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");
											}
										}																				
										if (lld.lonf() < sb_lon_bt)
										{
											lon1 = sb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");	
											}
										}																				
									}
									else
									{
										System.out.println("Weird things in border buffer. Exiting...A");
										System.exit(1);	
									}
									
									lld.dcrsfromll();
									
								}
								else {
									// passed the soft border-- stop after border, no reflection, update d, and continue
									dprint("passed");
									lld.d = sb_dd+step_d;
									d-=(sb_dd+step_d);
									lld.llffromdcrs();
									if ( (lat1 > sb_lat_bt) && (lon1 > sb_lon_bt) )	
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();									
										if ( (lld.latf() < sb_lat_bt) && (sb_dy == 0) )
										{
											lat1 = sb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}
										if ( (lld.lonf() < sb_lon_bt) && (sb_dx == 0) )
										{
											lon1 = sb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}																				
									}
									else if ( (lat1 < sb_lat_bt) && (lon1 < sb_lon_bt) )
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();									
										if ( (lld.latf() > sb_lat_bt) && (sb_dy == 0))
										{
											lat1 = sb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}																				
										if ( (lld.lonf() > sb_lon_bt) && (sb_dx == 0) )
										{
											lon1 = sb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}																				
									}
									else if ( (lat1 > sb_lat_bt) && (lon1 < sb_lon_bt) )
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();									
										if ( (lld.latf() < sb_lat_bt) && (sb_dy == 0) )
										{
											lat1 = sb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}																				
										if ( (lld.lonf() > sb_lon_bt) && (sb_dx == 0) )
										{
											lon1 = sb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}																				
									}
									else if ( (lat1 < sb_lat_bt) && (lon1 > sb_lon_bt) )
									{	
										lat1 = lld.latf();
										lon1 = lld.lonf();									
										if ( (lld.latf() > sb_lat_bt) && (sb_dy == 0) )
										{
											lat1 = sb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}																				
										if ( (lld.lonf() < sb_lon_bt) && (sb_dx == 0) )
										{
											lon1 = sb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}																				
									}
									else
									{
										System.out.println("Weird things in border buffer. Exiting...B");
										System.exit(1);	
									}
									
									lld.dcrsfromll();
									
									if (d <= 0.000001)			// ADDED BY JMB -- in case this individual would not go through another iteration of the travel loop
									{
										lld.d = d;
										n.lat = lat1;
										n.lon = lon1;
										nextGeneration.add( n );															

										d=0;										
									}
																		
								}

								continue travelloop;
							}
							else if( sb_dd < hb_dd ) {
								dprint("*** Checking Soft Border");
								XYFunction sb = null;
								if (n.generation-1 == end_gen)									// JMB -- Added to make sure that children of the final generation get the right soft borders
									sb = settings.getSoftBorders(n.generation-1);
								else
									sb = settings.getSoftBorders(n.generation);
								dprint("I\'m at ("+sb.toX(lon1,minlon,maxlon)+","+sb.toY(lat1,minlat,maxlat)+")");
								dprint("I\'m checking ("+(sb.toX(lon1,minlon,maxlon)+sb_dx)+","+(sb.toY(lat1,minlat,maxlat)+sb_dy)+")");
								if( rand.nextDouble() <= sb.f(sb.toX(n.lon,minlon,maxlon)+sb_dx, sb.toY(n.lat,minlat,maxlat)+sb_dy) ) {
									// failed the soft border-- stop before border, reflect back, update d, and continue
									dprint("failed");
									lld.d = (sb_dd-step_d);
									d-= (sb_dd-step_d);
									crs = rand.nextDouble() * 2 * PI;// / 4+3*PI/4;
									lld.llffromdcrs();
									if ( (lat1 > sb_lat_bt) && (lon1 > sb_lon_bt) )		// Fudging to keep poorly estimated positions from crossing boundaries
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if (lld.latf() < sb_lat_bt)
										{
											lat1 = sb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");																				
											}
										}																				
										if (lld.lonf() < sb_lon_bt)
										{
											lon1 = sb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");																				
											}
										}																				
									}
									else if ( (lat1 < sb_lat_bt) && (lon1 < sb_lon_bt) )
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if (lld.latf() > sb_lat_bt)
										{
											lat1 = sb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");
											}
										}																				
										if (lld.lonf() > sb_lon_bt)
										{
											lon1 = sb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");	
												System.out.println("Border buffer used!");
												System.out.println("");
											}
										}																				
									}
									else if ( (lat1 > sb_lat_bt) && (lon1 < sb_lon_bt) )
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if (lld.latf() < sb_lat_bt)
										{
											lat1 = sb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");																				
											}
										}																				
										if (lld.lonf() > sb_lon_bt)
										{
											lon1 = sb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");																				
											}
										}																				
									}
									else if ( (lat1 < sb_lat_bt) && (lon1 > sb_lon_bt) )
									{	
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if (lld.latf() > sb_lat_bt)
										{
											lat1 = sb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");
											}
										}																				
										if (lld.lonf() < sb_lon_bt)
										{
											lon1 = sb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Border buffer used!");
												System.out.println("");
											}
										}																				
									}
									else
									{
										System.out.println("Weird things in border buffer. Exiting...C");
										System.exit(1);	
									}
									
									lld.dcrsfromll();
									
								}
								else {
									// passed the soft border-- stop after border, no reflection, update d, and continue
									dprint("passed");
									lld.d = sb_dd+step_d;
									d-=(sb_dd+step_d);
									lld.llffromdcrs();									
									if ( (lat1 > sb_lat_bt) && (lon1 > sb_lon_bt) )	
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if ( (lld.latf() < sb_lat_bt) && (sb_dy == 0) )
										{
											lat1 = sb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");																															
											}
										}
										if ( (lld.lonf() < sb_lon_bt) && (sb_dx == 0) )
										{
											lon1 = sb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");
											}
										}																				
									}
									else if ( (lat1 < sb_lat_bt) && (lon1 < sb_lon_bt) )
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if ( (lld.latf() > sb_lat_bt) && (sb_dy == 0))
										{
											lat1 = sb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
										if ( (lld.lonf() > sb_lon_bt) && (sb_dx == 0) )
										{
											lon1 = sb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
									}
									else if ( (lat1 > sb_lat_bt) && (lon1 < sb_lon_bt) )
									{
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if ( (lld.latf() < sb_lat_bt) && (sb_dy == 0) )
										{
											lat1 = sb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
										if ( (lld.lonf() > sb_lon_bt) && (sb_dx == 0) )
										{
											lon1 = sb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
									}
									else if ( (lat1 < sb_lat_bt) && (lon1 > sb_lon_bt) )
									{	
										lat1 = lld.latf();
										lon1 = lld.lonf();
										if ( (lld.latf() > sb_lat_bt) && (sb_dy == 0) )
										{
											lat1 = sb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
										if ( (lld.lonf() < sb_lon_bt) && (sb_dx == 0) )
										{
											lon1 = sb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Soft Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
									}
									else
									{
										System.out.println("Weird things in border buffer. Exiting...D");
										System.exit(1);	
									}
								
									lld.dcrsfromll();
									
									if (d <= 0.000001)		// ADDED BY JMB -- in case this individual would not go through another iteration of the travel loop
									{
										lld.d = d;
										lld.llffromdcrs();
										n.lat = lat1;
										n.lon = lon1;
										nextGeneration.add( n );
	
										d=0;										
									}									
																		
								}

								continue travelloop;
							}
							else {
								dprint("*** Checking Hard Border");
								XYFunction hb = null;
								if (n.generation-1 == end_gen)
									hb = settings.getHardBorders(n.generation-1);
								else
									hb = settings.getHardBorders(n.generation);
								if( rand.nextDouble() <= hb.f(hb.toX(lon1,minlon,maxlon)+hb_dx,hb.toY(lat1,minlat,maxlat)+hb_dy) ) {
									dprint("failed");

								
									n.parent.children.remove( n.parent.children.indexOf( n ) );  // Added by JMB -- 4.14.10 -- Child needs to be removed from parent's children vector if it is not added to the next generation.
									
									/*if (n.generation ==  && n.unique == )
										System.out.println(" DIED AT HARD BORDER CHECK 2");*/
										
									continue childrenloop; // this exits the travel loop immediately, so the current child never gets added to the next generation
															// JMB -- Would this lead to pruning problems?
								}
								else {
									dprint("passed");
									lld.d = hb_dd+step_d;
									d-=(hb_dd+step_d);
									if ( (lat1 > hb_lat_bt) && (lon1 > hb_lon_bt) )	
									{
										lat1 = lld.latf();		// JMB -- Not here before, but shouldn't it be?
										lon1 = lld.lonf();		// JMB -- Not here before, but shouldn't it be?
										if ( (lld.latf() < hb_lat_bt) && (hb_dy == 0) )
										{
											lat1 = hb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Hard Border Buffer Used!");
												System.out.println("");																															
											}
										}
										if ( (lld.lonf() < hb_lon_bt) && (hb_dx == 0) )
										{
											lon1 = hb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Hard Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
									}
									else if ( (lat1 < hb_lat_bt) && (lon1 < hb_lon_bt) )
									{
										lat1 = lld.latf();		// JMB -- Not here before, but shouldn't it be?
										lon1 = lld.lonf();		// JMB -- Not here before, but shouldn't it be?
										if ( (lld.latf() > hb_lat_bt) && (hb_dy == 0))
										{
											lat1 = hb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Hard Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
										if ( (lld.lonf() > hb_lon_bt) && (hb_dx == 0) )
										{
											lon1 = hb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Hard Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
									}
									else if ( (lat1 > hb_lat_bt) && (lon1 < hb_lon_bt) )
									{
										lat1 = lld.latf();		// JMB -- Not here before, but shouldn't it be?
										lon1 = lld.lonf();		// JMB -- Not here before, but shouldn't it be?
										if ( (lld.latf() < hb_lat_bt) && (hb_dy == 0) )
										{
											lat1 = hb_lat_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Hard Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
										if ( (lld.lonf() > hb_lon_bt) && (hb_dx == 0) )
										{
											lon1 = hb_lon_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Hard Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
									}
									else if ( (lat1 < hb_lat_bt) && (lon1 > hb_lon_bt) )
									{	
										lat1 = lld.latf();		// JMB -- Not here before, but shouldn't it be?
										lon1 = lld.lonf();		// JMB -- Not here before, but shouldn't it be?
										if ( (lld.latf() > hb_lat_bt) && (hb_dy == 0) )
										{
											lat1 = hb_lat_bt - 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Hard Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
										if ( (lld.lonf() < hb_lon_bt) && (hb_dx == 0) )
										{
											lon1 = hb_lon_bt + 0.00001;
											if ( debug )
											{
												System.out.println("");
												System.out.println("Passthrough Hard Border Buffer Used!");
												System.out.println("");																				
											}
										}																				
									}
									else
									{
										System.out.println("Weird things in border buffer. Exiting...E");
										System.exit(1);	
									}
									
									lld.dcrsfromll();
									
									if (d <= 0.000001)		// ADDED BY JMB -- in case this individual would not go through another iteration of the travel loop
									{
										lld.d = d;
										// lld.llffromdcrs();
										n.lat = lat1;
										n.lon = lon1;
										nextGeneration.add( n );
										d=0;																				
									}
									continue travelloop;
								}
							}
						}
					}
					catch( Exception e ) {
						System.out.println("Someone tried to fall off the border.");
						System.out.println(e.toString());
						e.printStackTrace();
						System.exit(1);
					}

				}
			}
			catch( Exception e ) {
				System.out.println("Exception!!!");
				System.out.println(e.toString());
				e.printStackTrace();
				System.exit(1);
			}
		}

	}

	class lldata
	{
		public double lats=0.0, lons=0.0;
		public double latf=0.0, lonf=0.0;
		public double d=0.0, crs=0.0;
		public double R=6371; // radius of the earth in the same units as d (default: km)

		public double lats() {return lats*180/PI;}
		public double latf() {return latf*180/PI;}
		public double lons() {return lons*180/PI;}
		public double lonf() {return lonf*180/PI;}
		public double crs() {return crs*180/PI;}

		public void setLatSDDeg(double ddeg) //set start lat from decimal degrees
		{
			lats = ddeg / 180 * Math.PI;
		}
		public void setLatFDDeg(double ddeg) //set final lat from decimal degrees
		{
			latf = ddeg / 180 * Math.PI;
		}
		public void setLonSDDeg(double ddeg) //set start lon from decimal degrees
		{
			lons = ddeg / 180 * Math.PI;
		}
		public void setLonFDDeg(double ddeg) //set final lon from decimal degrees
		{
			lonf = ddeg / 180 * Math.PI;
		}
		public void setR(double Ro) //set radius of the earth (must be same units as d)
		{
			R = Ro;
		}
		public void setCrsDDeg(double crso) //set course from decimal degrees
		{
			crs = crso / 180 * Math.PI;
		}
		public double getLatSDDeg() //get start lon from decimal degrees
		{
			return (lats / Math.PI * 180);
		}
		public double getLonSDDeg() //get final lon from decimal degrees
		{
			return (lons / Math.PI * 180);
		}	
		public double getLatFDDeg() //get start lon from decimal degrees
		{
			return (latf / Math.PI * 180);
		}
		public double getLonFDDeg() //get final lon from decimal degrees
		{
			return (lonf / Math.PI * 180);
		}
		public double getCrsDDeg() //get start lon from decimal degrees
		{
			return (crs / Math.PI * 180);
		}
		public double getD() //get distance
		{
			return d;
		}

		public double mod(double y, double x)
		{
			return (y - (x*Math.floor(y/x)));
		}

		public void dcrsfromll()			// JMB COMMENT -- 10.19.09 -- DETERMINES DISTANCE (D) AND COURSE (CRS) FROM ENDING LAT/LON
		{
			double dlat = latf - lats;
			double dlon = lonf - lons;
			double a = Math.pow(Math.sin(dlat/2),2) + Math.cos(lats)*Math.cos(latf)*Math.pow(Math.sin(dlon/2),2);

			this.d = R * 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
			// this.d = R * 2 * Math.asin(Math.sqrt(a)); //	Implemented by JMB from formula given at http://williams.best.vwh.net/avform.htm#Par
			// this.crs = this.mod(Math.atan2(Math.sin(dlon)*Math.cos(latf),Math.cos(lats)*Math.sin(latf)-Math.sin(lats)*Math.cos(latf)*Math.cos(dlon)),2*Math.PI);  //	Implemented by JMB from formula given at http://williams.best.vwh.net/avform.htm#Par
			this.crs = this.mod(Math.atan2(Math.sin(dlon)*Math.cos(latf),Math.cos(lats)*Math.sin(latf)-Math.sin(lats)*Math.cos(latf)*Math.cos(dlon)),2*Math.PI);
		}

		public void llffromdcrs()			// JMB COMMENT -- 10.19.09 -- DETERMINES ENDING LAT/LON FROM DISTANCE (D) AND COURSE (CRS)
		{
			this.latf = Math.asin(Math.sin(lats)*Math.cos(d/R)+Math.cos(lats)*Math.sin(d/R)*Math.cos(crs));
			this.lonf = lons + Math.atan2(Math.sin(crs)*Math.sin(d/R)*Math.cos(lats),Math.cos(d/R)-Math.sin(lats)*Math.sin(latf));
		}
		
		/*public void llffromdcrs()		// Implemented by JMB using formulae from http://williams.best.vwh.net/avform.htm#Par
		{
			this.latf = Math.asin(Math.sin(lats)*Math.cos(d/R)+Math.cos(lats)*Math.sin(d/R)*Math.cos(crs));
			double dlon = Math.atan2(Math.sin(crs)*Math.sin(d/R)*Math.cos(lats),Math.cos(d/R)-Math.sin(lats)*Math.sin(latf));
			this.lonf = this.mod(lons-dlon+Math.PI,2*Math.PI)-Math.PI;
		}*/

		/*public lldata latdfromlon3(double lon3o) // in degrees
		{
			double lon3 = lon3o*PI/180;

			lldata ret = new lldata();
			ret.lats = this.lats;
			ret.lons = this.lons;
			ret.R = this.R;

			double lat1 = this.lats;
			double lon1 = this.lons;
			double lat2 = this.latf;
			double lon2 = this.lonf;

			double ea1 = sin(lat1-lat2)*sin((lon1+lon2)/2)*cos((lon1-lon2)/2)-sin(lat1+lat2)*cos((lon1+lon2)/2)*sin((lon1-lon2)/2);
			double ea2 = sin(lat1-lat2)*cos((lon1+lon2)/2)*cos((lon1-lon2)/2)+sin(lat1+lat2)*sin((lon1+lon2)/2)*sin((lon1-lon2)/2);
			double ea3 = cos(lat1)*cos(lat2)*sin(lon1-lon2);

			double den = sqrt( ea1*ea1 + ea2*ea2 + ea3*ea3 );
			ea1 = ea1 / den;
			ea2 = ea2 / den;
			ea3 = ea3 / den;


			lat1 = -89*PI/180; // this won't work if lats or latf within 1 deg of pole
			lon1 = lon3;
			lat2 = 89*PI/180;
			lon2 = lon3;

			double eb1 = sin(lat1-lat2)*sin((lon1+lon2)/2)*cos((lon1-lon2)/2)-sin(lat1+lat2)*cos((lon1+lon2)/2)*sin((lon1-lon2)/2);
			double eb2 = sin(lat1-lat2)*cos((lon1+lon2)/2)*cos((lon1-lon2)/2)+sin(lat1+lat2)*sin((lon1+lon2)/2)*sin((lon1-lon2)/2);
			double eb3 = cos(lat1)*cos(lat2)*sin(lon1-lon2);

			den = sqrt( eb1*eb1 + eb2*eb2 + eb3*eb3 );
			eb1 = eb1 / den;
			eb2 = eb2 / den;
			eb3 = eb3 / den;

			double e1 = ea2*eb3-eb2*ea3;
			double e2 = ea3*eb1-eb3*ea1;
			double e3 = ea1*eb2-ea2*eb1;

			double lat = atan2(e3,sqrt(e1*e1+e2*e2));
			double lon = atan2(-e2,e1);

			if( (this.lats <= lat && this.latf >= lat) || (this.lats >= lat && this.latf <= lat) ) {
				ret.latf = lat;
				ret.lonf = lon;
			}
			else {
				ret.latf = -lat;
				ret.lonf = (lon+PI);
			}

			ret.dcrsfromll();						

			return ret;

		}*/

		
		public lldata latdfromlon3(double lon3o) // // Implementation by JMB from http://williams.best.vwh.net/avform.htm#Par  -- lat=atan(  (sin(lat1)*cos(lat2)*sin(lon-lon2)-sin(lat2)*cos(lat1)*sin(lon-lon1))  /  (cos(lat1)*cos(lat2)*sin(lon1-lon2))  )
		{
			double lon3 = lon3o*PI/180;
			
			lldata ret =  new lldata();
			ret.lats = this.lats;
			ret.lons = this.lons;
			ret.R = this.R;
			
			double lat1 = this.lats;
			double lon1 = this.lons;
			double lat2 = this.latf;
			double lon2 = this.lonf;
			
			ret.latf = Math.atan(((Math.sin(lat1)*Math.cos(lat2)*Math.sin(lon3-lon2))-(Math.sin(lat2)*Math.cos(lat1)*Math.sin(lon3-lon1)))/(Math.cos(lat1)*Math.cos(lat2)*Math.sin(lon1-lon2)));
			
			ret.lonf = lon3;
			
			/*if( !((ret.latf >= lat1 && ret.latf <= lat2) || (ret.latf <= lat1 && ret.latf >= lat2)) ) // Checks to make sure predicted value is in range of movement...if not, sets d to arbirarily large value (1,000,000)
			{
				ret.d=1000000;
				return ret;
			}*/		
			
			ret.dcrsfromll();
			
			return ret;
		}

		
		public lldata londfromlat3(double lat3o) // in degrees		JMB COMMENT -- 10.19.09 -- FINDS LONGITUDE AT WHICH NEAREST LAT IS CROSSED W/
		{														//								GREAT CIRCLE DISTANCE
			// from http://williams.best.vwh.net/avform.htm#Par
			double lat3 = lat3o * PI / 180;
			lldata ret = new lldata();
			ret.lats = this.lats;
			ret.lons = this.lons;
			//ret.latf = this.latf;
			//ret.lonf = this.lonf;
			ret.crs = this.crs;
			ret.d = this.d;
			ret.R = this.R;

			double lat1 = this.lats;
			double lat2 = this.latf;
			double lon1 = this.lons;
			double lon2 = this.lonf;

			double l12 = lon1-lon2;
			double A = sin(lat1)*cos(lat2)*cos(lat3)*sin(l12);
			double B = sin(lat1)*cos(lat2)*cos(lat3)*cos(l12) - cos(lat1)*sin(lat2)*cos(lat3);
			double C = cos(lat1)*cos(lat2)*sin(lat3)*sin(l12);
			double lon = atan2(B,A);
			if (abs(C) >sqrt(pow(A,2) + pow(B,2)))
			{
				//System.out.println("never crosses");
				ret.d = 10000000;
			}
			else if( lat1 == lat3 ) {
				ret.latf = lat1;
				ret.lonf = lon1;
				ret.d = 0;
				ret.crs = 0;
			}
			else {
				double dlon = acos(C/sqrt(pow(A,2)+pow(B,2)));
				double lon3_1=(this.mod((lon1+dlon+lon+PI),(2*PI))-PI);
				double lon3_2=(this.mod((lon1-dlon+lon+PI),(2*PI))-PI);

				ret.latf = lat3;
				//System.out.println(lat3*180/PI);
				//System.out.println(lon3_1*180/PI);
				//System.out.println(lon3_2*180/PI);
				lldata lon3_1_d = new lldata();
				lon3_1_d.lats = ret.lats;
				lon3_1_d.lons = ret.lons;
				lon3_1_d.latf = ret.latf;
				lon3_1_d.lonf = lon3_1;
				lon3_1_d.dcrsfromll();
				
				lldata lon3_2_d = new lldata();
				lon3_2_d.lats = ret.lats;
				lon3_2_d.lons = ret.lons;
				lon3_2_d.latf = ret.latf;
				lon3_2_d.lonf = lon3_2;
				lon3_2_d.dcrsfromll();					
				
				if( ((lon3_1 >= lon1 && lon3_1 <= lon2) || (lon3_1 <= lon1 && lon3_1 >= lon2)) && (lon3_1_d.d < lon3_2_d.d) )
					ret.lonf = lon3_1;
				else
					ret.lonf = lon3_2;
				
				/*if( !((ret.lonf >= lon1 && ret.lonf <= lon2) || (ret.lonf <= lon1 && ret.lonf >= lon2)) )		// Checks to make sure predicted value is in range of movement...if not, sets d to arbirarily large value (1,000,000)
				{
					ret.d=1000000;
					return ret;
				}*/
					
				ret.dcrsfromll();				// JMB COMMENT -- 10.19.09 -- SETS D AND CRS FOR RET
			}

			return ret;
		}
	}


	// OLD MIGRATION LOOP
	//nextGeneration.addAll( children );

	/*travelloop:
while( dr > 1.0 ) {
	double theta = rand.nextDouble() * 2 * Math.PI;

	double lat1 = lat*Math.PI/180;
	double lon1 = lon*Math.PI/180;
	double crs12 = theta;
	double d12 = dr / (180*60/Math.PI);
	double lat2, lon2;

	lat2 = Math.asin( Math.sin(lat1)*Math.cos(d12)+Math.cos(lat1)*Math.sin(d12)*Math.cos(crs12) );
	if (Math.abs(Math.cos(lat2))<0.00000000005){
	    lon2=0.0; //endpoint a pole
	  }else{
	    double dlon = Math.atan2(Math.sin(crs12)*Math.sin(d12)*Math.cos(lat1),
	                  Math.cos(d12)-Math.sin(lat1)*Math.sin(lat2));
	    lon2 = ((lon1-dlon+Math.PI) % (2*Math.PI)) - Math.PI;
	  }

	double nlat = lat2 * 180.0 / Math.PI;
	double nlon = lon2 * 180.0 / Math.PI;

	ArrayList<Integer> swalk = settings.getSoftBorders(n.generation).getWalk(lat,lon,nlat,nlon,settings.getMinLat(),settings.getMaxLat(),settings.getMinLon(),settings.getMaxLon());
    ArrayList<Integer> hwalk = settings.getHardBorders(n.generation).getWalk(lat,lon,nlat,nlon,settings.getMinLat(),settings.getMaxLat(),settings.getMinLon(),settings.getMaxLon());

	for(int k=0; k<(swalk.size()/2.0 * hwalk.size()/2.0); k++) {
		if( (double)k/(swalk.size()/2.0) == (int)Math.floor((double)k/(swalk.size()/2.0)) ) {
			int j=(int)Math.floor((double)k/(swalk.size()/2.0));
			if( swalk.get(2*j) < 0 || swalk.get(2*j) >= settings.getSoftBorders(n.generation).getMaxX() || swalk.get(2*j+1) < 0 || swalk.get(2*j+1) >= settings.getSoftBorders(n.generation).getMaxY() )
				continue travelloop;
			if( rand.nextDouble() < settings.getSoftBorders(n.generation).f(swalk.get(2*j), swalk.get(2*j+1)) ) {
				double percentComplete = 2.0*j / swalk.size();
				//System.out.println("% complete: "+percentComplete);
				if( percentComplete == 0.0 )
					percentComplete = 0.1;
				dr = dr * (1 - percentComplete);

				double lat10 = lat*Math.PI/180;
				double lon10 = lon*Math.PI/180;
				double crs120 = theta;
				double d120 = (dr*percentComplete) / (180*60/Math.PI);
				double lat20, lon20;

				lat20 = Math.asin( Math.sin(lat10)*Math.cos(d120)+Math.cos(lat10)*Math.sin(d120)*Math.cos(crs120) );
				if (Math.abs(Math.cos(lat20))<0.00000000005){
				    lon20=0.0; //endpoint a pole
				  }else{
				    double dlon0 = Math.atan2(Math.sin(crs120)*Math.sin(d120)*Math.cos(lat10),
				                  Math.cos(d120)-Math.sin(lat10)*Math.sin(lat20));
				    lon20 = ((lon10-dlon0+Math.PI) % (2*Math.PI)) - Math.PI;
				  }

				lat = lat20 * 180.0 / Math.PI;
				lon = lon20 * 180.0 / Math.PI;
				continue travelloop;							
			}
		}
		if( (double)k/(hwalk.size()/2.0) == (int)Math.floor((double)k/(hwalk.size()/2.0)) ) {
			int j=(int)Math.floor((double)k/(hwalk.size()/2.0));
			if( hwalk.get(2*j) < 0 || hwalk.get(2*j) >= settings.getHardBorders(n.generation).getMaxX() || hwalk.get(2*j+1) < 0 || hwalk.get(2*j+1) >= settings.getHardBorders(n.generation).getMaxY() )
				continue travelloop;
			if( rand.nextDouble() < settings.getHardBorders(n.generation).f(hwalk.get(2*j), hwalk.get(2*j+1)) ) {
				n.parent.children.remove(n);
				children.remove(n);
				i--;
				continue childrenloop;
			}
		}
	}

	lat = nlat;
	lon = nlon;
	dr = 0.0;	
}

n.lat = lat;
n.lon = lon;*/

	/*			OLD MIGRATION LOOP
	 * 			childrenloop:
for(int i=0; i<children.size(); i++) {
//System.out.println("Thread Number "+threadNum);
Node n = children.get(i);

double dr = settings.getDispersalRadius(n.generation,rand);

double lat = n.parent.lat;
double lon = n.parent.lon;

travelloop:
while( dr > 1.0 ) {
double theta = rand.nextDouble() * 2 * Math.PI;

double lat1 = lat*Math.PI/180;
double lon1 = lon*Math.PI/180;
double crs12 = theta;
double d12 = dr / (180*60/Math.PI);
double lat2, lon2;

lat2 = Math.asin( Math.sin(lat1)*Math.cos(d12)+Math.cos(lat1)*Math.sin(d12)*Math.cos(crs12) );
if (Math.abs(Math.cos(lat2))<0.00000000005){
    lon2=0.0; //endpoint a pole
  }else{
    double dlon = Math.atan2(Math.sin(crs12)*Math.sin(d12)*Math.cos(lat1),
                  Math.cos(d12)-Math.sin(lat1)*Math.sin(lat2));
    lon2 = ((lon1-dlon+Math.PI) % (2*Math.PI)) - Math.PI;
  }

double nlat = lat2 * 180.0 / Math.PI;
double nlon = lon2 * 180.0 / Math.PI;

ArrayList<Integer> swalk = settings.getSoftBorders(n.generation).getWalk(lat,lon,nlat,nlon,settings.getMinLat(),settings.getMaxLat(),settings.getMinLon(),settings.getMaxLon());
ArrayList<Integer> hwalk = settings.getHardBorders(n.generation).getWalk(lat,lon,nlat,nlon,settings.getMinLat(),settings.getMaxLat(),settings.getMinLon(),settings.getMaxLon());

for(int k=0; k<(swalk.size()/2.0 * hwalk.size()/2.0); k++) {
	if( (double)k/(swalk.size()/2.0) == (int)Math.floor((double)k/(swalk.size()/2.0)) ) {
		int j=(int)Math.floor((double)k/(swalk.size()/2.0));
		if( swalk.get(2*j) < 0 || swalk.get(2*j) >= settings.getSoftBorders(n.generation).getMaxX() || swalk.get(2*j+1) < 0 || swalk.get(2*j+1) >= settings.getSoftBorders(n.generation).getMaxY() )
			continue travelloop;
		if( rand.nextDouble() < settings.getSoftBorders(n.generation).f(swalk.get(2*j), swalk.get(2*j+1)) ) {
			double percentComplete = 2.0*j / swalk.size();
			//System.out.println("% complete: "+percentComplete);
			if( percentComplete == 0.0 )
				percentComplete = 0.1;
			dr = dr * (1 - percentComplete);

			double lat10 = lat*Math.PI/180;
			double lon10 = lon*Math.PI/180;
			double crs120 = theta;
			double d120 = (dr*percentComplete) / (180*60/Math.PI);
			double lat20, lon20;

			lat20 = Math.asin( Math.sin(lat10)*Math.cos(d120)+Math.cos(lat10)*Math.sin(d120)*Math.cos(crs120) );
			if (Math.abs(Math.cos(lat20))<0.00000000005){
			    lon20=0.0; //endpoint a pole
			  }else{
			    double dlon0 = Math.atan2(Math.sin(crs120)*Math.sin(d120)*Math.cos(lat10),
			                  Math.cos(d120)-Math.sin(lat10)*Math.sin(lat20));
			    lon20 = ((lon10-dlon0+Math.PI) % (2*Math.PI)) - Math.PI;
			  }

			lat = lat20 * 180.0 / Math.PI;
			lon = lon20 * 180.0 / Math.PI;
			continue travelloop;							
		}
	}
	if( (double)k/(hwalk.size()/2.0) == (int)Math.floor((double)k/(hwalk.size()/2.0)) ) {
		int j=(int)Math.floor((double)k/(hwalk.size()/2.0));
		if( hwalk.get(2*j) < 0 || hwalk.get(2*j) >= settings.getHardBorders(n.generation).getMaxX() || hwalk.get(2*j+1) < 0 || hwalk.get(2*j+1) >= settings.getHardBorders(n.generation).getMaxY() )
			continue travelloop;
		if( rand.nextDouble() < settings.getHardBorders(n.generation).f(hwalk.get(2*j), hwalk.get(2*j+1)) ) {
			n.parent.children.remove(n);
			children.remove(n);
			i--;
			continue childrenloop;
		}
	}
}

lat = nlat;
lon = nlon;
dr = 0.0;	
}

n.lat = lat;
n.lon = lon;
}
}
catch( Exception e ) {
System.out.println("Exception!!!");
}

nextGeneration.addAll( children );*/
	//}