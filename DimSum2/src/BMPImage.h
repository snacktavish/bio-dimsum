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

#ifndef BMPIMAGE_H_
#define BMPIMAGE_H_
#include "BMPHeader.h"

class BMPImage
{

public:

  BMPImage(const char* filename, double maxValue, int color);
  virtual
  ~BMPImage();

  const float*
  getF()
  {
    return f;
  }

  int
  getMaxX()
  {
    return maxX;
  }

  int
  getMaxY()
  {
    return maxY;
  }

private:
  float *f;
  int maxX, maxY;

};

#endif /* BMPIMAGE_H_ */
