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

#include "NDNAOutput.h"

#include <fstream>
#include <queue>

NDNAOutput::NDNAOutput(std::vector<Node*> rootnodes, std::string filename,
    int outputrate, std::string delimeter,  bool trim) :
  OutputFunction(filename, "txt", outputrate), _delimeter(delimeter),
       _trim(trim),
      _rootnodes(rootnodes)
{
}

NDNAOutput::~NDNAOutput()
{
}

void
NDNAOutput::doOutput(std::string filename, std::vector<Node*> nodes,
    int generation)
{
	std::fstream fout(filename.c_str(), std::fstream::out);

	int numAlleles = 2;

	std::string spacer = "\n";

	for(unsigned int loci = 0; loci < Node::numLoci;loci++)
	{

		for(unsigned int allele =0;allele < numAlleles;allele++)
		{
			fout << "\n\nloci " << loci << " allele:" << allele << ":";
			for (unsigned int i = 0; i < _rootnodes.size(); i++)
			{
			  std::string addition = "";
				addition = (_rootnodes[i]->printAlleleTree(_delimeter, generation,
					loci,allele));
			  if (_trim)
				{
				  if (addition.length() >= 2 && addition[0] == '('
					  && addition[addition.length() - 1] == ')')
					addition = addition.substr(1, addition.length() - 1);
				}
			  if (addition != "")
				fout << spacer << addition;
			  spacer = "\n";
			}
		}
	}
}
