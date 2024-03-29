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

#ifndef XYFUNCTION_H_
#define XYFUNCTION_H_
#include <string>
#include "BMPImage.h"

class XYFunction
{
public:
  //XYFunction(std::string filename, double setborder);
  XYFunction(std::string imagename, double maxValue, int sgen, int egen,
      int color);


  virtual
  ~XYFunction();

  /**
   * Return the value of the XYFunction at (x,y)
   */
  double
  f(int x, int y);

  /**
   * Transforms lat/lon on x/y xORy should be 0 for x and 1 for y
   */
  int
  toX(double lon, double minlon, double maxlon, int xORy);

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

  double
  fmax()
  {
    return _fmax;
  }

  const float*
  getF() const
  {
    return _f;
  }

  bool checkGeneration(int generation) {
    return (startgeneration <= generation
        && endgeneration >= generation);
  }

private:
  int startgeneration, endgeneration;
  const float* _f;
  int maxX, maxY;
  double _fmax;
  BMPImage *image;


  float*
  readBMP(const char* filename, double maxValue, int color);
};

#endif /* XYFUNCTION_H_ */
