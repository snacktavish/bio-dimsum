



public class lldataf
{
	public final static int _lats = 0;
	public final static int _lons = 1;
	public final static int _latf = 2;
	public final static int _lonf = 3;
	public final static int _d = 4;
	public final static int _crs = 5;
	public final static int _R = 6;
	public final static int _lldataSize = 7;
	public final static float _stdR = 6371f;
	

	public static double lats(double[] lldata) {return lldata[_lats]*180/PI;}
	public static double latf(double[] lldata) {return lldata[_latf]*180/PI;}
	public static double lons(double[] lldata) {return lldata[_lons]*180/PI;}
	public static double lonf(double[] lldata) {return lldata[_lonf]*180/PI;}
	public static double crs(double[] lldata) {return lldata[_crs]*180/PI;}
	
	public final static double PI = (double)Math.PI; 
	
	
	
	public static double min(double a, double b) {
		return Math.min(a, b);
	}
	
	public static double floorf(double f) {
		return (double)Math.floor(f);
	}
	
	public static double sinf(double f) {
		return (double)Math.sin(f);
	}
	
	public static double cosf(double f) {
		return (double)Math.cos(f);
	}
	
	public static double acosf(double f) {
		return (double)Math.acos(f);
	}
	
	public static double asinf(double f) {
		return (double)Math.asin(f);
	}

	public static double atan2f(double A , double B) {
		return (double)Math.atan2(A,B);
	}
	
	public static double atanf(double A ) {
		return (double)Math.atan(A);
	}
	
	public static double powf(double A, double e) {
		return (double)Math.pow(A, e);
	}
	
	public static double sqrtf(double A) {
		return (double)Math.sqrt(A);
	}
	
	public static double absf(double A) {
		return (double)Math.abs(A);
	}

	public static void setLatSDDeg(double[] lldata,double ddeg) //set start lat from decimal degrees
	{
		lldata[_lats] = ddeg / 180.0f * PI;
	}
	/*public void setLatFDDeg(double ddeg) //set final lat from decimal degrees
	{
		lldata[_latf] = ddeg / 180 * PI;
	}*/
	public static void setLonSDDeg(double[] lldata,double ddeg) //set start lon from decimal degrees
	{
		lldata[_lons] = ddeg / 180 * PI;
	}
	/*public void setLonFDDeg(double ddeg) //set final lon from decimal degrees
	{
		lldata[_lonf] = ddeg / 180 * PI;
	}
	/*public void setR(double Ro) //set radius of the earth (must be same units as d)
	{
		lldata[_R] = Ro;
	}*/
	/*public void setCrsDDeg(double crso) //set course from decimal degrees
	{
		lldata[_crs] = crso / 180 * PI;
	}
	public double getLatSDDeg() //get start lon from decimal degrees
	{
		return (lldata[_lats] / PI * 180);
	}*/
	/*public double getLonSDDeg() //get final lon from decimal degrees
	{
		return (lldata[_lons] / PI * 180);
	}*/	
	public static double getLatFDDeg(double[] lldata) //get start lon from decimal degrees
	{
		return (lldata[_latf] / PI * 180);
	}
	public static double getLonFDDeg(double[] lldata) //get final lon from decimal degrees
	{
		return (lldata[_lonf] / PI * 180);
	}
	/*public double getCrsDDeg() //get start lon from decimal degrees
	{
		return (lldata[_crs] / PI * 180);
	}
	public double getD() //get distance
	{
		return lldata[_d];
	}*/

	public static double mod(double y, double x)
	{
		return (y - (x*floorf(y/x)));
	}

	public static void dcrsfromll(double[] lldata)			// JMB COMMENT -- 10.19.09 -- DETERMINES DISTANCE (D) AND COURSE (CRS) FROM ENDING LAT/LON
	{
		double dlat = lldata[_latf] - lldata[_lats];
		double dlon = lldata[_lonf] - lldata[_lons];
		double a = powf(sinf(dlat/2),2) + cosf(lldata[_lats])*cosf(lldata[_latf])*powf(sinf(dlon/2),2);

		lldata[_d] = lldata[_R] * 2 * atan2f(Math.sqrt(a),Math.sqrt(1-a));
		// this.d = R * 2 * asinf(Math.sqrt(a)); //	Implemented by JMB from formula given at http://williams.best.vwh.net/avform.htm#Par
		// this.crs = this.mod(Math.atan2(sinf(dlon)*cosf(latf),cosf(lats)*sinf(latf)-sinf(lats)*cosf(latf)*cosf(dlon)),2*PI);  //	Implemented by JMB from formula given at http://williams.best.vwh.net/avform.htm#Par
		lldata[_crs] = mod(atan2f(sinf(dlon)*cosf(lldata[_latf]),cosf(lldata[_lats])*sinf(lldata[_latf])-sinf(lldata[_lats])*cosf(lldata[_latf])*cosf(dlon)),2*PI);
	}

