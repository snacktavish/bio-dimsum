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
import java.io.*;
import org.jdom.*;

class OutputFunction {
	
	public static int outputnum = 1;

	String ofile = "";

	String type = "";
	
	String delimeter = "";
	String _output = "";
	boolean trim = false;
	boolean resol = false;
	double mutationrate = 0.0;

	
	PrintWriter out;
	
	String generations = ""; // -1 is output all generations
	
	public int output_every_n_generations = -1; // 0 for only at the end, 20 for output every 20th generation.
	
	ArrayList<Double> samplerects = new ArrayList<Double>();
	ArrayList<Integer> n = new ArrayList<Integer>();
	ArrayList<OutputFunction> optionalSampleOuts = new ArrayList<OutputFunction>();
	
	BufferedWriter outputfile = null;
	
	public OutputFunction(String type0, String delimeter0, String ofile0) {
		type = type0;
		delimeter=delimeter0;
		ofile=ofile0;
		trim = false;
		
		new OutputFunction("treestructure","parent","");
	}

	@SuppressWarnings("unchecked")
	public OutputFunction(DispersalSettings ds, Element input) throws Exception {
		
		if (input.getAttribute("resolve") != null)		// Added by JMB -- 4.8.10
		{
			try{
				resol = input.getAttribute("resolve").getBooleanValue();
			}catch(Exception e){
				resol = false;
			}
		}
		
		
		if( input.getAttribute("file") != null ) {
			try{ 
				ofile = input.getAttribute("file").getValue();
			}
			catch(Exception e) {
				ofile = ds.getSimName() + "_output" + outputnum + ".txt";
				outputnum++;
			}
		}
		
		if( input.getAttribute("output_every") != null ) {
			try {
				output_every_n_generations = Integer.parseInt(input.getAttribute("output_every").getValue());
			}
			catch(Exception e) {
				System.out.println("Unrecognized output_every attribute; outputting only at end of simulation.\n");
				output_every_n_generations = -1;
			}
		}
		else {
			output_every_n_generations = -1;
		}
		
		type = input.getName();
		if( type.equalsIgnoreCase("treestructure") ) {
			try {
				delimeter = input.getAttribute("delimeter").getValue();
			} catch( Exception e ) {
				delimeter = "#";
			}
			try {
				_output = input.getAttribute("output").getValue();
			} catch( Exception e ) {
				_output = "parent";
			}
			try {
				trim = input.getAttribute("trim").getValue().equalsIgnoreCase("true");
			} catch( Exception e ) {
				trim = false;
			}
			if( _output.equalsIgnoreCase("distance") ) {
				try {
					mutationrate = Double.parseDouble(input.getAttribute("mutationrate").getValue());
				} catch( Exception e ) {
					mutationrate = 1.0;
				}
			}
		}
		
		if( type.equalsIgnoreCase("locations") ) {
			try {
				generations = input.getAttribute("generations").getValue();
			}
			catch( Exception e ) {
				generations = "all";
			}
		}
		
		if( type.equalsIgnoreCase("sample") ) {
			Element ee = input;
			if( ee.getAttribute("type") == null ) 
				throw new Exception("sample tag without type attribute!");
			if( ee.getAttribute("type").getValue().equalsIgnoreCase("random") ) {
				samplerects.add(ds.getMinLat());
				samplerects.add(ds.getMinLon());
				samplerects.add(ds.getMaxLat());
				samplerects.add(ds.getMaxLon());
				n.add(  ee.getAttribute("n").getIntValue() );
			}
			else if( ee.getAttribute("type").getValue().equalsIgnoreCase("grid") ) {
				double dlat = ee.getAttribute("dlat").getDoubleValue();
				double dlon = ee.getAttribute("dlon").getDoubleValue();
				for(double lat = ds.getMinLat(); lat+dlat <= ds.getMaxLat(); lat+=dlat ) {
					for(double lon = ds.getMinLon(); lon+dlon <= ds.getMaxLon(); lon+=dlon ) {
						samplerects.add(lat);
						samplerects.add(lon);
						samplerects.add(lat+dlat);
						samplerects.add(lon+dlon);
						n.add( ee.getAttribute("n").getIntValue() );
					}
				}
			}
			else if( ee.getAttribute("type").getValue().equalsIgnoreCase("freeform") ) {
				List dataelem = ee.getChildren("rect");
				for(int i=0; i<dataelem.size(); i++) {
					Element e = (Element)dataelem.get(i);
					samplerects.add( e.getAttribute("lat").getDoubleValue() );
					samplerects.add( e.getAttribute("lon").getDoubleValue() );
					samplerects.add( e.getAttribute("lat").getDoubleValue() + e.getAttribute("dlat").getDoubleValue() );
					samplerects.add( e.getAttribute("lon").getDoubleValue() + e.getAttribute("dlon").getDoubleValue() );
					n.add( e.getAttribute("n").getIntValue() );
				}
			}
			
			java.util.List sampleChildren = ee.getChildren();	// Added by JMB -- 4.12.10
			
 			for (int i=0; i<sampleChildren.size(); i++)			// Added by JMB -- 4.12.10
			{
				optionalSampleOuts.add(new OutputFunction(ds,(Element)sampleChildren.get(i)));
			}
		}
	}

