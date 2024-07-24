package ar.edu.itba.algorithms.utils.interval;


public class AllenInterval {
	
	private final Interval intervalA;
	private final Interval intervalB;
	private static allenRel al = null;
	

	public AllenInterval(Interval intervalA, Interval intervalB){
		this.intervalA = intervalA;
		this.intervalB = intervalB;
		this.al = get_rel(intervalA,intervalB);
	}
	public Interval get_IntervalA(){
		return this.intervalA;
	}
	public Interval get_IntervalB(){
		return this.intervalB;
	}
	public allenRel get_allenRel(){
		return this.al;
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
	public Interval getIntervalA() {
		return intervalA;
	}
	
	  public  static int getInverse(allenRel a) {
			return 13-a.getValue();
		}
	
	public static void main(String[] args) {
	    Interval a = new Interval(30000L,40000L,Granularity.DATE);
        Interval b = new Interval(30000L,40000L,Granularity.DATE);
        AllenInterval ai = new AllenInterval(a,b);
        System.out.println(ai.get_allenRel());
        System.out.println( allenRel.getNameFromValue( getInverse( ai.get_allenRel()) ) );
	}
}
