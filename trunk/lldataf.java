import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;



public class lldataf
{
	public final static double PI = (double)Math.PI; 
	
	public double floorf(double f) {
		return (double)Math.floor(f);
	}
	
	public double sinf(double f) {
		return (double)Math.sin(f);
	}
	
	public double cosf(double f) {
		return (double)Math.cos(f);
	}
	
	public double acosf(double f) {
		return (double)Math.acos(f);
	}
	
	public double asinf(double f) {
		return (double)Math.asin(f);
	}

	public double atan2f(double A , double B) {
		return (double)Math.atan2(A,B);
	}
	
	public double atanf(double A ) {
		return (double)Math.atan(A);
	}
	
	public double powf(double A, double e) {
		return (double)Math.pow(A, e);
	}
	
	public double lats=0.0f, lons=0.0f;
	public double latf=0.0f, lonf=0.0f;
	public double d=0.0f, crs=0.0f;
	public double R=6371f; // radius of the earth in the same units as d (default: km)

	public double lats() {return lats*180/PI;}
	public double latf() {return latf*180/PI;}
	public double lons() {return lons*180/PI;}
	public double lonf() {return lonf*180/PI;}
	public double crs() {return crs*180/PI;}

	public void setLatSDDeg(double ddeg) //set start lat from decimal degrees
	{
		lats = ddeg / 180.0f * PI;
	}
	public void setLatFDDeg(double ddeg) //set final lat from decimal degrees
	{
		latf = ddeg / 180 * PI;
	}
	public void setLonSDDeg(double ddeg) //set start lon from decimal degrees
	{
		lons = ddeg / 180 * PI;
	}
	public void setLonFDDeg(double ddeg) //set final lon from decimal degrees
	{
		lonf = ddeg / 180 * PI;
	}
	public void setR(double Ro) //set radius of the earth (must be same units as d)
	{
		R = Ro;
	}
	public void setCrsDDeg(double crso) //set course from decimal degrees
	{
		crs = crso / 180 * PI;
	}
	public double getLatSDDeg() //get start lon from decimal degrees
	{
		return (lats / PI * 180);
	}
	public double getLonSDDeg() //get final lon from decimal degrees
	{
		return (lons / PI * 180);
	}	
	public double getLatFDDeg() //get start lon from decimal degrees
	{
		return (latf / PI * 180);
	}
	public double getLonFDDeg() //get final lon from decimal degrees
	{
		return (lonf / PI * 180);
	}
	public double getCrsDDeg() //get start lon from decimal degrees
	{
		return (crs / PI * 180);
	}
	public double getD() //get distance
	{
		return d;
	}

	public double mod(double y, double x)
	{
		return (y - (x*floorf(y/x)));
	}

	public void dcrsfromll()			// JMB COMMENT -- 10.19.09 -- DETERMINES DISTANCE (D) AND COURSE (CRS) FROM ENDING LAT/LON
	{
		double dlat = latf - lats;
		double dlon = lonf - lons;
		double a = powf(sinf(dlat/2),2) + cosf(lats)*cosf(latf)*powf(sinf(dlon/2),2);

		this.d = R * 2 * atan2f(Math.sqrt(a),Math.sqrt(1-a));
		// this.d = R * 2 * asinf(Math.sqrt(a)); //	Implemented by JMB from formula given at http://williams.best.vwh.net/avform.htm#Par
		// this.crs = this.mod(Math.atan2(sinf(dlon)*cosf(latf),cosf(lats)*sinf(latf)-sinf(lats)*cosf(latf)*cosf(dlon)),2*PI);  //	Implemented by JMB from formula given at http://williams.best.vwh.net/avform.htm#Par
		this.crs = this.mod(atan2f(sinf(dlon)*cosf(latf),cosf(lats)*sinf(latf)-sinf(lats)*cosf(latf)*cosf(dlon)),2*PI);
	}

