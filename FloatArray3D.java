
public class FloatArray3D {
	public float _data[];
	private int _xsize,_ysize,_zsize;
	private int[] _size;
	
	public FloatArray3D (int xsize,int ysize, int zsize) {
		
		_data = new float[xsize*ysize*zsize];
		_xsize = xsize;
		_ysize = ysize;
		_zsize = zsize;
		_size = new int[3];
		_size[0] = xsize;
		_size[1] = ysize;

		_size[2] = zsize;
		System.out.println("YSIZE"+_ysize);

	}
	
	public float get(int x,int z, int y){
		
		if(x+y*_xsize+z*_xsize*_ysize<0)
			System.out.println(x+ " "+y+" "+z);
		return _data[x+y*_xsize+z*_xsize*_ysize];
	}
	
	public void set(int x, int z, int y, float val) {
		
		_data[x+y*_xsize+z*_xsize*_ysize] = val;
	}
	/*
	public void add(int x, float[][] data) {
		for(int i=0;i<_ysize;i++)
			for(int j=0;j<_zsize;j++)
				_data[x+i*_xsize+j*_xsize*_ysize] = data[i][j]; 
	}
	
	public void add(int x, int y, float[] data) {
		for(int j=0;j<_zsize;j++)
			_data[x+y*_xsize+j*_xsize*_ysize] = data[j]; 
	}
	*/
	public int[] size() {
		return _size;
	}
	public int xsize() {
		return _size[0];
	}
	
	public String toString() {
		String tmp ="";
		for(int i=0;i<_xsize;i++) {
			for(int j=0;j<_ysize;j++) {
				tmp += "(";
				for(int k=0;k<_zsize;k++) {
					tmp += get(i,j,k)+ ", ";
				}
				tmp += ") ";
			}
			tmp += "\n\n";
		}
		tmp +="\n";
		return tmp;
	}
	
	public float[] getData() {
		return _data;
	}
	
	

}
