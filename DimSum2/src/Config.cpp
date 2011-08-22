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

#include "ConfigFile.h"
#include "Config.h"
#include "XYFunction.h"
#include "Pfunction.h"
#include <cstdio>
#include <iostream>
#include "VisualOutput.h"
#include "TreeOutput.h"
#include "LocationsOutput.h"

Config::Config() :
  simulationname("")
{
}

Config::~Config()
{
  for (unsigned int i = 0; i < carryingcapacity.size(); i++)
    delete carryingcapacity[i];
  carryingcapacity.clear();
  for (unsigned int i = 0; i < hardborders.size(); i++)
    delete hardborders[i];
  hardborders.clear();
  for (unsigned int i = 0; i < softborders.size(); i++)
    delete softborders[i];
  softborders.clear();
  for (unsigned int i = 0; i < dispersalradius.size(); i++)
    delete dispersalradius[i];
  dispersalradius.clear();
  for (unsigned int i = 0; i < noffspring.size(); i++)
    delete noffspring[i];
  noffspring.clear();
  for (unsigned int i = 0; i < initialpopulation.size(); i++)
    delete initialpopulation[i];
  initialpopulation.clear();
  for (unsigned int i = 0; i < outputfunctions.size(); i++)
    delete outputfunctions[i];
  outputfunctions.clear();
}

void
Config::parseXYtype(xyType::data_sequence& dataSequence, std::vector<
    XYFunction*> &list, int color)
{
  xyType::data_const_iterator dataIterator = dataSequence.begin();
  while (dataIterator != dataSequence.end())
    {

      std::string filename = dataIterator->file();
      int maxvalue = dataIterator->maxvalue();
      int startgeneration = -1;
      if (dataIterator->startgeneration().present())
        {
          startgeneration = dataIterator->startgeneration().get();
        }
      int endgeneration = -1;
      if (dataIterator->endgeneration().present())
        {
          endgeneration = dataIterator->endgeneration().get();
        }
      if (dataIterator->type() == data::type_type::file)
        {
          //todo:list.push_back(new XYFunction(filename,maxvalue));
        }
      else if (dataIterator->type() == data::type_type::image)
        {
          list.push_back(new XYFunction(filename, maxvalue, startgeneration,
              endgeneration, color));
        }
      dataIterator++;

    }

}

void
Config::parsePtype(pType::distribution_sequence& dataSequence, std::vector<
    Pfunction*> &list)
{

  pType::distribution_const_iterator dataIterator = dataSequence.begin();
  while (dataIterator != dataSequence.end())
    {
      int startgeneration = -1;
      if (dataIterator->startgeneration().present())
        {
          startgeneration = dataIterator->startgeneration().get();
        }

      int endgeneration = -1;
      if (dataIterator->endgeneration().present())
        {
          endgeneration = dataIterator->endgeneration().get();
        }
      std::vector<double> pvector;
      distribution::p_type myP = dataIterator->p();
      std::vector<float, std::allocator<float> >::iterator pIter = myP.begin();
      for (; pIter != myP.end(); pIter++)
        pvector.push_back(*pIter);
      std::vector<double> offspringvector;
      distribution::offspring_type myoffspring = dataIterator->offspring();
      for (pIter = myoffspring.begin(); pIter != myoffspring.end(); pIter++)
        offspringvector.push_back(*pIter);

      list.push_back(new Pfunction(offspringvector, pvector, startgeneration,
          endgeneration));
      dataIterator++;
    }
}

