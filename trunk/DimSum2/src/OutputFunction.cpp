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

#include "OutputFunction.h"
#include <sstream>
OutputFunction::OutputFunction(std::string filename, std::string filetype,
    int outputrate) :
  _outputrate(outputrate), _filename(filename), _filetype(filetype)
{

}

OutputFunction::~OutputFunction()
{
}

void
OutputFunction::doOutput(std::vector<Node*> nodes, int generation)
{
  if (_outputrate != 0 && generation % _outputrate == 0)
    {
      std::stringstream fnss;
      fnss << _filename << "_" << generation << "." << _filetype;
      doOutput(fnss.str(), nodes, generation);
    }
}
