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

#ifndef NDNAOUTPUT_H_
#define NDNAOUTPUT_H_
#include "OutputFunction.h"

class NDNAOutput : public OutputFunction
{
public:

	NDNAOutput(std::vector<Node*> rootnodes, std::string filename,
      int outputrate, std::string delimeter,  bool trim);

  virtual
  ~NDNAOutput();

protected:

  virtual void
  doOutput(std::string filename, std::vector<Node*> nodes, int generation);

private:


  std::string _delimeter;

  bool _trim;

  std::vector<Node*> _rootnodes;

};

#endif /* NDNAOUTPUT_H_ */
