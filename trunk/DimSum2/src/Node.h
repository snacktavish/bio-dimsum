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

#ifndef NODE_H_
#define NODE_H_
#include <string>
#include <vector>

class Node
{

public:

  Node(int gen, Node *par);

  Node(double lat0, double lon0, int r, int g, int b);

  virtual
  ~Node();

  void
  remove(Node *n);

  std::string
  getName();

  std::string
  toString();

  double
  getDistance(Node& parent);

  std::string
  printTreeStructure(std::string delimeter, int curr_gen, bool resolve);

  std::string
  printDistance(std::string delimeter, double mutationrate, int curr_gen,
      bool resolve);

  //todo: -> private
  std::vector<Node*> children;
  static int lastUnique;
  int generation, unique;
  double lat, lon, d;
  Node* parent;
  int _r, _g, _b;
};

#endif /* NODE_H_ */
