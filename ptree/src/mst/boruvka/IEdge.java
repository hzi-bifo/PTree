package mst.boruvka;

import java.util.Comparator;


import java.util.Random;

import ptree.Vertex;


/**
 * Represents a weighted edge that can be added to a MST.
 * */
public class IEdge implements Comparable<IEdge>, Comparator<IEdge>  {

	Vertex v1;
	Vertex v2;
	short dist;
	byte code;
	
	private static Random rand = new Random(123);
	
	protected IEdge(Vertex v1, Vertex v2, short dist){
		this.v1 = v1;
		this.v2 = v2;
		this.dist = dist;
		synchronized (rand){
			code = (byte)rand.nextInt();
		}
	}
	
	
	protected void modify(Vertex v1, Vertex v2, short dist){
		this.v1 = v1;
		this.v2 = v2;
		this.dist = dist;
	}

	
	@Override
	public int compareTo(IEdge arg0) {
		if (this.equals(arg0)){
			return 0;
		} else {
			if (dist < arg0.dist) {
				return -1;
			} else {
				if (dist > arg0.dist){
					return 1; 
				} else {
					
					if (this.code < arg0.code){
						return -1;
					} else {
						if (this.code > arg0.code){
							return 1;
						} else {
							return (encode(this.v1.hashCode(),this.v2.hashCode()) < encode(arg0.v1.hashCode(),arg0.v2.hashCode()))?-1:1;
						}
					}
					
					//return (encode(this.v1.hashCode(),this.v2.hashCode()) < encode(arg0.v1.hashCode(),arg0.v2.hashCode()))?-1:1;
					
				/*	if (this.v2.hashCode() < arg0.v2.hashCode()){
						return -1;
					} else {
						if (this.v2.hashCode() > arg0.v2.hashCode()){
							return 1;
						} else {
							return (this.v1.hashCode() < arg0.v1.hashCode())?-1:1;
						}
					}*/
				} 
			}
		}
	}
	
	
	@Override
	public int hashCode(){
		return v1.hashCode() - v2.hashCode();
	}
	
	
	@Override
	public boolean equals(Object o){
		IEdge rec = (IEdge)o;
		return ((this.v1 == rec.v1) && (this.v2 == rec.v2))?true:false; 
	}

	
	@Override
	public int compare(IEdge o1, IEdge o2) {
		if (o1.equals(o2)){
			return 0;
		} else {
			if (o1.dist < o2.dist){ 
				return -1;
			} else { 
				if (o1.dist > o2.dist){
					return 1;
				} else {
					if (o1.code < o2.code){
						return -1;
					} else {
						if (o1.code > o2.code){
							return 1;
						} else {
							return (encode(o1.v1.hashCode(),o1.v2.hashCode()) < encode(o2.v1.hashCode(),o2.v2.hashCode()))?-1:1;
							
						}
					}
					
					/*if (o1.v2.hashCode() < o2.v2.hashCode()){ 
						return -1;
					} else {
						if (o1.v2.hashCode() > o2.v2.hashCode()){
							return 1;
						} else {
							return (o1.v1.hashCode() < o2.v1.hashCode())?-1:1;
						}*/
					}	
			}
		}
	}
	
	private long encode(int first, int second){
		return ((long)first << 32) | second;
	}
	
	
	@Override
	public String toString(){
		return ("(" + v1.toString().substring(0,9) + "," + v2.toString().substring(0,9) + "):" + dist);
	}
}

