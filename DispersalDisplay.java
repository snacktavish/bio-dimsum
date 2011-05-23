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


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import javax.imageio.ImageIO;

class DispersalDisplayData
{
	public int generation;
	public ArrayList<Node> thisGeneration;
	
	public DispersalDisplayData(ArrayList<Node> tG, int gen)
	{
		generation =gen;
		thisGeneration = tG;
	}
}

//public class DispersalDisplay extends JFrame implements ItemListener
public class DispersalDisplay extends Thread implements ItemListener
{
	public ArrayList<Node> thisGeneration;
	public DispersalSettings ds;
	public BufferedImage bi;
	public BufferedImage background;
	
	JCheckBoxMenuItem carryingcapacity, hardborders, softborders, samplerects;
	boolean bcc=true, bhb=true, bsb=true, bsr=false;
	
	public ArrayList<DispersalDisplayData> dataQueue = new ArrayList<DispersalDisplayData>();
	
	public boolean sourceTerminated = false;
	
	public void run()
	{
		
		try {
			while( !sourceTerminated )
			{
				while( dataQueue.size() == 0 && !sourceTerminated )
					sleep(100);
				if( dataQueue.size() > 0 ) {
					process(dataQueue.get(0).thisGeneration, dataQueue.get(0).generation);
					dataQueue.remove(0);
				}
			}
			while( dataQueue.size() > 0 )
			{
				System.out.println("Outputting generation "+dataQueue.get(0).generation);
				process(dataQueue.get(0).thisGeneration, dataQueue.get(0).generation);
				dataQueue.remove(0);
			}
		
		}
		catch( Exception e )
		{
			System.out.println("Exception in DispersalDisplay thread!");
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
	}
	
	public synchronized void update(ArrayList<Node> population, int genNumber)
	{
		if (genNumber % ds.visualfreq == 0 || genNumber == 1)
			dataQueue.add( new DispersalDisplayData(population,genNumber) );
	}
	
	public synchronized void terminateSource()
	{
		sourceTerminated = true;
	}
	
	public DispersalDisplay(DispersalSettings ds0)
	{
		thisGeneration = null;
		ds = ds0;
		
/*		super("Dispersal Display");
		
		bi = new BufferedImage(800,600,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0,800,600);
		
		
		JLabel label = new JLabel(new ImageIcon(bi));
		this.add(label);
		
		JMenuBar menbar = new JMenuBar();
		JMenu show = new JMenu("Show");
		carryingcapacity = new JCheckBoxMenuItem("Carrying Capacity");
		hardborders = new JCheckBoxMenuItem("Hard Borders");
		softborders = new JCheckBoxMenuItem("Soft Borders");
		samplerects = new JCheckBoxMenuItem("Sample Rectangles");
		carryingcapacity.addItemListener(this);
		hardborders.addItemListener(this);
		softborders.addItemListener(this);
		samplerects.addItemListener(this);
		show.add(carryingcapacity);
		show.add(hardborders);
		show.add(softborders);
		menbar.add(show);
		setJMenuBar(menbar);
		
		this.pack();
		this.show();*/
	}
	
	public void drawBackground(XYFunction cc, XYFunction hb, XYFunction sb)
	{
		background = new BufferedImage(800,600,BufferedImage.TYPE_INT_ARGB);
				
		Graphics2D g2d = background.createGraphics();
		g2d.setColor(new Color(0,0,0,255));
		g2d.fillRect(0,0,background.getWidth(),background.getHeight());
		
		
		for(int x=0;x<background.getWidth();x++) {
			for(int y=0;y<background.getHeight();y++) {
				int r=0,g=0,b=0;
				if( bcc ) {
					g = (int)Math.floor(255.0*cc.f((int)Math.floor((double)cc.getMaxX()*x/background.getWidth()),(int)Math.floor((double)cc.getMaxY()*y/background.getHeight()))/cc.fmax());
				}
				if( bhb ) {
					r = (int)Math.floor(255.0*hb.f((int)Math.floor((double)hb.getMaxX()*x/background.getWidth()),(int)Math.floor((double)hb.getMaxY()*y/background.getHeight()))/hb.fmax());
				}
				if( bsb ) {
					b = (int)Math.floor(255.0*sb.f((int)Math.floor((double)sb.getMaxX()*x/background.getWidth()),(int)Math.floor((double)sb.getMaxY()*y/background.getHeight()))/sb.fmax());
				}
				
				g2d.setColor(new Color(r,g,b,100));
				g2d.drawLine( x,y,x,y);
			}
		}

	}
	
	XYFunction lcc=null, lsb=null, lhb=null;
	
	public void process(ArrayList<Node> population, int genNumber) throws Exception
	{
		thisGeneration = population;
		//Dispersion.generation = genNumber;
		
		if( lcc != ds.getCarryingCapacity(genNumber) || lsb != ds.getSoftBorders(genNumber) || lhb != ds.getHardBorders(genNumber) ) {
			lcc = ds.getCarryingCapacity(genNumber);
			lhb = ds.getHardBorders(genNumber);
			lsb = ds.getSoftBorders(genNumber);
			
			drawBackground(lcc,lhb,lsb);
		}
		
		bi = new BufferedImage(background.getWidth(),background.getHeight(),BufferedImage.TYPE_INT_ARGB);
		
		drawGen(genNumber);
		
		String filename = ""+genNumber;
		while( filename.length() < 4 ) filename = "0"+filename;
		filename = "Generation"+filename+".png";
		
		ImageIO.write(bi,"png",new File(filename));
	}
	
	public void drawGen(int genNumber) throws Exception
	{
		Graphics2D g2d = bi.createGraphics();
		g2d.drawImage(background,0,0,null);

		for(int i=0; i<thisGeneration.size(); i++) {
			int x = (int)(bi.getWidth()*(thisGeneration.get(i).lon - ds.getMinLon()) / (ds.getMaxLon() - ds.getMinLon()));
			int y = (int)(bi.getHeight()*(thisGeneration.get(i).lat - ds.getMinLat()) / (ds.getMaxLat() - ds.getMinLat()));
			g2d.setColor( thisGeneration.get(i).c );
			g2d.drawLine(x,y,x+1,y+1);
			g2d.drawLine(x+1,y,x,y+1);
		}
		
		g2d.setColor(new Color(255,255,255,255));
		g2d.drawString(("Generation: "+genNumber), 8, 15);
		g2d.drawString(("Size: "+thisGeneration.size()), 8, 33);
		
		//this.repaint();
	}
	
	public void itemStateChanged(ItemEvent e) 
	{
		Object source = e.getItemSelectable();
		
		if( source == carryingcapacity ) {
			bcc = carryingcapacity.getState();
		}
		else if( source == hardborders ) {
			bhb = hardborders.getState();
		}
		else if( source == softborders ) {
			bsb = softborders.getState();
		}
		else if( source == samplerects ) {
			bsr = samplerects.getState();
		}
		
		try {
			//update();
		}
		catch( Exception e2 ) {
			System.out.println("Unable to update display!");
		}
	}
}