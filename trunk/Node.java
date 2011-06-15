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
import java.awt.*;

class Node {
	
	//double last_value=0.0;
	//String last_check="";
	//String last_at="";
	//boolean hb_final=false;					// ADDED BY JMB
	//boolean sb_final_1=false;					// ADDED BY JMB
	//boolean sb_final_2=false;					// ADDED BY JMB
	//boolean moved = false;					// ADDED BY JMB -- 10.19.09 --  AS A MARKER TO SEE IF INDIVIDUALS WERE PROPERLY MOVED
//	double par_lat = 0.0;					// ADDED BY JMB -- 10.19.09
//	double par_lon = 0.0;					// ADDED BY JMB -- 10.19.09
//	double end_move_lat = 0.0;				// ADDED BY JMB -- 10.19.09
//	double end_move_lon = 0.0;				// ADDED BY JMB -- 10.19.09
//	boolean failed_one = false;				// ADDED BY JMB -- 10.19.09
//	boolean failed_two = false;				// ADDED BY JMB -- 10.19.09
//	boolean added_nextgen = false;			// ADDED BY JMB -- 10.19.09
//	double last_d = 0.0;					// ADDED BY JMB -- 10.19.09
//	double last_crs = 0.0;					// ADDED BY JMB -- 10.19.09
	static int lastGen=0,lastUnique=0;

	int generation,unique;
	double lat=0.0,lon=0.0;
	Node parent=null;
	ArrayList<Node> children=new ArrayList<Node>();
	Color c;
	public Node(int gen, Node par) {
		if( lastGen != gen ) {
			lastUnique = 1;
			lastGen = gen;
		}
		generation = gen;
		unique = lastUnique++;
		parent = par;
		c = par.c;
	}
	public Node()	// Added by JMB -- 4.13.10
	{
		generation = 0;
		unique = 0;
		parent = null;
	}
	public Node(double lat0, double lon0,int r, int g, int b) {
		lastGen = generation = 1;
		lat=lat0;
		lon=lon0;
		if( lastUnique == 0 )
			lastUnique = 1;
		unique = lastUnique++;
		parent = null;
		c = new Color(r,g,b,255);
	}
	public String getName() {
		return ""+generation+"_"+unique;
	}
	public String toString() {
		return getName() + " " + lat + " " + lon;
	}
	public double getDistance(Node parent) {
		return (double)(this.generation - parent.generation);
	}
	public String printTreeStructure(String delimeter,int curr_gen, boolean resolve, boolean pruneTips) {
		//if( children.size() == 0 && generation != curr_gen)
		if( children.size() == 0)
			return getName();
		//if ( children.size() == 0 && generation == curr_gen)
		//	return "";
		if( pruneTips && generation == (curr_gen+1))
			return "";
		if( pruneTips && generation == curr_gen)
			return getName();
		String ret = "(";
		String spacer = "";
		if (resolve && children.size()>2)		// Added by JMB -- 4.8.10
		{										// Sort of a hack...better way would be to actually add in new dummy nodes in the tree
			for(int i=0; i<children.size()-2; i++)
			{
				ret += (spacer + children.get(i).printTreeStructure(delimeter,curr_gen,resolve,pruneTips));
				spacer = ",(";
			}
			for(int i=children.size()-2; i<children.size(); i++) {
				ret += (spacer + children.get(i).printTreeStructure(delimeter,curr_gen,resolve,pruneTips));
				spacer = ",";
			}			
			for (int i=0; i<(children.size()-2); i++)
			{
				ret += ")";
			}
		}
		else
		{
			for(int i=0; i<children.size(); i++) {
				ret += (spacer + children.get(i).printTreeStructure(delimeter,curr_gen,resolve,pruneTips));
				spacer = ",";
			}
		}
		// ret += (delimeter+getName()+")");
		ret += (")"+delimeter+getName());		// MODIFIED BY JMB -- 10.16.09
		
		return ret;		
	}
	public String printDistance(String delimeter,double mutationrate,int curr_gen, boolean resolve, boolean pruneTips) {
		//if( children.size() == 0 && generation != curr_gen)
		if( children.size() == 0)
			return getName();
		//if( children.size() == 0 && generation == curr_gen)
		//	return "";
		if( pruneTips && generation == (curr_gen+1))
			return "";
		if( pruneTips && generation == curr_gen)
			return getName();
		String ret = "(";
		String spacer = "";
		if (resolve && children.size()>2)	// Added by JMB -- 4.8.10
		{									// Sort of a hack...better way would be to actually add in new dummy nodes in the tree
			for (int i=0; i<(children.size()-2); i++)
			{
				ret += (spacer + children.get(i).printDistance(delimeter,mutationrate,curr_gen,resolve,pruneTips) + delimeter + (children.get(i).getDistance(this) * mutationrate));
				spacer = ",(";
			}
			for (int i=children.size()-2; i<children.size(); i++)
			{
				ret += (spacer + children.get(i).printDistance(delimeter,mutationrate,curr_gen,resolve,pruneTips) + delimeter + (children.get(i).getDistance(this) * mutationrate));
				spacer = ",";
			}
			for (int i=0; i<(children.size()-2); i++)
			{
				ret += "):0";
			}			
		}
		else
		{
			for(int i=0; i<children.size(); i++) {
				ret += (spacer + children.get(i).printDistance(delimeter,mutationrate,curr_gen,resolve,pruneTips) + delimeter + (children.get(i).getDistance(this) * mutationrate));
				spacer = ",";
			}
		}
		ret += ")";
		
		return ret;
	}
}
