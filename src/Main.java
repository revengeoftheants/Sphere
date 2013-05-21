import java.util.*;
import processing.core.*;
import processing.event.*;
import ddf.minim.*;
import remixlab.proscene.*;


/**
 * 
 * @author Kevin Dean
 *
 */
public class Main extends PApplet {
	
	// ***************************************
	// Global Constants
	// ***************************************
	String EXT_DAT_FILE_PATH_TXT = "Song of Los_Minim_FFTFreqBand_FrameRate30_BufferSize1024.txt";
	int TARGET_FRAME_RATE = 30;
	
	// *************
	// Global Variables
	// *************
	float _modelResNbr = 10;
	float _modelRadiusLenNbr = (float) 300.0;	//mean radius of the model in pixels (so that it fits into the viewport)
	Sphere _sphere;
	Minim _minim;
	AudioPlayer _player;
	Scene _camScene;
	
	
	// ***************************************
	// Public Methods
	// ***************************************
	/**
	 * Main method for running outside the Processing IDE.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		PApplet.main(new String[] { "Main" });
	}

	
	/**
	 * Setup.
	 */
	public void setup() {
		size(1200, 800, P3D);
		frameRate(TARGET_FRAME_RATE);
		
		setupCamera();
		
		_sphere = new Sphere(this, _modelResNbr, _modelRadiusLenNbr, EXT_DAT_FILE_PATH_TXT);
		
		_minim = new Minim(this);
		_player = _minim.loadFile("Song of Los.wav", 1024);
	}
	
	
	/**
	 * Draw loop.
	 */
	public void draw() {		
		background(109,108,120);
		smooth();
		ambientLight(128, 128, 128);
		directionalLight(128, 128, 128, 0, 0, -1);
		lightFalloff(1, 0, 0);
		lightSpecular(0, 0, 0);
		
		text("Frame rate: " + frameRate, -550, -350);
		
        //_sphere.createVertices((frameCount % _sphere.rtrvAudioFrameCnt()) - 1);
		//_sphere.createVertices(millis()/TARGET_FRAME_RATE);
        
        // Draw the body inside its own InteractiveFrame. You must apply its transformation in order to use its coordinate system.
        pushMatrix();
        _camScene.interactiveFrame().applyTransformation();
        //_camScene.drawAxis(_modelRadiusLenNbr + 50);
        
		strokeWeight(1);
        fill(255, 94, 94);
        
        rotateX(HALF_PI);
		_sphere.drawSphere((frameCount % _sphere.rtrvAudioFrameCnt()) - 1);
		
		popMatrix();
		
		if (frameCount > 1 && _player.isPlaying() == false) {
			exit();
		} else if (frameCount == 1) {
			_player.play();
		}
	}
	
	
	/**
	 * Transforms spherical coordinates to Cartesian (rectilinear) coordinates.
	 * 
	 * @param inpLatitudeDegNbr
	 * @param inpLongitudeDegNbr
	 * @param inpRadiusLenNbr
	 * @return
	 */
	static public PVector toCartesian(float inpLatitudeDegNbr, float inpLongitudeDegNbr, float inpRadiusLenNbr) {
		
		// The polar angle lies in the domain 0-180deg and is equal to 90deg - latitude.
		float polarAngleDegNbr = 90 - inpLatitudeDegNbr;

		double x = inpRadiusLenNbr * Math.cos(Math.toRadians(inpLongitudeDegNbr)) * Math.sin(Math.toRadians(polarAngleDegNbr));
		double y = inpRadiusLenNbr * Math.sin(Math.toRadians(inpLongitudeDegNbr)) * Math.sin(Math.toRadians(polarAngleDegNbr));
		double z = inpRadiusLenNbr * Math.cos(Math.toRadians(polarAngleDegNbr));

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
	 * Performs cleanup when program terminates.
	 */
	public void stop() {
		_player.close();
		_minim.stop();
		super.stop();
	}
	
	
	
	// ***************************************
	// Private Methods
	// ***************************************
	
	/**
	 * Sets up the camera.
	 */
	private void setupCamera() {
		_camScene = new Scene(this);
		_camScene.setFrameRate(TARGET_FRAME_RATE);
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
		_camScene.setRadius(_modelRadiusLenNbr * 1.2f);
		
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
