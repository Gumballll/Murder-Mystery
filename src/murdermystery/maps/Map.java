package murdermystery.maps;

public class Map {
	String name;
	public Integer[] spawnPoint;
	public Integer[] startPoint;
	public String baseMapName;
	
	public Map(String n,Integer[] spawnpoint,Integer[] startpoint,String bmn) {
		this.name = n;
		this.spawnPoint = spawnpoint;
		this.startPoint = startpoint;
		this.baseMapName = bmn;
	}
}
