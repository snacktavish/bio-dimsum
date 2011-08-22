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

#include "XYFunction.h"
#include <cstdio>
#include <iostream>

XYFunction::~XYFunction()
{
  delete image;
}

XYFunction::XYFunction(std::string imagename, double maxValue, int sgen,
    int egen, int color) :
  startgeneration(sgen), endgeneration(egen), _fmax(maxValue)
{

  image = new BMPImage(imagename.c_str(), maxValue, color);
  _f = image->getF();
  maxX = image->getMaxX();
  maxY = image->getMaxY();
}

double
XYFunction::f(int x, int y)
{
  if (x < maxX && y < maxY && x > -1 && y > -1)
    {
      return _f[y * maxX + x];
    }
  else
    {
      std::cerr << "XYFunction::f(): " << x << " " << y << std::endl;
      return 0;
    }
}

int
XYFunction::toX(double lon, double minlon, double maxlon, int xORy)
{
  int max = xORy == 0 ? maxX : maxY;
  if (lon >= maxlon)
    return max - 1;
  if (lon <= minlon)
    return 0;
  return (int) (max * (lon - minlon) / (maxlon - minlon));
}

