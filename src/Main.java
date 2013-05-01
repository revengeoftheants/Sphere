import java.util.*;
import processing.core.*;
import processing.event.*;
import remixlab.proscene.*;


//***we will work in KILOMETERS and RADIANS***

public class Main extends PApplet {

	float _modelResNbr = 5;
	float _modelRadiusNbr = (float) 300.0;	//mean radius of the model in pixels (so that it fits into the viewport)
	Scene _camScene;
	Sphere _thisSphere;
	
	
	public static void main(String args[]) {
		PApplet.main(new String[] { "Main" });
	}

	
	public void setup() {
		size(1200, 800, P3D);
		frameRate(1);
		
		setupCamera();
		
		_thisSphere = new Sphere(this, _modelResNbr, _modelRadiusNbr);
	}
	
	
	public void draw() {
		background(109,108,120);
		smooth();
		ambientLight(128, 128, 128);
		directionalLight(128, 128, 128, 0, 0, -1);
		lightFalloff(1, 0, 0);
		lightSpecular(0, 0, 0);
        
        _thisSphere.createVertices();
        
        // Draw the body inside its own InteractiveFrame. You must apply its transformation in order to use its coordinate system.
        pushMatrix();
        _camScene.interactiveFrame().applyTransformation();
        //camScene.drawAxis(modelRadius + 50);
        
		//stroke(255, 129, 94);
		strokeWeight(1);
        fill(255, 94, 94);
        
        rotateX(HALF_PI);
		_thisSphere.drawSphere();
		
		stroke(255, 129, 94);
		strokeWeight(2);

		//theMoon.drawCraters();
		popMatrix();
	}
	
	
	/**
	 * Transforms spherical coordinates to Cartesian (rectilinear) coordinates.
	 * 
	 * @param lat
	 * @param lng
	 * @param elev
	 * @return
	 */
	static public PVector toCartesian(float lat, float lng, float elev) {
		
		// The polar angle lies in the domain 0-180deg and is equal to 90deg - latitude.
		float polarAngleDegrees = 90 - lat;

		double x = elev * Math.cos(Math.toRadians(lng)) * Math.sin(Math.toRadians(polarAngleDegrees));
		double y = elev * Math.sin(Math.toRadians(lng)) * Math.sin(Math.toRadians(polarAngleDegrees));
		double z = elev * Math.cos(Math.toRadians(polarAngleDegrees));

		return new PVector((float) x,(float) y,(float) z);
	}
	
	
	/**
	 * Handles keyboard key presses.
	 */
	public void keyPressed() {
		if (key=='s' || key=='S') {
			saveFrame(timestamp()+"_##.png");
		}
	}
	
	
	/**
	 * Sets up the camera.
	 */
	private void setupCamera() {
		_camScene = new Scene(this);
		// Create an InteractiveFrame within which to draw the sphere, and set the Scene so that this Interactive Frame
		// is always in focus. This will allow us to manipulate the sphere's coordinate system without manipulating the 
		// world's coordinate system.
		_camScene.setInteractiveFrame(new InteractiveFrame(_camScene));
		//camScene.interactiveFrame().rotate(new Quaternion(1, 0, 0, HALF_PI)); // This rotates the entire InteractiveFrame.
		_camScene.setDrawInteractiveFrame();
		
		// Do not draw the world grid or axes.
		_camScene.setGridIsDrawn(false);
		_camScene.setAxisIsDrawn(false);
		
		_camScene.interactiveFrame().setSpinningFriction(0.7f);
		_camScene.interactiveFrame().setTossingFriction(0.7f);
		_camScene.setRadius(_modelRadiusNbr * 1.2f);
		
		// We will use only the arcball camera profile.
		_camScene.unregisterCameraProfile("FIRST_PERSON");
		_camScene.unregisterCameraProfile("THIRD_PERSON");
		_camScene.unregisterCameraProfile("WHEELED_ARCBALL");
		
		// Remove some functionality from our arcball camera profile because we don't want the user
		// to be able to do things such as move the camera.
		CameraProfile arcballProfile = _camScene.getCameraProfiles()[0];
		arcballProfile.removeAllShortcuts();
		
		// The normal SHIFT+LEFT_ARROW action is to zoom, which we do not want.
		arcballProfile.setCameraMouseBinding(Event.SHIFT, LEFT, Scene.MouseAction.ROLL);
		// Currently middle and right mouse keys are not working in Proscene in Processing 2.
		arcballProfile.setCameraMouseBinding(Event.SHIFT, RIGHT, Scene.MouseAction.ROTATE);
		
		_camScene.showAll();
	}
	
	
	/**
	 * Gets the current timestamp, formatted.
	 * 
	 * @return Formatted timestamp.
	 */
	static private String timestamp() {
		  Calendar now = Calendar.getInstance();
		  return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", now);
		}
}
