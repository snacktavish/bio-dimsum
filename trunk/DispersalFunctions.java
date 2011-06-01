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

class DispersalFunctions {
	DispersalSettings settings = null;
	Random rand = null;		// Added by JMB -- 4.5.10
	
	public DispersalFunctions(DispersalSettings settings0, Random rand0) {
		settings = settings0;
		rand = rand0;		// Added by JMB -- 4.5.10
	}

	public ArrayList<Node> populateAndMigrateThreaded(ArrayList<Node> thisGeneration, int end_gen) throws Exception {	// END_GEN ADDED BY JMB -- 10.20.09
		settings.pAmTTimer.start();
		ArrayList<DispersalThread> dthreads = new ArrayList<DispersalThread>();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for(int i=0; i<settings.getNThreads(); i++) {						// JMB COMMENT -- 10.19.09 -- CREATES # OF DTHREADS SPECIFIED BY SETTINGS FILE
			dthreads.add( new DispersalThread(settings, end_gen, rand.nextInt()) );
			for(int j=i;j<thisGeneration.size(); j+=settings.getNThreads())		// JMB COMMENT -- 10.19.09 -- ADDS EVERY #DTHREADS INDIVIDUAL TO A PARTICULAR THREAD
				dthreads.get(i).add( thisGeneration.get(j) );

			threads.add( new Thread( dthreads.get(i) ) );		// JMB COMMENT -- 10.19.09 -- PLACES EACH DTHREADS OBJECT IN A THREADS OBJECT...MORE GENERAL IMPLEMENTATION OF A RUNNABLE OBJECT
			if( i != settings.getNThreads()-1 )
				threads.get(i).start();
		}
		
		threads.get( threads.size()-1 ).run();
		
		for(int i=0; i<settings.getNThreads()-1; i++) {
			threads.get(i).join();
		}
		
		
		ArrayList<Node> nextGeneration = new ArrayList<Node>();
		for(int i=0; i<dthreads.size(); i++) {
			nextGeneration.addAll( dthreads.get(i).nextGeneration );
		}
		settings.pAmTTimer.stop();
		//System.out.println("from dispersalfunctions: ng="+nextGeneration.size());
		return checkCarryingCapacity(nextGeneration,end_gen);	// END_GEN ADDED BY JMB -- 10.20.09
	}
	


	public ArrayList<Node> populateNextGeneration(ArrayList<Node> thisGeneration) {
		ArrayList<Node> nextGeneration = new ArrayList<Node>();
		
		for(int i=0; i<thisGeneration.size(); i++) {
			// Decide how many offspring and add to the next generation (uninitialized in lat, lon)
			int numberOfChildren = settings.getNOffspring(Dispersion.generation,rand);
			for(int j=0 ; j < numberOfChildren ; j++ )
			{
				Node child = new Node(Dispersion.generation+1, thisGeneration.get(i)); // generation, parent
				thisGeneration.get(i).children.add(child);
				nextGeneration.add(child);
			}
		}
		
		return nextGeneration;
	}
/*
	public ArrayList<Node> migrate(ArrayList<Node> children, int end_gen) throws Exception {	// END_GEN ADDED BY JMB -- 10.20.09
		
		childrenloop:
		for(int i=0; i<children.size(); i++) {
			Node n = children.get(i);
			
			double dr = settings.getDispersalRadius(n.generation,rand);
			
			double lat = n.parent.lat;
			double lon = n.parent.lon;
			
			travelloop:
			while( dr > 1.0 ) {
				double theta = rand.nextDouble() * 2 * Math.PI;		// Modified by JMB -- 4.5.10
				
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
						if( rand.nextDouble() < settings.getSoftBorders(n.generation).f(swalk.get(2*j), swalk.get(2*j+1)) ) {	// Modified by JMB -- 4.5.10
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
						if( rand.nextDouble() < settings.getHardBorders(n.generation).f(hwalk.get(2*j), hwalk.get(2*j+1)) ) {	// Modified by JMB -- 4.5.10
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
		
		return checkCarryingCapacity(children,end_gen);	// END_GEN ADDED BY JMB -- 10.20.09
	}*/
	
