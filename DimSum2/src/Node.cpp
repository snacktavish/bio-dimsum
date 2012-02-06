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
int Node::numLoci = 1;
float *Node::_recombinationRate = NULL;


Node::~Node()
{
	delete[] _motherChrom;
	delete[] _fatherChrom;
}

Node::Node(int gen, Node *par, Node *father)
{
  unique = Node::lastUnique++;
  generation = gen;
  parent = par;
  _father = father;
  lat = par->lat;
  lon = par->lon;
  _r = par->_r;
  _g = par->_g;
  _b = par->_b;
  _male = (float)rand()/(float)RAND_MAX < 0.5f;
  _motherChrom = new int[numLoci];
  _fatherChrom = new int[numLoci];
  bool lastAllelMale = true;
  bool recombine = false;

  for(int i=0;i<Node::numLoci;i++) {
	  recombine = (float)rand()/(float)RAND_MAX < _recombinationRate[i];
	  if((recombine && lastAllelMale) || (!recombine && !lastAllelMale)) {
		  _motherChrom[i] = par->_motherChrom[i];
		  lastAllelMale = false;
	  } else {
		  _motherChrom[i] = par->_fatherChrom[i];
		  lastAllelMale = true;
	  }
  }


  lastAllelMale = false;
  recombine = false;

  for(int i=0;i<Node::numLoci;i++) {
 	  recombine = (float)rand()/(float)RAND_MAX < _recombinationRate[i];
 	  if((recombine && lastAllelMale) || (!recombine && !lastAllelMale)) {
 		  _fatherChrom[i] = _father->_motherChrom[i];
 		  lastAllelMale = false;
 	  } else {
 		  _fatherChrom[i] = _father->_fatherChrom[i];
 		  lastAllelMale = true;
 	  }
   }



}

Node::Node(Node *from, int genNum, std::vector<Node*> newLastGen)
{
		unique = from->unique;
		generation = from->generation;


		lat = from->lat;
		lon = from->lon;
		  _r = from->_r;
		  _g = from->_g;
		  _b = from->_b;

			parent = NULL;
		_father = NULL;
		  _male = from->_male;
		  _motherChrom = new int[numLoci];
		  _fatherChrom = new int[numLoci];

		  for(int i=0;i<numLoci;i++) {
			  _motherChrom[i] = from->_motherChrom[i];
			  _fatherChrom[i] = from->_fatherChrom[i];
		  }


			for (int i=0; i<from->children.size(); i++)
			{
				children.push_back(new Node(from->children[i],genNum,newLastGen ) );	// Recursively adding children, as needed
			}

		for (int i=0; i<children.size(); i++)	// Setting parent for all newly created children
		{
			children[i]->parent = this;
		}
		if (generation == genNum)	// Populating new ArrayList of nodes in the final generation
			newLastGen.push_back(this);
}

Node::Node(double lat0, double lon0, int r, int g, int b, bool male, std::vector<int> motherL, std::vector<int> fatherL)
{
  if (Node::lastUnique == 0)
    Node::lastUnique = 1;
  unique = Node::lastUnique++;
  generation = 1;
  parent = NULL;
  _father = NULL;
  lat = lat0;
  lon = lon0;
  _r = r;
  _g = g;
  _b = b;
  _male = male;
  _motherChrom = new int[numLoci];
  _fatherChrom = new int[numLoci];
  for(int i=0;i<numLoci;i++) {
	  _motherChrom[i] = motherL[i];
	  _fatherChrom[i] = fatherL[i];
  }
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
Node::getString()
{
  std::stringstream tmp;
  if(_male)
  tmp << generation << "_M" << unique << "@" << lat << "," << lon;
  else
	  tmp << generation << "_F" << unique << "@" << lat << "," << lon;

  return tmp.str();
}


double
Node::getDistance(Node &parent)
{
  return (double) (this->generation - parent.generation);
}

std::string Node::printAlleleTree(std::string delimeter, int curr_gen, int loci, int allel) {
	if (_motherChrom[loci] == allel || _fatherChrom[loci] == allel) {

		if (children.size() == 0) {
			return getName();
		}

		std::stringstream ret;
		ret << "(";
		for (unsigned int i = 0; i < children.size(); i++) {
			std::string tmp = children[i]->printAlleleTree(delimeter, curr_gen,
					loci, allel);

			if(tmp != "") {
				ret << tmp;
				if(i <children.size() -1 ) //todo
					ret<< ",";
			}

		}
		ret << (")" + delimeter + getName());

		return ret.str();
	} else
		return "";
}

std::string
Node::printTreeStructure(std::string delimeter, int curr_gen, bool resolve)
{

  if (children.size() == 0 || _male)
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

  if (children.size() == 0 || _male)
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
