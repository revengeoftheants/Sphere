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
	
	PApplet _parApp;
	float _resNbr; //resolution of sphere
	int _latResNbr;
	int _lngResNbr;
	float _modelRadiusNbr; //mean radius of the model (so that it fits into the viewport)
	float _zOffsetNbr = 0;
	float _tmNbr = 0;
	int _fillClrNbr = 0;
	float _lerpClrAmt = 0;
	float _lerpClrDiffAmt = 0;
	ArrayList<LatitudeStrip> _strips; 	//we then need an HashMap to hold each latitude strip (key: latitude)

	
	public Sphere(PApplet inpParApp, float inpResNbr, float modelRadValue) {
		_parApp = inpParApp;
		_resNbr = inpResNbr;
		_modelRadiusNbr = modelRadValue;
		
		_latResNbr = (int)(180/_resNbr);
		_lngResNbr = (int)(360/_resNbr);
		
		FROM_CLR_NBR = _parApp.color(144, 29, 31);
		TO_CLR_NBR = _parApp.color(215, 180, 15);
	}
	
	public void createVertices() {  //load all the non-crater vertices according to the resolution
		float xOffsetNbr = 0;
		float yOffsetNbr = 0;
		_tmNbr += 0.01;
		_strips = new ArrayList<LatitudeStrip>();
		
		for(int i = -_latResNbr/2; i <= _latResNbr/2; i++) {
			float lat = _resNbr * i;
			float cosAngle = PApplet.cos(PApplet.radians(90 - lat));
			float sinAngle = PApplet.sin(PApplet.radians(_resNbr * -_lngResNbr/2));
			
			/*
			float noiseValue = parent.noise(NOISE_SCALE_NBR * cosAngle + NOISE_SCALE_NBR, 
											NOISE_SCALE_NBR * sinAngle + NOISE_SCALE_NBR,
											timeNbr);
			*/
			float noiseNbr = _parApp.random(0, MAX_RADIUS_DIFF_NBR);
			
			PVector initVector = Main.toCartesian(lat, 0, _modelRadiusNbr + noiseNbr);
			
			/*
			for (Crater thisCrater : craters) {
				if (thisCrater.isPointInsideCrater(initVector)) {
					initVector = Main.toCartesian(lat, 0, modelRadius - (3 * mapToScreen((float)thisCrater.getDepth())));
				}
			}
			*/
			
			LatitudeStrip ls = new LatitudeStrip(_parApp, initVector);
			
			for(int j = -_lngResNbr/2; j <= _lngResNbr/2; j++) {
				yOffsetNbr += 0.1;
				float lng = _resNbr * j;
				
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
				noiseNbr = _parApp.random(0, MAX_RADIUS_DIFF_NBR);
				
				PVector newVec = Main.toCartesian(lat, lng, _modelRadiusNbr + noiseNbr);
				
				/*
				for (Crater thisCrater : craters) {
					if (thisCrater.isPointInsideCrater(newVec)) {
						newVec = Main.toCartesian(lat, lng, modelRadius - (3 * mapToScreen((float)thisCrater.getDepth())));
					}
				}
				*/
				ls.addPointToVertices(newVec);
			}
				
			_strips.add(ls);
			xOffsetNbr += 0.1;
		}
		
		// The z-offset is used to add noise over time, so only increment it once per draw() loop.
		_zOffsetNbr+=0.01;
	}
	
	
	/**
	 * Draws the sphere.
	 */
	public void drawSphere() {
        LatitudeStrip previousStrip = null;
        PVector prevStripVertex, currStripVertex;
        int cnt = 0;
        
    	int remainderNbr = _parApp.frameCount % 25;
    	if (_fillClrNbr == 0 || remainderNbr == 0) {
    		
    		// A rate faster than 0.01 causes the color change to be noticeable.
    		if (_lerpClrAmt <= 0 || (_lerpClrAmt < 1 && _lerpClrDiffAmt > 0)) {
    			_lerpClrDiffAmt = 0.01f;
    		} else if (_lerpClrAmt >= 1 || (_lerpClrAmt > 0 && _lerpClrDiffAmt < 0)) {
    			_lerpClrDiffAmt = -0.01f;
    		} else {
    			_lerpClrDiffAmt = 0.01f;
    		}
    		
    		_fillClrNbr = (int) PApplet.lerpColor(FROM_CLR_NBR, TO_CLR_NBR, _lerpClrAmt, PApplet.RGB);
    		_lerpClrAmt += _lerpClrDiffAmt; // A rate faster than this causes the color change to be noticeable.
    	}
        
        for(LatitudeStrip currentStrip : _strips) {
        	if(cnt > 0) {
				previousStrip = _strips.get(cnt-1);
			}
        	
        	_parApp.beginShape(PConstants.TRIANGLE_STRIP);
        	for(int j=0; j<currentStrip.vertices.length; j++) {
				if(cnt > 0) {
					prevStripVertex = previousStrip.vertices[j];
					
					_parApp.stroke(_fillClrNbr);
					//_parApp.noStroke();
					_parApp.fill(_fillClrNbr);
					_parApp.vertex(prevStripVertex.x, prevStripVertex.y, prevStripVertex.z);
				}
				currStripVertex = currentStrip.vertices[j];
				_parApp.vertex(currStripVertex.x, currStripVertex.y, currStripVertex.z);
        	}
        	_parApp.endShape();
        	cnt++;
        }
	}

	
	public void printVertices() {
		for(LatitudeStrip ls : _strips) {
			for(int i=0; i<ls.vertices.length; i++) {
				PVector v = ls.vertices[i];
				System.out.println(v.x + ", " + v.y + ", " + v.z);
			}
		}
	}
	
	/*
	public float mapToScreen(float initRadius) { //a function for mapping the radius of each point of the sphere from kilometers to screen pixels
		float screenRadius = PApplet.map(initRadius, 0, planetRadius, 0, modelRadius);
		return screenRadius;
	}
	*/
}
