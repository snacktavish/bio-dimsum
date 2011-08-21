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

#include <math.h>
#include <cuda.h>
#include <cuda_runtime_api.h>
#include "kernel.cuh"
#include <stdio.h>
#include <iostream>

#define DIMSUM_mode_northeast 0
#define DIMSUM_mode_southeast 1
#define DIMSUM_mode_northwest 2
#define DIMSUM_mode_southwest 3
#define PI 3.1415926536
#define DIMSUM_STD_R 6371.0f // radius of spherical earth in km (TODO: use value(# or units?) from xml)
#if __CUDA_ARCH__ < 130
#define DIMSUM_MIN_D 0.001
#define DIMSUM_KA2_EPSILON 0.00001745329252
#define DIMSUM_EPSILON 0.0017453292520000
#define EPSILON 0.00001
#else
#define DIMSUM_MIN_D 0.000001
#define DIMSUM_KA2_EPSILON 0.00000000001745329252
#define DIMSUM_EPSILON 0.00000017453292520000
#define EPSILON 0.000000000001
#endif

texture<float, 2, cudaReadModeElementType> softborderDATA;
texture<float, 2, cudaReadModeElementType> hardborderDATA;

cudaArray* softborderArray;
cudaArray* hardborderArray;

float *_sb_DATA;
float *_hb_DATA;
int _sbx, _sby, _hbx, _hby;

