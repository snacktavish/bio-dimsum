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


class RCFunction {
	public int startgeneration=-1, endgeneration=-1;
	
	double[][] f;
	int nRows,nColumns;
	double border;
	
	double fmax=-1.0;
	DispersalSettings settings;
	
	public RCFunction(DispersalSettings ds, Image image, String color, double maxValue, double setborder) throws Exception {
		settings = ds;
		border = setborder;
		
		f = new double[image.getHeight(null)][image.getWidth(null)];
		nRows = f.length;
		nColumns = f[0].length;
		if( nRows == 0 || nColumns == 0 )
			throw new Exception("Image height or width is equal to zero.");
		
		BufferedImage buffImage = new BufferedImage( image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = buffImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		
		for(int r=0; r<f.length; r++) {
			for(int c=0; c<f[0].length; c++) {
				int pixel = buffImage.getRGB(c,r);
				
				if( color.equalsIgnoreCase("red") ) pixel = (pixel >> 16) & 0xff;
				else if( color.equalsIgnoreCase("green") ) pixel = (pixel >> 8 ) & 0xff;
				else if( color.equalsIgnoreCase("blue") ) pixel = pixel & 0xff;
				else throw new Exception("Color attribute \""+color+"\" unrecognized.");
				
				f[r][c] = ((double)pixel/255.0) * maxValue;
				if( f[r][c] > fmax )
					fmax = f[r][c];
			}
		}
	}
	
	public RCFunction(DispersalSettings ds, String filename,double setborder) throws Exception {
		settings = ds;
		border = setborder;
		
		BufferedReader buff = new BufferedReader(new FileReader(filename));
		ArrayList<StringTokenizer> rows = new ArrayList<StringTokenizer>();
		while( buff.ready() ) {
			rows.add( new StringTokenizer(buff.readLine()," ") );
		}
		for(int r=0; r<rows.size(); r++) {
			if( rows.get(0).countTokens() != rows.get(r).countTokens() )
				throw new Exception("Uneven number of columns across rows in xy input file -- row "+(r+1));
		}
		
		nRows = rows.size();
		nColumns = rows.get(0).countTokens();
		f = new double[nRows][nColumns];
		
		for(int r=0; r<nRows; r++) {
			for(int c=0; c<nColumns; c++) {
				f[r][c] = Double.parseDouble(rows.get(r).nextToken());

				if( f[r][c] > fmax )
					fmax = f[r][c];
			}
		}
	}
	
	public double f(int x,int y) {
		if( x < 0 || x >= nColumns || y < 0 || y >= nRows )
			return border;
		return f[y][x];
	}
	
	
}