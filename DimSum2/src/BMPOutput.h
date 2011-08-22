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

#ifndef BMPOUTPUT_H_
#define BMPOUTPUT_H_
#include "BMPHeader.h"

class BMPOutput
{

public:
  BMPOutput(int xsize, int ysize);

  virtual
  ~BMPOutput();

  /**
   * Writes the image as a bmp file with the name filename to the hd
   */
  void
  writeImage(const char* filename, float* image, double maxValue);

private:

  bmpfile_magic *_bitmapMagic;
  bmpfile_header *_bitmapFileHeader;
  bmpinfo_header *_bitmapInfoHeader;
};

#endif /* BMPOUTPUT_H_ */
