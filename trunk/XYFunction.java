//  DIM SUM Version 0.9 -- Demography and Individual Migration Simulated Using a Markov chain
//  Copyright (C) 2009 Jeremy M. Brown and Kevin Savidge

//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.

//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.

//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

//  If you have questions or comments, please email JMB at jeremymbrown@gmail.com


import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;

class XYFunction {
	//public int startgeneration=-1, endgeneration=-1;
	public IntArray2D _size_gen;
	
//	private float[] f;
	public FloatArray3D _f;
	int _index = 0;

	public final static int _xsize = 0;
	public final static int _ysize = 1;

	public final static int _startgeneration = 2;
	public final static int _endgeneration = 3;
	
	//private int maxX, maxY;
	private double _fmax=-1;
	
	public XYFunction(int numGenerations, int xmax, int ymax) {
		_f = new FloatArray3D(numGenerations, xmax, ymax);
		_size_gen = new IntArray2D(numGenerations, 4);
		_index = 0;
	}
	
	public void add(Image image, String color, double maxValue, double setborder, int startgeneration, int endgeneration) throws Exception {
		double fmax=-1.0;
		_size_gen.set(_index, _startgeneration, startgeneration);
		_size_gen.set(_index, _endgeneration , endgeneration);
		//f = new float[ image.getHeight(null)* image.getWidth(null) ];
		_size_gen.set(_index, _xsize, image.getWidth(null));
		_size_gen.set(_index, _ysize, image.getHeight(null));
		if( _size_gen.get(_index, _xsize) == 0 || _size_gen.get(_index, _ysize)  == 0 )
			throw new Exception("Image map height or width is 0, this is unacceptable.");
		
		BufferedImage buffImage = new BufferedImage( image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = buffImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		
		for(int r=0; r<_size_gen.get(_index, _ysize) ; r++) {
			for(int c=0; c<_size_gen.get(_index, _xsize) ; c++) {
				int pixel = buffImage.getRGB(c,r);
				if( color.equalsIgnoreCase("red") ) pixel = (pixel >> 16) & 0xff;
				else if( color.equalsIgnoreCase("green") ) pixel = (pixel >> 8 ) & 0xff;
				else if( color.equalsIgnoreCase("blue") ) pixel = pixel & 0xff;
				else
					throw new Exception("Color attribute \""+color+"\" unrecognized.");


			/*	f[r+c*maxY] = (float) (((double)pixel/255.0) * maxValue);
				if( ((r == 0 || r == maxY-1) || (c == 0 || c == maxX-1)) && setborder != -1.0 )
					f[r+c*maxY] = (float) setborder;
				if( f[r+c*maxY] > fmax )
					fmax = f[r+c*maxY];*/
				_f.set(_index,r,c,(float) (((double)pixel/255.0) * maxValue));
				if( ((r == 0 || r == _size_gen.get(_index, _ysize) -1) || (c == 0 || c == _size_gen.get(_index, _xsize) -1)) && setborder != -1.0 )
					_f.set(_index,r,c, (float) setborder);
				if( _f.get(_index,r,c) > fmax )
					fmax = _f.get(_index,r,c);
				//if(pixel != 0)
			//		System.out.println((double)pixel/255.0+" "+_f.get(index,r,c));
			}
		}
		_fmax = fmax;
		_index++;
	//	System.out.println(_f);
	}
	
	public void add(String filename,double setborder, int startgeneration, int endgeneration) throws Exception {
		double fmax=-1.0;
		_size_gen.set(_index, _startgeneration, startgeneration);
		_size_gen.set(_index, _endgeneration , endgeneration);
		BufferedReader buff = new BufferedReader(new FileReader(filename));
		ArrayList<StringTokenizer> rows = new ArrayList<StringTokenizer>();
		while( buff.ready() ) {
			rows.add( new StringTokenizer(buff.readLine()," ") );
		}
		for(int r=0; r<rows.size(); r++) {
			if( rows.get(0).countTokens() != rows.get(r).countTokens() )
				throw new Exception("Uneven number of columns across rows in xy input file -- row "+(r+1));
		}
		
		_size_gen.set(_index, _xsize,rows.get(0).countTokens());
		_size_gen.set(_index, _ysize, rows.size());
		/*f = new float[maxY*maxX];
		
		for(int r=0; r<maxY; r++) {
			for(int c=0; c<maxX; c++) {
				f[r+c*maxY] = Float.parseFloat(rows.get(r).nextToken());
				if( ((r == 0 || r == maxY-1) || (c == 0 || c == maxX-1)) && setborder != -1.0 )
					f[r+c*maxY] = (float) setborder;
				if( f[r+c*maxY] > fmax )
					fmax = f[r+c*maxY];
			}
		}*/
		
		for(int r=0; r<_size_gen.get(_index, _ysize) ; r++) {
			for(int c=0; c<_size_gen.get(_index, _xsize) ; c++) {
				_f.set(_index,r,c, Float.parseFloat(rows.get(r).nextToken()));
				if( ((r == 0 || r == _size_gen.get(_index, _ysize) -1) || (c == 0 || c == _size_gen.get(_index, _xsize) -1)) && setborder != -1.0 )
					_f.set(_index,r,c, (float) setborder);
				if( _f.get(_index,r,c) > fmax )
					fmax = _f.get(_index,r,c);
			}
		}
		_fmax = (int)fmax;
		_index++;
	}
	
	public double f(Index i, int x,int y) {
		//System.out.println(generation +" "+ calcIndex(generation));
		return _f.get(i.value(), y, x);
	}
	
	
	int lastgen=-1 ;
	int lastindex=-1;
	public Index calcIndex(int generation) {
		if(generation== lastgen)
			return new Index(lastindex);
		int ind =-1;
		for(int i=0;i<_f.xsize(); i++)
			if( _size_gen.get(i,_startgeneration) <= generation && _size_gen.get(i,_endgeneration) >= generation )
				ind = i;;
		if( ind == -1 ) {
			for(int i=0;i<_f.xsize();i++)
				if( _size_gen.get(i,_startgeneration) == -1 && _size_gen.get(i,_endgeneration) == -1 ) {
					ind = i;
					break;
				}
		}
		if(ind != 0)
			System.out.println(ind+ " "+ generation);
		lastgen = generation;
		lastindex = ind;
		return new Index(ind);
	}
	
	public int getMaxX(Index i) {
		return _size_gen.get(i.value(), _xsize) ;
	}
	
	public int getMaxY(Index i) {
		return _size_gen.get(i.value(), _ysize) ;
	}
	
	public double fmax(Index i) {
		return _fmax;
	}
	
	public ArrayList<Integer> getWalk(Index index, double slat, double slon, double elat, double elon, double minlat, double maxlat, double minlon, double maxlon) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		//int index = calcIndex(gen);
		
		double dx = toX(index,elon,minlon,maxlon)-toX(index,slon,minlon,maxlon);
		double dy = toY(index,elat,minlat,maxlat)-toY(index,slat,minlat,maxlat);
		int nsteps = (int)(Math.max(Math.abs(dx),Math.abs(dy))+1.0);
		dx /= nsteps;
		dy /= nsteps;
		
		for(int i=1; i<=nsteps; i++)
		{
			ret.add(toX(index,slon,minlon,maxlon)+(int)Math.floor(i*dx));
			ret.add(toY(index,slat,minlat,maxlat)+(int)Math.floor(i*dy));
		}
		
		return ret;
	}
	
