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

public class Dispersion {
	//public final static int[] genSizeOLDd = {4, 5, 6, 9, 11, 13, 18, 26, 29, 33, 42, 53, 64, 81, 103, 130, 167, 201, 255, 305, 377, 444, 555, 663, 766, 905, 1040, 1196, 1313, 1464, 1621, 1732, 1853, 1875, 1966, 2059, 2132, 2182, 2168, 2235, 2291, 2310, 2330, 2361, 2410, 2416, 2404, 2454, 2435, 2434, 2425, 2454, 2453, 2451, 2459, 2434, 2429, 2433, 2449, 2469, 2474, 2509, 2533, 2523, 2499, 2486, 2488, 2467, 2488, 2461, 2488, 2496, 2489, 2491, 2554, 2543, 2567, 2567, 2512, 2529, 2485, 2509, 2530, 2506, 2520, 2508, 2500, 2496, 2526, 2555, 2531, 2482, 2487, 2473, 2476, 2476, 2471, 2457, 2461, 2496, 0 };
	//public final static int[] genSizeOLDdeg2rad = { 4, 5, 7, 10, 14, 20, 28, 42, 57, 73, 97, 126, 140, 184, 235, 291, 356, 442, 518, 631, 717, 852, 991, 1160, 1293, 1447, 1602, 1712, 1826, 1924, 2017, 2071, 2071, 2158, 2158, 2176, 2173, 2255, 2248, 2269, 2280, 2296, 2330, 2347, 2390, 2395, 2406, 2428, 2436, 2449, 2482, 2499, 2493, 2461, 2501, 2511, 2541, 2534, 2562, 2529, 2500, 2525, 2540, 2524, 2507, 2490, 2470, 2506, 2544, 2533, 2533, 2543, 2559, 2542, 2539, 2506, 2535, 2561, 2528, 2504, 2494, 2540, 2500, 2466, 2485, 2458, 2468, 2501, 2516, 2513, 2539, 2524, 2526, 2515, 2511, 2486, 2507, 2500, 2481, 2485, 0};
	public final static int[] genSize = {4, 5, 8, 11, 16, 18, 27, 38, 52, 63, 85, 102, 126, 163, 192, 230, 288, 335, 389, 461, 549, 656, 778, 886, 1015, 1142, 1296, 1445, 1586, 1721, 1853, 1916, 1986, 2053, 2137, 2191, 2210, 2210, 2249, 2309, 2314, 2324, 2346, 2395, 2360, 2389, 2432, 2443, 2442, 2510, 2491, 2505, 2474, 2468, 2466, 2490, 2470, 2517, 2473, 2459, 2494, 2484, 2513, 2513, 2496, 2499, 2526, 2558, 2552, 2537, 2533, 2527, 2553, 2546, 2534, 2500, 2527, 2541, 2541, 2550, 2520, 2552, 2524, 2484, 2457, 2482, 2483, 2487, 2502, 2518, 2495, 2515, 2507, 2487, 2532, 2539, 2509, 2465, 2496, 2529, 0};
		
	static DispersalSettings settings;
	static DispersalFunctions functions;
	static DispersalDisplay display;
	public static int generation;
	static Random rand;			// Added by JMB -- 4.5.10
	
    public static void main(String[] args) throws Exception {
    	
    	System.loadLibrary("DispersalFunctionC");
    	if( args.length < 1 )
    		throw new Exception("No XML input file specified!");
    	if( args.length > 1 )
    		throw new Exception("More than one command line parameter! Abort!");

		settings = new DispersalSettings(args[0]);
		
		if (settings.seed == -1)		// Added by JMB -- 4.5.10
			rand = new Random();
		else
			rand = new Random(settings.seed);
		
		
		functions = new DispersalFunctions(settings,rand);
		Thread dispthread=null;
		if( settings.visualoutput ) {
			display = new DispersalDisplay(settings);
			dispthread = new Thread(display);
			dispthread.start();
		}
		
		System.out.println("Running Simulation: "+settings.getSimName() );
		
		// Pass the initial population to simulate, which will return the final population after n generations; n is set by settings.
		settings.simulateTimer.start();
		ArrayList<Node> lastGeneration = simulate( (ArrayList<Node>)settings.getInitialPopulation().clone(), rand);
		settings.simulateTimer.stop();
		
		System.out.println("");
		System.out.println("");
		System.out.println(settings.pAmTTimer);
		System.out.println(settings.cCCTimer);
		System.out.println(settings.pruneTimer);
		System.out.println(settings.outputTimer);
		System.out.println(settings.simulateTimer);
		System.out.println("");
		System.out.println("");
		
		settings.doOutput(lastGeneration,-1,rand);

		if ( settings.visualoutput )	// IF STATEMENT ADDED BY JMB -- 10.20.09
		{
			display.terminateSource();
			dispthread.join(); // wait for display thread to finish before exiting
		}
		
		//System.out.println("Done! Press any key to continue...");
		//System.in.read();
		//System.in.read();
		
		System.exit(0);
    }
    
