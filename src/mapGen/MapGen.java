package mapGen;
import java.util.ArrayList;
public class MapGen {

	private static double random(int c) {
		 
		return c* Math.random(); 
	 }
	
	public static int[][] genMap(){
		
		 ArrayList<ArrayList<Junction>> cWalls=new ArrayList<ArrayList<Junction>>();

		 
		 int numRooms=30;
		 int roomSizeMin=5;
		  int roomSizeMax=13;

		  int i;
		 ArrayList<Junction> jMap = new ArrayList<Junction>();
		 ArrayList<Junction> fullJMap = new ArrayList<Junction>();
		 int [][] map = new int[60][60];
		
		 jMap.add(new Junction(2,map.length/2,2));
		  fullJMap.add(new Junction(2,map.length/2,2));
		  for(int k=0;k<map.length;k++){
		    for(int p = 0;p<map.length;p++){
		      if(k==0||k==map.length-1||p==0||p==map.length-1){
		        map[k][p]=1;
		      }}}
		  
		  
		  
		  while(jMap.size()>0){
		   int dir = (int) random(4); 
		   int L = (int) random(15)+4;
		   Junction temp =  jMap.get(jMap.size()-1);
		   map[temp.xPos][temp.yPos] = 1;
		   if(temp.north&&temp.south&&temp.east&&temp.west){
		    jMap.remove(jMap.size()-1);

		   }
		   else{
		     if(dir==0&&!temp.north){
		       jMap.get(jMap.size()-1).north = true;
		       for( i =1;i<L&&temp.checkSpace(i,dir,map) ;i++){
		         map[temp.xPos] [temp.yPos-i]=1;
		       }
		      if(i==1){}else{
		       jMap.add(new Junction(temp.xPos,temp.yPos-i,0));
		       fullJMap.add(new Junction(temp.xPos,temp.yPos-i,0));
		      }
		     }
		     else if(dir==1&&!temp.south){
		     jMap.get(jMap.size()-1).south = true;
		     for( i =1;i<L&&temp.checkSpace(i,dir,map);i++){
		         map[temp.xPos] [temp.yPos+i]=1;
		       }
		       if(i==1){}else{
		       jMap.add(new Junction(temp.xPos,temp.yPos+i,1));
		       fullJMap.add(new Junction(temp.xPos,temp.yPos+i,1));
		       }
		   }
		     else if(dir == 2&&!temp.west){
		     jMap.get(jMap.size()-1).west = true;
		     for( i =1;i<L&&temp.checkSpace(i,dir,map);i++){
		          map[temp.xPos-i] [temp.yPos]=1;
		       }
		       if(i==1){}else{
		         jMap.add(new Junction(temp.xPos-i,temp.yPos,2));
		         fullJMap.add(new Junction(temp.xPos-i,temp.yPos,2));
		       } }
		    else if(dir==3&&!temp.east){
		      jMap.get(jMap.size()-1).east = true;
		       for( i =1;i<L&&temp.checkSpace(i,dir,map) ;i++){
		         map[temp.xPos+i] [temp.yPos]=1;
		       }
		       if(i==1){}else{
		       jMap.add(new Junction(temp.xPos+i,temp.yPos,3));
		       jMap.add(new Junction(temp.xPos+i,temp.yPos,3));
		       }
		    }
	}
	
	
		  }
	
	
		  
		  
		  
		  
		  
		  
		  
		  
		  
		  
		  
		  
		  for(int j=0;j<numRooms;j++){
			    Junction temp = fullJMap.get( 2 + (int) random(fullJMap.size()-3));
			    if(temp.xPos+1+roomSizeMax>map.length||temp.yPos+1+roomSizeMax>map[0].length){
			    j--;
			    }
			    else{
			      int x= 1+roomSizeMin + (int) random(roomSizeMax-roomSizeMin);
			      int y= 1+roomSizeMin + (int) random(roomSizeMax-roomSizeMin);
			    cWalls.add(  temp.genRoom(map,x,y));
			    }
			    
			   }  
		  
		  
		  
		  
		  
		  
		  
		  for(int k=0;k<map.length;k++){
			   for(int p = 0;p<map.length;p++){
			     if(k==0||k==map.length-1||p==0||p==map.length-1){
			       map[k][p]=0;
			     }}}
		  
		  
		 
		  
		  
		  for(int b=0;b<cWalls.size();b++){
			    for(int t = 0;t<cWalls.get(b).size();t++){
			      map[cWalls.get(b).get(t).xPos][cWalls.get(b).get(t).yPos]=cWalls.get(b).get(t).getAround(map);
			  
			    }
			  }
	
		  map[fullJMap.get((fullJMap.size()-1)/2).xPos][fullJMap.get((fullJMap.size()-1)/2).yPos]=2;
		  
		  return map;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	}
}

	
	
	
	
	
	
	
	
	
	

