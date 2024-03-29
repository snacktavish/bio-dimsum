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

class DispersalFunctions {
	public final static int _xsize = 0;
	public final static int _ysize = 1;
	//public final static int numRand = 60000;
	XYFunction sb_old ;
	XYFunction hb_old ;

	GPU gpu;
	
	DispersalSettings settings = null;
	Random rand = null;		// Added by JMB -- 4.5.10
	
	public DispersalFunctions(DispersalSettings settings0, Random rand0, GPU gpu) {
		settings = settings0;
		rand = rand0;		// Added by JMB -- 4.5.10
		this.gpu = gpu;
	}

    public ArrayList<Node> populateAndMigrateThreaded(ArrayList<Node> thisGeneration, int end_gen) throws Exception {       // END_GEN ADDED BY JMB -- 10.20.09
        
        ArrayList<DispersalThread> dthreads = new ArrayList<DispersalThread>();
        ArrayList<Thread> threads = new ArrayList<Thread>();
        
        for(int i=0; i<settings.getNThreads(); i++) {                                           // JMB COMMENT -- 10.19.09 -- CREATES # OF DTHREADS SPECIFIED BY SETTINGS FILE
                dthreads.add( new DispersalThread(settings, end_gen, rand.nextInt()) );
                for(int j=i;j<thisGeneration.size(); j+=settings.getNThreads())         // JMB COMMENT -- 10.19.09 -- ADDS EVERY #DTHREADS INDIVIDUAL TO A PARTICULAR THREAD
                        dthreads.get(i).add( thisGeneration.get(j) );

                threads.add( new Thread( dthreads.get(i) ) );           // JMB COMMENT -- 10.19.09 -- PLACES EACH DTHREADS OBJECT IN A THREADS OBJECT...MORE GENERAL IMPLEMENTATION OF A RUNNABLE OBJECT
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
        
        //System.out.println("from dispersalfunctions: ng="+nextGeneration.size());
        return checkCarryingCapacity(nextGeneration,end_gen);   // END_GEN ADDED BY JMB -- 10.20.09
    }


	
	public ArrayList<Node> populate4GPU(ArrayList<Node> thisGeneration,java.util.Random rand2) {
		ArrayList<Node> children = new ArrayList<Node>();
		
		for(int i=0; i<thisGeneration.size(); i++) {
			// Decide how many offspring and add to the next generation (uninitialized in lat, lon)
			int numberOfChildren = settings.getNOffspring(Dispersion.generation, rand2);
			for(int j=0 ; j < numberOfChildren ; j++ )
			{
				Node child = new Node(Dispersion.generation+1, thisGeneration.get(i)); // generation, parent	JMB COMMENT -- CREATES NEXT GEN NODE BY PASSING NEXT GEN # AND PARENT TO CONSTRUCTOR
				thisGeneration.get(i).children.add(child);
				children.add(child);
			}
		}
		return children;
	}
	

	
	public ArrayList<Node> populateAndMigrate4GPU(ArrayList<Node> thisGeneration, int end_gen) throws Exception {	// END_GEN ADDED BY JMB -- 10.20.09
		settings.pAmTTimer.start();
		
		java.util.Random rand2 = new java.util.Random(rand.nextInt());		// Added by JMB -- 4.5.10

		
		ArrayList<Node> children = populate4GPU(thisGeneration,rand2);
		long[] parami = new long[Migrate.DIMSUM_PARAMI_RANDINDEX+children.size()];
		XYFunction hb = null;
		if (children.get(0).generation-1 == end_gen)								// JMB -- Added to make sure that children of the final generation get the right hard borders
			hb = settings.getHardBorders(children.get(0).generation-1);
		else
			hb = settings.getHardBorders(children.get(0).generation);
		XYFunction sb = null;
		if (children.get(0).generation-1 == end_gen)									// JMB -- Added to make sure that children of the final generation get the right soft borders
			sb = settings.getSoftBorders(children.get(0).generation-1);
		else
			sb = settings.getSoftBorders(children.get(0).generation);
		
		
		if(settings.mode == DispersalSettings.MODE_CUDA) {
			if(sb != sb_old) {
				sb_old = sb;
				gpu.updateGPU(gpu.sb_DEV, sb.getF());
				
				
			}
			if(hb != hb_old) {
				hb_old = hb;
				gpu.updateGPU(gpu.hb_DEV, hb.getF());
				
			}
		}
		
		if(settings.mode == DispersalSettings.MODE_CPP && hb != hb_old || sb != sb_old) {
			hb_old = hb;
			sb_old = sb;
			Migrate.updateF(sb,hb);
		}
		
		double minlat = settings.getMinLat();
		double maxlat = settings.getMaxLat();
		double minlon = settings.getMinLon();
		double maxlon = settings.getMaxLon();
		double sb_lonspace = 0.0;
		double sb_latspace = 0.0;
		double hb_lonspace = 0.0;
		double hb_latspace = 0.0;
		sb_lonspace = (maxlon-minlon)/(sb.getMaxX());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LONGITUDES IN DECIMAL DEGREES (UNIT: DEGREES/PIXEL)
		sb_latspace = (maxlat-minlat)/(sb.getMaxY());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LATITUDES IN DECIMAL DEGREES
		hb_lonspace = (maxlon-minlon)/(hb.getMaxX());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LONGITUDES IN DECIMAL DEGREES
		hb_latspace = (maxlat-minlat)/(hb.getMaxY());		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LATITUDES IN DECIMAL DEGREES						
		double[] latlon = new double[8];
		double lat_offset = 0;//settings.getMinLat();
		double lon_offset = 0;//settings.getMinLon();
		latlon[0] = minlat-lat_offset;
		latlon[1] = maxlat-lat_offset;
		latlon[2] = minlon-lon_offset;
		latlon[3] = maxlon-lon_offset;
		latlon[4] = sb_lonspace;
		latlon[5] = sb_latspace;
		latlon[6] = hb_lonspace;
		latlon[7] = hb_latspace;


		int rm[] = new int[children.size()];
		double[] d = new double[children.size()];

		parami[Migrate.DIMSUM_PARAMI_GENERATION] = children.get(0).generation;
		parami[Migrate.DIMSUM_PARAMI_NUMCHILDREN] = children.size();
		parami[Migrate.DIMSUM_PARAMI_SB_XSIZE] = sb.getMaxX();
		parami[Migrate.DIMSUM_PARAMI_SB_YSIZE] = sb.getMaxY();
		parami[Migrate.DIMSUM_PARAMI_HB_XSIZE] = hb.getMaxX();
		parami[Migrate.DIMSUM_PARAMI_HB_YSIZE] = hb.getMaxY();

		for(int i=0;i<children.size();i++) {
			children.get(i).d = settings.getDispersalRadius((int)parami[Migrate.DIMSUM_PARAMI_GENERATION] ,rand2);
		}
		Collections.sort(children);
		
		
		double[] node = new double[2*children.size()];

		for(int i=0; i<children.size(); i++) {
			Node n = children.get(i);
			d[i] = n.d;
			node[i*2+Migrate.DIMSUM_NODE_lat] = n.parent.lat-lat_offset;
			node[i*2+Migrate.DIMSUM_NODE_lon] = n.parent.lon-lon_offset;
			}


		for(int i=Migrate.DIMSUM_PARAMI_RANDINDEX;i<Migrate.DIMSUM_PARAMI_RANDINDEX+children.size();i++)
			parami[i] = rand2.nextLong();
		
		settings.pAmTTimer2.start();

		if(settings.mode == DispersalSettings.MODE_CUDA) {
		settings.cuda.migrate(node,rm,d,latlon,parami);
		}else if(settings.mode == DispersalSettings.MODE_CPP) {
			
			kernel.migrateCPU( node, rm,  d,  latlon, parami,  0);
		}
		else System.out.println("populateAndMigrate4GPU: unknwon mode");
		
		settings.pAmTTimer2.stop();
		
		
		for(int i=0;i<children.size(); i++) {
			Node n = children.get(i);
			if(rm[i]==1)
				n.parent.children.remove( n );
		}
		
		int length=0;
		for(int i=0;i<rm.length;i++) 
			if(rm[i]==0) length++;
		
		ArrayList<Node> nextGeneration = new ArrayList<Node>();

		for(int i=0;i<children.size();i++) {
			if(rm[i]==0) {
				Node n = children.get(i);
				n.lat = node[i*2+Migrate.DIMSUM_NODE_lat]+lat_offset;
				n.lon = node[i*2+Migrate.DIMSUM_NODE_lon]+lon_offset;
				nextGeneration.add(n);
			}
		}

		settings.pAmTTimer.stop();

		return nextGeneration;	// END_GEN ADDED BY JMB -- 10.20.09
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
			XYFunction ccap = settings.getCarryingCapacity(children.get(0).generation);
			if (children.get(0).generation-1 == end_gen)
				ccap = settings.getCarryingCapacity(children.get(0).generation-1);
			int[][] count = new int[ccap.getMaxY()][ccap.getMaxX()];
			int max_count =0;
			int x,y;
			for(int i=0; i<children.size(); i++) {
				y = ccap.toX(children.get(i).lat,settings.getMinLat(),settings.getMaxLat(),_ysize);
				x = ccap.toX(children.get(i).lon,settings.getMinLon(),settings.getMaxLon(),_xsize);
				count[y][x] += 1;
				if(count[y][x]>max_count)
					max_count = count[y][x];
			}
	/*			for(int r=0; r<ccap.getMaxY(); r++, System.out.println())
				for(int c=0; c<ccap.getMaxX(); c++)
					System.out.print(count[r][c]+" ");
			System.out.println();*/ //this outputs the count to console
		
			boolean rmchild[][][] = new boolean[ccap.getMaxY()][ccap.getMaxX()][max_count];
			
			int r;
			for(x=0;x<ccap.getMaxX();x++) {
				for(y=0;y<ccap.getMaxY();y++) { // this could be parallelized
					for(int i =0; i<count[y][x]-ccap.f(x,y); i++) {
						r = (int)(rand.nextDouble() * count[y][x]);
						while(rmchild[y][x][r])
							r = (int)(rand.nextDouble() * count[y][x]);
						rmchild[y][x][r] = true;
					}
				}
			}
			
			for(int i=0;i<children.size();i++) {
				y = ccap.toX(children.get(i).lat,settings.getMinLat(),settings.getMaxLat(),_ysize);
				x = ccap.toX(children.get(i).lon,settings.getMinLon(),settings.getMaxLon(),_xsize);
				count[y][x]--;
				if(rmchild[y][x][count[y][x]]) {
					//if( children.get(i).parent.children.indexOf( children.get(i) ) != -1 )
						children.get(i).parent.children.remove( children.get(i) );
				} else
					children2.add(children.get(i));
			}
		}
		settings.cCCTimer.stop();
		return children2;
	}

