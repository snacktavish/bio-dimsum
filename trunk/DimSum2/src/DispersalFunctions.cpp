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

#include "DispersalFunctions.h"
#include "kernel.cuh"
#include <iostream>
#include <cstdio>

bool
comp(const Node* a, const Node* b)
{
  return a->d < b->d;
}

DispersalFunctions::DispersalFunctions(Config *settings0, Random* rand0,
    bool cuda) :
  sb_old(0), hb_old(0), settings(settings0), _rand(rand0), _cuda(cuda)
{
  if (_cuda)
    {
      hb = settings->getHB(0);
      sb = settings->getSB(0);
      sb_old = sb;
      hb_old = hb;
      initGPU(sb->getF(), sb->getMaxX(), sb->getMaxY(), hb->getF(),
          hb->getMaxX(), hb->getMaxY());
    }
}

DispersalFunctions::~DispersalFunctions()
{
  if (_cuda)
    {
      shutdownGPU();
    }
}

std::vector<Node*>
DispersalFunctions::populate(std::vector<Node*> thisGeneration, int generation)
{
  std::vector<Node*> children;

  for (unsigned int i = 0; i < thisGeneration.size(); i++)
    {
      int numberOfChildren = settings->getNOffspring(generation, _rand);
      for (int j = 0; j < numberOfChildren; j++)
        {
          Node *child = new Node(generation + 1, thisGeneration[i]);
          (thisGeneration[i])->children.push_back(child);
          children.push_back(child);
        }
    }

  return children;
}

std::vector<Node*>
DispersalFunctions::migrate4GPU(std::vector<Node*> children, int generation,
    int end_gen)
{

  long *parami = new long[DIMSUM_PARAMI_RANDINDEX + children.size()];
  hb = 0;
  if (children[0]->generation - 1 == end_gen) // JMB -- Added to make sure that children of the final generation get the right hard borders
    hb = settings->getHB(children[0]->generation - 1);
  else
    hb = settings->getHB(children[0]->generation);
  sb = 0;
  if (children[0]->generation - 1 == end_gen) // JMB -- Added to make sure that children of the final generation get the right soft borders
    sb = settings->getSB(children[0]->generation - 1);
  else
    sb = settings->getSB(children[0]->generation);

  bool setArray = false;
  if (sb != sb_old)
    {
      sb_old = sb;
      setArray = true;
    }

  if (hb != hb_old)
    {
      hb_old = hb;
      setArray = true;
    }

  if (setArray)
    {
      if (_cuda)
        setArraysGPU(sb->getF(), sb->getMaxX(), sb->getMaxY(), hb->getF(),
            hb->getMaxX(), hb->getMaxY());
      else
        setArraysCPU(sb->getF(), sb->getMaxX(), sb->getMaxY(), hb->getF(),
            hb->getMaxX(), hb->getMaxY());
    }
  double minlat = settings->getMinLat();
  double maxlat = settings->getMaxLat();
  double minlon = settings->getMinLon();
  double maxlon = settings->getMaxLon();
  double sb_lonspace = 0.0;
  double sb_latspace = 0.0;
  double hb_lonspace = 0.0;
  double hb_latspace = 0.0;
  sb_lonspace = (maxlon - minlon) / (sb->getMaxX()); // JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LONGITUDES IN DECIMAL DEGREES (UNIT: DEGREES/PIXEL)
  sb_latspace = (maxlat - minlat) / (sb->getMaxY()); // JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LATITUDES IN DECIMAL DEGREES
  hb_lonspace = (maxlon - minlon) / (hb->getMaxX()); // JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LONGITUDES IN DECIMAL DEGREES
  hb_latspace = (maxlat - minlat) / (hb->getMaxY()); // JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LATITUDES IN DECIMAL DEGREES
  double latlon[8];
  latlon[0] = minlat;
  latlon[1] = maxlat;
  latlon[2] = minlon;
  latlon[3] = maxlon;
  latlon[4] = sb_lonspace;
  latlon[5] = sb_latspace;
  latlon[6] = hb_lonspace;
  latlon[7] = hb_latspace;

  int *rm = new int[children.size()];
  double *d = new double[children.size()];
  double *node = new double[2 * children.size()];

  int rmsize = children.size();

  parami[DIMSUM_PARAMI_GENERATION] = children[0]->generation;
  parami[DIMSUM_PARAMI_NUMCHILDREN] = children.size();
  parami[DIMSUM_PARAMI_SB_XSIZE] = sb->getMaxX();
  parami[DIMSUM_PARAMI_SB_YSIZE] = sb->getMaxY();
  parami[DIMSUM_PARAMI_HB_XSIZE] = hb->getMaxX();
  parami[DIMSUM_PARAMI_HB_YSIZE] = hb->getMaxY();

  for (unsigned int i = 0; i < children.size(); i++)
    {
      d[i] = settings->getDispersalRadius(
          (int) parami[DIMSUM_PARAMI_GENERATION], _rand);
      children[i]->d = d[i];
    }

  std::sort(children.begin(), children.end(), comp);

  for (unsigned int i = DIMSUM_PARAMI_RANDINDEX; i < DIMSUM_PARAMI_RANDINDEX
      + children.size(); i++)
    {
      parami[i] = _rand->nextLong();
    }

  for (unsigned int i = 0; i < children.size(); i++)
    {
      node[i * 2 + DIMSUM_NODE_lat] = children[i]->parent->lat;
      node[i * 2 + DIMSUM_NODE_lon] = children[i]->parent->lon;
      if (node[i * 2 + DIMSUM_NODE_lat] < minlat || node[i * 2
          + DIMSUM_NODE_lat] > maxlat)
        std::cout << node[i * 2 + DIMSUM_NODE_lat] << " lat " << node[i * 2
            + DIMSUM_NODE_lon] << std::endl;
      if (node[i * 2 + DIMSUM_NODE_lon] < minlon || node[i * 2
          + DIMSUM_NODE_lon] > maxlon)
        std::cout << node[i * 2 + DIMSUM_NODE_lat] << " lon " << node[i * 2
            + DIMSUM_NODE_lon] << std::endl;

    }

  if (_cuda)
    migrateGPU(node, rm, d, latlon, parami);
  else
    migrateCPU(node, rm, d, latlon, parami);

  for (unsigned int i = 0; i < children.size(); i++)
    {
      Node *n = children[i];
      children[i]->lat = node[i * 2 + DIMSUM_NODE_lat];
      children[i]->lon = node[i * 2 + DIMSUM_NODE_lon];
      if (rm[i] == 1)
        {
          n->parent->remove(n);
        }
    }

  int length = 0;
  for (int i = 0; i < rmsize; i++)
    if (rm[i] == 0)
      length++;

  std::vector<Node*> nextGeneration;

  for (unsigned int i = 0; i < children.size(); i++)
    {
      if (rm[i] == 0)
        {
          Node *n = children[i];
          nextGeneration.push_back(n);
        }
      else
        delete children[i];
    }
  children.clear();
  delete[] parami;
  delete[] rm;
  delete[] d;
  delete[] node;
  return nextGeneration;
}

