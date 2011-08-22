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

#ifndef CONFIG_H_
#define CONFIG_H_
#include <string>
#include <vector>
#include "Node.h"
#include "Random.h"
#include "XYFunction.h"
#include "Pfunction.h"
#include "OutputFunction.h"
#include "ConfigFile.h"
#include <iostream>

class Config
{
public:
  Config();
  virtual
  ~Config();

  /**
   * Parses the config file
   */
  void
  readConfig(const char* file);

  /**
   * Returns the offspring for the generation
   */
  int
  getNOffspring(int generation, Random *rand);

  /**
   * Returns the dispersal radius for the generation
   */
  double
  getDispersalRadius(int generation, Random *rand);

  /**
   * Returns the initial population
   */
  std::vector<Node*>
  getInitialPopulation();

  /**
   * Writes all the output files defined in the config
   */
  void
  doOutput(std::vector<Node*> generation, int gen_num);

  XYFunction*
  getCC(int generation)
  {
    return carryingcapacity[calcIndex(generation, carryingcapacity)];
  }

  XYFunction*
  getSB(int generation)
  {
    return softborders[calcIndex(generation, softborders)];
  }

  XYFunction*
  getHB(int generation)
  {
    return hardborders[calcIndex(generation, hardborders)];
  }

  double
  getMinLat()
  {
    return _minLat;
  }

  double
  getMaxLat()
  {
    return _maxLat;
  }

  double
  getMinLon()
  {
    return _minLon;
  }

  double
  getMaxLon()
  {
    return _maxLon;
  }

  std::string
  getSimName()
  {
    return simulationname;
  }

  int
  getNGenerations()
  {
    return _nGenerations;
  }

  long
  getSeed()
  {
    return _seed;
  }

private:

  std::vector<Node*> initialpopulation;
  std::vector<XYFunction*> carryingcapacity, hardborders, softborders;
  std::vector<Pfunction*> noffspring, dispersalradius;
  std::vector<OutputFunction*> outputfunctions;

  std::string simulationname;
  int _nGenerations;
  long _seed;
  float _minLat, _maxLat, _minLon, _maxLon;
  std::string edges;
  std::string name;

  void
  readBMP(char* filename);

  int
  calcIndex(int generation, std::vector<XYFunction*>& xyf);

  Pfunction*
  getPFunction(int generation, std::vector<Pfunction*>& pf);

  void
  parseXYtype(xyType::data_sequence& dataSequence,
      std::vector<XYFunction*> &list, int color);

  void
  parsePtype(pType::distribution_sequence& dataSequence,
      std::vector<Pfunction*> &list);
};

#endif /* CONFIG_H_ */