void
Config::readConfig(const char* file)
{

  try
    {
      std::auto_ptr<simulation> simulation(simulation_(file));

      _nGenerations = simulation->generations();
      _minLat = simulation->latitude().minimum();
      _maxLat = simulation->latitude().maximum();
      _minLon = simulation->longitude().minimum();
      _maxLon = simulation->longitude().maximum();

      //	_seed = -1;
      _seed = simulation->seed();

      simulationname = simulation->name();

      //todo: simulation->edges();

      xyType::data_sequence& dataSequence =
          simulation->carryingcapacity().data();
      parseXYtype(dataSequence, carryingcapacity, 1);

      dataSequence = simulation->hardborders().data();
      parseXYtype(dataSequence, hardborders, 0);

      dataSequence = simulation->softborders().data();
      parseXYtype(dataSequence, softborders, 2);

      pType::distribution_sequence& distSequence =
          simulation->dispersalradius().distribution();
      parsePtype(distSequence, dispersalradius);

      distSequence = simulation->reproductiveability().distribution();
      parsePtype(distSequence, noffspring);

      simulation::initialpopulation_type::node_sequence nodes =
          simulation->initialpopulation().node();
      simulation::initialpopulation_type::node_sequence::iterator nIter =
          nodes.begin();
      for (; nIter != nodes.end(); nIter++)
        {
          initialpopulation.push_back(new Node(nIter->lat(), nIter->lon(),
              nIter->r(), nIter->b(), nIter->g()));
        }

      //visualfreq = 0;
      if (simulation->output().visual().present())
        {
          int visualfreq = simulation->output().visual().get().output_every();
          int xsize = 800;
          if (simulation->output().visual().get().xsize().present())
            xsize = simulation->output().visual().get().xsize().get();
          int ysize = 600;
          if (simulation->output().visual().get().ysize().present())
            ysize = simulation->output().visual().get().ysize().get();
          outputfunctions.push_back(new VisualOutput(this, visualfreq, xsize,
              ysize));
        }

      simulation::output_type outputcfg = simulation->output();
      simulation::output_type::treestructure_sequence treeseq =
          outputcfg.treestructure();

      simulation::output_type::treestructure_const_iterator treeIterator =
          treeseq.begin();
      while (treeIterator != treeseq.end())
        {
          std::string delimeter = "#";
          if (treeIterator->delimeter().present())
            delimeter = treeIterator->delimeter().get();
          bool distance = false;
          if (treeIterator->output()
              == simulation::output_type::treestructure_type::output_type::parent)
            {
              distance = false;
            }
          else if (treeIterator->output()
              == simulation::output_type::treestructure_type::output_type::distance)
            {
              distance = true;
            }
          std::string file = treeIterator->file();
          bool trim = false;
          if (treeIterator->trim().present())
            trim = treeIterator->trim().get();
          bool resolve = false;
          if (treeIterator->resolve().present())
            resolve = treeIterator->resolve().get();

          float mutationrate = 1.0;
          if (treeIterator->mutationrate().present())
            resolve = treeIterator->mutationrate().get();
          int outputrate = treeIterator->output_every();
          outputfunctions.push_back(new TreeOutput(initialpopulation, file,
              outputrate, delimeter, distance, trim, resolve, mutationrate));
          treeIterator++;
        }

      simulation::output_type::locations_sequence locseq =
          outputcfg.locations();

      simulation::output_type::locations_const_iterator locIterator =
          locseq.begin();
      while (locIterator != locseq.end())
        {
          bool outputall = false;
          if (locIterator->generation()
              == simulation::output_type::locations_type::generation_type::last)
            {
              outputall = false;
            }
          else if (locIterator->generation()
              == simulation::output_type::locations_type::generation_type::all)
            {
              outputall = true;
            }
          std::string file = locIterator->file();

          int outputrate = locIterator->output_every();

          outputfunctions.push_back(new LocationsOutput(initialpopulation,
              file, outputrate, outputall));
          locIterator++;
        }

    }
  catch (const xml_schema::exception& e)
    {
      std::cerr << e << std::endl;
      return;
    }
  catch (const xml_schema::properties::argument&)
    {
      std::cerr << "invalid property argument (empty namespace or location)"
          << std::endl;
      return;
    }
  catch (const xsd::cxx::xml::invalid_utf16_string&)
    {
      std::cerr << "invalid UTF-16 text in DOM model" << std::endl;
      return;
    }
  catch (const xsd::cxx::xml::invalid_utf8_string&)
    {
      std::cerr << "invalid UTF-8 text in object model" << std::endl;
      return;
    }

}

int
Config::getNOffspring(int generation, Random *rand)
{
  Pfunction *thefunc = getPFunction(generation, noffspring);
  return (int) thefunc->draw(rand);
}

double
Config::getDispersalRadius(int generation, Random *rand)
{
  Pfunction *thefunc = getPFunction(generation, dispersalradius);
  return thefunc->draw(rand);
}

Pfunction*
Config::getPFunction(int generation, std::vector<Pfunction*>& pf)
{
  Pfunction *thefunc = NULL;
  for (unsigned int i = 0; i < pf.size(); i++)
    if (pf[i]->checkGeneration(-1))
      {
        thefunc = pf[i];
        break;
      }
  for (unsigned int i = 0; i < pf.size(); i++)
    if (pf[i]->checkGeneration(generation))
      thefunc = pf[i];
  return thefunc;
}

int
Config::calcIndex(int generation, std::vector<XYFunction*>& xyf)
{

  int ind = -1;
  for (unsigned int i = 0; i < xyf.size(); i++)
    if (xyf[i]->checkGeneration(generation))
      ind = i;
  ;
  if (ind == -1)
    {
      for (unsigned int i = 0; i < xyf.size(); i++)
        if (xyf[i]->checkGeneration(-1))
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

std::vector<Node*>
Config::getInitialPopulation()
{
  return initialpopulation;
}

void
Config::doOutput(std::vector<Node*> generation, int gen_num)
{
  for (unsigned int i = 0; i < outputfunctions.size(); i++)
    outputfunctions[i]->doOutput(generation, gen_num);
}