	public int getWalkLength(Index index, double slat, double slon, double elat, double elon, double minlat, double maxlat, double minlon, double maxlon) {
	//	ArrayList<Integer> ret = new ArrayList<Integer>();
		//int index = calcIndex(gen);
		
		double dx = toX(index,elon,minlon,maxlon)-toX(index,slon,minlon,maxlon);
		double dy = toY(index,elat,minlat,maxlat)-toY(index,slat,minlat,maxlat);
		int nsteps = (int)(Math.max(Math.abs(dx),Math.abs(dy))+1.0);
	//	dx /= nsteps;
		//dy /= nsteps;
		
	/*	for(int i=1; i<=nsteps; i++)
		{
			ret.add(toX(index,slon,minlon,maxlon)+(int)Math.floor(i*dx));
			ret.add(toY(index,slat,minlat,maxlat)+(int)Math.floor(i*dy));
		}*/
		
		return nsteps;
	}
	
	public int getWalkX(int i, float length, Index index, double slat, double slon, double elat, double elon, double minlat, double maxlat, double minlon, double maxlon) {
		//	int index = calcIndex(gen);
			
			double dy = toY(index,elat,minlat,maxlat)-toY(index,slat,minlat,maxlat);
			dy /= length;
			return toY(index,slon,minlon,maxlon)+(int)(i*dy);
		}
	
	public int getWalkY(int i, float length, Index index, double slat, double slon, double elat, double elon, double minlat, double maxlat, double minlon, double maxlon) {
		//int index = calcIndex(gen);
		
		double dx = toX(index,elon,minlon,maxlon)-toX(index,slon,minlon,maxlon);

		dx /= length;

		
		return toX(index,slon,minlon,maxlon)+(int)(i*dx);
	}

	

	
	public int toX(Index i,double lon, double minlon, double maxlon) {
	
		if( lon >= maxlon )
			return _size_gen.get(i.value(), _xsize) -1;
		if( lon <= minlon )
			return 0;
		return (int)Math.floor(_size_gen.get(i.value(), _xsize)  * (lon-minlon)/(maxlon-minlon));
	}
	
	public int toY(Index i, double lat, double minlat, double maxlat) {
		if( lat >= maxlat )
			return _size_gen.get(i.value(), _ysize) -1;
		if( lat <= minlat )
			return 0;
		return (int)Math.floor(_size_gen.get(i.value(), _ysize)  * (lat-minlat)/(maxlat-minlat));
	}
}
