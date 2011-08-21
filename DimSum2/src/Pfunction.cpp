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

#include "Pfunction.h"

Pfunction::Pfunction(std::vector<double> outcomes,
    std::vector<double> probabilities, int sgen, int egen) :
  startgeneration(sgen), endgeneration(egen), _outcomes(outcomes),
      _probabilities(probabilities)
{
}

Pfunction::~Pfunction()
{
}

double
Pfunction::draw(Random *rand0)
{
  double p = rand0->nextFloat();
  double c = 0.00;
  for (unsigned int i = 0; i < _probabilities.size(); i++)
    {
      if (p >= c && p <= c + _probabilities[i])
        return _outcomes[i];
      c += _probabilities[i];
    }
  return _outcomes[_outcomes.size() - 1];
}

