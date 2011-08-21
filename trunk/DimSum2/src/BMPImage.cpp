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

#include "BMPImage.h"
#include <cstdio>
#include <cmath>

BMPImage::BMPImage(const char* filename, double maxValue, int color)
{
  FILE *file;
  bmpfile_magic bitmapMagic;
  bmpfile_header bitmapFileHeader;
  bmpinfo_header bitmapInfoHeader;

  int res;
  unsigned char *_image;
  float* _f;
  file = fopen(filename, "rb");
  res = fread(&bitmapMagic, sizeof(bmpfile_magic), 1, file);
  res = fread(&bitmapFileHeader, sizeof(bmpfile_header), 1, file);
  res = fread(&bitmapInfoHeader, sizeof(bmpinfo_header), 1, file);

  int xlength = (bitmapInfoHeader.width * 3 + 3) & ~3;
  unsigned int length = xlength * bitmapInfoHeader.height;

  _image = new unsigned char[length];

  res = fread(_image, sizeof(unsigned char), length, file);

  int q = 0, w = 0, e = 0;

  _f = new float[bitmapInfoHeader.width * bitmapInfoHeader.height];
  int x = 0, y = 0;

  for (int j = 0; j < bitmapInfoHeader.height; j++)
    {

      for (int i = 0; i < xlength; i += 3)
        {
          if (i < bitmapInfoHeader.width * 3)
            {
              if ((int) _image[j * xlength + i] != 0)
                q++;
              if ((int) _image[(j * xlength + i + 1)] != 0)
                w++;
              if ((int) _image[(j * xlength + i + 2)] != 0)
                e++;
              //std::cout << i<< " "<<j<< std::endl;
              _f[y * bitmapInfoHeader.width + x] = (float) _image[(j * xlength
                  + i + color)] * maxValue / 255.0;
              //std::cout << i<< " "<<j<< std::endl;
              x++;
            }
          /*	tmp = _image[i];
           _image[i] = _image[i + 2];
           _image[i + 2] = tmp;*/
        }
      y++;
      x = 0;
    }

  delete[] _image;

  f = _f;
  maxX = bitmapInfoHeader.width;
  maxY = bitmapInfoHeader.height;
  fclose(file);
}

BMPImage::~BMPImage()
{
  delete[] f;
}