	public void llffromdcrs()			// JMB COMMENT -- 10.19.09 -- DETERMINES ENDING LAT/LON FROM DISTANCE (D) AND COURSE (CRS)
	{
		this.latf = asinf(sinf(lats)*cosf(d/R)+cosf(lats)*sinf(d/R)*cosf(crs));
		this.lonf = lons + atan2f(sinf(crs)*sinf(d/R)*cosf(lats),cosf(d/R)-sinf(lats)*sinf(latf));
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

	
	public lldataf latdfromlon3(double lon3o) // // Implementation by JMB from http://williams.best.vwh.net/avform.htm#Par  -- lat=atan(  (sin(lat1)*cosf(lat2)*sin(lon-lon2)-sin(lat2)*cosf(lat1)*sin(lon-lon1))  /  (cosf(lat1)*cosf(lat2)*sin(lon1-lon2))  )
	{
		double lon3 = lon3o*PI/180;
		
		lldataf ret =  new lldataf();
		ret.lats = this.lats;
		ret.lons = this.lons;
		ret.R = this.R;
		
		double lat1 = this.lats;
		double lon1 = this.lons;
		double lat2 = this.latf;
		double lon2 = this.lonf;
		
		ret.latf = atanf(((sinf(lat1)*cosf(lat2)*sinf(lon3-lon2))-(sinf(lat2)*cosf(lat1)*sinf(lon3-lon1)))/(cosf(lat1)*cosf(lat2)*sinf(lon1-lon2)));
		
		ret.lonf = lon3;
		
		/*if( !((ret.latf >= lat1 && ret.latf <= lat2) || (ret.latf <= lat1 && ret.latf >= lat2)) ) // Checks to make sure predicted value is in range of movement...if not, sets d to arbirarily large value (1,000,000)
		{
			ret.d=1000000;
			return ret;
		}*/		
		
		ret.dcrsfromll();
		
		return ret;
	}

	
	public lldataf londfromlat3(double lat3o) // in degrees		JMB COMMENT -- 10.19.09 -- FINDS LONGITUDE AT WHICH NEAREST LAT IS CROSSED W/
	{														//								GREAT CIRCLE DISTANCE
		// from http://williams.best.vwh.net/avform.htm#Par
		double lat3 = lat3o * PI / 180;
		lldataf ret = new lldataf();
		ret.lats = this.lats;
		ret.lons = this.lons;
		//ret.latf = this.latf;
		//ret.lonf = this.lonf;
		ret.crs = this.crs;
		ret.d = this.d;
		ret.R = this.R;

		double lat1 = this.lats;
		double lat2 = this.latf;
		double lon1 = this.lons;
		double lon2 = this.lonf;

		double l12 = lon1-lon2;
		double A = sinf(lat1)*cosf(lat2)*cosf(lat3)*sinf(l12);
		double B = sinf(lat1)*cosf(lat2)*cosf(lat3)*cosf(l12) - cosf(lat1)*sinf(lat2)*cosf(lat3);
		double C = cosf(lat1)*cosf(lat2)*sinf(lat3)*sinf(l12);
		double lon = atan2f(B,A);
		if (abs(C) >sqrt(pow(A,2) + pow(B,2)))
		{
			//System.out.println("never crosses");
			ret.d = 10000000;
		}
		else if( lat1 == lat3 ) {
			ret.latf = lat1;
			ret.lonf = lon1;
			ret.d = 0;
			ret.crs = 0;
		}
		else {
			double dlon = acosf(C/sqrt(pow(A,2)+pow(B,2)));
			double lon3_1=(this.mod((lon1+dlon+lon+PI),(2*PI))-PI);
			double lon3_2=(this.mod((lon1-dlon+lon+PI),(2*PI))-PI);

			ret.latf = lat3;
			//System.out.println(lat3*180/PI);
			//System.out.println(lon3_1*180/PI);
			//System.out.println(lon3_2*180/PI);
			lldata lon3_1_d = new lldata();
			lon3_1_d.lats = ret.lats;
			lon3_1_d.lons = ret.lons;
			lon3_1_d.latf = ret.latf;
			lon3_1_d.lonf = lon3_1;
			lon3_1_d.dcrsfromll();
			
			lldata lon3_2_d = new lldata();
			lon3_2_d.lats = ret.lats;
			lon3_2_d.lons = ret.lons;
			lon3_2_d.latf = ret.latf;
			lon3_2_d.lonf = lon3_2;
			lon3_2_d.dcrsfromll();					
			
			if( ((lon3_1 >= lon1 && lon3_1 <= lon2) || (lon3_1 <= lon1 && lon3_1 >= lon2)) && (lon3_1_d.d < lon3_2_d.d) )
				ret.lonf = lon3_1;
			else
				ret.lonf = lon3_2;
			
			/*if( !((ret.lonf >= lon1 && ret.lonf <= lon2) || (ret.lonf <= lon1 && ret.lonf >= lon2)) )		// Checks to make sure predicted value is in range of movement...if not, sets d to arbirarily large value (1,000,000)
			{
				ret.d=1000000;
				return ret;
			}*/
				
			ret.dcrsfromll();				// JMB COMMENT -- 10.19.09 -- SETS D AND CRS FOR RET
		}

		return ret;
	}
}
