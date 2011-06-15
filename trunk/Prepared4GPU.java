


public class Prepared4GPU extends lldataf
{	
	public final int northeast = 0;
	public final int southeast = 1;
	public final int northwest = 2;
	public final int southwest = 3;
	public final static String modename[] = {"northeast","southeast","northwest","southwest"};
	public final int northORsouth[] = {1,-1,1,-1};
	public final int eastORwest[] = {1,1,-1,-1};
	public final static int numRand = 100000;
	
	private double[] randArray;
	private int _index;

	public DispersalSettings settings;

	public int end_gen = -1;	// Added by JMB
	
//	public java.util.Random rand;	// Added by JMB -- 4.5.10
	
	public Prepared4GPU(DispersalSettings ds, int end_generation, double[] randArray)	// Modified by JMB -- 4.5.10
	{
		settings = ds;
		//thisGeneration = thisGen;
		//nextGeneration = new ArrayList<Node>();
		end_gen = end_generation;
		 //java.util.Random rand = new java.util.Random(ran_seed);		// Added by JMB -- 4.5.10
		_index = 0;
		this.randArray = randArray;
	}
	
	
	private double nextRand() {
		if(_index>numRand) {
			System.err.println("nextRand(): index>numRand");
			System.exit(-1);
		}
		double r =  randArray[_index];
		_index++;
		
		return r;
	}
	
	
	
	

	public void migrate(Node[] children,boolean rm[], double d[])
	{
		for(int i=0; i<children.length; i++) {
			Node n = children[i];
			double di = settings.getDispersalRadius(n.generation,nextRand());
			rm[i] = migrate(n,di);
			
		}
	}
	


	public void pause() throws Exception
	{
		// System.in.read();
		// System.in.read();
	}

	public void dprint(String so)
	{
		// System.out.println(so);
	}
	
	// the next crossings of lat,lon lines
	private double getILat(double lat1, double minlat, double latspace, int mode) {
		double ilat;
		switch(mode) {
		case southeast:
		case southwest:
			ilat = lat1 - ((lat1-minlat)%latspace);
			break;
		case northeast:
		case northwest:
			ilat = lat1 - ((lat1-minlat)%latspace) + latspace;
			break;
		default:
			ilat =0;
			break;
		}
		return ilat;
	}
	
