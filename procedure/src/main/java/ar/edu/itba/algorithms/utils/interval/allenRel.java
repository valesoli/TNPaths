package ar.edu.itba.algorithms.utils.interval;

public enum allenRel {
	ALPHA01(0),
	ALPHA02(1),
	ALPHA03(2),
	ALPHA04(3),
	ALPHA05(4),
	ALPHA06(5),
	ALPHA07(6),
	ALPHA08(7),
	ALPHA09(8),
	ALPHA10(9),
	ALPHA11(10),
	ALPHA12(11),
	ALPHA13(12);
	
	private final int value;

	allenRel(int value){
        this.value = value;
    }
  
    public int getValue(){
        return this.value;
    }

}