	public void execute(ArrayList<Node> lastGeneration,int generation_number, java.util.Random rand) throws Exception {
		if( ofile.equalsIgnoreCase("") )
			out = new PrintWriter(System.out);
		else {
			try {
				//if( generation_number != -1 && !type.equalsIgnoreCase("sample"))
				//	outputfile = new BufferedWriter(new FileWriter(ofile+(generation_number+1)));
				if (generation_number != -1)
					out = new PrintWriter(new FileWriter(ofile+generation_number));
				/*else if( generation_number != -1 && type.equalsIgnoreCase("sample"))
					outputfile = new BufferedWriter(new FileWriter(ofile+generation_number));*/
				else if( generation_number == -1)
					out = new PrintWriter(new FileWriter(ofile));
				/*String[] lines = output.split("\n");
				for(int i=0; i<lines.length; i++) {
					out.write(lines[i]);
					out.newLine();
				}*/
				
	
			}
			catch( Exception e ) {
				System.out.println("Unable to output to file \""+ofile+"\"! Outputting to console instead:");
				//System.out.println(output);
				out = new PrintWriter(System.out);
			}
		}
		
		
	//	String output = "";
		
		if( type.equalsIgnoreCase("treestructure") ) {
			treestructure(lastGeneration,false);
		}
		if( type.equalsIgnoreCase("locations") ) {
			locations(lastGeneration);
		}
		if( type.equalsIgnoreCase("sample") ) {
			int seed = rand.nextInt();				// JMB -- 4.12.10 -- Fixes seed to replicate results for return of string output and nodes from two diff. functions
			ArrayList<Node> lastGenCopy = deepTreeCopy(lastGeneration);
			sample(lastGenCopy,seed);
			ArrayList<Node> sampled = sampleNodes(lastGenCopy,seed);  // JMB -- 4.12.10 -- Records sampled Nodes
			for (int i=0; i<optionalSampleOuts.size(); i++)
				optionalSampleOuts.get(i).execute(lastGenCopy, sampled,generation_number,i);			// JMB -- 4.12.10 -- Overloaded execute() required
		}
		
		out.close();
	}
	
	public void execute(ArrayList<Node> all, ArrayList<Node> sampled,int gen_num, int z)	// JMB -- 4.12.10 -- Overloaded execute() for treestructure and locations elements
	{																	//					   that are children of sample elements.
		if( ofile.equalsIgnoreCase("") )
			out = new PrintWriter(System.out);
		else {
			try {
				//if( generation_number != -1 && !type.equalsIgnoreCase("sample"))
				//	outputfile = new BufferedWriter(new FileWriter(ofile+(generation_number+1)));
				if (gen_num != -1)
					out = new PrintWriter(new FileWriter(ofile+gen_num));
				/*else if( generation_number != -1 && type.equalsIgnoreCase("sample"))
					outputfile = new BufferedWriter(new FileWriter(ofile+generation_number));*/
				else if( gen_num == -1)
					out = new PrintWriter(new FileWriter(ofile));
				/*String[] lines = output.split("\n");
				for(int i=0; i<lines.length; i++) {
					out.write(lines[i]);
					out.newLine();
				}*/
				
			
			}
			catch( Exception e ) {
				System.out.println("Unable to output to file \""+ofile+"\"! Outputting to console instead:");
				//System.out.println(output);
				out = new PrintWriter(System.out);
			}
		}
		
		
		//String output = "";
		
		if (type.equalsIgnoreCase("treestructure"))
		{
			try{
				if (z == 0)
					all = samplePrune(all,sampled);
				treestructure(all,false);
			}catch(Exception e){
				System.out.println();
				System.out.println("Error in printing pruned treestructure!!");
				e.printStackTrace();
				System.out.println();
			}
		}
		
		if (type.equalsIgnoreCase("locations"))
		{
			try{
				if (z == 0)
					all = samplePrune(all,sampled);
				locations(all);
			}catch(Exception e){
				System.out.println();
				System.out.println("Error in printing pruned locations!!");
				e.printStackTrace();
				System.out.println();
			}
		}
		out.close();
		
	/*	if (ofile.equalsIgnoreCase(""))
			System.out.println(output);
		else
		{
			try{
				if (gen_num != -1)
					outputfile = new BufferedWriter(new FileWriter(ofile+gen_num));
				else
					outputfile = new BufferedWriter(new FileWriter(ofile));
				String[] lines = output.split("\n");
				for (int i=0; i<lines.length; i++)
				{
					outputfile.write(lines[i]);
					outputfile.newLine();
				}
				outputfile.close();
			}catch(Exception e){
				System.out.println("Unable to output to file \""+ofile+"\"! Outputting to console instead:");
				System.out.println(output);
			}
		}*/
	}
		
