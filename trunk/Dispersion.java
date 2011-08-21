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

	static DispersalSettings settings;
	static DispersalFunctions functions;
	static DispersalDisplay display;
	public static int generation;
	static Random rand;			// Added by JMB -- 4.5.10
	
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
    	
    	if( args.length < 1 )
    		throw new Exception("No XML input file specified!");
    	if( args.length > 2 )
    		throw new Exception("More than one command line parameter! Abort!");
    	
    	
    	
    	if( args.length == 1 ) {
    		settings = new DispersalSettings(args[0]);
    		settings.mode = DispersalSettings.MODE_CUDA;}
    	else if( args.length == 2 ) {
    		settings = new DispersalSettings(args[1]);
    		if(args[0].charAt(1) == 'c') {
    			settings.mode = DispersalSettings.MODE_CPP;
    		}if(args[0].charAt(1) == 'g') {
    			settings.mode = DispersalSettings.MODE_CUDA;
    		} else if(args[0].charAt(1) == 'j')  {
    			settings.mode = DispersalSettings.MODE_JAVA;
    		} else System.out.println("main(): unknown mode");
    		
    		
    	}
		
		if (settings.seed == -1)		// Added by JMB -- 4.5.10
			rand = new Random();
		else
			rand = new Random(settings.seed);
		
		if(settings.mode == DispersalSettings.MODE_CPP) {
			System.loadLibrary("kernel");
			Migrate.updateF(settings.softborders.get(0),settings.hardborders.get(0));
		}
		
		if(settings.mode == DispersalSettings.MODE_CUDA) {
			settings.cuda.cpBorders2GPU(settings.getSoftBorders(0), settings.getHardBorders(0));
		}
		
		functions = new DispersalFunctions(settings,rand, settings.cuda);
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
		System.out.println(settings.pAmTTimer2);
		System.out.println(settings.cCCTimer);
		System.out.println(settings.pruneTimer);
		System.out.println(settings.outputTimer);
		System.out.println(settings.outputTimer2);
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

    	generation=1;
		ArrayList<Node> thisGeneration = initialGeneration;
		
		//Node root = initialGeneration.get(0);				// Added by JMB -- 10.19.09 -- Retains a pointer to the root if started with one individual -- arbitrarily selects the first individual if
															//								population is started with more than one individual.
		

		// For each generation
		for(;generation<= settings.getNGenerations() && thisGeneration.size() != 0;generation++) {
			
			System.out.println("Generation number "+generation+" has "+thisGeneration.size()+" individuals.");

			settings.outputTimer.start();
			if( display != null )
			{
				//ArrayList<Node>thisGenCopy = OutputFunction.deepTreeCopy(thisGeneration);
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
				display.update(thisGeneration,generation);
			}
			
			
			settings.doOutput(thisGeneration,generation,rand);
			settings.outputTimer.stop();
			
			ArrayList<Node> nextGeneration = new ArrayList<Node>();

			// Threaded
			if(settings.mode == DispersalSettings.MODE_JAVA) {
				nextGeneration = functions.checkCarryingCapacity(functions.populateAndMigrateThreaded(thisGeneration,settings.getNGenerations()),settings.getNGenerations());	// SETTINGS.GETNGENERATIONS() ADDED BY JMB -- 10.20.09
				
			}else {
				nextGeneration = functions.checkCarryingCapacity(functions.populateAndMigrate4GPU(thisGeneration,settings.getNGenerations()),settings.getNGenerations());	// SETTINGS.GETNGENERATIONS() ADDED BY JMB -- 10.20.09
			}
			
/*			// Unthreaded
			// nextGeneration uninitialized in lat,lon
			nextGeneration = functions.populateNextGeneration(thisGeneration);
			
			// Now generate latitudes and longitudes for the next generation.
			nextGeneration = functions.migrate(nextGeneration,settings.getNGenerations());
*/
			settings.pruneTimer.start();
			functions.prune(thisGeneration,generation);
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
