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
        public int startgeneration=-1,endgeneration=-1;
        ArrayList<Double> outcomes, probabilities;
        
        public PFunction(String outcomeslist,String probabilitieslist) throws Exception {
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
        }
        
        public double draw(java.util.Random rand) {
                double p = rand.nextDouble();
                double c = 0.0;
                int i=0;
                for(i=0; i<probabilities.size(); i++) {
                        if( p >= c && p <= c+probabilities.get(i) )
                                return outcomes.get(i);
                        c+=probabilities.get(i);
                }
                return outcomes.get(outcomes.size()-1);
        }
}
