package ar.edu.itba.records.utils;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalStringifier;
import ar.edu.itba.algorithms.utils.interval.allenRel;


public class IntervalAlphaTreeNode {
	private IntervalAlphaTreeNode parent = null;
	private final Interval interval;
	private final String alpha;

	public IntervalAlphaTreeNode(Interval interval) {
		this.interval = interval;
		this.alpha = null;
		this.parent = null;
	}
	
	public IntervalAlphaTreeNode(Interval interval, IntervalAlphaTreeNode parent) {
		this.interval = interval;
	    this.parent = parent;
		this.alpha=get_rel(interval, parent.getInterval()).toString();
	}
	
	public IntervalAlphaTreeNode(Interval interval, IntervalAlphaTreeNode parent, Boolean out) {
		this.interval = interval;
	    this.parent = parent;
	    if (out)
	    	this.alpha=get_rel(parent.getInterval(),interval ).toString();
	    else
	    	this.alpha=get_inverse_rel(interval, parent.getInterval()).toString();
	}
 
    public Interval getInterval() {
        return interval;
    }

    public IntervalAlphaTreeNode getParent() {
        return parent;
    }

    public String getAlpha() {
		return alpha;
	}
    
    @Override
    public String toString() {
    	return interval.toString() + " " + this.getAlpha();
    }
	
	public static allenRel get_rel(Interval IntervalA, Interval IntervalB){
		
		String A = IntervalStringifier.intervalToString(IntervalA);
		String B = IntervalStringifier.intervalToString(IntervalB);
		
		allenRel ar = allenRel.ALPHA09;
		if (Long.compare(IntervalA.getStart(), IntervalB.getStart())==0){
			if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==0)
				ar = allenRel.ALPHA07;
			else
				if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==-1)
					ar = allenRel.ALPHA08;
				else
						ar = allenRel.ALPHA06;
		}
		else
			if (Long.compare(IntervalB.getStart(),IntervalA.getStart())==-1)
				if (IntervalStringifier.getStart(A).equals(IntervalStringifier.getEnd(B)))
					ar = allenRel.ALPHA02;
				else
					if (Long.compare(IntervalB.getEnd(),IntervalA.getStart())==-1)
						ar = allenRel.ALPHA01;
					else 
					    if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==0)
					    	ar = allenRel.ALPHA04;
					    else
					    	if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==1)
					    		ar = allenRel.ALPHA03;
					    	else
					    		ar = allenRel.ALPHA05;
				else
					if (IntervalStringifier.getStart(A).equals(IntervalStringifier.getEnd(B)))
						ar = allenRel.ALPHA02;
					else
						
				if (IntervalStringifier.getEnd(A).equals(IntervalStringifier.getStart(B)))
					ar = allenRel.ALPHA12;
				else
					if (Long.compare(IntervalA.getEnd(),IntervalB.getStart())==-1)
						ar = allenRel.ALPHA13;
					else
						
						if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==-1)
							ar = allenRel.ALPHA11;
					else
						if (Long.compare(IntervalB.getEnd(),IntervalA.getEnd())==0)
							ar = allenRel.ALPHA10;
				
		return ar;
	
	}

	public static allenRel get_inverse_rel(Interval IntervalA, Interval IntervalB){
		
		String A = IntervalStringifier.intervalToString(IntervalA);
		String B = IntervalStringifier.intervalToString(IntervalB);
		
		allenRel ar = allenRel.ALPHA05;
		if (Long.compare(IntervalA.getStart(), IntervalB.getStart())==0){
			if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==0)
				ar = allenRel.ALPHA07;
			else
				if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==-1)
					ar = allenRel.ALPHA06;
				else
						ar = allenRel.ALPHA08;
		}
		else
			if (Long.compare(IntervalB.getStart(),IntervalA.getStart())==-1)
				if (IntervalStringifier.getStart(A).equals(IntervalStringifier.getEnd(B)))
					ar = allenRel.ALPHA12;
				else
					if (Long.compare(IntervalB.getEnd(),IntervalA.getStart())==-1)
						ar = allenRel.ALPHA13;
					else 
					    if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==0)
					    	ar = allenRel.ALPHA10;
					    else
					    	if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==1)
					    		ar = allenRel.ALPHA11;
					    	else
					    		ar = allenRel.ALPHA09;
				else
					if (IntervalStringifier.getStart(A).equals(IntervalStringifier.getEnd(B)))
						ar = allenRel.ALPHA12;
					else
						
				if (IntervalStringifier.getEnd(A).equals(IntervalStringifier.getStart(B)))
					ar = allenRel.ALPHA02;
				else
					if (Long.compare(IntervalA.getEnd(),IntervalB.getStart())==-1)
						ar = allenRel.ALPHA01;
					else
						
						if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==-1)
							ar = allenRel.ALPHA03;
					else
						if (Long.compare(IntervalB.getEnd(),IntervalA.getEnd())==0)
							ar = allenRel.ALPHA04;
				
		return ar;
	
	}
	 public static void main(String[] args) {
		 
		 	Interval A = IntervalParser.fromStringLimits("2010","2020");
		 	Interval B = IntervalParser.fromStringLimits("2021","2024");
	    	System.out.println(get_rel(A,B));
	 }

}
