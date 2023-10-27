package ar.edu.itba.records.utils;

import ar.edu.itba.algorithms.utils.interval.Interval;
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
 
    public Interval getInterval() {
        return interval;
    }

    public IntervalAlphaTreeNode getParent() {
        return parent;
    }

    public String getAlpha() {
		return alpha;
	}
	
	private allenRel get_rel(Interval IntervalA, Interval IntervalB){
		allenRel ar = allenRel.ALPHA13;
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
				if (Long.compare(IntervalB.getEnd(),IntervalA.getStart())==1)
					if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==0)
						ar = allenRel.ALPHA04;
					else
						if (Long.compare(IntervalA.getEnd(),IntervalB.getEnd())==1)
							ar = allenRel.ALPHA03;
						else
							ar = allenRel.ALPHA05;
				else
					if (Long.compare(IntervalB.getEnd(),IntervalA.getStart())==0)
						ar = allenRel.ALPHA02;
					else
						ar = allenRel.ALPHA01;
			else
				if (Long.compare(IntervalB.getStart(),IntervalA.getEnd())==-1)
					if (Long.compare(IntervalB.getEnd(),IntervalA.getEnd())==-1)
						ar = allenRel.ALPHA09;
					else
						if (Long.compare(IntervalB.getEnd(),IntervalA.getEnd())==0)
							ar = allenRel.ALPHA10;
						else
							ar = allenRel.ALPHA11;
				else
					if (Long.compare(IntervalB.getStart(),IntervalA.getEnd())==0)
						ar = allenRel.ALPHA12;
	
		return ar;
	
	}

	

}
