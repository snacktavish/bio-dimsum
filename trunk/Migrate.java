public class Migrate
{	
	//TODO:	remove
	public DispersalSettings ds;
	
	
	public final static int __DIMSUM_NODE_lat = 0;
	public final static int __DIMSUM_NODE_lon = 1;
	public final static int __DIMSUM_PARAMI_RANDINDEX =8;
	//public final static int __DIMSUM_PARAMI_RANDMAX =1;
	public final static int __DIMSUM_PARAMI_SB_INDEX =0;
	public final static int __DIMSUM_PARAMI_HB_INDEX =1;
	public final static int __DIMSUM_PARAMI_GENERATION =2;
	public final static int __DIMSUM_PARAMI_NUMCHILDREN =3;
	public final static int  __DIMSUM_PARAMI_SB_XSIZE =4;
	public final static int  __DIMSUM_PARAMI_SB_YSIZE =5;
	public final static int  __DIMSUM_PARAMI_HB_XSIZE =6;
	public final static int  __DIMSUM_PARAMI_HB_YSIZE =7;

	public final static double PI = 3.1415926536;// (double)Math.PI; 

	private double[] randArray;
	private float[] sb_DATA;
	private int[] sb_META;
	private int[] sb_SIZE;
	private float[] hb_DATA;
	private int[] hb_META;
	private int[] hb_SIZE;
		
	public Migrate(DispersalSettings ds, double[] randArray)	// Modified by JMB -- 4.5.10
	{
		//this.ds = ds;
	/*	this.sb_DATA = ds.softborders._f.getData();
		sb_SIZE = ds.softborders._f.size();
		sb_META = ds.softborders._size_gen.getData();
		this.hb_DATA = ds.hardborders._f.getData();
		hb_SIZE = ds.hardborders._f.size();
		hb_META = ds.hardborders._size_gen.getData();
		//DispersalFunctionC.setArrays(sb_DATA,sb_META,sb_SIZE,hb_DATA,hb_META,hb_SIZE);
		this.randArray = randArray;*/
		//System.out.println(System.getProperty("java.library.path"));
		//System.out.println(System.getProperty("java.vm.name"));
		
		//System.loadLibrary("DispersalFunctionC");
		//DispersalFunctionC.setRandArray(randArray);
		
		this.ds = ds;
	}

}
