#include <math.h>
#include <stdio.h>

#define numRand 100000

#define __DIMSUM_STD_R 6371.0f
#define __DIMSUM_mode_northeast 0
#define __DIMSUM_mode_southeast 1
#define __DIMSUM_mode_northwest 2
#define __DIMSUM_mode_southwest 3
#define __DIMSUM_NODE_lat 0
#define __DIMSUM_NODE_lon 1
#define __DIMSUM_PARAMI_RANDINDEX 0
#define __DIMSUM_PARAMI_RANDMAX 1
#define __DIMSUM_PARAMI_SB_INDEX 2
#define __DIMSUM_PARAMI_HB_INDEX 3
#define __DIMSUM_PARAMI_GENERATION 4
#define __DIMSUM_PARAMI_NUMCHILDREN 5
#define __DIMSUM_XYFUNCTION_xsize 0
#define __DIMSUM_XYFUNCTION_ysize 1
#define __DIMSUM_XYFUNCTION_meta_length 4
#define __DIMSUM_XYFUNCTION_Fdim 3
#define PI 3.1415926536

double* randArray;
float* sb_DATA;
int* sb_META;
int* sb_SIZE;
float* hb_DATA;
int* hb_META;
int* hb_SIZE;

void setArrays(double* _randArray,float* _sb_DATA,int* _sb_META,int* _sb_SIZE,float* _hb_DATA,int* _hb_META,int* _hb_SIZE) {
	randArray = new double[numRand];
	for(int i=0;i<numRand;i++)
		randArray[i] = _randArray[i];
	sb_SIZE = new int[__DIMSUM_XYFUNCTION_Fdim];
	hb_SIZE = new int[__DIMSUM_XYFUNCTION_Fdim];
	for(int i=0;i<__DIMSUM_XYFUNCTION_Fdim;i++) {
		sb_SIZE[i] = _sb_SIZE[i];
		hb_SIZE[i] = _hb_SIZE[i];
	}

	int sb_length = sb_SIZE[0];
	int hb_length = hb_SIZE[0];
	sb_META = new int[__DIMSUM_XYFUNCTION_meta_length * sb_length];
	hb_META = new int[hb_length * __DIMSUM_XYFUNCTION_meta_length];
	for(int i=0;i<(__DIMSUM_XYFUNCTION_meta_length * sb_length);i++) {
		sb_META[i] = _sb_META[i];
	}

	for(int i=0;i<(hb_length * __DIMSUM_XYFUNCTION_meta_length);i++) {
			hb_META[i] = _hb_META[i];
		}
	sb_DATA = new float[sb_length * sb_SIZE[1] * sb_SIZE[2]];
	hb_DATA = new float[hb_length * hb_SIZE[1] * hb_SIZE[2]];
	for(int i=0;i<(sb_length * sb_SIZE[1] * sb_SIZE[2]);i++) {
		sb_DATA[i] = _sb_DATA[i];
		}
	for(int i=0;i<(hb_length * hb_SIZE[1] * hb_SIZE[2]);i++) {
			hb_DATA[i] = _hb_DATA[i];
			}
}

double abs(double x) {
	if (x < 0)
		return -x;
	return x;
}

double min(double x, double y) {
	if (x < y)
		return x;
	return y;
}

double mod(double y, double x)
{
	return (y - (x*floor(y/x)));
}

double geq (double lat1, double  sb_lat_bt) {
	if(lat1 >= sb_lat_bt)
		return 1;
	else //if (lat1 < sb_lat_bt)
		return -1;

}

double nextRand(int* _index) {

	if(_index[__DIMSUM_PARAMI_RANDINDEX]>=_index[__DIMSUM_PARAMI_RANDMAX]-10) {
		printf("nextRand(): index>numRand");

	}

	double r = randArray[_index[__DIMSUM_PARAMI_RANDINDEX]];
	_index[__DIMSUM_PARAMI_RANDINDEX]++;
	return r;
}

int getMetaIndex(int i, int type) {
	return i+type*sb_SIZE[0];
}



float sb_f(int i, int y,int x) {
	return sb_DATA[i+x*sb_SIZE[0]+y*sb_SIZE[0]*sb_SIZE[1]];
}


float hb_f(int i, int x,int y) {
	return hb_DATA[i+x*hb_SIZE[0]+y*hb_SIZE[0]*hb_SIZE[1]];
}