	public ArrayList<Node> checkCarryingCapacity(ArrayList<Node> children, int end_gen) throws Exception {
		settings.cCCTimer.start();
		ArrayList<Node> children2 = new ArrayList<Node>(children.size());
		if( children.size() > 0 ) {
			Index ccap = settings.carryingcapacity.calcIndex(children.get(0).generation);
			if (children.get(0).generation-1 == end_gen)
				ccap = settings.carryingcapacity.calcIndex(children.get(0).generation-1);
			int[][] count = new int[settings.carryingcapacity.getMaxY(ccap)][settings.carryingcapacity.getMaxX(ccap)];
			int max_count =0;
			int x,y;
			for(int i=0; i<children.size(); i++) {
				y = settings.carryingcapacity.toY(ccap,children.get(i).lat,settings.getMinLat(),settings.getMaxLat());
				x = settings.carryingcapacity.toX(ccap,children.get(i).lon,settings.getMinLon(),settings.getMaxLon());
				count[y][x] += 1;
				if(count[y][x]>max_count)
					max_count = count[y][x];
			}
	/*			for(int r=0; r<ccap.getMaxY(); r++, System.out.println())
				for(int c=0; c<ccap.getMaxX(); c++)
					System.out.print(count[r][c]+" ");
			System.out.println();*/ //this outputs the count to console
		
			boolean rmchild[][][] = new boolean[settings.carryingcapacity.getMaxY(ccap)][settings.carryingcapacity.getMaxX(ccap)][max_count];
			
			int r;
			for(x=0;x<settings.carryingcapacity.getMaxX(ccap);x++) {
				for(y=0;y<settings.carryingcapacity.getMaxY(ccap);y++) { // this could be parallelized
					for(int i =0; i<count[y][x]-settings.carryingcapacity.f(ccap,x,y); i++) {
						r = (int)(rand.nextDouble() * count[y][x]);
						while(rmchild[y][x][r])
							r = (int)(rand.nextDouble() * count[y][x]);
						rmchild[y][x][r] = true;
					}
				}
			}
			
			for(int i=0;i<children.size();i++) {
				y = settings.carryingcapacity.toY(ccap,children.get(i).lat,settings.getMinLat(),settings.getMaxLat());
				x = settings.carryingcapacity.toX(ccap,children.get(i).lon,settings.getMinLon(),settings.getMaxLon());
				count[y][x]--;
				if(rmchild[y][x][count[y][x]]) {
					if( children.get(i).parent.children.indexOf( children.get(i) ) != -1 )
						children.get(i).parent.children.remove( children.get(i).parent.children.indexOf( children.get(i) ));
				} else
					children2.add(children.get(i));
			}
		}
		settings.cCCTimer.stop();
		return children2;
	}

	public void prune(ArrayList<Node> thisGeneration) {
		// Each member of the generation must be tested for pruning.
		
		Iterator thisGenItr = thisGeneration.iterator();
		
		//for(int i=0; i<thisGeneration.size(); i++)
		while (thisGenItr.hasNext())
		{
			//Node current = thisGeneration.get(i);
			Node current = (Node)thisGenItr.next();
			// If a member of this generation is pruned, it might make a member of the last generation eligible for pruning.
			// Hence, the while loop.
			while( current != null && current.parent != null ) 
			{
				// To remove a node from memory, we must remove all pointers to it.
				if( current.children.size() == 0 )															//	NOTE BY JMB -- 10.19.09 -- THIS LOOP DOES THE ACTUAL DELETING OF EXTINCT LINEAGES	
				{
					// if( current.parent.children.indexOf(current) != -1 )
						current.parent.children.remove( current.parent.children.indexOf( current ) );
					if (thisGeneration.indexOf(current) != -1)
						thisGenItr.remove();																// JMB -- 4.13.10 -- Hadn't previously been removing individual from current generation array list!
					Node temp = current.parent;																// ADDED BY JMB -- 10.19.09 -- THESE TWO LINES SHOULDN'T MATTER, BUT MAY MAKE THE DELETION CLEANER
					current.parent = null;																	// ADDED BY JMB -- 10.19.09
					current = temp;
					temp = null;																			// ADDED BY JMB -- 10.19.09
				}
				else if( current.children.size() == 1 && current.generation != 1)							// NOTE BY JMB -- 10.19.09 -- THIS LOOP RECONNECTS POINTERS AROUND INDIVIDUALS
				{																							//								WITH ONLY ONE DESCENDANT
					current.children.get(0).parent = current.parent;
					current.parent.children.add( current.children.get(0) );
					// if( current.parent.children.indexOf(current) != -1 )
						current.parent.children.remove( current.parent.children.indexOf( current ) );
					if (thisGeneration.indexOf(current) != -1)
						thisGenItr.remove();																// JMB -- 4.13.10 -- Hadn't previously been removing individual from current generation array list!
					Node temp = current.parent;																// ADDED BY JMB -- 10.19.09 -- LINES TURN CURRENT POINTERS TO NULL, SO THEY DON'T POINT OUT
					current.parent = null;																	// ADDED BY JMB -- 10.19.09
					int j = 0;																				// ADDED BY JMB -- 10.19.09
					while (current.children.size() > 0)														// ADDED BY JMB -- 10.19.09
					{																						// ADDED BY JMB -- 10.19.09	
						current.children.remove(j);															// ADDED BY JMB -- 10.19.09	
						j++;																				// ADDED BY JMB -- 10.19.09
					}																						// ADDED BY JMB -- 10.19.09
					current = temp;
					temp = null;																			// ADDED BY JMB -- 10.19.09
				}
				else
					break;
			}
		}
	}
	
