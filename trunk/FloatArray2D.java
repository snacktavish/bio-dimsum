
public class FloatArray2D {
	public float _data[];
	private int _xsize,_ysize,_zsize;
	private int[] _size;
	
	public FloatArray2D (int xsize,int ysize) {
		_data = new float[xsize*ysize];
		_xsize = xsize;
		_ysize = ysize;

		_size = new int[3];
		_size[0] = xsize;
		_size[1] = ysize;



	}
	
	public float get(int y,int x){
		if(y>_ysize) {
			System.err.println("y > ysize");
			System.exit(1);
		}
		if(x>_xsize) {
			System.err.println("x > xsize");
		System.exit(1);
	}
		return _data[x+y*_xsize];
	}
	
	public void set(int y, int x, float val) {
		_data[x+y*_xsize] = val;
	}
	
	
	public float[] getData() {
		return _data;
	}

	
	public void add(int x, float[] data) {
		for(int j=0;j<_zsize;j++)
			_data[x+j*_xsize] = data[j]; 
	}
	
	public int[] size() {
		return _size;
	}
	
	public String toString() {
		String tmp ="";
		for(int i=0;i<_xsize;i++) {
			for(int j=0;j<_ysize;j++) {
				tmp += get(i,j)+ ", ";
	
			}
			tmp += "\n\n";
		}
		tmp +="\n";
		return tmp;
	}
}
