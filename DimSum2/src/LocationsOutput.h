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

#ifndef LOCATIONSOUTPUT_H_
#define LOCATIONSOUTPUT_H_
#include "Node.h"
#include "OutputFunction.h"

class LocationsOutput : public OutputFunction
{
public:
  LocationsOutput(std::vector<Node*> rootnodes, std::string filename,
      int outputrate, bool locationsType);

  virtual
  ~LocationsOutput();

protected:

  virtual void
  doOutput(std::string filename, std::vector<Node*> nodes, int generation);

private:
  static const bool DIMSUM_LOCATIONSTYPE_ALL = true;
  static const bool DIMSUM_LOCATIONSTYPE_LAST = false;

  bool _locationsType;
  std::vector<Node*> _rootnodes;
};

#endif /* LOCATIONSOUTPUT_H_ */