std::vector<Node*>
DispersalFunctions::checkCarryingCapacity(std::vector<Node*> children,
    int end_gen)
{

  std::vector<Node*> children2;
  if (children.size() > 0)
    {
      int x = 0, y = 0;
      XYFunction* ccap = settings->getCC(children[0]->generation);
      if (children[0]->generation - 1 == end_gen)
        ccap = settings->getCC(children[0]->generation - 1);
      int maxx = ccap->getMaxX(), maxy = ccap->getMaxY();
      int *count = new int[maxx * maxy];
      for (y = 0; y < ccap->getMaxY(); y++)
        {
          for (x = 0; x < ccap->getMaxX(); x++)
            {

              count[y * maxx + x] = 0;
            }
        }
      int max_count = 0;

      for (unsigned int i = 0; i < children.size(); i++)
        {
          y = ccap->toX(children[i]->lat, settings->getMinLat(),
              settings->getMaxLat(), _ysize);
          x = ccap->toX(children[i]->lon, settings->getMinLon(),
              settings->getMaxLon(), _xsize);

          count[y * maxx + x] += 1;
          if (count[y * maxx + x] > max_count)
            max_count = count[y * maxx + x];
        }

      bool *rmchild = new bool[maxy * maxx * max_count];

      for (y = 0; y < ccap->getMaxY(); y++)
        {
          for (x = 0; x < ccap->getMaxX(); x++)
            {
              for (int k = 0; k < max_count; k++)
                {
                  rmchild[((y * maxx) + x) * max_count + k] = false;
                }
            }
        }

      int r = 0;

      for (y = 0; y < ccap->getMaxY(); y++)
        {
          for (x = 0; x < ccap->getMaxX(); x++)
            {
              int asd = count[y * maxx + x] - ccap->f(x, y);
              for (int i = 0; i < asd; i++)
                {
                  r = (int) (_rand->nextFloat() * count[y * maxx + x]);
                  while (rmchild[((y * maxx) + x) * max_count + r])
                    r = (int) (_rand->nextFloat() * count[y * maxx + x]);
                  rmchild[((y * maxx) + x) * max_count + r] = true;
                }
            }
        }
      for (unsigned int i = 0; i < children.size(); i++)
        {

          y = ccap->toX(children[i]->lat, settings->getMinLat(),
              settings->getMaxLat(), _ysize);
          x = ccap->toX(children[i]->lon, settings->getMinLon(),
              settings->getMaxLon(), _xsize);
          count[y * maxx + x]--;

          if (count[y * maxx + x] >= 0 && rmchild[((y * maxx) + x) * max_count
              + count[y * maxx + x]])
            {
              if (children[i]->parent != NULL)
                children[i]->parent->remove(children[i]);

              delete children[i];

            }
          else
            children2.push_back(children[i]);
        }

      delete[] rmchild;
      delete[] count;

    }
  children.clear();

  return children2;
}

void
DispersalFunctions::prune(std::vector<Node*> thisGeneration, int generation)
{
  bool *rm = new bool[thisGeneration.size()];
  for (unsigned int i = 0; i < thisGeneration.size(); i++)
    rm[i] = false;

  for (unsigned int i = 0; i < thisGeneration.size(); i++)
    {
      Node *current = thisGeneration[i];
      // If a member of this generation is pruned, it might make a member of the last generation eligible for pruning.
      // Hence, the while loop.
      while (current != NULL && current->parent != NULL)
        {
          if (current->children.size() == 0)
            {
              current->parent->remove(current);
              if (current->generation == generation)
                {
                  rm[i] = true;
                }
              current = current->parent;
            }
          else if (current->children.size() == 1 && current->generation != 1)
            {

              current->children[0]->parent = current->parent;
              current->parent->remove(current);
              current->parent->children.push_back(current->children[0]);

              if (current->generation == generation)
                {
                  rm[i] = true;
                }

              current = current->parent;
            }
          else
            break;
        }
    }
  std::vector<Node*> tmp;
  for (unsigned int i = 0; i < thisGeneration.size(); i++)
    if (!rm[i])
      tmp.push_back(thisGeneration[i]);
    else
      delete thisGeneration[i];
  thisGeneration.clear();
  thisGeneration = tmp;
  delete[] rm;
}

