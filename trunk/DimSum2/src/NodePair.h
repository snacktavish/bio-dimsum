//  DIM SUM 2 -- Demography and Individual Migration Simulated Using a Markov chain
//  Copyright (C) 2011 Peter Hoffmann <p-hoffmann@web.de>
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

#ifndef NODEPAIR_H_
#define NODEPAIR_H_
#include <ctime>
#include <string>

class NodePair
{
public:
  NodePair(int indexF, int indexM, double distance);

  virtual
  ~NodePair();


  bool operator<( const NodePair& n2) const
  {
	  return _tmpDistance < n2._tmpDistance;
  }


  int indexFemale;
  int indexMale;
  double _distance;
  double _tmpDistance;
};

/*bool operator< (NodePair &n1, NodePair &n2)
 {
     return n1._tmpDistance < n2._tmpDistance;
 }*/

#endif /* NODEPAIR_H_ */