	public static void llffromdcrs(double[] lldata)			// JMB COMMENT -- 10.19.09 -- DETERMINES ENDING LAT/LON FROM DISTANCE (D) AND COURSE (CRS)
	{
		lldata[_latf] = asinf(sinf(lldata[_lats])*cosf(lldata[_d]/lldata[_R])+cosf(lldata[_lats])*sinf(lldata[_d]/lldata[_R])*cosf(lldata[_crs]));
		lldata[_lonf] = lldata[_lons] + atan2f(sinf(lldata[_crs])*sinf(lldata[_d]/lldata[_R])*cosf(lldata[_lats]),cosf(lldata[_d]/lldata[_R])-sinf(lldata[_lats])*sinf(lldata[_latf]));
	}
	
	/*public void llffromdcrs()		// Implemented by JMB using formulae from http://williams.best.vwh.net/avform.htm#Par
	{
		this.latf = asinf(sinf(lats)*cosf(d/R)+cosf(lats)*sinf(d/R)*cosf(crs));
		double dlon = Math.atan2(sinf(crs)*sinf(d/R)*cosf(lats),cosf(d/R)-sinf(lats)*sinf(latf));
		this.lonf = this.mod(lons-dlon+PI,2*PI)-PI;
	}*/

	/*public lldata latdfromlon3(double lon3o) // in degrees
	{
		double lon3 = lon3o*PI/180;

		lldata ret = new lldata();
		ret.lats = this.lats;
		ret.lons = this.lons;
		ret.R = this.R;

		double lat1 = this.lats;
		double lon1 = this.lons;
		double lat2 = this.latf;
		double lon2 = this.lonf;

		double ea1 = sin(lat1-lat2)*sin((lon1+lon2)/2)*cosf((lon1-lon2)/2)-sin(lat1+lat2)*cosf((lon1+lon2)/2)*sin((lon1-lon2)/2);
		double ea2 = sin(lat1-lat2)*cosf((lon1+lon2)/2)*cosf((lon1-lon2)/2)+sin(lat1+lat2)*sin((lon1+lon2)/2)*sin((lon1-lon2)/2);
		double ea3 = cosf(lat1)*cosf(lat2)*sin(lon1-lon2);

		double den = sqrt( ea1*ea1 + ea2*ea2 + ea3*ea3 );
		ea1 = ea1 / den;
		ea2 = ea2 / den;
		ea3 = ea3 / den;


		lat1 = -89*PI/180; // this won't work if lats or latf within 1 deg of pole
		lon1 = lon3;
		lat2 = 89*PI/180;
		lon2 = lon3;

		double eb1 = sin(lat1-lat2)*sin((lon1+lon2)/2)*cosf((lon1-lon2)/2)-sin(lat1+lat2)*cosf((lon1+lon2)/2)*sin((lon1-lon2)/2);
		double eb2 = sin(lat1-lat2)*cosf((lon1+lon2)/2)*cosf((lon1-lon2)/2)+sin(lat1+lat2)*sin((lon1+lon2)/2)*sin((lon1-lon2)/2);
		double eb3 = cosf(lat1)*cosf(lat2)*sin(lon1-lon2);

		den = sqrt( eb1*eb1 + eb2*eb2 + eb3*eb3 );
		eb1 = eb1 / den;
		eb2 = eb2 / den;
		eb3 = eb3 / den;

		double e1 = ea2*eb3-eb2*ea3;
		double e2 = ea3*eb1-eb3*ea1;
		double e3 = ea1*eb2-ea2*eb1;

		double lat = atan2(e3,sqrt(e1*e1+e2*e2));
		double lon = atan2(-e2,e1);

		if( (this.lats <= lat && this.latf >= lat) || (this.lats >= lat && this.latf <= lat) ) {
			ret.latf = lat;
			ret.lonf = lon;
		}
		else {
			ret.latf = -lat;
			ret.lonf = (lon+PI);
		}

		ret.dcrsfromll();						

		return ret;

	}*/

	
	public static double[] latdfromlon3(double[] lldata, double lon3o) // // Implementation by JMB from http://williams.best.vwh.net/avform.htm#Par  -- lat=atan(  (sin(lat1)*cosf(lat2)*sin(lon-lon2)-sin(lat2)*cosf(lat1)*sin(lon-lon1))  /  (cosf(lat1)*cosf(lat2)*sin(lon1-lon2))  )
	{
		double lon3 = lon3o*PI/180;
		
		double[] ret =  new double[_lldataSize];
		ret[_lats] = lldata[_lats];
		ret[_lons] = lldata[_lons];
		ret[_R] = lldata[_R];
		
		double lat1 = lldata[_lats];
		double lon1 = lldata[_lons];
		double lat2 = lldata[_latf];
		double lon2 = lldata[_lonf];
		
		ret[_latf] = atanf(((sinf(lat1)*cosf(lat2)*sinf(lon3-lon2))-(sinf(lat2)*cosf(lat1)*sinf(lon3-lon1)))/(cosf(lat1)*cosf(lat2)*sinf(lon1-lon2)));
		
		ret[_lonf] = lon3;
		
		/*if( !((ret.latf >= lat1 && ret.latf <= lat2) || (ret.latf <= lat1 && ret.latf >= lat2)) ) // Checks to make sure predicted value is in range of movement...if not, sets d to arbirarily large value (1,000,000)
		{
			ret.d=1000000;
			return ret;
		}*/		
		
		lldataf.dcrsfromll(ret);
		
		return ret;
	}

	
	public static double[] londfromlat3(double[] lldata, double lat3o) // in degrees		JMB COMMENT -- 10.19.09 -- FINDS LONGITUDE AT WHICH NEAREST LAT IS CROSSED W/
	{														//								GREAT CIRCLE DISTANCE
		// from http://williams.best.vwh.net/avform.htm#Par
		double lat3 = lat3o * PI / 180;
		double[] ret = new double[_lldataSize];
		ret[_lats] = lldata[_lats];
		ret[_lons] = lldata[_lons];
		//ret.latf = this.latf;
		//ret.lonf = this.lonf;
		ret[_crs] = lldata[_crs];
		ret[_d] = lldata[_d];
		ret[_R] = lldata[_R];

		double lat1 = lldata[_lats];
		double lat2 = lldata[_latf];
		double lon1 = lldata[_lons];
		double lon2 = lldata[_lonf];

		double l12 = lon1-lon2;
		double A = sinf(lat1)*cosf(lat2)*cosf(lat3)*sinf(l12);
		double B = sinf(lat1)*cosf(lat2)*cosf(lat3)*cosf(l12) - cosf(lat1)*sinf(lat2)*cosf(lat3);
		double C = cosf(lat1)*cosf(lat2)*sinf(lat3)*sinf(l12);
		double lon = atan2f(B,A);
		if (absf(C) >sqrtf(powf(A,2) + powf(B,2)))
		{
			//System.out.println("never crosses");
			ret[_d] = 10000000;
		}
		else if( lat1 == lat3 ) {
			ret[_latf] = lat1;
			ret[_lonf] = lon1;
			ret[_d] = 0;
			ret[_crs] = 0;
		}
		else {
			double dlon = acosf(C/sqrtf(powf(A,2)+powf(B,2)));
			double lon3_1=(mod((lon1+dlon+lon+PI),(2*PI))-PI);
			double lon3_2=(mod((lon1-dlon+lon+PI),(2*PI))-PI);

			ret[_latf] = lat3;
			//System.out.println(lat3*180/PI);
			//System.out.println(lon3_1*180/PI);
			//System.out.println(lon3_2*180/PI);
			double[] lon3_1_d = new double[_lldataSize];
			lon3_1_d[_R] = _stdR;
			lon3_1_d[_lats] = ret[_lats];
			lon3_1_d[_lons] = ret[_lons];
			lon3_1_d[_latf] = ret[_latf];
			lon3_1_d[_lonf] = lon3_1;
			
			lldataf.dcrsfromll(lon3_1_d);
			
			double[] lon3_2_d = new double[_lldataSize];
			lon3_2_d[_R] = _stdR;
			lon3_2_d[_lats] = ret[_lats];
			lon3_2_d[_lons] = ret[_lons];
			lon3_2_d[_latf] = ret[_latf];
			lon3_2_d[_lonf] = lon3_2;
			lldataf.dcrsfromll(lon3_2_d);					
			
			if( ((lon3_1 >= lon1 && lon3_1 <= lon2) || (lon3_1 <= lon1 && lon3_1 >= lon2)) && (lon3_1_d[_d] < lon3_2_d[_d]) )
				ret[_lonf] = lon3_1;
			else
				ret[_lonf] = lon3_2;
			
			/*if( !((ret.lonf >= lon1 && ret.lonf <= lon2) || (ret.lonf <= lon1 && ret.lonf >= lon2)) )		// Checks to make sure predicted value is in range of movement...if not, sets d to arbirarily large value (1,000,000)
			{
				ret.d=1000000;
				return ret;
			}*/
				
			lldataf.dcrsfromll(ret);				// JMB COMMENT -- 10.19.09 -- SETS D AND CRS FOR RET
		}

		return ret;
	}
}
