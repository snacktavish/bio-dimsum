public class Migrate
{		
	public final static int DIMSUM_NODE_lat = 0;
	public final static int DIMSUM_NODE_lon = 1;
	public final static int DIMSUM_PARAMI_RANDINDEX =8;
	public final static int DIMSUM_PARAMI_GENERATION =2;
	public final static int DIMSUM_PARAMI_NUMCHILDREN =3;
	public final static int  DIMSUM_PARAMI_SB_XSIZE =4;
	public final static int  DIMSUM_PARAMI_SB_YSIZE =5;
	public final static int  DIMSUM_PARAMI_HB_XSIZE =6;
	public final static int  DIMSUM_PARAMI_HB_YSIZE =7;

	public final static double PI = 3.1415926536;// (double)Math.PI; 

	
	public static void updateF(XYFunction soft, XYFunction hard) {
		kernel.setArrays(soft.getF().getData(),soft.getMaxX(),soft.getMaxY(),hard.getF().getData(),hard.getMaxX(),hard.getMaxY());
	}

}
