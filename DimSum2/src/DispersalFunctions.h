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

#ifndef DISPERSALFUNCTIONS_H_
#define DISPERSALFUNCTIONS_H_
#include <vector>
#include "Node.h"
#include "Config.h"
#include "Random.h"

class DispersalFunctions
{
public:
  DispersalFunctions(Config *settings0, Random* rand0, bool cuda);

  virtual
  ~DispersalFunctions();

  void
  prune(std::vector<Node*> thisGeneration, int generation);

  std::vector<Node*>
  migrate4GPU(std::vector<Node*> thisGeneration, int generation, int end_gen);

  std::vector<Node*>
  populate(std::vector<Node*> thisGeneration, int generation);

  std::vector<Node*>
  checkCarryingCapacity(std::vector<Node*> children, int end_gen);

private:
  XYFunction* sb;
  XYFunction* sb_old;
  XYFunction* hb;
  XYFunction* hb_old;
  Config* settings;
  Random* _rand;

  static const int _xsize = 0;
  static const int _ysize = 1;

  bool _cuda;
};

#endif /* DISPERSALFUNCTIONS_H_ */
