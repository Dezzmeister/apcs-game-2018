package mapGen;

import java.util.ArrayList;

public class Junction {

	 public boolean north;
	 public boolean south;
	 public boolean east;
	 public boolean west;
	 
	 public int xPos;
	 public int yPos;
	 private double random(int c) {
		 
		return c* Math.random(); 
	 }
	 
	 public Junction(int x, int y, int direction){
	   xPos = x;
	   yPos = y;
	   north = direction ==0;
	   south = direction == 1;
	   west =direction ==2;
	   east = direction ==3;
	  }
	  public boolean checkSpace(int count, int dir,int[][]lvl){
	    if(dir == 0){
	      Junction tJ = new Junction(xPos,yPos-count,0);
	      if(lvl[tJ.xPos][tJ.yPos-1]==0&&lvl[tJ.xPos+1][tJ.yPos]==0&&lvl[tJ.xPos-1][tJ.yPos]==0&&lvl[tJ.xPos][tJ.yPos]==0){
	        return true;
	      }
	    }
	    else if(dir ==1){
	      Junction tJ = new Junction(xPos,yPos+count,0);
	       if(lvl[tJ.xPos][tJ.yPos+1]==0&&lvl[tJ.xPos+1][tJ.yPos]==0&&lvl[tJ.xPos-1][tJ.yPos]==0&&lvl[tJ.xPos][tJ.yPos]==0){
	         return true;
	       }
	    }
	    else if(dir ==2){
	      Junction tJ = new Junction(xPos-count,yPos,0);
	      if(lvl[tJ.xPos][tJ.yPos-1]==0&&lvl[tJ.xPos-1][tJ.yPos]==0&&lvl[tJ.xPos][tJ.yPos+1]==0&&lvl[tJ.xPos][tJ.yPos]==0){
	         return true;
	       }
	    }
	    else if(dir ==3){
	      Junction tJ = new Junction(xPos+count,yPos,0);
	      if(lvl[tJ.xPos][tJ.yPos-1]==0&&lvl[tJ.xPos+1][tJ.yPos]==0&&lvl[tJ.xPos][tJ.yPos+1]==0&& lvl[tJ.xPos][tJ.yPos]==0){
	         return true;
	       }
	    }
	   return false;
	   }
	  
	  public ArrayList<Junction> genRoom(int[][] lvl, int rX, int rY){
	   ArrayList<Junction> cW = new ArrayList<Junction>();
	    int[][] temp = new int[rX][rY];
	   
	   
	   int numWalls = 2+(int) random(3);
	   int wL = 3+(int)random(4);
	     for(int k = 0;k<numWalls-1;k++){
	      int wXC =1+ (int) random(rX-2); //+ (int) random(((rX-2)/numWalls)-2); 
	      int wYC =1+(int) random(rY-2); //+ (int) random(((rY-2)/numWalls)-2);
	      temp[wXC][wYC]=1;
	      for(int j=0;j<wL;j++){
	         int dir = (int) random(4);
	         if(dir==0){
	           if(wYC-1>1&&new Junction(wXC,wYC,dir).checkSpace(1,dir,temp)){
	           wYC--;
	           temp[wXC][wYC]=1;
	           }
	         }
	         else if(dir==1){
	           if(wYC+1<temp[0].length-2&&new Junction(wXC,wYC,dir).checkSpace(1,dir,temp)){
	            wYC++; 
	            temp[wXC][wYC]=1;
	           }
	         }
	         else if(dir==2){
	            if(wXC-1>1&&new Junction(wXC,wYC,dir).checkSpace(1,dir,temp)){
	           wXC--;
	            temp[wXC][wYC]=1;
	            }
	         }
	         else if(dir==3){
	            if(wXC+1<temp.length-2&&new Junction(wXC,wYC,dir).checkSpace(1,dir,temp)){
	           wXC++;
	           temp[wXC][wYC]=1;
	         }
	         
	        
	      }
	       
	       
	     }
	  
	  
	  
	  }
	   
	   for(int w=0;w<temp.length;w++){
	     for(int e=0;e<temp[0].length;e++){
	       if (temp[w][e]==1){
	         lvl[xPos+w][yPos+e]=4;
	         cW.add(new Junction(xPos+w,yPos+e,0));
	       }
	       else if(temp[w][e]==0){
	         lvl[xPos+w][yPos+e]=3;
	       }
	     }
	   }
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   return cW;
	   
	   
	   
	   
	   
	  
	  } 
	  
	  public int getAround(int[][]lvl){
	    int count = 0;
	   if(lvl[xPos][yPos-1]>=4){
	     count+=1;
	   }
	   else if(lvl[xPos][yPos+1]>=4){
	     count+=2;
	   }
	   else if(lvl[xPos-1][yPos]>=4){
	     count+=4;
	   }
	   else if(lvl[xPos+1][yPos]>=4){
	     count+=8;
	   }
	 return count+3; 
	}
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	}