	public ArrayList<Node> samplePrune(ArrayList<Node> all, ArrayList<Node> sampled)  // JMB -- 4.12.10 -- To prune generation down to just sampled individuals
	{
		Iterator<Node> allitr = all.iterator();
		while (allitr.hasNext())	// Iterates across all nodes in "all"
		{
			Node curr = (Node)allitr.next();
			if (!sampled.contains(curr))  // Identifies any nodes in "all" that are not in "sampled"
			{
				// Remove terminal node from parent's children
				if (curr.parent != null)
				{
					curr.parent.children.remove( curr.parent.children.indexOf( curr ));
					curr = curr.parent;
				}
				
				// Check to see if parent has additional children.  If not, remove parent and repeat.
				while (curr.children.size() < 2)
				{
					if (curr.children.size() == 0)
					{
						if (curr.parent != null)
							curr.parent.children.remove( curr.parent.children.indexOf( curr ));
						curr = curr.parent;
						if (curr == null || curr.parent == null)
							break; 
					}
					else if (curr.children.size() == 1 && curr.generation != 1)
					{
						curr.children.get(0).parent = curr.parent;
						curr.parent.children.add( curr.children.get(0) );
						curr.parent.children.remove( curr.parent.children.indexOf( curr ));
						curr = curr.parent;
						if (curr == null || curr.parent == null)
							break;
					}
					else
						break;
				}
			}
		}
		
		return all;
	}
	
	public static ArrayList<Node> deepTreeCopy(ArrayList<Node> lastGen)	// Added by JMB -- 4.13.10
	{
		ArrayList<Node> newLastGen = new ArrayList<Node>();
		ArrayList<Node> roots = new ArrayList<Node>();
		for (int i=0; i<lastGen.size(); i++)	// Finds the root node for each member of the current generation and
		{										//		creates an array of all unique root nodes.
			Node temp = (Node)lastGen.get(i);
			while (temp.parent != null)
				temp = temp.parent;
			if (roots.indexOf(temp) == -1)
				roots.add(temp);
		}
		
		ArrayList<Node> newRoots = new ArrayList<Node>(roots.size());
		
		for (int i=0; i<roots.size(); i++)
		{
			newRoots.add(i, nodeCopy((Node)roots.get(i), ((Node)lastGen.get(0)).generation, newLastGen));
		}
		
		return newLastGen;
	}
	
	public static Node nodeCopy(Node from, int genNum, ArrayList<Node> newLastGen)	// Added by JMB -- 4.13.10
	{
		Node to = new Node();
		to.generation = from.generation;
		to.unique = from.unique;
		to.lat = from.lat;
		to.lon = from.lon;
		to.c = from.c;
		to.parent = null;
		if (from.children.size() != 0)
		{
			for (int i=0; i<from.children.size(); i++)
			{
				to.children.add(i, nodeCopy( (Node)from.children.get(i),genNum,newLastGen ) );	// Recursively adding children, as needed
			}
		}
		for (int i=0; i<to.children.size(); i++)	// Setting parent for all newly created children
		{
			((Node)to.children.get(i)).parent = to;
		}
		if (to.generation == genNum)	// Populating new ArrayList of nodes in the final generation
			newLastGen.add(to);
		return to;
	}
	
