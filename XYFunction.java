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
	public int startgeneration=-1, endgeneration=-1;
	
	double[][] f;
	int maxX, maxY;
	double fmax=-1.0;
	
	public XYFunction(Image image, String color, double maxValue, double setborder) throws Exception {
		f = new double[ image.getHeight(null) ][ image.getWidth(null) ];
		maxX = image.getWidth(null);
		maxY = image.getHeight(null);
		if( maxX == 0 || maxY == 0 )
			throw new Exception("Image map height or width is 0, this is unacceptable.");
		
		BufferedImage buffImage = new BufferedImage( image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = buffImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		
		for(int r=0; r<f.length; r++) {
			for(int c=0; c<f[0].length; c++) {
				int pixel = buffImage.getRGB(c,r);
				if( color.equalsIgnoreCase("red") ) pixel = (pixel >> 16) & 0xff;
				else if( color.equalsIgnoreCase("green") ) pixel = (pixel >> 8 ) & 0xff;
				else if( color.equalsIgnoreCase("blue") ) pixel = pixel & 0xff;
				else
					throw new Exception("Color attribute \""+color+"\" unrecognized.");
				f[r][c] = ((double)pixel/255.0) * maxValue;
				if( ((r == 0 || r == f.length-1) || (c == 0 || c == f[0].length-1)) && setborder != -1.0 )
					f[r][c] = setborder;
				if( f[r][c] > fmax )
					fmax = f[r][c];
			}
		}
	}
	
	public XYFunction(String filename,double setborder) throws Exception {
		BufferedReader buff = new BufferedReader(new FileReader(filename));
		ArrayList<StringTokenizer> rows = new ArrayList<StringTokenizer>();
		while( buff.ready() ) {
			rows.add( new StringTokenizer(buff.readLine()," ") );
		}
		for(int r=0; r<rows.size(); r++) {
			if( rows.get(0).countTokens() != rows.get(r).countTokens() )
				throw new Exception("Uneven number of columns across rows in xy input file -- row "+(r+1));
		}
		
		maxX = rows.get(0).countTokens();
		maxY = rows.size();
		f = new double[maxY][maxX];
		
		for(int r=0; r<maxY; r++) {
			for(int c=0; c<maxX; c++) {
				f[r][c] = Double.parseDouble(rows.get(r).nextToken());
				if( ((r == 0 || r == f.length-1) || (c == 0 || c == f[0].length-1)) && setborder != -1.0 )
					f[r][c] = setborder;
				if( f[r][c] > fmax )
					fmax = f[r][c];
			}
		}
	}
	
	public double f(int x,int y) {
		return f[y][x];
	}
	
	public int getMaxX() {
		return maxX;
	}
	
	public int getMaxY() {
		return maxY;
	}
	
	public double fmax() {
		return fmax;
	}
	
	public ArrayList<Integer> getWalk(double slat, double slon, double elat, double elon, double minlat, double maxlat, double minlon, double maxlon) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		double dx = toX(elon,minlon,maxlon)-toX(slon,minlon,maxlon);
		double dy = toY(elat,minlat,maxlat)-toY(slat,minlat,maxlat);
		int nsteps = (int)(Math.max(Math.abs(dx),Math.abs(dy))+1.0);
		dx /= nsteps;
		dy /= nsteps;
		
		for(int i=1; i<=nsteps; i++)
		{
			ret.add(toX(slon,minlon,maxlon)+(int)Math.floor(i*dx));
			ret.add(toY(slat,minlat,maxlat)+(int)Math.floor(i*dy));
		}
		
		return ret;
	}
	

	
	public int toX(double lon, double minlon, double maxlon) {
		if( lon >= maxlon )
			return maxX-1;
		if( lon <= minlon )
			return 0;
		return (int)Math.floor(maxX * (lon-minlon)/(maxlon-minlon));
	}
	public int toY(double lat, double minlat, double maxlat) {
		if( lat >= maxlat )
			return maxY-1;
		if( lat <= minlat )
			return 0;
		return (int)Math.floor(maxY * (lat-minlat)/(maxlat-minlat));
	}
}