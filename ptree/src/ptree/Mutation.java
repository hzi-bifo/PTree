package ptree;

/**
 * Represents one point mutation.
 * (Character_from, Character_to, position)
 * 
 * Positions in DNAs are counted from 1. 
 * */
public class Mutation {

	private final byte fromChar;
	private final byte toChar;
	private short position;
	
	/**
	 * Constructor.
	 * 
	 * @param position is counted from 1
	 * */
	protected Mutation(byte fromChar, int position, byte toChar){
		this.fromChar = fromChar;
		this.position = (short)position;
		this.toChar = toChar;
	}
	
	
	protected Mutation(Mutation m, int newPosition){
		this.fromChar = m.fromChar;
		this.toChar = m.toChar;
		this.position = (short)newPosition;
	}
	
	protected void setPosition(int position){
		this.position = (short)position;
	}
	
	
	/**
	 * Get the inverse mutation of this mutation.
	 * */
	protected Mutation getInverse(){
		return new Mutation(this.toChar,(int)this.position,this.fromChar);
	}
	
	
	protected byte getFromChar(){
		return this.fromChar;
	}
	
	
	public byte getToChar(){
		return this.toChar;
	}


	/**
	 * @return the position is counted from 1
	 * */
	public int getPosition(){
		return (int)this.position;
	}
	
	
	@Override
	public String toString(){
		return String.valueOf((char)fromChar) + position + String.valueOf((char)toChar);
	}
	
	
	@Override
	public boolean equals(Object arg){
		Mutation m = (Mutation)arg;
		if (this.position == m.position && this.fromChar == m.fromChar && this.toChar == m.toChar){
			return true;
		} else {
			return false;
		}
	}
	
	
	@Override
	public int hashCode(){
		return (int)(this.fromChar*this.position*this.toChar);
	}
	
}