int sb_toX(int i,double lon, double minlon, double maxlon, int _size) {
	if( lon >= maxlon )
		return sb_META[getMetaIndex(i, _size)] -1;
	if( lon <= minlon )
		return 0;
	return (int)(sb_META[getMetaIndex(i, _size)]  * (lon-minlon)/(maxlon-minlon));
}

int hb_toX(int i,double lon, double minlon, double maxlon, int _size) {
	if( lon >= maxlon )
		return hb_META[getMetaIndex(i, _size)] -1;
	if( lon <= minlon )
		return 0;
	return (int)(hb_META[getMetaIndex(i, _size)]  * (lon-minlon)/(maxlon-minlon));
}

int getMode(double crs) {
	int mode = -1;
	if( crs >= 0 && crs <= PI/2 ) {		// JMB COMMENT -- DETERMINES IF COURSE IS NORTHEAST...OR IS THIS SOUTHEAST, SINCE INPUT MAPS ARE FLIPPED??
		mode = __DIMSUM_mode_northeast;
	}
	else if( crs > PI/2 && crs <= PI ) {			// JMB COMMENT -- Determines if course is southeast.  Note that this coordinate system is upside down relative to the standard.
		mode = __DIMSUM_mode_southeast;
	}
	else if( crs > PI && crs <= 3*PI/2 ) {  // JMB COMMENT -- DETERMINES IF COURSE IS SOUTHWEST
		mode = __DIMSUM_mode_southwest;
	}
	else if( crs > 3*PI/2 && crs <= 2*PI ) {   // JMB COMMENT -- 10.19.09 -- DETERMINES IF COURSE IS NORTHWEST
		mode = __DIMSUM_mode_northwest;
	}
	return mode;
}

int northORsouth(int i) {
	if(i == __DIMSUM_mode_northeast || i == __DIMSUM_mode_northwest)
		return 1;
	else return -1;
}

int eastORwest(int i) {
	if(i == __DIMSUM_mode_northeast || i == __DIMSUM_mode_southeast)
		return 1;
	else return -1;
}

double getI(double lat1, double minlat, double latspace, int dir) {
	double ilat;
	if(dir == 1)
		ilat = lat1 - mod((lat1-minlat),latspace) + latspace;
	else
		ilat = lat1 - mod((lat1-minlat),latspace);

	return ilat;
}

double toDeg(double data) { return data*180/PI;}

double dfromll(double lats, double lons, double latf, double lonf)			// JMB COMMENT -- 10.19.09 -- DETERMINES DISTANCE (D) AND COURSE (CRS) FROM ENDING LAT/LON
{
	double dlat = latf - lats;
	double dlon = lonf - lons;
	double a = pow(sin(dlat/2),2) + cos(lats)*cos(latf)*pow(sin(dlon/2),2);

	return __DIMSUM_STD_R * 2 * atan2(sqrt(a),sqrt(1-a));
}


/*private static double crsfromll( double lats,double lons, double latf,  double lonf)			// JMB COMMENT -- 10.19.09 -- DETERMINES DISTANCE (D) AND COURSE (CRS) FROM ENDING LAT/LON
{
	double dlon = lonf - lons;
	return mod(atan2(sin(dlon)*cos(latf),cos(lats)*sin(latf)-sin(lats)*cos(latf)*cos(dlon)),2*PI);
}*/


void llffromdcrs(double lld_lats,double lld_lons, double lld_latf,  double lld_lonf, double lld_d, double lld_crs)			// JMB COMMENT -- 10.19.09 -- DETERMINES ENDING LAT/LON FROM DISTANCE (D) AND COURSE (CRS)
{
	lld_latf = asin(sin(lld_lats)*cos(lld_d/__DIMSUM_STD_R)+cos(lld_lats)*sin(lld_d/__DIMSUM_STD_R)*cos(lld_crs));
	lld_lonf = lld_lons + atan2(sin(lld_crs)*sin(lld_d/__DIMSUM_STD_R)*cos(lld_lats),cos(lld_d/__DIMSUM_STD_R)-sin(lld_lats)*sin(lld_latf));
}


