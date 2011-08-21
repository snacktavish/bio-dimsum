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

#include "BMPOutput.h"
#include <cstdio>
#include <cmath>

BMPOutput::BMPOutput(int xsize, int ysize)
{
  _bitmapMagic = new bmpfile_magic;
  _bitmapFileHeader = new bmpfile_header;
  _bitmapInfoHeader = new bmpinfo_header;

  _bitmapMagic->magic[0] = 0x42;
  _bitmapMagic->magic[1] = 0x4D;

  int xlength = (xsize * 3 + 3) & ~3;

  _bitmapFileHeader->filesize = xlength * ysize + 54;
  _bitmapFileHeader->creator1 = 0;
  _bitmapFileHeader->creator2 = 0;
  _bitmapFileHeader->offset = 54;

  _bitmapInfoHeader->headersize = 40;
  _bitmapInfoHeader->width = xsize;
  _bitmapInfoHeader->height = ysize;
  _bitmapInfoHeader->nplanes = 1;
  _bitmapInfoHeader->bitperpixel = 24;
  _bitmapInfoHeader->compression = 0;
  _bitmapInfoHeader->imagesize = xlength * ysize;
  _bitmapInfoHeader->hres = 1181;
  _bitmapInfoHeader->vres = 1181;
  _bitmapInfoHeader->ncolors = 0;
  _bitmapInfoHeader->importantcolors = 0;

}

BMPOutput::~BMPOutput()
{
  delete _bitmapInfoHeader;
  delete _bitmapFileHeader;
  delete _bitmapMagic;
}

void
BMPOutput::writeImage(const char* filename, float* image, double maxValue)
{
  //std::cout << "writing " << filename << std::endl;
  FILE *file;
  file = fopen(filename, "wb");
  int res;

  res = fwrite(_bitmapMagic, sizeof(bmpfile_magic), 1, file);
  res = fwrite(_bitmapFileHeader, sizeof(bmpfile_header), 1, file);
  res = fwrite(_bitmapInfoHeader, sizeof(bmpinfo_header), 1, file);

  int xlength = (_bitmapInfoHeader->width * 3 + 3) & ~3;
  unsigned int length = xlength * _bitmapInfoHeader->height;

  unsigned char *outputImage = new unsigned char[length];
  int i = 0, x = 0, y = 0;

  for (int j = 0; j < _bitmapInfoHeader->height; j++, y++)
    {
      for (i = 0, x = 0; i < xlength; i++)
        {
          if (i < _bitmapInfoHeader->width * 3)
            {
              outputImage[(j * xlength + i)] = round(image[y
                  * _bitmapInfoHeader->width * 3 + x] / maxValue);
              x++;
            }
          else
            {
              outputImage[(j * xlength + i)] = 0;
            }

        }
    }

  res = fwrite(outputImage, sizeof(unsigned char), length, file);
  fclose(file);
  delete[] outputImage;
}
