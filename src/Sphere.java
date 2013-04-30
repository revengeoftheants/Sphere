import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import processing.core.*;

public class Sphere {
	
	int MAX_RADIUS_DIFF_NBR = 50;
	float NOISE_SCALE_NBR = 1.0f;
	int FROM_CLR_NBR, TO_CLR_NBR;
	
	PApplet parent;
	String name;
	float res; //resolution of sphere (for non-crater points only)
	int latRes;
	int lngRes;
	float planetRadius; //mean radius of the planet in kilometers
	float modelRadius; //mean radius of the model (so that it fits into the viewport)
	float zOffsetNbr = 0;
	float timeNbr = 0;
	int fillClrNbr = 0;
	float lerpClrAmt = 0;

	ArrayList<Crater> craters = new ArrayList<Crater>();  //list of all craters on planet
	ArrayList<LatitudeStrip> strips; 	//we then need an HashMap to hold each latitude strip (key: latitude)

	
	public Sphere(PApplet p, String nameValue, float resValue, float planetRadValue, float modelRadValue) {
		this.parent = p;
		this.name = nameValue;
		this.res = resValue;
		this.planetRadius = planetRadValue;
		this.modelRadius = modelRadValue;
		
		this.latRes = (int)(180/res);
		this.lngRes = (int)(360/res);
		
		FROM_CLR_NBR = parent.color(144, 29, 31);
		TO_CLR_NBR = parent.color(215, 180, 15);
	}
	
