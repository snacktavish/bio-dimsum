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

#include "LocationsOutput.h"
#include <queue>
#include <fstream>

LocationsOutput::LocationsOutput(std::vector<Node*> rootnodes,
    std::string filename, int outputrate, bool locationsType) :
  OutputFunction(filename, "txt", outputrate), _locationsType(locationsType),
      _rootnodes(rootnodes)
{
}

LocationsOutput::~LocationsOutput()
{
}

void
LocationsOutput::doOutput(std::string filename, std::vector<Node*> nodes,
    int generation)
{
  std::fstream fout(filename.c_str(), std::fstream::out);
  std::string spacer = "\n";

  if (_locationsType == DIMSUM_LOCATIONSTYPE_ALL)
    {

      std::queue<Node*> relevantNodes;
      for (unsigned int i = 0; i < _rootnodes.size(); i++)
        relevantNodes.push(_rootnodes[i]);
      spacer = "";
      while (!relevantNodes.empty())
        {
          Node *tmp = relevantNodes.front();
          relevantNodes.pop();
          fout << spacer << tmp;
          for (unsigned int i = 0; i < tmp->children.size(); i++)
            relevantNodes.push(tmp->children[i]);

          spacer = "\n";
        }
    }
  else
    {
      spacer = "";
      for (unsigned int i = 0; i < nodes.size(); i++)
        {
          fout << spacer << nodes[i];
          spacer = "\n";
        }
    }
}
