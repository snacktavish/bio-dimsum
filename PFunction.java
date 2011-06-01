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
import java.lang.*;

class PFunction {
	//private float[] _outcomes_probabilities;
	public FloatArray3D _outcomes_probabilities;
	public IntArray2D _size_gen;
	int _index =0;
	public final static int _outcomes = 0;
	public final static int _probabilities = 1;

	public final static int startgeneration = 2;
	public final static int endgeneration = 3;
	
	public PFunction(int x,int y) {
		_outcomes_probabilities = new FloatArray3D(x, y, 2);
		_size_gen = new IntArray2D(x, 4);
	}
	
	public void add(String outcomeslist,String probabilitieslist,int sg, int eg) throws Exception {
		ArrayList<Double> outcomes, probabilities;

		outcomes = new ArrayList<Double>();
		probabilities = new ArrayList<Double>();
		
		StringTokenizer strtok = new StringTokenizer(outcomeslist);
		while( strtok.hasMoreTokens() )
			outcomes.add( new Double( Double.parseDouble(strtok.nextToken()) ) );
			
		strtok = new StringTokenizer(probabilitieslist);
		while( strtok.hasMoreTokens() )
			probabilities.add( new Double( Double.parseDouble(strtok.nextToken()) ) );
			
		if( outcomes.size() != probabilities.size() )
			throw new Exception("Size of probability list and outcome list does not match.");
		//_outcomes_probabilities = new float[outcomes.size()*2];
		for(int i=0;i<outcomes.size();i++)
		{
			double tmp = outcomes.get(i);
			_outcomes_probabilities.set(_index, i, _outcomes,(float)tmp);
			tmp = probabilities.get(i);
			_outcomes_probabilities.set(_index,i,_probabilities,(float)tmp);
		}
		_size_gen.set(_index, _outcomes, outcomes.size());
		_size_gen.set(_index, _probabilities, probabilities.size());

		_size_gen.set(_index, startgeneration, sg);
		_size_gen.set(_index, endgeneration, eg);

		_index++;
	}
	
	public double getP(int generation, java.util.Random rand) {
		//PFunction thefunc=null;
		int ind=-1;
		for(int i=0;i<_outcomes_probabilities.xsize(); i++)
			if( _size_gen.get(i, startgeneration) <= generation &&  _size_gen.get(i, endgeneration)>= generation )	// MODIFIED BY JMB -- 10.20.09 -- CHANGED FINAL > TO >=
				ind = i;
		if( ind == -1 ) {
			for(int i=0;i<_outcomes_probabilities.xsize();i++)
				if( _size_gen.get(i, startgeneration) == -1 && _size_gen.get(i, endgeneration) == -1 ) {
					ind = i;
					break;
				}
		}
		if(ind ==-1) {
			System.err.println("asdasdasdas");
			System.exit(-1);
		}
			
		return draw(ind, rand);
	}
	
	public double draw(int x, java.util.Random rand) {
		double p = rand.nextDouble();
		double c = 0.0;
		int i=0;
		for(i=0; i<_size_gen.get(x, _outcomes); i++) {
			if( p >= c && p <= c+_outcomes_probabilities.get(x, i, _probabilities) )
				return _outcomes_probabilities.get(x, i, _outcomes);
			c+=_outcomes_probabilities.get(x, i, _probabilities);
		}
		return _outcomes_probabilities.get(x, _size_gen.get(x,_outcomes)-1, _outcomes);
	}
}
