package net.preibisch.distribution.algorithm.task;

public interface Params {

	public Params fromJson(String path) throws Exception;
	public void toJson(String path);
	
}
