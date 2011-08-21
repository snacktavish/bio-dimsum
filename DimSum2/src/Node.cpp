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

#include "Node.h"
#include <iostream>
#include <sstream>

int Node::lastUnique = 0;

Node::~Node()
{
}

Node::Node(int gen, Node *par)
{
  unique = Node::lastUnique++;
  generation = gen;
  parent = par;
  lat = par->lat;
  lon = par->lon;
  _r = par->_r;
  _g = par->_g;
  _b = par->_b;
}

Node::Node(double lat0, double lon0, int r, int g, int b)
{
  if (Node::lastUnique == 0)
    Node::lastUnique = 1;
  unique = Node::lastUnique++;
  generation = 1;
  parent = NULL;
  lat = lat0;
  lon = lon0;
  _r = r;
  _g = g;
  _b = b;
}

void
Node::remove(Node *n)
{
  int index = -1;
  for (unsigned int i = 0; i < children.size(); i++)
    {
      if (children[i] == n)
        {
          index = i;
          break;
        }
    }

  if (index > -1)
    children.erase(children.begin() + index);
}

std::string
Node::getName()
{
  std::stringstream tmp;
  tmp << generation << "_" << unique;
  return tmp.str();
}
std::string
Node::toString()
{
  std::stringstream tmp;
  tmp << getName() << " " << lat << " " << lon;
  return tmp.str();
}
double
Node::getDistance(Node &parent)
{
  return (double) (this->generation - parent.generation);
}
std::string
Node::printTreeStructure(std::string delimeter, int curr_gen, bool resolve)
{

  if (children.size() == 0)
    return getName();

  std::stringstream ret;
  ret << "(";
  std::string spacer = "";
  if (resolve && children.size() > 2)
    { // Sort of a hack...better way would be to actually add in new dummy nodes in the tree
      for (unsigned int i = 0; i < children.size() - 2; i++)
        {
          ret << spacer;
          ret << children[i]->printTreeStructure(delimeter, curr_gen, resolve);
          spacer = ",(";
        }
      for (unsigned int i = children.size() - 2; i < children.size(); i++)
        {
          ret << spacer;
          ret << children[i]->printTreeStructure(delimeter, curr_gen, resolve);
          spacer = ",";
        }
      for (int i = 0; i < (children.size() - 2); i++)
        {
          ret << ")";
        }
    }
  else
    {
      for (unsigned int i = 0; i < children.size(); i++)
        {
          ret << spacer;
          ret << children[i]->printTreeStructure(delimeter, curr_gen, resolve);
          spacer = ",";
        }
    }
  ret << (")" + delimeter + getName());

  return ret.str();
}

std::string
Node::printDistance(std::string delimeter, double mutationrate, int curr_gen,
    bool resolve)
{

  if (children.size() == 0)
    return getName();
  std::stringstream ret;
  ret << "(";
  std::string spacer = "";
  if (resolve && children.size() > 2) // Added by JMB -- 4.8.10
    { // Sort of a hack...better way would be to actually add in new dummy nodes in the tree
      for (int i = 0; i < (children.size() - 2); i++)
        {
          ret << spacer;
          ret << children[i]->printDistance(delimeter, mutationrate, curr_gen,
              resolve);
          ret << delimeter;
          ret << (children[i]->getDistance(*this) * mutationrate);
          spacer = ",(";
        }
      for (int i = (children.size() - 2); i < (int) children.size(); i++)
        {
          ret << spacer;
          ret << children[i]->printDistance(delimeter, mutationrate, curr_gen,
              resolve);
          ret << delimeter;
          ret << (children[i]->getDistance(*this) * mutationrate);
          spacer = ",";
        }
      for (int i = 0; i < (children.size() - 2); i++)
        {
          ret << "):0";
        }
    }
  else
    {
      for (unsigned int i = 0; i < children.size(); i++)
        {

          ret << spacer;
          ret << children[i]->printDistance(delimeter, mutationrate, curr_gen,
              resolve);
          ret << delimeter;
          ret << (children[i]->getDistance(*this) * mutationrate);
          spacer = ",";
        }
    }
  ret << ")";

  return ret.str();
}
