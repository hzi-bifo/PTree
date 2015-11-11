package tests;

public class StatInt {

	private long processedIntermediates;
	
	public StatInt(){
		this.processedIntermediates = 0;
	}
	
	public void addIntermediates(long num){
		processedIntermediates += num;
	}
	
	public long getProcessedIntermediates(){
		return processedIntermediates;
	}
	
}
