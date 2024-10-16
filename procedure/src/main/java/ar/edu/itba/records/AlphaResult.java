package ar.edu.itba.records;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AlphaResult {
	private List<List<String>> st_intervals;
	private List<List<String>> st_alphas;
	
	public AlphaResult(List<List<String>> st_intervals, List<List<String>> st_alphas) {
		this.st_intervals=st_intervals;
		this.st_alphas=st_alphas;
	}
	
	public AlphaResult(Set<List<String>> st_intervals, List<List<String>> st_alphas) {
		List<List<String>> list_set = new ArrayList<>();
		list_set.addAll(st_intervals);
		this.st_intervals=list_set;
		this.st_alphas=st_alphas;
	}
	
	public List<List<String>> getSt_intervals() {
		return st_intervals;
	}
	public void setSt_intervals(List<List<String>> st_intervals) {
		this.st_intervals = st_intervals;
	}
	public List<List<String>> getSt_alphas() {
		return st_alphas;
	}
	public void setSt_alphas(List<List<String>> st_alphas) {
		this.st_alphas = st_alphas;
	}

}
