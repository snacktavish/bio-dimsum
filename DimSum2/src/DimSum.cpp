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

#include <stdio.h>
#include <stdlib.h>
#include "Config.h"
#include "DispersalFunctions.h"
#include "Random.h"
#include <iostream>
#include <vector>
#include "Timer.h"

static Config config;
static DispersalFunctions *functions;
static int generation;

static std::vector<Node*>
simulate(std::vector<Node*> initialGeneration)
{
  std::vector<Node*> thisGeneration(initialGeneration);
  Timer::enabled = true;
  Timer migrateTimer("migrate()", false);
  Timer cccTimer("checkCarryingCapacity()", false);
  Timer pruneTimer("prune()", false);
  Timer outputTimer("output()", false);
  Timer simulateTimer("simulate()", false);
  Timer distanceTimer("populate",false);

  for (generation = 1; generation <= config.getNGenerations()
      && thisGeneration.size() != 0; generation++)
    {

      simulateTimer.start();
      std::cout << "Generation number " << generation << " has "
          << thisGeneration.size() << " individuals." << std::endl;

      outputTimer.start();
      config.doOutput(thisGeneration, generation);
      outputTimer.stop();

      std::vector<Node*> nextGeneration;

      distanceTimer.start();
      nextGeneration = functions->populate(thisGeneration, generation);
      distanceTimer.stop();

      migrateTimer.start();
      if(nextGeneration.size() > 0)
		  nextGeneration = functions->migrate4GPU(nextGeneration, generation,
			  config.getNGenerations());
      migrateTimer.stop();

      cccTimer.start();
      nextGeneration = functions->checkCarryingCapacity(nextGeneration,
          config.getNGenerations());
      cccTimer.stop();

      pruneTimer.start();
      functions->prune(thisGeneration, generation);
      pruneTimer.stop();

      thisGeneration = nextGeneration;
      simulateTimer.stop();
    }

  std::cout << "Generation number " << generation << " has "
      << thisGeneration.size() << " individuals." << std::endl;

  std::cout << std::endl << std::endl;
  migrateTimer.print();
  cccTimer.print();
  pruneTimer.print();
  outputTimer.print();
  simulateTimer.print();
  distanceTimer.print();

  return thisGeneration;
}

int
main(int argc, const char* argv[])
{

  Random *random;
  if (argc < 2)
    std::cerr << "No XML input file specified!" << std::endl;
  if (argc > 2)
    std::cerr << "More than one command line parameter! Abort!" << std::endl;

  config.readConfig(argv[1]);
std::cout << " read conf" << std::endl;
  if (config.getSeed() == -1)
    random = new Random();
  else
    random = new Random(config.getSeed());
  functions = new DispersalFunctions(&config, random, false);
  std::cout << "Running Simulation: " << config.getSimName() << std::endl;

  std::vector<Node*> lastGeneration = simulate(config.getInitialPopulation());

  return 0;
}