	public void treestructure(ArrayList<Node> thisGeneration, boolean pruneTips) throws Exception	{
		//String ret = "";
		
		ArrayList<Node> relevantFirstGeneration = new ArrayList<Node>();
		for(int i=0; i<thisGeneration.size(); i++)
		{
			Node c = thisGeneration.get(i);
			while( c.parent != null )
				c = c.parent;
			if( relevantFirstGeneration.indexOf(c) == -1 )
				relevantFirstGeneration.add(c);
		}
		String spacer = "";
		for(int i=0; i<relevantFirstGeneration.size(); i++)
		{
			String addition = "";
			if( _output.equalsIgnoreCase("distance") )										// "resol" added by JMB -- 4.8.10
				addition = ( relevantFirstGeneration.get(i).printDistance(delimeter,mutationrate,thisGeneration.get(0).generation,resol,pruneTips) );
			else
				addition = ( relevantFirstGeneration.get(i).printTreeStructure(delimeter,thisGeneration.get(0).generation,resol,pruneTips) );
			if( trim ) {
				if( addition.length() >= 2 && addition.charAt(0) == '(' && addition.charAt(addition.length()-1) == ')' )
					addition = addition.substring(1,addition.length()-1);
			}
			if (addition != "")
				out.print( (spacer + addition));
			spacer = "\n";
		}
		
		//return ret;
	}
	
	public void locations(ArrayList<Node> thisGeneration) throws Exception	{
	//	String ret = "";
		if( generations.equalsIgnoreCase("all") ) {
			LinkedList<Node> relevantNodes = new LinkedList<Node>();
			for(int i=0; i<thisGeneration.size(); i++)
			{
				Node c = thisGeneration.get(i);
				while( c.parent != null )
					c = c.parent;
				if( relevantNodes.indexOf(c) == -1 )
					relevantNodes.add(c);
			}
			

			String spacer = "";
			while( !relevantNodes.isEmpty() )
			{
				Node tmp = relevantNodes.poll();
				out.print( ( spacer + tmp ));
				for(int i=0; i<tmp.children.size(); i++)
					relevantNodes.add( tmp.children.get(i) );
				
				spacer = "\n";
			}
		}
		else if( generations.equalsIgnoreCase("last") ) {
			String spacer = "";
			for(int i=0; i<thisGeneration.size(); i++) {
				out.print( (spacer + thisGeneration.get(i)));
				spacer = "\n";
			}
		}
		
		//return ret;
	}
	
	public void sample(ArrayList<Node> generation, int seed) {
		
		java.util.Random rand = new java.util.Random(seed);
		
	//	String ret = "";
		for(int i=0; 4*i+3<samplerects.size(); i++) {
			ArrayList<Node> contained = new ArrayList<Node>();
			for(int j=0; j<generation.size(); j++) {
				if( generation.get(j).lat >= samplerects.get(4*i) && generation.get(j).lat <= samplerects.get(4*i+2) &&
				    generation.get(j).lon >= samplerects.get(4*i+1) && generation.get(j).lon <= samplerects.get(4*i+3) ) {
				    contained.add(  generation.get(j) );
				}
			}
			
			ArrayList<Node> removed = new ArrayList<Node>();
			for(int j=0;j<n.get(i) && contained.size()>0;j++)
				removed.add( contained.remove( (int)Math.floor(contained.size() * rand.nextDouble()) ) );
			// Grid from (lat0,lon0) to (lat1,lon1)
			if( removed.size() != 0 ) {
				out.print( "("+samplerects.get(4*i)+","+samplerects.get(4*i+1)+") ("+samplerects.get(4*i+2)+","+samplerects.get(4*i+3)+") ");
				for(int j=0;j<removed.size();j++)
					out.print( ""+removed.get(j).getName()+" ");
				out.print( "\n");
			}
		}
		
	//	return ret;
	}
	
	public ArrayList<Node> sampleNodes(ArrayList<Node> generation, int seed) {  // Added by JMB -- 4.12.10 -- To get list of nodes for sampled individuals to return
																				//								pruned tree and pruned list of locations.
		java.util.Random rand = new java.util.Random(seed);
		
		ArrayList<Node> contained = new ArrayList<Node>();
		ArrayList<Node> removed = new ArrayList<Node>();
		for(int i=0; 4*i+3<samplerects.size(); i++) {
			for(int j=0; j<generation.size(); j++) {
				if( generation.get(j).lat >= samplerects.get(4*i) && generation.get(j).lat <= samplerects.get(4*i+2) &&
				   generation.get(j).lon >= samplerects.get(4*i+1) && generation.get(j).lon <= samplerects.get(4*i+3) ) {
				    contained.add(  generation.get(j) );
				}
			}
	
			for(int j=0;j<n.get(i) && contained.size()>0;j++)
				removed.add( contained.remove( (int)Math.floor(contained.size() * rand.nextDouble()) ) );
		}
		
		return removed;
	}
	
	
}