	private double getILon(double lon1, double minlon, double lonspace, int mode) {
		double ilon;
		switch(mode) {
		case southeast:
		case northeast:
			ilon = lon1 - ((lon1-minlon)%lonspace) + lonspace; 
			break;
		case southwest:
		case northwest:
			 ilon = lon1 - ((lon1-minlon)%lonspace);
			 break;
		default:
			ilon =0;
			break;
		}
		return ilon;
	}
	
	
	private  double[] ka(int mode, double lat1,double lon1, double minlat, double minlon, double sb_latspace,double sb_lonspace, double hb_latspace, double hb_lonspace,double[] lld, boolean debug, double d) {
		// check soft borders first
		double sb_lat_bt, sb_lon_bt,hb_lat_bt,hb_lon_bt;
		double hb_dd=100000000,sb_dd=100000000;
		int hb_dx=0,hb_dy=0,sb_dx=0,sb_dy=0;
		double result[] = new double[10];
		double ilat = getILat(lat1,minlat,sb_latspace, mode);
		double ilon = getILon(lon1,minlon,sb_lonspace,mode);


		// System.out.println("NW ilat: "+ilat);
		// System.out.println("NW ilon: "+ilon);
		dprint("Next sb latitude crossing south: "+ilat);
		dprint("Next sb longitude crossing east: "+ilon);

		if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
		{					// If so, pushes it barely off in a random direction.
			if (nextRand() > 0.5)
				lat1 += 0.000000001;
			else
				lat1 -= 0.000000001;
			ilat = getILat(lat1,minlat,sb_latspace, mode);
		}
		
		if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
		{
			if (nextRand() > 0.5)
				lon1 += 0.000000001;
			else
				lon1 -= 0.000000001;
			ilon = getILon(lon1,minlon,sb_lonspace,mode);
		}								
		
		sb_lat_bt = ilat;
		sb_lon_bt = ilon;
		dprint("Next sb latitude crossing north: "+ilat);
		dprint("Next sb longitude crossing east: "+ilon);

		double[] i1 = londfromlat3(lld,ilat);		// JMB COMMENT -- FINDS COORDINATES FOR NEAREST LAT BORDER CROSSING
		double[] i2 = latdfromlon3(lld,ilon);		// JMB COMMENT -- FINDS COORDINATES FOR NEAREST LON BORDER CROSSING

		if (debug)
		{
			System.out.println("");
			System.out.println(modename[mode] + " Soft Border Info");
			System.out.println("d: "+d+" i1.d: "+i1[_d]+" i1.d: "+i2[_d]);	// ADDED BY JMB
			System.out.println("LLD Start Lat: "+lats(lld));
			System.out.println("LLD End Lat: "+latf(lld));
			System.out.println("LLD Start Lon: "+lons(lld));
			System.out.println("LLD End Lon: "+lonf(lld));
			System.out.println("sb_i1.d: "+i1[_d]+"	sb_i1.latf: "+latf(i1)+"	sb_i1.lonf: "+lonf(i1));
			System.out.println("sb_i2.d: "+i2[_d]+"	sb_i2.latf: "+latf(i2)+"	sb_i2.lonf: "+lonf(i2));
			System.out.println("sb_ilat: "+ilat);
			System.out.println("sb_ilon: "+ilon);
			System.out.println("");
		}
		if( i1[_d] <= i2[_d]  && i1[_d] < d) {
			dprint("sb - lat crossing is closer");
			dprint("start: lat="+lats(i1)+" lon="+lons(i1));
			dprint("target (lat crossing): lat="+latf(i1)+" lon="+lonf(i1)+" d="+i1[_d]);
			if (debug)
				System.out.println("target (lat crossing): lat="+latf(i1)+" lon="+lonf(i1)+" d="+i1[_d]+" crs="+i1[_crs]);
			sb_dd = i1[_d];
			sb_dx = 0; 
			sb_dy = northORsouth[mode];
		}
		else if( i2[_d] < d ) {
			dprint("sb - lon crossing is closer");
			dprint("start: lat="+lats(i2)+" lon="+lons(i2));
			dprint("target (lon crossing): lat="+latf(i2)+" lon="+lonf(i2)+" d="+i2[_d]);
			if (debug)
				System.out.println("target (lat crossing): lat="+latf(i2)+" lon="+lonf(i2)+" d="+i2[_d]+" crs="+i2[_d]);
			sb_dd = i2[_d];
			sb_dx = eastORwest[mode];
			sb_dy = 0;
		}


		// check hard border crossing next
		ilat = getILat(lat1,minlat,hb_latspace,mode);
		ilon = getILon(lon1,minlon,hb_lonspace,mode);
		dprint("Next hb latitude crossing south: "+ilat);
		dprint("Next hb longitude crossing east: "+ilon);

		if (lat1 == ilat)	// Checking to see if individual is starting RIGHT ON a lat line. 
		{					// If so, pushes it barely off in a random direction.
			if (nextRand() > 0.5)
				lat1 += 0.000000001;
			else
				lat1 -= 0.000000001;
		//	ilat = lat1 - ((lat1-minlat)%sb_latspace);
			ilat = getILat(lat1,minlat,sb_latspace,mode); //TODO: check sb?
		}
		
		if (lon1 == ilon)	// Same nudging, if individual RIGHT ON a lon line.
		{
			if (nextRand() > 0.5)
				lon1 += 0.000000001;
			else
				lon1 -= 0.000000001;
			//ilon = lon1 - ((lon1-minlon)%sb_lonspace) + sb_lonspace; 
			ilat = getILon(lon1,minlon,sb_lonspace,mode); //TODO: check sb?
		}																
		
		hb_lat_bt = ilat;
		hb_lon_bt = ilon;
		
		i1 = londfromlat3(lld,ilat);
		i2 = latdfromlon3(lld,ilon);
		if (debug)
		{
			System.out.println("");
			System.out.println(modename[mode]+"Southeast Hard Border Info");
			System.out.println(d+" "+i1[_d]+" "+i2[_d]+" ");  // ADDED BY JMB
			System.out.println("LLD Start Lat: "+lats(lld));
			System.out.println("LLD End Lat: "+latf(lld));
			System.out.println("LLD Start Lon: "+lons(lld));
			System.out.println("LLD End Lon: "+lonf(lld));
			System.out.println("hb_i1.d: "+i1[_d]);
			System.out.println("hb_i2.d: "+i2[_d]);
			System.out.println("hb_ilat: "+ilat);
			System.out.println("hb_ilon: "+ilon);
			System.out.println("");
		}
		if( i1[_d] <= i2[_d] && i1[_d] < d ) {
			dprint("hb - lat crossing is closer");
			dprint("start: lat="+lats(i1)+" lon="+lons(i1));
			dprint("target (lat crossing): lat="+latf(i1)+" lon="+lonf(i1)+" d="+i1[_d]);
			hb_dd = i1[_d];
			hb_dx = 0; 
			hb_dy = northORsouth[mode];
		}
		else if( i2[_d] < d ) {
			dprint("hb - lon crossing is closer");
			dprint("start: lat="+lats(i2)+" lon="+lons(i2));
			dprint("target (lon crossing): lat="+latf(i2)+" lon="+lonf(i2)+" d="+i2[_d]);
			hb_dd = i2[_d];
			hb_dx = eastORwest[mode];
			hb_dy = 0;
		}
		result[0] = sb_lat_bt;
		result[1] = sb_lon_bt;
		result[2] = hb_lat_bt;
		result[3] = hb_lon_bt;
		result[4] = hb_dx;
		result[5] = hb_dd;
		result[6] = hb_dy;
		result[7] = sb_dx;
		result[8] = sb_dd;
		result[9] = sb_dy;

		return result;
	}

