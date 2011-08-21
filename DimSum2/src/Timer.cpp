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

#include "Timer.h"
#include <iostream>

Timer::Timer(std::string name0, bool v) :
  name(name0), running(false), verbose(v), overallTime(0)
{
}

Timer::~Timer()
{
}

void
Timer::start()
{

  if (!running)
    {
      tmpTime = clock();
      running = true;
    }
  else
    std::cerr << "Timer " << name << " allready running" << std::endl;
}

clock_t
Timer::stop()
{

  if (running)
    {
      clock_t diff = clock() - tmpTime;
      overallTime += diff;
      tmpTime = 0;
      if (verbose)
        std::cout << name << ": " << ((double) diff * 1000.0)
            / (double) CLOCKS_PER_SEC << "ms" << std::endl;
      running = false;
      return diff;
    }
  else
    {
      std::cerr << "Timer " << name << " not running" << std::endl;
    }

  return 0;
}

void
Timer::print()
{

  std::cout << name << ": Overall: " << ((double) overallTime * 1000.0)
      / (double) CLOCKS_PER_SEC << "ms" << std::endl;
}
