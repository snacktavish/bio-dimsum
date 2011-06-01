
public class IntArray2D {
	public int _data[];
	private int _xsize,_ysize,_zsize;
	private int[] _size;
	
	public IntArray2D (int xsize,int ysize) {
		_data = new int[xsize*ysize];
		_xsize = xsize;
		_ysize = ysize;

		_size = new int[3];
		_size[0] = xsize;
		_size[1] = ysize;



	}
	
	public int get(int x,int y){
		return _data[x+y*_xsize];
	}
	
	public void set(int x, int y, int val) {
		_data[x+y*_xsize] = val;
	}
	

	
	public void add(int x, int[] data) {
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
