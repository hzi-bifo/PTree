package common;

public class TimeStamp {

	private final long timeStampMillis;
	
	public static TimeStamp getTimeStamp(){
		return new TimeStamp(System.currentTimeMillis());
	}
	
	private TimeStamp(long timeStampMillis){
		this.timeStampMillis = timeStampMillis;
	}
	
	public long getTimeStampMillis(){
		return timeStampMillis;
	}
}
