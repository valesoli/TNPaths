package ar.edu.itba.algorithms.utils.interval;

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

}