	// ***************	ADDED BY JMB -- 10.19.09 **************************************
	
	public void debug_prune(ArrayList<Node> thisGeneration, int generation, Node root)
	{
		Node problem = null;
		
		// Test for error
		problem = flag_prune(root,generation);
		
		// If error, print data on problem individuals and exit
		if (problem != null)
		{
			System.out.println("Somebody wasn't pruned properly!!!");
			System.out.println("Current Generation: "+generation);
			System.out.println("Problem Node: "+problem.getName());
			System.out.println("Tree Structure Above: "+problem.printTreeStructure("",problem.generation,false,false));  // Boolean added by JMB -- 4.8.10
			if (problem.parent != null)
			{
				System.out.println("Parent: "+problem.parent.getName());
				System.out.println("No. Parent Offspring: "+problem.parent.children.size());
				System.out.println("Parent offspring no.: "+problem.parent.children.indexOf(problem));
			}
			System.out.println("Lat/Long: "+problem.lat+"/"+problem.lon);
			System.out.println("Problem generation: "+problem.generation);
			System.out.println("Problem unique: "+problem.unique);
			System.out.println("Offspring moved flag: "+problem.moved);
			System.out.println("Initial problem lat: "+problem.par_lat);
			System.out.println("Initial problem lon: "+problem.par_lon);
			System.out.println("Post-moving lat: "+problem.end_move_lat);
			System.out.println("Post-moving lon: "+problem.end_move_lon);
			System.out.println("Failed flag one: "+problem.failed_one);
			System.out.println("Failed flag two: "+problem.failed_two);
			System.out.println("Added to next gen flag: "+problem.added_nextgen);
			System.out.println("Problematic dispersal distance: "+problem.last_d);
			System.out.println("Problematic course: "+problem.last_crs);
			System.out.println("HB final flag: "+problem.hb_final);
			System.out.println("SB final 1 flag: "+problem.sb_final_1);
			System.out.println("SB final 2 flag: "+problem.sb_final_2);
			System.out.println("Last at: "+problem.last_at);
			System.out.println("Last check: "+problem.last_check);
			System.out.println("Last value: "+problem.last_value);
			System.exit(1);
		}
	}
	
	private Node flag_prune(Node node, int generation)
	{
		if (node.generation < generation && node.children.size() == 0)  // Case for nodes that should have been deleted
			return node;
		else if (node.generation >= generation)	// Case for current and next generation nodes
			return null;
		else if (node.generation < generation && node.children.size() > 0)			// Case for normal non-final generation nodes
		{
			for (int i=0; i<node.children.size(); i++)
			{
				if (flag_prune(node.children.get(i),generation) != null)
					return flag_prune(node.children.get(i),generation);
			}
			return null;
		}
		else									// Shouldn't ever get here!!
		{
			System.out.println("*********** How did I get here?? ***********");
			return null;
		}
	}
	
	// *******************************************************************************
}
