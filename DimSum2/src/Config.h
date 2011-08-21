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
  void
  readConfig(const char* file);

  int
  getNOffspring(int generation, Random *rand);

  int
  getNOffspring(int generation, double rand);

  double
  getDispersalRadius(int generation, Random *rand);

  double
  getDispersalRadius(int generation, double rand);

  std::vector<Node*>
  getInitialPopulation();

  void
  doOutput(std::vector<Node*> generation, int gen_num);

  void
  parseXYtype(xyType::data_sequence& dataSequence,
      std::vector<XYFunction*> &list, int color);
  void
  parsePtype(pType::distribution_sequence& dataSequence,
      std::vector<Pfunction*> &list);
  void
  readBMP(char* filename);

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

  bool
  isEdgeDeadly()
  {
    return false; //todo:return simulation-> document.getRootElement().getChild("edges").getAttribute("type").getValue().equalsIgnoreCase("deadly");
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

  //int visualfreq;

  std::string simulationname;
  int _nGenerations;
  long _seed;
  float _minLat, _maxLat, _minLon, _maxLon;
  std::string edges;
  std::string name;

  int
  calcIndex(int generation, std::vector<XYFunction*>& xyf)
  {

    int ind = -1;
    for (unsigned int i = 0; i < xyf.size(); i++)
      if (xyf[i]->startgeneration <= generation && xyf[i]->endgeneration
          >= generation)
        ind = i;
    ;
    if (ind == -1)
      {
        for (unsigned int i = 0; i < xyf.size(); i++)
          if (xyf[i]->startgeneration == -1 && xyf[i]->endgeneration == -1)
            {
              ind = i;
              break;
            }
      }
    if (ind < 0)
      {
        std::cerr << "Config::calcIndex(): " << ind << " " << generation
            << std::endl;
        return 0;
      }
    return ind;
  }
};

#endif /* CONFIG_H_ */