extern "C"
  {

    /**
     * Converts x from radians to decimal degree
     */
    __host__ __device__
    double toDeg(double x)
      { return x*180.0f/PI;}

    /**
     * Converts x from decimal degree to radians
     */
    __host__ __device__
    double toRad(double x)
      { return x*PI/180.0f;}

    /**
     * Modulus function for double
     */
    __host__ __device__
    double mod(double y, double x)
      {
        return (y - (x*floor(y/x)));
      }

    /**
     * Return 1 if lat >= sb_lat_bt and -1 if not
     */
    __host__ __device__
    double geq (double lat1, double sb_lat_bt)
      {
        if(lat1 >= sb_lat_bt)
        return 1;
        else //if (lat1 < sb_lat_bt)
        return -1;
      }

    /*
     * Generates a random number
     */
    __host__ __device__
    float nextRand(long* seed, int id)
      {
        seed[DIMSUM_PARAMI_RANDINDEX+id] = (seed[DIMSUM_PARAMI_RANDINDEX+id] * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        float r = (int)(seed[DIMSUM_PARAMI_RANDINDEX+id] >> 24);
        r = r/((float) (1 << 24));
        //printf("%d %f\n", id, r);
        return r;
      }

    /**
     * Return the value of the softborder at (x,y)
     */
    __host__ __device__
    float sb_f(int x,int y)
      {
#ifdef __CUDA_ARCH__
        return tex2D(softborderDATA,x,y);
#else

        if (x < _sbx && y < _sby && x > -1 && y > -1)
          {
            return _sb_DATA[y * _sbx + x];
          }
        else
          {
            //std::cout << x << " " << y << std::endl;
            return 0;
          }
#endif
      }

    /**
     * Return the value of the hardborder at (x,y)
     */
    __host__ __device__
    float hb_f(int x,int y)
      {
#ifdef __CUDA_ARCH__
        return tex2D(hardborderDATA,x,y);
#else
        if (x < _hbx && y < _hby && x > -1 && y > -1)
          {
            return _hb_DATA[y * _hbx + x];
          }
        else
          {
            //std::cout << x << " " << y << std::endl;
            return 0;
          }
#endif
      }

    /**
     * Converts lng/lon to x/y coordinates of f
     */
    __host__ __device__
    int toX(double lon, double minlon, double maxlon, int img_size)
      {
        if( lon >= maxlon )
        return img_size -1;
        if( lon <= minlon )
        return 0;
        return (int)(img_size * (lon-minlon)/(maxlon-minlon));
      }

    /**
     * Returns the direction of the course
     */
    __host__ __device__
    int getMode(double crs)
      {
        int mode = -1;
        if( crs >= 0 && crs <= PI/2 )
          { // JMB COMMENT -- DETERMINES IF COURSE IS NORTHEAST...OR IS THIS SOUTHEAST, SINCE INPUT MAPS ARE FLIPPED??
            mode = DIMSUM_mode_northeast;
          }
        else if( crs > PI/2 && crs <= PI )
          { // JMB COMMENT -- Determines if course is southeast.  Note that this coordinate system is upside down relative to the standard.
            mode = DIMSUM_mode_southeast;
          }
        else if( crs > PI && crs <= 3*PI/2 )
          { // JMB COMMENT -- DETERMINES IF COURSE IS SOUTHWEST
            mode = DIMSUM_mode_southwest;
          }
        else if( crs > 3*PI/2 && crs <= 2*PI )
          { // JMB COMMENT -- 10.19.09 -- DETERMINES IF COURSE IS NORTHWEST
            mode = DIMSUM_mode_northwest;
          }
        return mode;
      }

    /**
     * Returns 1 if the direction is north and -1 if the direction is south
     */
    __host__ __device__
    int northORsouth(int i)
      {
        if(i == DIMSUM_mode_northeast || i == DIMSUM_mode_northwest)
        return 1;
        else return -1;
      }

    /**
     * Returns 1 if the direction is east and -1 if the direction is west
     */
    __host__ __device__
    int eastORwest(int i)
      {
        if(i == DIMSUM_mode_northeast || i == DIMSUM_mode_southeast)
        return 1;
        else return -1;
      }

    /**
     * Returns the next latspace on course
     */
    __host__ __device__
    double getI(double lat1, double minlat, double latspace, int dir)
      {
        double ilat;
        double mlat = lat1-minlat;
        if(dir == 1)
        ilat = minlat + (latspace*floor(mlat/latspace)) + latspace;
        else
        ilat = minlat + (latspace*floor(mlat/latspace));

        return ilat;
      }

    /**
     * JMB COMMENT -- 10.19.09 -- DETERMINES DISTANCE (D) FROM ENDING LAT/LON
     */
    __host__ __device__
    double dfromll(double lats, double lons, double latf, double lonf)
      {
        double dlat = latf - lats;
        double dlon = lonf - lons;
        double a = pow(sin(dlat/2),2) + cos(lats)*cos(latf)*pow(sin(dlon/2),2);

        return DIMSUM_STD_R * 2 * atan2(sqrt(a),sqrt(1-a));
      }

    /**
     * Determines distance to lon3 from lat1,lon1 over lat2,lon2
     */
    __host__ __device__
    double latdfromlon3(double lat1,double lon1, double lat2, double lon2, double lon3) // // Implementation by JMB from http://williams.best.vwh.net/avform.htm#Par  -- lat=atan(  (sin(lat1)*cosf(lat2)*sin(lon-lon2)-sin(lat2)*cosf(lat1)*sin(lon-lon1))  /  (cosf(lat1)*cosf(lat2)*sin(lon1-lon2))  )

      {
        double latf = atan(((sin(lat1)*cos(lat2)*sin(lon3-lon2))-(sin(lat2)*cos(lat1)*sin(lon3-lon1)))/(cos(lat1)*cos(lat2)*sin(lon1-lon2)));

        return dfromll(lat1,lon1,latf, lon3);
      }

    /**
     * Determines distance to lat3 from lat1,lon1 over lat2,lon2
     */
    __host__ __device__
    double londfromlat3(double lat1,double lon1, double lat2, double lon2, double lat3) // in degrees		JMB COMMENT -- 10.19.09 -- FINDS LONGITUDE AT WHICH NEAREST LAT IS CROSSED W/

      { //								GREAT CIRCLE DISTANCE
        // from http://williams.best.vwh.net/avform.htm#Par
        double d;

        double A = sin(lat1)*cos(lat2)*cos(lat3)*sin(lon1-lon2);
        double B = sin(lat1)*cos(lat2)*cos(lat3)*cos(lon1-lon2) - cos(lat1)*sin(lat2)*cos(lat3);
        double C = cos(lat1)*cos(lat2)*sin(lat3)*sin(lon1-lon2);
        double lon = atan2(B,A);
        if (abs(C) >sqrt(pow(A,2) + pow(B,2)))
          {
            d = 10000000;
          }
        else if( lat1 == lat3 )
          {
            d = 0;
          }
        else
          {
            double dlon = acos(C/sqrt(pow(A,2)+pow(B,2)));
            double lon3_1=(mod((lon1+dlon+lon+PI),(2*PI))-PI);
            double lon3_2=(mod((lon1-dlon+lon+PI),(2*PI))-PI);
            double lon3_1_d_D = dfromll(lat1, lon1,lat3,lon3_1);
            double lon3_2_d_D = dfromll(lat1, lon1,lat3, lon3_2);

            if( ((lon3_1 >= lon1 && lon3_1 <= lon2) || (lon3_1 <= lon1 && lon3_1 >= lon2)) && (lon3_1_d_D < lon3_2_d_D ))
            d = lon3_1_d_D;
            else
            d = lon3_2_d_D;

          }

        return d;
      }

    /**
     * Return X if X is not on x_bt and X + prefix*DIMSUM_EPSILON otherwise
     */
    __host__ __device__
    double setX(double X, double x_bt, double prefix, double dXY)
      {
        double r = X;
        if (((prefix > 0 && r < x_bt) || (prefix < 0 && r > x_bt)) && dXY == 0)
        r = x_bt + DIMSUM_EPSILON*prefix;
        return r;
      }

    /**
     * Returns the next latspace on course
     */
    __host__ __device__
    double nearestBorderCrossing(double lat1,double minlat, double sb_latspace, int dirLat,long* _index, int id)
      {
        double ilat = getI(lat1,minlat,sb_latspace, dirLat);
        if (abs(lat1-ilat) < EPSILON)//(lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line.

          { // If so, pushes it barely off in a random direction.
            if (nextRand(_index, id) > 0.5)
            lat1 += DIMSUM_KA2_EPSILON;
            else
            lat1 -= DIMSUM_KA2_EPSILON;
            ilat = getI(lat1,minlat,sb_latspace, dirLat);
          }
        return ilat;
      }

    __host__ __device__
    void migrate(double* node, double* dA, int* rm, long* parami, double* paramv, int id)
      {

        double d = dA[id];

        int sb_xsize = (int)parami[DIMSUM_PARAMI_SB_XSIZE];
        int sb_ysize = (int)parami[DIMSUM_PARAMI_SB_YSIZE];
        int hb_xsize = (int)parami[DIMSUM_PARAMI_HB_XSIZE];
        int hb_ysize = (int)parami[DIMSUM_PARAMI_HB_YSIZE];
        double lat1 = toRad(node[id*2+DIMSUM_NODE_lat]);
        double lon1 = toRad(node[id*2+DIMSUM_NODE_lon]);

        double minlat = toRad(paramv[0]);
        double maxlat = toRad(paramv[1]);
        double minlon = toRad(paramv[2]);
        double maxlon = toRad(paramv[3]);
        double sb_lonspace = toRad(paramv[4]);
        double sb_latspace = toRad(paramv[5]);
        double hb_lonspace = toRad(paramv[6]);
        double hb_latspace = toRad(paramv[7]);

        double step_d = 0.0001;
        double sb_lat_bt,sb_lon_bt,hb_lat_bt,hb_lon_bt,hb_dd,sb_dd; // JMB -- Using this to keep track of lat/lon value for border reflections and adjusting inexact positions, if necessary
        double crs = nextRand(parami,id) * 2 * PI; // Modified by JMB -- 4.5.10
        double lld_d, lld_latf=0, lld_lonf=0,lld_crs;

        while( d >= DIMSUM_MIN_D )
          {
            lld_d = d; // JMB COMMENT -- SETS D INTERNALLY IN LLD OBJECT
            lld_crs = crs; // JMB COMMENT -- SETS CRS INTERNALLY WITHIN LLD OBJECT
            lld_latf = asin(sin(lat1)*cos(lld_d/DIMSUM_STD_R)+cos(lat1)*sin(lld_d/DIMSUM_STD_R)*cos(lld_crs));// JMB COMMENT -- GETS ENDING LAT/LON FROM DISTANCE AND COURSE AND STORES INTERNALLY IN LLD OBJECT
            lld_lonf = lon1 + atan2(sin(lld_crs)*sin(lld_d/DIMSUM_STD_R)*cos(lat1),cos(lld_d/DIMSUM_STD_R)-sin(lat1)*sin(lld_latf));

            hb_dd=100000000;
            sb_dd=100000000;
            int hb_dx=0,hb_dy=0,sb_dx=0,sb_dy=0;

            int mode = getMode(crs);

            sb_lat_bt = nearestBorderCrossing(lat1,minlat,sb_latspace,northORsouth(mode),parami,id);
            double i1_d = londfromlat3(lat1,lon1,lld_latf,lld_lonf,sb_lat_bt); // JMB COMMENT -- FINDS COORDINATES FOR NEAREST LAT BORDER CROSSING

            sb_lon_bt = nearestBorderCrossing(lon1,minlon,sb_lonspace,eastORwest(mode),parami,id);
            double i2_d = latdfromlon3(lat1,lon1,lld_latf,lld_lonf,sb_lon_bt); // JMB COMMENT -- FINDS COORDINATES FOR NEAREST LON BORDER CROSSING

            if( i1_d <= i2_d && i1_d < d)
              {
                sb_dd = i1_d;
                sb_dy = northORsouth(mode);
              }
            else if( i2_d < d )
              {
                sb_dd = i2_d;
                sb_dx = eastORwest(mode);
              }

            hb_lat_bt = nearestBorderCrossing(lat1,minlat,hb_latspace,northORsouth(mode),parami,id);
            i1_d = londfromlat3(lat1,lon1,lld_latf,lld_lonf,hb_lat_bt); // JMB COMMENT -- FINDS COORDINATES FOR NEAREST LAT BORDER CROSSING

            hb_lon_bt = nearestBorderCrossing(lon1,minlon,hb_lonspace,eastORwest(mode),parami,id);
            i2_d = latdfromlon3(lat1,lon1,lld_latf,lld_lonf,hb_lon_bt); // JMB COMMENT -- FINDS COORDINATES FOR NEAREST LON BORDER CROSSING

            if( i1_d <= i2_d && i1_d < d)
              {
                hb_dd = i1_d;
                hb_dy = northORsouth(mode);
              }
            else if( i2_d < d )
              {
                hb_dd = i2_d;
                hb_dx = eastORwest(mode);
              }

            if( d < min(sb_dd,hb_dd) )
              { // JMB COMMENT -- 10.20.09 -- DISPERSAL OCCURS BEFORE PIXEL BOUNDARY IS CROSSED,
                lld_d = d;
                //*********************** Fudging to keep poorly estimated positions (due to step_d alterations) from crossing border boundaries inadvertently ***********************
                // IS THIS CHECK NECESSARY AT THIS POINT IN THE LOOP?  PERHAPS NOT, BUT SHOULD MAKE SURE BEFORE REMOVING IT.

                lat1 = setX(lld_latf,sb_lat_bt,geq(lat1,sb_lat_bt),0);
                lon1 = setX(lld_lonf,sb_lon_bt,geq(lon1,sb_lon_bt),0);
                lat1 = setX(lld_latf,hb_lat_bt,geq(lat1,hb_lat_bt),0);
                lon1 = setX(lld_lonf,hb_lon_bt,geq(lon1,hb_lon_bt),0);
                d=0;
              }
            else if( abs(sb_dd-hb_dd) < step_d || sb_dd < hb_dd )
              { // JMB COMMENT -- 10.20.09 -- BOTH SOFT AND HARD PIXEL BOUNDARIES WILL BE CROSSED
                // both soft & hard must be checked at the same time -- but the order is up to you
                // I arbitrarily chose to check hard borders first
                if(abs(sb_dd-hb_dd) < step_d )
                if( nextRand(parami,id) <= hb_f(toX(lon1,minlon,maxlon,hb_xsize)+hb_dx,toX(lat1,minlat,maxlat,hb_ysize)+hb_dy) )
                  { // JMB COMMENT -- FINDS HARD BORDER VALUE FOR NEXT PIXEL WITH RESPECT TO LONGITUDE AND CHECKS TO SEE IF INDIVIDUAL SURVIVES HARD BORDER CROSSING.
                    rm[id] = 1; //continue childrenloop; // this exits the travel loop immediately, so the current child never gets added to the next generation
                    return;
                  }

                if( nextRand(parami,id) <= sb_f(toX(lon1,minlon,maxlon,sb_xsize)+sb_dx, toX(lat1,minlat,maxlat,sb_ysize)+sb_dy) )
                  {
                    // failed the soft border-- stop before border, reflect back, update d, and continue
                    crs = nextRand(parami,id) * 2 * PI;// / 4+3*PI/4;
                    sb_dy = 0;
                    sb_dx = 0;
                    lld_d = (sb_dd-step_d);
                  }
                else
                  {
                    lld_d = sb_dd+step_d;
                  }

                d-= lld_d;
                lld_latf = asin(sin(lat1)*cos(lld_d/DIMSUM_STD_R)+cos(lat1)*sin(lld_d/DIMSUM_STD_R)*cos(lld_crs));
                lld_lonf = lon1 + atan2(sin(lld_crs)*sin(lld_d/DIMSUM_STD_R)*cos(lat1),cos(lld_d/DIMSUM_STD_R)-sin(lat1)*sin(lld_latf));
                // Fudging to keep poorly estimated positions from crossing boundaries
                lat1 = setX(lld_latf,sb_lat_bt,geq(lat1,sb_lat_bt),sb_dy);
                lon1 = setX(lld_lonf,sb_lon_bt,geq(lon1,sb_lon_bt),sb_dx);
              }
            else
              {
                if( nextRand(parami,id) <= hb_f(toX(lon1,minlon,maxlon,hb_xsize)+hb_dx,toX(lat1,minlat,maxlat,hb_ysize)+hb_dy) )
                  {
                    rm[id] = 1; //continue childrenloop; // this exits the travel loop immediately, so the current child never gets added to the next generation
                    return; // JMB -- Would this lead to pruning problems?
                  }
                else
                  {
                    lld_d = hb_dd+step_d;
                    d-=lld_d;
                    lat1 = setX(lld_latf,hb_lat_bt,geq(lat1,hb_lat_bt),hb_dy);
                    lon1 = setX(lld_lonf,hb_lon_bt,geq(lon1,hb_lon_bt),hb_dx);
                  }
              }
          }
        node[id*2+DIMSUM_NODE_lat] = toDeg(lat1);
        node[id*2+DIMSUM_NODE_lon] = toDeg(lon1);
        //	printf("node: %lf %lf\n", node[id*2+DIMSUM_NODE_lat],node[id*2+DIMSUM_NODE_lon]);
        rm[id] = 0;
      }

    __global__
    void migrateGLOBAL(double* children, int* rm, double* d, double* paramd, long* parami, int size)
      {
        int id = blockIdx.x*blockDim.x+threadIdx.x;
        /*if(id == 0) {
         for(int i=0;i<size+8;i++)
         printf("%d ",parami[i]);
         printf("\n");
         }*/
        if(id < size)
          {
            //printf("test %d %d\n",id, size);
            migrate(children, d, rm ,parami,paramd,id);
          }

      }

    void setArraysCPU(const float* sb_DATA, int sbx, int sby, const float* hb_DATA, int hbx, int hby)
      {
        _sbx = sbx;
        _sby = sby;
        _hbx = hbx;
        _hby = hby;
        _sb_DATA = new float[sbx*sby];
        _hb_DATA = new float[hbx*hby];
        for(int i=0;i<sbx*sby;i++)
        _sb_DATA[i] = sb_DATA[i];
        for(int i=0;i<hbx*hby;i++)
        _hb_DATA[i] = hb_DATA[i];
      }

    cudaArray* cp2Texture(const float* sb_DATA, int sbx, int sby, texture<float, 2, cudaReadModeElementType>& tex)
      {
        cudaChannelFormatDesc channelDesc = cudaCreateChannelDesc(32, 0, 0, 0, cudaChannelFormatKindFloat);
        cudaArray* cu_array;
        cudaMallocArray( &cu_array, &channelDesc, sbx, sby );

        cudaMemcpy2DToArray( cu_array, 0,0, sb_DATA, sbx*sizeof(float), sbx*sizeof(float), sby, cudaMemcpyHostToDevice);

        tex.addressMode[0] = cudaAddressModeClamp;
        tex.addressMode[1] = cudaAddressModeClamp;
        tex.filterMode = cudaFilterModePoint;
        tex.normalized = false;

        cudaBindTextureToArray( tex, cu_array, channelDesc);
        return cu_array;
      }

    void setArraysGPU(const float* sb_DATA, int sbx, int sby, const float* hb_DATA, int hbx, int hby)
      {
        cudaFreeArray(softborderArray);
        softborderArray = cp2Texture(sb_DATA,sbx,sby,softborderDATA);
        cudaFreeArray(hardborderArray);
        hardborderArray = cp2Texture(hb_DATA,hbx,hby,hardborderDATA);
      }

    void migrateGPU(double* children, int* rm, double* d, double* paramd, long* parami)
      {

        int size = parami[DIMSUM_PARAMI_NUMCHILDREN];
        int block_size =128;
        int block_num = (int)ceil((double)size/(double)block_size);

        //CHILDREN
        double* d_children = NULL;
        cudaMalloc( (void**) &d_children, sizeof(double)*size*2);
        cudaMemcpy( d_children,children, sizeof(double)*size*2, cudaMemcpyHostToDevice);

        //RM
        int* d_rm = NULL;
        cudaMalloc( (void**) &d_rm, sizeof(int)*size);
        cudaMemcpy(d_rm, rm, sizeof(int)*size, cudaMemcpyHostToDevice);

        //d
        double* d_d = NULL;
        cudaMalloc( (void**) &d_d, sizeof(double)*size);
        cudaMemcpy(d_d, d, sizeof(double)*size, cudaMemcpyHostToDevice);

        //paramd
        double* d_paramd = NULL;
        cudaMalloc( (void**) &d_paramd, sizeof(double)*8);
        cudaMemcpy(d_paramd, d, sizeof(double)*8, cudaMemcpyHostToDevice);

        //parami
        long* d_parami = NULL;
        cudaMalloc( (void**) &d_parami, sizeof(long)*(size+DIMSUM_PARAMI_RANDINDEX));
        cudaMemcpy( d_parami, parami, sizeof(long)*(size+DIMSUM_PARAMI_RANDINDEX), cudaMemcpyHostToDevice);

        migrateGLOBAL<<< block_num, block_size>>>( d_children, d_rm, d_d, d_paramd,d_parami, size);
        //cuCtxSynchronize();

#if CUDART_VERSION >= 4000
        cudaDeviceSynchronize();
#else
        cudaThreadSynchronize();
#endif

        cudaMemcpy( children,d_children, sizeof(double)*size*2, cudaMemcpyDeviceToHost);
        cudaMemcpy( rm,d_rm, sizeof(int)*size, cudaMemcpyDeviceToHost);

        cudaFree(d_parami);
        cudaFree(d_paramd);
        cudaFree(d_d);
        cudaFree(d_rm);
        cudaFree(d_children);

      }

    void migrateCPU(double* children, int* rm, double* d, double* paramd, long* parami)
      {
        for(int id =0;id < parami[DIMSUM_PARAMI_NUMCHILDREN];id++)
          {
            migrate(children, d, rm ,parami,paramd,id);
          }

      }

    void initGPU(const float* sb_DATA, int sbx, int sby, const float* hb_DATA, int hbx, int hby)
      {
        int devID;
        cudaDeviceProp props;

        cudaGetDevice(&devID);
        cudaGetDeviceProperties(&props, devID);
        printf("Device %d: \"%s\" with Compute %d.%d capability\n\n",
            devID, props.name, props.major, props.minor);

        softborderArray = cp2Texture(sb_DATA,sbx,sby,softborderDATA);
        hardborderArray = cp2Texture(hb_DATA,hbx,hby,hardborderDATA);
      }

    void shutdownGPU()
      {
        cudaFreeArray(softborderArray);
        cudaFreeArray(hardborderArray);
      }
  }

