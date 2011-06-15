import java.awt.Color;
import java.util.ArrayList;


public class FatNode extends Node{
	double last_value=0.0;
	String last_check="";
	String last_at="";
	boolean hb_final=false;					// ADDED BY JMB
	boolean sb_final_1=false;					// ADDED BY JMB
	boolean sb_final_2=false;					// ADDED BY JMB
	boolean moved = false;					// ADDED BY JMB -- 10.19.09 --  AS A MARKER TO SEE IF INDIVIDUALS WERE PROPERLY MOVED
	double par_lat = 0.0;					// ADDED BY JMB -- 10.19.09
	double par_lon = 0.0;					// ADDED BY JMB -- 10.19.09
	double end_move_lat = 0.0;				// ADDED BY JMB -- 10.19.09
	double end_move_lon = 0.0;				// ADDED BY JMB -- 10.19.09
	boolean failed_one = false;				// ADDED BY JMB -- 10.19.09
	boolean failed_two = false;				// ADDED BY JMB -- 10.19.09
	boolean added_nextgen = false;			// ADDED BY JMB -- 10.19.09
	double last_d = 0.0;					// ADDED BY JMB -- 10.19.09
	double last_crs = 0.0;					// ADDED BY JMB -- 10.19.09
	static int lastGen=0,lastUnique=0;
	int generation,unique;
	double lat=0.0,lon=0.0;
	Node parent=null;
	ArrayList<Node> children=new ArrayList<Node>();
	Color c;
	
	public FatNode(int gen, Node par) {
		super(gen,par);
	}
}