	public void prune(ArrayList<Node> thisGeneration, int generation) {
		// Each member of the generation must be tested for pruning.
		boolean[] rm = new boolean[thisGeneration.size()];
		for(int i=0;i<rm.length;i++)
			rm[i] = false;
		
	
		for(int i=0; i<thisGeneration.size(); i++)
		//while (thisGenItr.hasNext())
		{
			Node current = thisGeneration.get(i);
		//	Node current = (Node)thisGenItr.next();
			// If a member of this generation is pruned, it might make a member of the last generation eligible for pruning.
			// Hence, the while loop.
			while( current != null && current.parent != null ) 
			{
				// To remove a node from memory, we must remove all pointers to it.
				if( current.children.size() == 0 )															//	NOTE BY JMB -- 10.19.09 -- THIS LOOP DOES THE ACTUAL DELETING OF EXTINCT LINEAGES	
				{	
					current.parent.children.remove(  current  );
					if  (current.generation == generation) {
						rm[i] = true;
					}

					current = current.parent;
				}
				else if( current.children.size() == 1 && current.generation != 1)							// NOTE BY JMB -- 10.19.09 -- THIS LOOP RECONNECTS POINTERS AROUND INDIVIDUALS
				{																							//								WITH ONLY ONE DESCENDANT
					current.children.get(0).parent = current.parent;
					current.parent.children.remove(  current  );
					current.parent.children.add( current.children.get(0) );
					
					if (current.generation == generation) {
						rm[i]=true;	
					}
					
					current = current.parent;
				}
				else
					break;
			}
		}
		ArrayList<Node> tmp = new ArrayList<Node>(thisGeneration.size());
		for(int i=0;i<rm.length;i++)
			if(!rm[i]) tmp.add(thisGeneration.get(i));
		thisGeneration.clear();
		thisGeneration = tmp;
		
	}
	
	// ***************	ADDED BY JMB -- 10.19.09 **************************************
	
	/*public void debug_prune(ArrayList<Node> thisGeneration, int generation, Node root)
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
//			System.out.println("HB final flag: "+problem.hb_final);
//			System.out.println("SB final 1 flag: "+problem.sb_final_1);
//			System.out.println("SB final 2 flag: "+problem.sb_final_2);
//			System.out.println("Last at: "+problem.last_at);
//			System.out.println("Last check: "+problem.last_check);
//			System.out.println("Last value: "+problem.last_value);
			System.exit(1);
		}
	}*/
	
	/*private Node flag_prune(Node node, int generation)
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
	}*/
	
	// *******************************************************************************
}
