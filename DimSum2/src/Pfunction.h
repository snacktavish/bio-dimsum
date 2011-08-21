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

#ifndef PFUNCTION_H_
#define PFUNCTION_H_
#include <string>
#include <vector>
#include "Random.h"

class Pfunction
{
public:
  Pfunction(std::vector<double> outcomes, std::vector<double> probabilities,
      int sgen, int egen);

  virtual
  ~Pfunction();

  double
  draw(Random *rand0);

  int startgeneration, endgeneration;

private:

  std::vector<double> _outcomes, _probabilities;
};

#endif /* PFUNCTION_H_ */