    public static ArrayList<Node> simulate(ArrayList<Node> initialGeneration,java.util.Random rand) throws Exception
    {
    	
		DispersalFunctionC.setArrays(settings.softborders._f.getData(),
				settings.softborders._size_gen.getData(),
				settings.softborders._f.size(),
				settings.hardborders._f.getData(),
				settings.hardborders._size_gen.getData(),
				settings.hardborders._f.size());
    	
    	
    	generation=1;
		ArrayList<Node> thisGeneration = initialGeneration;
		
		Node root = initialGeneration.get(0);				// Added by JMB -- 10.19.09 -- Retains a pointer to the root if started with one individual -- arbitrarily selects the first individual if
															//								population is started with more than one individual.
		// For each generation
		for(;generation<= settings.getNGenerations() && thisGeneration.size() != 0;generation++) {
			settings.outputTimer.start();
			System.out.println("Generation number "+generation+" has "+thisGeneration.size()+" individuals.");
			if(thisGeneration.size() != genSize[generation-1]) {
				System.err.println("simulate: generation size mismatch: "+ "size: " + thisGeneration.size()+" should be: "+genSize[generation-1]);
			}
			
			if( display != null )
			{
				ArrayList<Node>thisGenCopy = OutputFunction.deepTreeCopy(thisGeneration);
				/*if (generation == 4)							// JMB -- 4.14.10 -- Code to compare original and copied trees, if necessary
				{
					Iterator cpItr = thisGenCopy.iterator();
					Iterator origItr = thisGeneration.iterator();
					System.out.println("Copy:");
					while(cpItr.hasNext())
						System.out.println(cpItr.next());
					System.out.println("Original:");
					while(origItr.hasNext())
						System.out.println(origItr.next());
					System.exit(1);
				}*/
				display.update(thisGenCopy,generation);
			}
			
			settings.doOutput(thisGeneration,generation,rand);
			settings.outputTimer.stop();
			
			ArrayList<Node> nextGeneration = new ArrayList<Node>();

			// Threaded
		//	Prepared4GPU p4g = new Prepared4GPU(settings, settings.getNGenerations(), rand.nextInt());
		//	nextGeneration = p4g.populateAndMigrateThreaded(thisGeneration,settings.getNGenerations());
			nextGeneration = functions.checkCarryingCapacity(functions.populateAndMigrate4GPU(thisGeneration,settings.getNGenerations()),settings.getNGenerations());	// SETTINGS.GETNGENERATIONS() ADDED BY JMB -- 10.20.09

		//	nextGeneration = functions.checkCarryingCapacity(functions.populateAndMigrateThreaded(thisGeneration,settings.getNGenerations()),settings.getNGenerations());	// SETTINGS.GETNGENERATIONS() ADDED BY JMB -- 10.20.09
			
/*			// Unthreaded
			// nextGeneration uninitialized in lat,lon
			nextGeneration = functions.populateNextGeneration(thisGeneration);
			
			// Now generate latitudes and longitudes for the next generation.
			nextGeneration = functions.migrate(nextGeneration,settings.getNGenerations());
*/
			settings.pruneTimer.start();
			functions.prune(thisGeneration);
			settings.pruneTimer.stop();
			
			//functions.debug_prune(thisGeneration,generation,root);	// ADDED BY JMB -- 10.19.09 -- FOR DEBUGGING WEIRD PRUNING PROBLEM
			
			//settings.doOutput(thisGeneration,generation,rand);
			
			thisGeneration = nextGeneration; // prepare for the next loop
		}//End For each generation

		
		System.out.println("Generation number "+generation+" has "+thisGeneration.size()+" individuals.");
		if( display != null && generation <= settings.getNGenerations())
				display.update(thisGeneration,generation);

		
		return thisGeneration;
    }
}
