package ar.edu.itba.algorithms.utils.interval;

import ar.edu.itba.algorithms.utils.time.Year;
import ar.edu.itba.algorithms.utils.interval.Interval;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public enum allenRel {
	ALPHA01(0,"alpha1"),
	ALPHA02(1,"alpha2"),
	ALPHA03(2,"alpha3"),
	ALPHA04(3,"alpha4"),
	ALPHA05(4,"alpha5"),
	ALPHA06(5,"alpha6"),
	ALPHA07(6,"alpha7"),
	ALPHA08(7,"alpha8"),
	ALPHA09(8,"alpha9"),
	ALPHA10(9,"alpha10"),
	ALPHA11(10,"alpha11"),
	ALPHA12(11,"alpha12"),
	ALPHA13(12,"alpha13");
	
	private final int value;
	private final String name;

	allenRel(int value, String name){
        this.value = value;
        this.name = name;
    }
  
    public int getValue(){
        return this.value;
    }
    
    public String getName(){
        return this.name;
    }
    
  
    
    public static String getNameFromValue(int i){
        return allenRel.values()[i-1].getName();
    }

    public static String getAllenfromStringInterval(String strIntervalA, String strIntervalB) {
    	String startA = IntervalStringifier.getStart(strIntervalA);
    	String endA = IntervalStringifier.getEnd(strIntervalA);
    	String startB = IntervalStringifier.getStart(strIntervalB);
    	String endB = IntervalStringifier.getEnd(strIntervalB);
    	Interval intervalA;
    	Interval intervalB;
    	if ((startA.equals(startB)) & (endA.equals(endB)))  return allenRel.ALPHA07.getName();
    	if (startA.equals(startB)) {
    		intervalA = IntervalParser.fromString(strIntervalA);
    		intervalB = IntervalParser.fromString(strIntervalB);
      		if (Long.compare(intervalA.getEnd(), intervalB.getEnd())==-1) return allenRel.ALPHA08.getName();
      			else return allenRel.ALPHA06.getName();
    	}
    	else
    		if (endA.equals(endB)){
        		intervalA = IntervalParser.fromString(strIntervalA);
        		intervalB = IntervalParser.fromString(strIntervalB);
          		if (Long.compare(intervalA.getStart(), intervalB.getStart())==-1) return allenRel.ALPHA10.getName();
          			else return allenRel.ALPHA04.getName();
        	}
    		else
    			if (startA.equals(endB)) return allenRel.ALPHA02.getName();
    			else if (endA.equals(startB)) return allenRel.ALPHA12.getName();
    			else {
    				intervalA = IntervalParser.fromString(strIntervalA);
            		intervalB = IntervalParser.fromString(strIntervalB);
              		if (Long.compare(intervalA.getStart(), intervalB.getStart())==-1) 
              			if (Long.compare(intervalA.getEnd(), intervalB.getStart())==-1) return allenRel.ALPHA13.getName();
              			else 
              				if (Long.compare(intervalA.getEnd(), intervalB.getEnd())==1) return allenRel.ALPHA09.getName();
              				else return allenRel.ALPHA11.getName();
              		else
              			if (Long.compare(intervalA.getStart(), intervalB.getEnd())==1) return allenRel.ALPHA01.getName(); 
              			else if (Long.compare(intervalA.getEnd(), intervalB.getEnd())==-1) return allenRel.ALPHA05.getName();
              			
    			}
    	return allenRel.ALPHA03.getName();
    }
    
    public static void main(String[] args) {
 	   String intervalA = "2022-04-10 03:50 — 2022-04-10 12:10";
 	   String intervalB = "2022-04-10 03:50 — 2022-04-10 10:10";
 	   
 	  // System.out.println(getAllenfromStringInterval(intervalA,intervalB));
 	  // System.out.println(getAllenfromStringInterval(intervalB,intervalA));
 	   
 	   List<String> a = new ArrayList<String>();
 	   a.add("a");
 	   a.add("b");
 	   a.add("c");
 	   a.add("d");
 	   System.out.println(a.size());
 	   for (Integer i= a.size()-1;i>=0;i--)
 		   System.out.println(a.get(i));
    }
}