double latdfromlon3(double lat1,double lon1, double lat2,  double lon2,  double lon3o) // // Implementation by JMB from http://williams.best.vwh.net/avform.htm#Par  -- lat=atan(  (sin(lat1)*cosf(lat2)*sin(lon-lon2)-sin(lat2)*cosf(lat1)*sin(lon-lon1))  /  (cosf(lat1)*cosf(lat2)*sin(lon1-lon2))  )
{
	double lon3 = lon3o*PI/180;
	double latf = atan(((sin(lat1)*cos(lat2)*sin(lon3-lon2))-(sin(lat2)*cos(lat1)*sin(lon3-lon1)))/(cos(lat1)*cos(lat2)*sin(lon1-lon2)));

	return dfromll(lat1,lon1,latf, lon3);
}


double londfromlat3(double lat1,double lon1, double lat2,  double lon2, double lat3o) // in degrees		JMB COMMENT -- 10.19.09 -- FINDS LONGITUDE AT WHICH NEAREST LAT IS CROSSED W/
{														//								GREAT CIRCLE DISTANCE
	// from http://williams.best.vwh.net/avform.htm#Par
	double lat3 = lat3o * PI / 180.0f;
	double d;

	double A = sin(lat1)*cos(lat2)*cos(lat3)*sin(lon1-lon2);
	double B = sin(lat1)*cos(lat2)*cos(lat3)*cos(lon1-lon2) - cos(lat1)*sin(lat2)*cos(lat3);
	double C = cos(lat1)*cos(lat2)*sin(lat3)*sin(lon1-lon2);
	double lon = atan2(B,A);
	if (abs(C) >sqrt(pow(A,2) + pow(B,2))) {
		d = 10000000;
	} else if( lat1 == lat3 ) {
		d = 0;
	}
	else {
		double dlon = acos(C/sqrt(pow(A,2)+pow(B,2)));
		double lon3_1=(mod((lon1+dlon+lon+PI),(2*PI))-PI);
		double lon3_2=(mod((lon1-dlon+lon+PI),(2*PI))-PI);
		double lonf = 0;
		double lon3_1_d_D = dfromll(lat1, lon1,lat3,lon3_1);
		double lon3_2_d_D = dfromll(lat1, lon1,lat3, lon3_2);

		if( ((lon3_1 >= lon1 && lon3_1 <= lon2) || (lon3_1 <= lon1 && lon3_1 >= lon2)) && (lon3_1_d_D < lon3_2_d_D ))
			lonf = lon3_1;
		else
			lonf = lon3_2;

		// JMB COMMENT -- 10.19.09 -- SETS D AND CRS FOR RET
		d = dfromll(lat1, lon1,lat3,lonf );
	}

	return d;
}

double setX(double X, double x_bt, double prefix, double dXY){
	double r = X;
	if (((prefix > 0 && X < x_bt) || (prefix < 0 && X > x_bt)) && dXY == 0)
		r = x_bt + 0.00001*prefix;
	return r;
}


double ka2(double lat1,double minlat, double sb_latspace, int dirLat,int* _index) {
	double ilat = getI(lat1,minlat,sb_latspace, dirLat);
	if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line.
	{					// If so, pushes it barely off in a random direction.
		if (nextRand(_index) > 0.5)
			lat1 += 0.000000001;
		else
			lat1 -= 0.000000001;
		ilat = getI(lat1,minlat,sb_latspace, dirLat);
	}
	return ilat;
}

