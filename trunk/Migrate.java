public class Migrate
{	
	//TODO:	remove
	public DispersalSettings ds;
	
	
	public final static int __DIMSUM_PARAMI_RANDINDEX =0;
	public final static int __DIMSUM_PARAMI_GENERATION =4;
	public final static int __DIMSUM_PARAMI_NUMCHILDREN =5;
	public final static int __DIMSUM_NODE_lat = 0;
	public final static int __DIMSUM_NODE_lon = 1;

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
		this.sb_DATA = ds.softborders._f.getData();
		sb_SIZE = ds.softborders._f.size();
		sb_META = ds.softborders._size_gen.getData();
		this.hb_DATA = ds.hardborders._f.getData();
		hb_SIZE = ds.hardborders._f.size();
		hb_META = ds.hardborders._size_gen.getData();
		//DispersalFunctionC.setArrays(sb_DATA,sb_META,sb_SIZE,hb_DATA,hb_META,hb_SIZE);
		this.randArray = randArray;
		//System.out.println(System.getProperty("java.library.path"));
		//System.out.println(System.getProperty("java.vm.name"));
		
		//System.loadLibrary("DispersalFunctionC");
		//DispersalFunctionC.setRandArray(randArray);
		
		this.ds = ds;
	}
	
	/*
	public void migrateLoop(double[] children, int rm[], double d[], double[] paramd, int[] parami)
	{
		/*double[] childrentmp =new double[children.length];
		System.arraycopy(children, 0, childrentmp,0 , children.length);
		int[] rmtmp =new int[rm.length];
		System.arraycopy(rm, 0, rmtmp,0 , rm.length);
		int[] pitmp =new int[parami.length];
		System.arraycopy(parami, 0, pitmp,0 , parami.length);
		double[] pdtmp =new double[paramd.length];
		System.arraycopy(paramd, 0, pdtmp,0 , paramd.length);
		*/
		
		/*for(int i=0; i<parami[__DIMSUM_PARAMI_NUMCHILDREN]; i++) {
			double di = ds.getDispersalRadius(parami[__DIMSUM_PARAMI_GENERATION],nextRand(parami));
			
			//if(children[2*i] != childrentmp[2*i] || children[2*i+1] != childrentmp[2*i+1] )
				//System.out.println(children[2*i] +" "+ childrentmp[2*i] +" "+ children[2*i+1] +" "+ childrentmp[2*i+1] );
			DispersalFunctionC.migrate(children, di, rm ,parami,paramd,i);
			//migrate(children, di, rm ,parami,paramd,i);
			//di = ds.getDispersalRadius(pitmp[__DIMSUM_PARAMI_GENERATION],nextRand(pitmp));
			//migrate(childrentmp, di, rmtmp ,pitmp,pdtmp,i);
			
		//	if(children[2*i] != childrentmp[2*i] || children[2*i+1] != childrentmp[2*i+1] )
			//	System.out.println(children[2*i] +" "+ childrentmp[2*i] +" "+ children[2*i+1] +" "+ childrentmp[2*i+1] +" "+ rm[i] +" "+ rmtmp[i]);
		}*/
	//}
	
	

	




	
	double nextRand(int[] _index) {
		/*if(_index[__DIMSUM_PARAMI_RANDINDEX]>=_index[__DIMSUM_PARAMI_RANDMAX]) {
			System.err.println("nextRand(): index>numRand");
			System.exit(-1);
		}*/
		double r =  randArray[_index[__DIMSUM_PARAMI_RANDINDEX]];
		_index[__DIMSUM_PARAMI_RANDINDEX]++;
		
		return r;
	}
	
}
