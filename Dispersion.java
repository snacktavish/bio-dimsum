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
	
    public static void main(String[] args) throws Exception {
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
		ArrayList<Node> lastGeneration = simulate( (ArrayList<Node>)settings.getInitialPopulation().clone(), rand);
		
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
		
		Node root = initialGeneration.get(0);				// Added by JMB -- 10.19.09 -- Retains a pointer to the root if started with one individual -- arbitrarily selects the first individual if
															//								population is started with more than one individual.
		// For each generation
		for(;generation<=settings.getNGenerations() && thisGeneration.size() != 0;generation++) {
			System.out.println("Generation number "+generation+" has "+thisGeneration.size()+" individuals.");
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
			
			ArrayList<Node> nextGeneration = new ArrayList<Node>();

			// Threaded			
			nextGeneration = functions.populateAndMigrateThreaded(thisGeneration,settings.getNGenerations());	// SETTINGS.GETNGENERATIONS() ADDED BY JMB -- 10.20.09
			
/*			// Unthreaded
			// nextGeneration uninitialized in lat,lon
			nextGeneration = functions.populateNextGeneration(thisGeneration);
			
			// Now generate latitudes and longitudes for the next generation.
			nextGeneration = functions.migrate(nextGeneration,settings.getNGenerations());
*/
			
			functions.prune(thisGeneration);
			
			
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