	private double setX(double X, double x_bt, double prefix, boolean debug){
		return setX(X,x_bt,prefix,debug,0);
	}
	
	private double setX(double X, double x_bt, double prefix, boolean debug, double dXY){
		double r = X;
		if (((prefix > 0 && X < x_bt) || (prefix < 0 && X > x_bt)) && dXY == 0)
		{
		    r = x_bt + 0.00001*prefix;
		    if ( debug )
		    {
		        System.out.println("");
		        System.out.println("Border buffer used when adding to next gen!");
		        System.out.println("");
		    }
		}
		return r;
	}
	
	private double geq (double lat1, double  sb_lat_bt) {
		if(lat1 >= sb_lat_bt)
			return 1;
		else if (lat1 < sb_lat_bt)
			return -1;
		else {
			System.out.println("Weird things in soft border buffer for individual being added to next gen. Exiting...");

			System.exit(-1);
			return 0;
		}
	}

	
	private boolean migrate(Node n, double d) {
		// this next chunk should be moved...
		double sb_lonspace = 0.0;
		double sb_latspace = 0.0;
		double hb_lonspace = 0.0;
		double hb_latspace = 0.0;
		
		double minlat = settings.getMinLat();
		double maxlat = settings.getMaxLat();
		double minlon = settings.getMinLon();
		double maxlon = settings.getMaxLon();
		
		double step_d = 0.0001;
	
		boolean debug = false;				// ADDED BY JMB -- Boolean to control the outputting of LOTS of extra information to help debug
		
		
		if (n.generation-1 == end_gen)	// To properly get soft borders for the children of individuals in the final generation
		{
			sb_lonspace = (maxlon-minlon)/(settings.softborders.getMaxX(settings.softborders.calcIndex(n.generation-1)));		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LONGITUDES IN DECIMAL DEGREES (UNIT: DEGREES/PIXEL)
			sb_latspace = (maxlat-minlat)/(settings.softborders.getMaxY(settings.softborders.calcIndex(n.generation-1)));		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LATITUDES IN DECIMAL DEGREES
			hb_lonspace = (maxlon-minlon)/(settings.hardborders.getMaxX(settings.hardborders.calcIndex(n.generation-1)));		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LONGITUDES IN DECIMAL DEGREES
			hb_latspace = (maxlat-minlat)/(settings.hardborders.getMaxY(settings.hardborders.calcIndex(n.generation-1)));		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LATITUDES IN DECIMAL DEGREES												
		}
		else
		{
			sb_lonspace = (maxlon-minlon)/(settings.softborders.getMaxX(settings.softborders.calcIndex(n.generation)));		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LONGITUDES IN DECIMAL DEGREES (UNIT: DEGREES/PIXEL)
			sb_latspace = (maxlat-minlat)/(settings.softborders.getMaxY(settings.softborders.calcIndex(n.generation)));		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR SOFT BORDER LATITUDES IN DECIMAL DEGREES
			hb_lonspace = (maxlon-minlon)/(settings.hardborders.getMaxX(settings.hardborders.calcIndex(n.generation)));		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LONGITUDES IN DECIMAL DEGREES
			hb_latspace = (maxlat-minlat)/(settings.hardborders.getMaxY(settings.hardborders.calcIndex(n.generation)));		// JMB COMMENT -- DETERMINES PIXEL SIZE FOR HARD BORDER LATITUDES IN DECIMAL DEGREES						
		}
							
		double crs = nextRand() * 2 * Math.PI;	// Modified by JMB -- 4.5.10
		
		double lat1 = n.parent.lat;
		double lon1 = n.parent.lon;
	
		/*if (n.generation ==  && n.unique == )
		{
			System.out.println(" created");
			System.out.println("d: "+d);
			System.out.println("crs: "+crs);
			System.out.println("lat1: "+lat1);
			System.out.println("lon1: "+lon1);
		}*/					
		
//NODE		n.par_lat = lat1;				// ADDED BY JMB
//NODE		n.par_lon = lon1;				// ADDED BY JMB 
//NODE		n.last_d = d;					// ADDED BY JMB 
//NODE		n.last_crs = crs;				// ADDED BY JMB 
		
		double[] lld = new double[_lldataSize];		// JMB -- lldataf CLASS STORES LAT/LON DATA AND DOES CONVERSIONS	
	
		travelloop:
		while( d >= 0.000001 ) {
			
			double sb_lat_bt = 0.0;		// JMB -- Using this to keep track of lat/lon value for border reflections and adjusting inexact positions, if necessary
			double sb_lon_bt = 0.0;
			double hb_lat_bt = 0.0;
			double hb_lon_bt = 0.0;
			double border_buffer = 0.001;
			
			if (debug)
			{
				System.out.println("");
				System.out.println("d: "+d);			// JMB -- FOR DEBUGGING
				System.out.println("crs: "+crs);		// JMB -- FOR DEBUGGING
				System.out.println("lat1: "+lat1);		// JMB -- FOR DEBUGGING
				System.out.println("lon1: "+lon1);		// JMB -- FOR DEBUGGING
			}
//NODE			n.moved = true;					// ADDED BY JMB -- TO MAKE SURE THAT NODE MADE IT TO THIS PART OF THE CODE
//NODE			n.end_move_lat = n.lat;		// ADDED BY JMB
//NODE			n.end_move_lon = n.lon;		// ADDED BY JMB							
			
			lld[_d] = d;					// JMB COMMENT -- SETS D INTERNALLY IN LLD OBJECT
			lld[_R] = 6371;				// radius of spherical earth in km (TODO: use value(# or units?) from xml)
			setLatSDDeg(lld,lat1);		// JMB COMMENT -- CONVERTS LATITUDE FROM DECIMAL DEGREES TO RADIANS
			setLonSDDeg(lld,lon1);		// JMB COMMENT -- CONVERTS LONGITUDE FROM DECIMAL DEGREES TO RADIANS
			lld[_crs] = crs;				// JMB COMMENT -- SETS CRS INTERNALLY WITHIN LLD OBJECT
			llffromdcrs(lld);			// JMB COMMENT -- GETS ENDING LAT/LON FROM DISTANCE AND COURSE AND STORES INTERNALLY IN LLD OBJECT

			dprint("initial -- start: lat="+lats(lld)+" lon="+lons(lld));
			dprint("initial --        crs="+crs(lld)+" d="+lld[_d]);
			dprint("initial --   end: lat="+latf(lld)+" lon="+lonf(lld));

			double hb_dd=100000000,sb_dd=100000000;
			int hb_dx=0,hb_dy=0,sb_dx=0,sb_dy=0;
			double result[] = new double[10];

			if( crs >= 0 && crs <= PI/2 ) {		// JMB COMMENT -- DETERMINES IF COURSE IS NORTHEAST...OR IS THIS SOUTHEAST, SINCE INPUT MAPS ARE FLIPPED??
				result = ka(northeast,lat1,lon1,minlat,minlon,sb_latspace,sb_lonspace,hb_latspace,hb_lonspace,lld,debug,d);
			}
			else if( crs > PI/2 && crs <= PI ) {			// JMB COMMENT -- Determines if course is southeast.  Note that this coordinate system is upside down relative to the standard.
				result = ka(southeast,lat1,lon1,minlat,minlon,sb_latspace,sb_lonspace,hb_latspace,hb_lonspace,lld,debug,d);							
			}
			else if( crs > PI && crs <= 3*PI/2 ) {  // JMB COMMENT -- DETERMINES IF COURSE IS SOUTHWEST
				result = ka(southwest,lat1,lon1,minlat,minlon,sb_latspace,sb_lonspace,hb_latspace,hb_lonspace,lld,debug,d);							
			}
			else if( crs > 3*PI/2 && crs <= 2*PI ) {   // JMB COMMENT -- 10.19.09 -- DETERMINES IF COURSE IS NORTHWEST
				result = ka(northwest,lat1,lon1,minlat,minlon,sb_latspace,sb_lonspace,hb_latspace,hb_lonspace,lld,debug,d);							
			}
			
			sb_lat_bt = result[0];
			sb_lon_bt = result[1];
			hb_lat_bt = result[2];
			hb_lon_bt = result[3];
			hb_dx = (int) result[4];
			hb_dd = result[5];
			hb_dy = (int) result[6];
			sb_dx = (int) result[7];
			sb_dd = result[8];
			sb_dy = (int) result[9];
			
			if( d < min(sb_dd,hb_dd) ) {	// JMB COMMENT -- 10.20.09 -- DISPERSAL OCCURS BEFORE PIXEL BOUNDARY IS CROSSED, 
				dprint("Case 1");			//								SO INDIVIDUAL IS ADDED TO THE NEXT GENERATION
				// System.out.println("Moving done. d: "+d+"  sb_dd: "+sb_dd+"  hb_dd: "+hb_dd);	// ADDED BY JMB
				//for (int z=0; z<4; z++)
					//System.out.println("");	// ADDED BY JMB
				
				lld[_d] = d;
				llffromdcrs(lld);
				
				//*********************** Fudging to keep poorly estimated positions (due to step_d alterations) from crossing border boundaries inadvertently ***********************
				
				// IS THIS CHECK NECESSARY AT THIS POINT IN THE LOOP?  PERHAPS NOT, BUT SHOULD MAKE SURE BEFORE REMOVING IT.
				
				
		        n.lat = getLatFDDeg(lld);
		        n.lon = getLonFDDeg(lld);
				n.lat = setX(n.lat,sb_lat_bt,geq(lat1,sb_lat_bt),debug);
				n.lon = setX(n.lon,sb_lon_bt,geq(lon1,sb_lon_bt),debug);		
				n.lat = setX(n.lat,hb_lat_bt,geq(lat1,hb_lat_bt),debug);
				n.lon = setX(n.lon,hb_lon_bt,geq(lon1,hb_lon_bt),debug);
				
				
			
				if (debug)
					System.out.println("Just before being added to next gen -- lat: "+n.lat+" lon: "+n.lon);
									
				//nextGeneration.add( n );
//NODE				n.added_nextgen=true;	// ADDED BY JMB -- 10.19.09
				d=0;
				
				/*if (n.generation ==  && n.unique == )
					System.out.println(" ADDED TO NEXT GEN");*/
				
				return false; //continue childrenloop;
			}
			else if( absf(sb_dd-hb_dd) < step_d ) {	// JMB COMMENT -- 10.20.09 -- BOTH SOFT AND HARD PIXEL BOUNDARIES WILL BE CROSSED 
				//d = 0;
				// both soft & hard must be checked at the same time -- but the order is up to you
				// I arbitrarily chose to check hard borders first
				dprint("*** Checking Hard Border");
													
				Index hb = null;
				if (n.generation-1 == end_gen)								// JMB -- Added to make sure that children of the final generation get the right hard borders
					hb = settings.hardborders.calcIndex(n.generation-1);
				else
					hb = settings.hardborders.calcIndex(n.generation);
				
				if( nextRand() <= settings.hardborders.f(hb,settings.hardborders.toX(hb,lon1,minlon,maxlon)+hb_dx,settings.hardborders.toY(hb,lat1,minlat,maxlat)+hb_dy) ) {	// JMB COMMENT -- FINDS HARD BORDER VALUE FOR NEXT PIXEL WITH RESPECT TO LONGITUDE
					dprint("failed");																				//								AND CHECKS TO SEE IF INDIVIDUAL SURVIVES HARD BORDER CROSSING.
					if (debug)
						System.out.println("Hard border death!");				// ADDED BY JMB -- FOR DEBUGGING
//NODE					n.failed_one = true;	// ADDED BY JMB
													
					n.parent.children.remove( n.parent.children.indexOf( n ) ); // Added by JMB -- 4.14.10 -- Child needs to be removed from parent's children vector if it is not added to the next generation.
					
					/*if (n.generation ==  && n.unique == )
						System.out.println(" DIED AT HARD BORDER CHECK 1");*/
					
					return true; //continue childrenloop; // this exits the travel loop immediately, so the current child never gets added to the next generation
				}
				else {
					// don't "travel" the distance, as that will be done by soft border check
					//							lld.d = hb_dd+step_d;
					//							d-=(hb_dd+step_d);
					dprint("passed");
				}

				dprint("*** Checking Soft Border");
				
				//ldd.d = sb_dd;
				//lld.llffromdcrs();
				Index sb = null;
				if (n.generation-1 == end_gen)									// JMB -- Added to make sure that children of the final generation get the right soft borders
					sb = settings.softborders.calcIndex(n.generation-1);
				else
					sb = settings.softborders.calcIndex(n.generation);
				dprint("I\'m at ("+settings.softborders.toX(sb,lon1,minlon,maxlon)+","+settings.softborders.toY(sb,lat1,minlat,maxlat)+")");
				dprint("I\'m checking ("+(settings.softborders.toX(sb,lon1,minlon,maxlon)+sb_dx)+","+(settings.softborders.toY(sb,lat1,minlat,maxlat)+sb_dy)+")");
				if (debug)
				{
					System.out.println("");
					System.out.println("Current Lon (x): "+lon1);
					System.out.println("Current Lat (y): "+lat1);
					System.out.println("crs: "+crs);																						// JMB - Debugging
					System.out.println("d: "+d);																							// JMB - Debugging
					System.out.println("I\'m at ("+settings.softborders.toX(sb,lon1,minlon,maxlon)+","+settings.softborders.toY(sb,lat1,minlat,maxlat)+")");							// JMB - Debugging
					System.out.println("I\'m checking ("+(settings.softborders.toX(sb,lon1,minlon,maxlon)+sb_dx)+","+(settings.softborders.toY(sb,lat1,minlat,maxlat)+sb_dy)+")");	// JMB - Debugging
				}
				double ran_num = nextRand();
				if (debug)
				{
					System.out.println("Random number is: "+ran_num);
					System.out.println("Border value being tested: "+settings.softborders.f(sb,settings.softborders.toX(sb,lon1,minlon,maxlon)+sb_dx, settings.softborders.toY(sb,lat1,minlat,maxlat)+sb_dy));
					System.out.println("sb_dx: "+sb_dx);
					System.out.println("sb_dy: "+sb_dy);
					System.out.println("");
				}
				if( ran_num <= settings.softborders.f(sb,settings.softborders.toX(sb,lon1,minlon,maxlon)+sb_dx, settings.softborders.toY(sb,lat1,minlat,maxlat)+sb_dy) ) {
					// failed the soft border-- stop before border, reflect back, update d, and continue
					dprint("failed");
					lld[_d] = (sb_dd-step_d);
					d-= (sb_dd-step_d);								
					crs = nextRand() * 2 * PI;// / 4+3*PI/4;
					llffromdcrs(lld);
					// Fudging to keep poorly estimated positions from crossing boundaries
					lat1 = setX(latf(lld),sb_lat_bt,geq(lat1,sb_lat_bt),debug);
					lon1 = setX(lonf(lld),sb_lon_bt,geq(lon1,sb_lon_bt),debug);	
					dcrsfromll(lld);
					
				}
				else {
					// passed the soft border-- stop after border, no reflection, update d, and continue
					dprint("passed");
					lld[_d] = sb_dd+step_d;
					d-=(sb_dd+step_d);
					llffromdcrs(lld);
					lat1 = setX(latf(lld),sb_lat_bt,geq(lat1,sb_lat_bt),debug,sb_dy);	
					lon1 = setX(lonf(lld),sb_lon_bt,geq(lon1,sb_lon_bt),debug,sb_dx);
					dcrsfromll(lld);
					
					if (d <= 0.000001)			// ADDED BY JMB -- in case this individual would not go through another iteration of the travel loop
					{
						lld[_d] = d;
						n.lat = lat1;
						n.lon = lon1;
					//	nextGeneration.add( n );															
//NODE						n.added_nextgen=true;	// ADDED BY JMB
//NODE						n.sb_final_1=true;
//NODE						n.last_at="I\'m at ("+settings.softborders.toX(sb,lon1,minlon,maxlon)+","+settings.softborders.toY(sb,lat1,minlat,maxlat)+")";
//NODE						n.last_check="I\'m checking ("+(settings.softborders.toX(sb,lon1,minlon,maxlon)+sb_dx)+","+(settings.softborders.toY(sb,lat1,minlat,maxlat)+sb_dy)+")";
//NODE						n.last_value=settings.softborders.f(sb,settings.softborders.toX(sb,lon1,minlon,maxlon)+sb_dx, settings.softborders.toY(sb,lat1,minlat,maxlat)+sb_dy);
						d=0;										
					}
														
				}

				continue travelloop;
			}
			else if( sb_dd < hb_dd ) {
				dprint("*** Checking Soft Border");
				Index sb = null;
				if (n.generation-1 == end_gen)									// JMB -- Added to make sure that children of the final generation get the right soft borders
					sb = settings.softborders.calcIndex(n.generation-1);
				else
					sb = settings.softborders.calcIndex(n.generation);
				dprint("I\'m at ("+settings.softborders.toX(sb,lon1,minlon,maxlon)+","+settings.softborders.toY(sb,lat1,minlat,maxlat)+")");
				dprint("I\'m checking ("+(settings.softborders.toX(sb,lon1,minlon,maxlon)+sb_dx)+","+(settings.softborders.toY(sb,lat1,minlat,maxlat)+sb_dy)+")");
				if( nextRand() <= settings.softborders.f(sb,settings.softborders.toX(sb,n.lon,minlon,maxlon)+sb_dx, settings.softborders.toY(sb,n.lat,minlat,maxlat)+sb_dy) ) {
					// failed the soft border-- stop before border, reflect back, update d, and continue
					dprint("failed");
					lld[_d] = (sb_dd-step_d);
					d-= (sb_dd-step_d);
					crs = nextRand() * 2 * PI;// / 4+3*PI/4;
					llffromdcrs(lld);
					// Fudging to keep poorly estimated positions from crossing boundaries
					lat1 = setX(latf(lld),sb_lat_bt,geq(lat1,sb_lat_bt),debug);	
					lon1 = setX(lonf(lld),sb_lon_bt,geq(lon1,sb_lon_bt),debug);
					dcrsfromll(lld);
					
				}
				else {
					// passed the soft border-- stop after border, no reflection, update d, and continue
					dprint("passed");
					lld[_d] = sb_dd+step_d;
					d-=(sb_dd+step_d);
					llffromdcrs(lld);									
					lat1 = setX(latf(lld),sb_lat_bt,geq(lat1,sb_lat_bt),debug,sb_dy);	
					lon1 = setX(lonf(lld),sb_lon_bt,geq(lon1,sb_lon_bt),debug,sb_dx);	
					dcrsfromll(lld);
					
					if (d <= 0.000001)		// ADDED BY JMB -- in case this individual would not go through another iteration of the travel loop
					{
						lld[_d] = d;
						llffromdcrs(lld);
						n.lat = lat1;
						n.lon = lon1;
						//nextGeneration.add( n );
//NODE						n.added_nextgen=true;	// ADDED BY JMB
//NODE						n.sb_final_2=true;
						d=0;										
					}									
														
				}

				continue travelloop;
			}
			else {
				dprint("*** Checking Hard Border");
				Index hb = null;
				if (n.generation-1 == end_gen)
					hb = settings.hardborders.calcIndex(n.generation-1);
				else
					hb = settings.hardborders.calcIndex(n.generation);
				if( nextRand() <= settings.hardborders.f(hb,settings.hardborders.toX(hb,lon1,minlon,maxlon)+hb_dx,settings.hardborders.toY(hb,lat1,minlat,maxlat)+hb_dy) ) {
					dprint("failed");
//NODE					n.failed_two = true;   // ADDED BY JMB -- 10.19.09
				
					n.parent.children.remove( n.parent.children.indexOf( n ) );  // Added by JMB -- 4.14.10 -- Child needs to be removed from parent's children vector if it is not added to the next generation.
					
					/*if (n.generation ==  && n.unique == )
						System.out.println(" DIED AT HARD BORDER CHECK 2");*/
						
					return true; //continue childrenloop; // this exits the travel loop immediately, so the current child never gets added to the next generation
											// JMB -- Would this lead to pruning problems?
				}
				else {
					dprint("passed");
					lld[_d] = hb_dd+step_d;
					d-=(hb_dd+step_d);
					double latV = geq(lat1,hb_lat_bt);
					double lonV = geq(lon1,hb_lon_bt);
					lat1 = latf(lld);
					lon1 = lonf(lld);									
					lat1 = setX(latf(lld),hb_lat_bt,latV,debug,hb_dy);	
					lon1 = setX(lonf(lld),hb_lon_bt,lonV,debug,hb_dx);
					dcrsfromll(lld);
					
					if (d <= 0.000001)		// ADDED BY JMB -- in case this individual would not go through another iteration of the travel loop
					{
						lld[_d] = d;
						// lld.llffromdcrs();
						n.lat = lat1;
						n.lon = lon1;
					//	nextGeneration.add( n );
//NODE						n.added_nextgen=true;	// ADDED BY JMB -- 10.19.09
//NODE						n.sb_final_2=true;
						d=0;																				
					}
					continue travelloop;
				}
			}
		}
		return false;
	}
}
