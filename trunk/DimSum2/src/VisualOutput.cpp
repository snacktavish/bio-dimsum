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

#include "VisualOutput.h"
#include <iostream>
#include <cstdlib>
#include <cmath>
#include <string>

VisualOutput::VisualOutput(Config * config, int outputrate, int xsize,
    int ysize) :
  OutputFunction("visual", "bmp", outputrate), _config(config), _xsize(xsize),
      _ysize(ysize)
{
  _background = new float[_xsize * _ysize * 3];
  _outputImage = new float[_xsize * _ysize * 3];
  _bmpOutputImage = new BMPOutput(_xsize, _ysize);
  cc = NULL;
  sb = NULL;
  hb = NULL;
}

VisualOutput::~VisualOutput()
{
  delete[] _background;
  delete[] _outputImage;
  delete _bmpOutputImage;
}

int
VisualOutput::toX(double lon, double minlon, double maxlon, int xORy)
{
  int max = xORy == 0 ? _xsize : _ysize;
  if (lon >= maxlon)
    return max - 1;
  if (lon <= minlon)
    return 0;
  return (int) (max * (lon - minlon) / (maxlon - minlon));
}

void
VisualOutput::doOutput(std::string filename, std::vector<Node*> nodes,
    int generation)
{

  bool redraw = false;
  if (cc != _config->getCC(generation))
    {
      cc = _config->getCC(generation);
      redraw = true;
    }
  if (sb != _config->getSB(generation))
    {
      sb = _config->getSB(generation);
      redraw = true;
    }
  if (hb != _config->getHB(generation))
    {
      hb = _config->getHB(generation);
      redraw = true;
    }

  if (redraw)
    {
      double minlat = _config->getMinLat();
      double maxlat = _config->getMaxLat();
      double minlon = _config->getMinLon();
      double maxlon = _config->getMaxLon();
      for (int i = 0; i < _ysize; i++)
        {
          for (int j = 0; j < _xsize; j++)
            {
              double lon = (double) j / (double) _xsize * (maxlon - minlon)
                  + minlon;
              double lat = (double) i / (double) _ysize * (maxlat - minlat)
                  + minlat;
              _background[(i * _xsize + j) * 3 + 1] = cc->f(cc->toX(lon,
                  minlon, maxlon, 0), cc->toX(lat, minlat, maxlat, 1)) * 255.0;
              _background[(i * _xsize + j) * 3 + 2] = sb->f(sb->toX(lon,
                  minlon, maxlon, 0), sb->toX(lat, minlat, maxlat, 1)) * 255.0;
              _background[(i * _xsize + j) * 3 + 0] = hb->f(hb->toX(lon,
                  minlon, maxlon, 0), hb->toX(lat, minlat, maxlat, 1)) * 255.0;
            }
        }
    }

  for (int i = 0; i < _xsize * _ysize * 3; i++)
    {
      _outputImage[i] = _background[i];
    }

  int x = 0, y = 0;
  for (unsigned int i = 0; i < nodes.size(); i++)
    {
      y = toX(nodes[i]->lat, _config->getMinLat(), _config->getMaxLat(), 1);
      x = toX(nodes[i]->lon, _config->getMinLon(), _config->getMaxLon(), 0);
      _outputImage[(y * _xsize + x) * 3 + 2] = nodes[i]->_r;
      _outputImage[(y * _xsize + x) * 3 + 0] = nodes[i]->_g;
      _outputImage[(y * _xsize + x) * 3 + 1] = nodes[i]->_b;

      _outputImage[(y * _xsize + x + 1) * 3 + 2] = nodes[i]->_r;
      _outputImage[(y * _xsize + x + 1) * 3 + 0] = nodes[i]->_g;
      _outputImage[(y * _xsize + x + 1) * 3 + 1] = nodes[i]->_b;

      _outputImage[((y + 1) * _xsize + x) * 3 + 2] = nodes[i]->_r;
      _outputImage[((y + 1) * _xsize + x) * 3 + 0] = nodes[i]->_g;
      _outputImage[((y + 1) * _xsize + x) * 3 + 1] = nodes[i]->_b;

      _outputImage[((y + 1) * _xsize + x + 1) * 3 + 2] = nodes[i]->_r;
      _outputImage[((y + 1) * _xsize + x + 1) * 3 + 0] = nodes[i]->_g;
      _outputImage[((y + 1) * _xsize + x + 1) * 3 + 1] = nodes[i]->_b;
    }
  _bmpOutputImage->writeImage(filename.c_str(), _outputImage, 1.0);

}
