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

#ifndef KERNEL_CUH_
#define KERNEL_CUH_

#define DIMSUM_NODE_lat 0
#define DIMSUM_NODE_lon 1
#define DIMSUM_PARAMI_RANDINDEX 8
#define DIMSUM_PARAMI_GENERATION 2
#define DIMSUM_PARAMI_NUMCHILDREN 3
#define DIMSUM_PARAMI_SB_XSIZE 4
#define DIMSUM_PARAMI_SB_YSIZE 5
#define DIMSUM_PARAMI_HB_XSIZE 6
#define DIMSUM_PARAMI_HB_YSIZE 7

extern "C"
  {
    void setArraysCPU(const float* sb_DATA, int sbx, int sby, const float* hb_DATA, int hbx, int hby);
    void setArraysGPU(const float* sb_DATA, int sbx, int sby, const float* hb_DATA, int hbx, int hby);
    void migrateCPU(double* children, int* rm, double* d, double* paramd, long* parami);
    void migrateGPU(double* children, int* rm, double* d, double* paramd, long* parami);
    void initGPU(const float* sb_DATA, int sbx, int sby, const float* hb_DATA, int hbx, int hby);
    void shutdownGPU();
  }
#endif /* KERNEL_CUH_ */
