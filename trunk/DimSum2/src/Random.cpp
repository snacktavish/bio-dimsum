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

#include "Random.h"
#include <cstdlib>
#include <ctime>
#include <cstdlib>

Random::Random()
{
  _seed = time(NULL);
  //srand(time(NULL));
}

Random::Random(long seed) :
  _seed(seed)
{
  //	srand(seed);
}

Random::~Random()
{
}

float
Random::nextFloat()
{
  //todo: use rand()  or mersenne twister
  _seed = (_seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
  float r = (int) (_seed >> 24);
  r = r / ((float) (1 << 24));
  return r;
}

long
Random::nextLong()
{
  _seed = (_seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
  return _seed; // rand()
}
