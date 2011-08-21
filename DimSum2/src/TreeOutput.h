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

#ifndef TREEOUTPUT_H_
#define TREEOUTPUT_H_
#include "OutputFunction.h"

class TreeOutput : public OutputFunction
{
public:

  TreeOutput(std::vector<Node*> rootnodes, std::string filename,
      int outputrate, std::string delimeter, bool treeType, bool trim,
      bool resolve, float mutationrate);

  virtual
  ~TreeOutput();

protected:

  virtual void
  doOutput(std::string filename, std::vector<Node*> nodes, int generation);

private:
  static const bool DIMSUM_TREETYPE_DISTANCE = true;
  static const bool DIMSUM_TREETYPE_ID = false;

  std::string _delimeter;
  bool _treeType;
  bool _trim;
  bool _resolve;
  std::vector<Node*> _rootnodes;
  float _mutationrate;

};

#endif /* TREEOUTPUT_H_ */
