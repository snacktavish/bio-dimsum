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

#ifndef VISUALOUTPUT_H_
#define VISUALOUTPUT_H_
#include "OutputFunction.h"
#include "BMPOutput.h"
#include "Config.h"
#include "XYFunction.h"

class VisualOutput : public OutputFunction
{
public:
  VisualOutput(Config * config, int outputrate, int xsize, int ysize);

  virtual
  ~VisualOutput();

protected:

  virtual void
  doOutput(std::string filename, std::vector<Node*> nodes, int generation);

private:
  Config *_config;
  int _xsize, _ysize;

  XYFunction* cc;
  XYFunction* sb;
  XYFunction* hb;

  float* _background;
  float* _outputImage;

  BMPOutput* _bmpOutputImage;

  int
  toX(double lon, double minlon, double maxlon, int xORy);

};

#endif /* VISUALOUTPUT_H_ */