	public void createVertices() {  //load all the non-crater vertices according to the resolution
		float xOffsetNbr = 0;
		float yOffsetNbr = 0;
		timeNbr += 0.01;
		strips = new ArrayList<LatitudeStrip>();
		
		for(int i = -latRes/2; i <= latRes/2; i++) {
			float lat = res * i;
			float cosAngle = PApplet.cos(PApplet.radians(90 - lat));
			float sinAngle = PApplet.sin(PApplet.radians(res * -lngRes/2));
			
			/*
			float noiseValue = parent.noise(NOISE_SCALE_NBR * cosAngle + NOISE_SCALE_NBR, 
											NOISE_SCALE_NBR * sinAngle + NOISE_SCALE_NBR,
											timeNbr);
			*/
			float noiseValue = parent.random(0, MAX_RADIUS_DIFF_NBR);
			
			PVector initVector = Main.toCartesian(lat, 0, modelRadius + PApplet.map(noiseValue, 0, 1, 0, MAX_RADIUS_DIFF_NBR));
			
			/*
			for (Crater thisCrater : craters) {
				if (thisCrater.isPointInsideCrater(initVector)) {
					initVector = Main.toCartesian(lat, 0, modelRadius - (3 * mapToScreen((float)thisCrater.getDepth())));
				}
			}
			*/
			
			LatitudeStrip ls = new LatitudeStrip(parent, initVector);
			
			for(int j = -lngRes/2; j <= lngRes/2; j++) {
				yOffsetNbr += 0.1;
				float lng = res * j;
				
				cosAngle = PApplet.cos(PApplet.radians(90 - lat));
				sinAngle = PApplet.sin(PApplet.radians(lng));
				
				if (lat == -90 || lat == 90) {
					sinAngle = PApplet.sin(PApplet.radians(0));
				}
				/*
				noiseValue = parent.noise(NOISE_SCALE_NBR * cosAngle + NOISE_SCALE_NBR,
										  NOISE_SCALE_NBR * sinAngle + NOISE_SCALE_NBR,
										  timeNbr);
				*/
				noiseValue = parent.random(0, MAX_RADIUS_DIFF_NBR);
				
				PVector newVec = Main.toCartesian(lat, lng, modelRadius + PApplet.map(noiseValue, 0, 1, 0, MAX_RADIUS_DIFF_NBR));
				
				/*
				for (Crater thisCrater : craters) {
					if (thisCrater.isPointInsideCrater(newVec)) {
						newVec = Main.toCartesian(lat, lng, modelRadius - (3 * mapToScreen((float)thisCrater.getDepth())));
					}
				}
				*/
				ls.addPointToVertices(newVec);
			}
				
			strips.add(ls);
			xOffsetNbr += 0.1;
		}
		
		// The z-offset is used to add noise over time, so only increment it once per draw() loop.
		zOffsetNbr+=0.01;
	}
	
	
	public void loadCraters(String filename) { //taken from Sam's Driver.java
		BufferedReader br = null;
		Crater newCrater;
		
		try {
			br = new BufferedReader(new FileReader(new File(filename)));
		} catch (FileNotFoundException e) {
			//error finding file
		}
		
		//surround in try/catch to handle IOException on reading input
		//throw the exception since closing the bufferedreader could throw an IOException as well
		try {
			//reuse to store current line split into string array
			String[] line = null;
			String name;
			double latitude, longitude, craterRadius, floorDiam, rimToFloorDepth, depth, rimFlankWidth;
			int ageClass;
			
			//keep reading while there's more to read
			while (br.ready()) {
				//split the current line into an array of strings
				line = br.readLine().split("\t");
				
				name = line[0];
				latitude = getValue(line[1]);
				longitude = getValue(line[2]);
				craterRadius = getValue(line[3]);
				floorDiam = getValue(line[4]);
				rimToFloorDepth = getValue(line[5]);
				depth = getValue(line[6]);
				rimFlankWidth = getValue(line[7]);
				ageClass = Integer.valueOf(line[8]);
				
				//new crater!
				newCrater = new Crater(parent, name, latitude, longitude, craterRadius, floorDiam, rimToFloorDepth, depth, rimFlankWidth, ageClass, modelRadius);
				//add new crater to our crater arraylist
				craters.add(newCrater);
				
				//System.out.println("Added: " + newCrater);
				
				//how many points will we use to define the crater circle?
				int numberOfCraterPoints = 24;
				newCrater.createNewRim(numberOfCraterPoints);
				//angle increments around crater circle
				float angleIncr = (float) (2*Math.PI)/numberOfCraterPoints;
				
				for(int i=0; i<numberOfCraterPoints; i++) {
					float angle = i * angleIncr;
					//get all the necessary crater points at that angle
					PVector newRimPoint = newCrater.getRimPoint(angle, planetRadius);
					newCrater.craterRim[i] = newRimPoint;
					PVector newRimFlankPoint = newCrater.getRimFlankPoint(angle, planetRadius);
					newCrater.craterRimFlank[i] = newRimFlankPoint;
					PVector newFloorPoint = newCrater.getFloorPoint(angle, planetRadius);
					newCrater.craterFloor[i] = newFloorPoint;
				}
			}
		} catch(IOException e) {
			System.out.println(e.getMessage());
		} finally {
			//make sure to close reader
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void drawSphere() { //draw the sphere
        LatitudeStrip previousStrip = null;
        PVector prevStripVertex, currStripVertex;
        float lerpClrDiffAmt = 0;
        int cnt = 0;
        
    	int remainderNbr = parent.frameCount % 50;
    	if (fillClrNbr == 0 || remainderNbr == 0) {
    		
    		// A rate faster than 0.01 causes the color change to be noticeable.
    		if (lerpClrAmt > 1) {
    			lerpClrDiffAmt = -0.01f;
    		} else {
    			lerpClrDiffAmt = 0.01f;
    		}
    		fillClrNbr = (int) PApplet.lerpColor(FROM_CLR_NBR, TO_CLR_NBR, lerpClrAmt, PApplet.RGB);
    		lerpClrAmt += lerpClrDiffAmt; // A rate faster than this causes the color change to be noticeable.
    	}
        
        for(LatitudeStrip currentStrip : strips) {
        	if(cnt > 0) {
				previousStrip = strips.get(cnt-1);
			}
        	
        	parent.beginShape(PConstants.TRIANGLE_STRIP);
        	for(int j=0; j<currentStrip.vertices.length; j++) {
				if(cnt > 0) {
					prevStripVertex = previousStrip.vertices[j];
					
					parent.fill(fillClrNbr);
					parent.vertex(prevStripVertex.x, prevStripVertex.y, prevStripVertex.z);
				}
				currStripVertex = currentStrip.vertices[j];
				parent.vertex(currStripVertex.x, currStripVertex.y, currStripVertex.z);
        	}
        	parent.endShape();
        	cnt++;
        }
	}
	
	public void drawCraters() {
		for(Crater cr : this.craters) {
			parent.beginShape(PConstants.TRIANGLE_FAN);
			for(int i=0; i<cr.craterRim.length; i++) {
				PVector rimPoint = cr.craterRim[i];
				parent.vertex(rimPoint.x, rimPoint.y, rimPoint.z);
				PVector rimFlankPoint = cr.craterRimFlank[i];
				parent.vertex(rimFlankPoint.x, rimFlankPoint.y, rimFlankPoint.z );
			}
			parent.endShape(PConstants.CLOSE);
		}
	}
	
	public void printVertices() {
		for(LatitudeStrip ls : strips) {
			for(int i=0; i<ls.vertices.length; i++) {
				PVector v = ls.vertices[i];
				System.out.println(v.x + ", " + v.y + ", " + v.z);
			}
		}
	}
	
	public float mapToScreen(float initRadius) { //a function for mapping the radius of each point of the sphere from kilometers to screen pixels
		float screenRadius = PApplet.map(initRadius, 0, planetRadius, 0, modelRadius);
		return screenRadius;
	}
	

	private static double getValue(String numberString) { //safely parse an input string to a double value 
		//default value to return if input cannot be parsed into a double (e.g. if it is the empty string)
		double convertedValue = 0.0;

		try {
			convertedValue = Double.valueOf(numberString);
		} catch (NumberFormatException ex) {
			//eat the exception. we'll just return 0.0 for missing/malformed values.
		}
		return convertedValue;
	}
}