void migrate(double* node, double d, int* rm,int* parami, double* paramv, int id) {

	int sb = parami[__DIMSUM_PARAMI_SB_INDEX];
	int hb = parami[__DIMSUM_PARAMI_HB_INDEX];
	double lat1 = node[id*2+__DIMSUM_NODE_lat];
	double lon1 = node[id*2+__DIMSUM_NODE_lon];
	double minlat = paramv[0];
	double maxlat = paramv[1];
	double minlon = paramv[2];
	double maxlon = paramv[3];
	double sb_lonspace = paramv[4];
	double sb_latspace = paramv[5];
	double hb_lonspace = paramv[6];
	double hb_latspace = paramv[7];
	double step_d = 0.0001;
	double sb_lat_bt,sb_lon_bt,hb_lat_bt,hb_lon_bt,hb_dd,sb_dd;				// JMB -- Using this to keep track of lat/lon value for border reflections and adjusting inexact positions, if necessary
	double crs = nextRand(parami) * 2 * PI;	// Modified by JMB -- 4.5.10
	double lld_d, lld_lats,lld_latf=0, lld_lons,lld_lonf=0,lld_crs;

	while( d >= 0.000001 ) {
		//printf("FROMC lat1: %lf lon1: %lf d: %lf\n",lat1,lon1,d);
		lld_d = d;					// JMB COMMENT -- SETS D INTERNALLY IN LLD OBJECT
		//lld_r = 6371;				// radius of spherical earth in km (TODO: use value(# or units?) from xml)
		lld_lats = lat1 / 180.0f * PI; //setLatSDDeg(lld,lat1);		// JMB COMMENT -- CONVERTS LATITUDE FROM DECIMAL DEGREES TO RADIANS
		lld_lons = lon1 / 180.0f * PI;//	setLonSDDeg(lld,lon1);		// JMB COMMENT -- CONVERTS LONGITUDE FROM DECIMAL DEGREES TO RADIANS
		lld_crs = crs;				// JMB COMMENT -- SETS CRS INTERNALLY WITHIN LLD OBJECT
		lld_latf = asin(sin(lld_lats)*cos(lld_d/__DIMSUM_STD_R)+cos(lld_lats)*sin(lld_d/__DIMSUM_STD_R)*cos(lld_crs));// JMB COMMENT -- GETS ENDING LAT/LON FROM DISTANCE AND COURSE AND STORES INTERNALLY IN LLD OBJECT
		lld_lonf = lld_lons + atan2(sin(lld_crs)*sin(lld_d/__DIMSUM_STD_R)*cos(lld_lats),cos(lld_d/__DIMSUM_STD_R)-sin(lld_lats)*sin(lld_latf));

		hb_dd=100000000;
		sb_dd=100000000;
		int hb_dx=0,hb_dy=0,sb_dx=0,sb_dy=0;

		int mode = getMode(crs);

		sb_lat_bt = ka2(lat1,minlat,sb_latspace,northORsouth(mode),parami);
		double i1_d = londfromlat3(lld_lats,lld_lons,lld_latf,lld_lonf,sb_lat_bt);		// JMB COMMENT -- FINDS COORDINATES FOR NEAREST LAT BORDER CROSSING

		sb_lon_bt = ka2(lon1,minlon,sb_lonspace,eastORwest(mode),parami);
		double i2_d = latdfromlon3(lld_lats,lld_lons,lld_latf,lld_lonf,sb_lon_bt);		// JMB COMMENT -- FINDS COORDINATES FOR NEAREST LON BORDER CROSSING

		if( i1_d <= i2_d  && i1_d < d) {
			sb_dd = i1_d;
			sb_dy = northORsouth(mode);
		}
		else if( i2_d < d ) {
			sb_dd = i2_d;
			sb_dx = eastORwest(mode);
		}

		hb_lat_bt = ka2(lat1,minlat,hb_latspace,northORsouth(mode),parami);
		i1_d = londfromlat3(lld_lats,lld_lons,lld_latf,lld_lonf,hb_lat_bt);		// JMB COMMENT -- FINDS COORDINATES FOR NEAREST LAT BORDER CROSSING

		hb_lon_bt = ka2(lon1,minlon,hb_lonspace,eastORwest(mode),parami);
		i2_d = latdfromlon3(lld_lats,lld_lons,lld_latf,lld_lonf,hb_lon_bt);		// JMB COMMENT -- FINDS COORDINATES FOR NEAREST LON BORDER CROSSING

		if( i1_d <= i2_d  && i1_d < d) {
			hb_dd = i1_d;
			hb_dy = northORsouth(mode);
		}
		else if( i2_d < d ) {
			hb_dd = i2_d;
			hb_dx = eastORwest(mode);
		}

		if( d < min(sb_dd,hb_dd) ) {	// JMB COMMENT -- 10.20.09 -- DISPERSAL OCCURS BEFORE PIXEL BOUNDARY IS CROSSED,
			lld_d = d;
			//*********************** Fudging to keep poorly estimated positions (due to step_d alterations) from crossing border boundaries inadvertently ***********************
			// IS THIS CHECK NECESSARY AT THIS POINT IN THE LOOP?  PERHAPS NOT, BUT SHOULD MAKE SURE BEFORE REMOVING IT.

		   // node[id*2+__DIMSUM_NODE_lat] = toDeg(lld_latf);//getLatFDDeg(lld);
			//node[id*2+__DIMSUM_NODE_lon] = toDeg(lld_lonf);//getLonFDDeg(lld);
			lat1 = setX(toDeg(lld_latf),sb_lat_bt,geq(lat1,sb_lat_bt),0);
			lon1 = setX(toDeg(lld_lonf),sb_lon_bt,geq(lon1,sb_lon_bt),0);
			lat1 = setX(toDeg(lld_latf),hb_lat_bt,geq(lat1,hb_lat_bt),0);
			lon1 = setX(toDeg(lld_lonf),hb_lon_bt,geq(lon1,hb_lon_bt),0);
			d=0;
		}
		else if( abs(sb_dd-hb_dd) < step_d || sb_dd < hb_dd ) {	// JMB COMMENT -- 10.20.09 -- BOTH SOFT AND HARD PIXEL BOUNDARIES WILL BE CROSSED
			// both soft & hard must be checked at the same time -- but the order is up to you
			// I arbitrarily chose to check hard borders first
			if(abs(sb_dd-hb_dd) < step_d )
			if(  nextRand(parami) <= hb_f(hb,hb_toX(hb,lon1,minlon,maxlon,__DIMSUM_XYFUNCTION_xsize)+hb_dx,hb_toX(hb,lat1,minlat,maxlat,__DIMSUM_XYFUNCTION_ysize)+hb_dy) ) {	// JMB COMMENT -- FINDS HARD BORDER VALUE FOR NEXT PIXEL WITH RESPECT TO LONGITUDE AND CHECKS TO SEE IF INDIVIDUAL SURVIVES HARD BORDER CROSSING.
				rm[id] =  true; //continue childrenloop; // this exits the travel loop immediately, so the current child never gets added to the next generation
				return;
			}

			if( nextRand(parami) <= sb_f(sb,sb_toX(sb,lon1,minlon,maxlon,__DIMSUM_XYFUNCTION_xsize)+sb_dx, sb_toX(sb,lat1,minlat,maxlat,__DIMSUM_XYFUNCTION_ysize)+sb_dy) ) {
				// failed the soft border-- stop before border, reflect back, update d, and continue
				crs = nextRand(parami) * 2 * PI;// / 4+3*PI/4;
				sb_dy = 0;
				sb_dx = 0;
				lld_d = (sb_dd-step_d);
			} else {
				lld_d = sb_dd+step_d;
			}

			d-= lld_d;
			lld_latf = asin(sin(lld_lats)*cos(lld_d/__DIMSUM_STD_R)+cos(lld_lats)*sin(lld_d/__DIMSUM_STD_R)*cos(lld_crs));
			lld_lonf = lld_lons + atan2(sin(lld_crs)*sin(lld_d/__DIMSUM_STD_R)*cos(lld_lats),cos(lld_d/__DIMSUM_STD_R)-sin(lld_lats)*sin(lld_latf));
			// Fudging to keep poorly estimated positions from crossing boundaries
			lat1 = setX(toDeg(lld_latf),sb_lat_bt,geq(lat1,sb_lat_bt),sb_dy);
			lon1 = setX(toDeg(lld_lonf),sb_lon_bt,geq(lon1,sb_lon_bt),sb_dx);
		}
		else {
			if( nextRand(parami) <= hb_f(hb,hb_toX(hb,lon1,minlon,maxlon,__DIMSUM_XYFUNCTION_xsize)+hb_dx,hb_toX(hb,lat1,minlat,maxlat,__DIMSUM_XYFUNCTION_ysize)+hb_dy) ) {
				rm[id] =  true; //continue childrenloop; // this exits the travel loop immediately, so the current child never gets added to the next generation
				return;						// JMB -- Would this lead to pruning problems?
			} else {
				lld_d = hb_dd+step_d;
				d-=lld_d;
				//TODO: check lat1 = toDeg(lld_latf);
				//TODO: check lon1 = toDeg(lld_lonf);
				lat1 = setX(toDeg(lld_latf),hb_lat_bt,geq(lat1,hb_lat_bt),hb_dy);
				lon1 = setX(toDeg(lld_lonf),hb_lon_bt,geq(lon1,hb_lon_bt),hb_dx);
			}
		}
	}
	node[id*2+__DIMSUM_NODE_lat] = lat1;
	node[id*2+__DIMSUM_NODE_lon] = lon1;
	rm[id] =  false;
}
