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

#ifndef BMPHEADER_H_
#define BMPHEADER_H_

typedef struct  {
  unsigned char magic[2];
}bmpfile_magic;

typedef struct  {
  unsigned int filesize;
  unsigned short creator1;
  unsigned short creator2;
  unsigned int offset;
}bmpfile_header;

typedef struct {
  unsigned int headersize;
  int width;
  int height;
  unsigned short nplanes;
  unsigned short bitperpixel;
  unsigned int compression;
  unsigned int imagesize;
  int hres;
  int vres;
  unsigned int ncolors;
  unsigned int importantcolors;
} bmpinfo_header;

#endif /* BMPHEADER_H_ */
