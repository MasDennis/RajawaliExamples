package com.monyetmabuk.rajawali.tutorials;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.animation.Animation3D;
import rajawali.animation.EllipticalOrbitAnimation3D;
import rajawali.animation.EllipticalOrbitAnimation3D.OrbitDirection;
import rajawali.bounds.IBoundingVolume;
import rajawali.lights.DirectionalLight;
import rajawali.materials.DiffuseMaterial;
import rajawali.math.Number3D;
import rajawali.parser.AParser.ParsingException;
import rajawali.parser.ObjParser;
import rajawali.primitives.Cube;
import rajawali.primitives.Sphere;
import rajawali.renderer.RajawaliRenderer;
import rajawali.scene.RajawaliScene;
import rajawali.scenegraph.IGraphNode.GRAPH_TYPE;
import rajawali.scenegraph.Octree;
import android.content.Context;
import android.opengl.Matrix;
import android.os.Handler;
import android.widget.TextView;

public class RajawaliSceneRenderer extends RajawaliRenderer {
	private DirectionalLight mLight1, mLight2;
	private DiffuseMaterial mMaterial;
	private BaseObject3D mInitialSphere;
	private BaseObject3D mInitialCube;
	private EllipticalOrbitAnimation3D mCameraAnim;
	private Number3D mFocal;
	private Number3D mPeriapsis;
	
	private RajawaliScene mScene1;
	private RajawaliScene mScene2; 
	
	private Handler mHandler;
	private TextView mObjectCount;
	private TextView mTriCount;
	
	private Camera mCamera1;
	private Camera mCamera2;

	private Random mRandom = new Random();
	private ArrayList<BaseObject3D> mSpheres = new ArrayList<BaseObject3D>();
	private ArrayList<BaseObject3D> mCubes = new ArrayList<BaseObject3D>();

	public RajawaliSceneRenderer(Context context, Handler handler, TextView obj, TextView tri) {
		super(context);
		setFrameRate(60);
		mHandler = handler;
		mObjectCount = obj;
		mTriCount = tri;
	}

	protected void initScene() {
		mCamera1 = getCurrentCamera(); //We will utilize the initial camera
		mCamera1.setPosition(0, 0, 20);
		mCamera1.setFieldOfView(60);
		mCamera1.setFarPlane(50);
		
		mCamera2 = new Camera(); //Lets create a second camera for the scene.
		mCamera2.setPosition(5, 0, -10);
		mCamera2.setLookAt(0.0f, 0.0f, 0.0f);
		mCamera2.setFarPlane(50);
		mCamera2.setFieldOfView(60);
		mCamera2.updateFrustum(mPMatrix,mVMatrix);
		
		//We are going to use our own scene, not the default
		mScene1 = new RajawaliScene(this, GRAPH_TYPE.OCTREE); 
		mScene1.displaySceneGraph(true);
		//Since we created a new scene, it has a default camera we need to replace
		mScene1.replaceAndSwitchCamera(mCamera1, 0); 
		mScene1.addCamera(mCamera2); //Add our second camera to the scene
		
		//We are creating a second scene
		mScene2 = new RajawaliScene(this, GRAPH_TYPE.OCTREE); 
		mScene2.displaySceneGraph(true);
		//Since we created a new scene, it has a default camera we need to replace
		mScene2.replaceAndSwitchCamera(mCamera1, 0);
		mScene2.addCamera(mCamera2); //Add our second camera to the scene
		
		mLight1 = new DirectionalLight(0, 1, -1);
		mLight2 = new DirectionalLight(0, -1, -1);

		mMaterial = new DiffuseMaterial();
		mMaterial.setUseColor(true);

		ObjParser objParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.teapot_obj);
		try {
			objParser.parse();
		} catch (ParsingException e) {
			e.printStackTrace();
		}
		//mInitialSphere = objParser.getParsedObject();
		mInitialSphere = new Sphere(1, 10, 10);
		mInitialSphere.setScale(0.250f);
		mInitialSphere.setColor(0xFF00BFFF);
		mInitialSphere.setMaterial(mMaterial);
		mInitialSphere.addLight(mLight1);
		mInitialSphere.addLight(mLight2);
		mInitialSphere.setPosition(0, 1, 0);
		mInitialSphere.setRotation(45f, 45f, 45f);
		mInitialSphere.setShowBoundingVolume(true);
		
		mInitialCube = new Cube(1);
		mInitialCube.setScale(0.250f);
		mInitialCube.setColor(0xFF00BFFF);
		mInitialCube.setMaterial(mMaterial);
		mInitialCube.addLight(mLight1);
		mInitialCube.addLight(mLight2);
		mInitialCube.setPosition(0, 1, 0);
		mInitialCube.setRotation(45f, 45f, 45f);
		mInitialCube.setShowBoundingVolume(true);

		mSpheres.add(mInitialSphere);
		mCubes.add(mInitialCube);
		mScene1.addChild(mInitialCube); //Add our cube to scene 1
		mScene2.addChild(mInitialSphere); //Add our sphere to scene 2

		Animation3D anim = new EllipticalOrbitAnimation3D(new Number3D(0, 0, -5), new Number3D(0, 0, 5), 0.0,
				OrbitDirection.CLOCKWISE);
		//Create a camera animation for camera 1
		mFocal = new Number3D(0, 0, 0);
		mPeriapsis = new Number3D(0, 0, 20);
		mCameraAnim = new EllipticalOrbitAnimation3D(mFocal, mPeriapsis, 0.0,
				OrbitDirection.CLOCKWISE);
		mCameraAnim.setDuration(10000);
		mCameraAnim.setRepeatMode(Animation3D.RepeatMode.INFINITE);
		mCameraAnim.setTransformable3D(mCamera1);
		mCameraAnim.play();
		//Register the animation with BOTH scenes
		mScene1.registerAnimation(mCameraAnim);
		mScene2.registerAnimation(mCameraAnim);
		anim.setDuration(10000);
		anim.setRepeatMode(Animation3D.RepeatMode.INFINITE);
		anim.setTransformable3D(mInitialSphere);
		//anim.play();
		mScene2.registerAnimation(anim);
		
		//Replace the default scene with our scene 1 and switch to it
		replaceAndSwitchScene(getCurrentScene(), mScene1);
		//Add scene 2 to the renderer
		addScene(mScene2);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		((RajawaliExampleActivity) mContext).showLoader();
		super.onSurfaceCreated(gl, config);
		((RajawaliExampleActivity) mContext).hideLoader();
	}

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
		Number3D tMin = getCurrentScene().getSceneMinBound();
		Number3D tMax = getCurrentScene().getSceneMaxBound();
		mFocal.x = tMin.x + (tMax.x - tMin.x) * .5f;
		mFocal.y = tMin.y + (tMax.y - tMin.y) * .5f;
		mFocal.z = tMin.z + (tMax.z - tMin.z) * .5f;
		mPeriapsis.y = mFocal.y;
		mPeriapsis.x = mFocal.x;
		getCurrentCamera().setLookAt(mFocal);
		/*float[] model = new float[16];
		Matrix.setIdentityM(model, 0);
		mCamera2.getTransformedBoundingVolume()
			.drawBoundingVolume(getCurrentCamera(), mPMatrix, mVMatrix, model);
		*/
		int length;
		if (getCurrentScene().equals(mScene2)) {
			length = mSpheres.size();
			for (int i = 0; i < length; ++i) {
				IBoundingVolume bcube = mSpheres.get(i).getGeometry().getBoundingBox();
				bcube.transform(mSpheres.get(i).getModelMatrix());
			}
		} else if (getCurrentScene().equals(mScene1)) {
			length = mCubes.size();
			for (int i = 0; i < length; ++i) {
				IBoundingVolume bcube = mCubes.get(i).getGeometry().getBoundingBox();
				bcube.transform(mCubes.get(i).getModelMatrix());
			}
		}
		if (mFrameCount % 20 == 0) { 
			mHandler.post(new Runnable() {
				public void run() {
					mObjectCount.setText("Object Count: " + getCurrentScene().getNumChildren());
					mTriCount.setText("   Triangle Count: " + getCurrentScene().getNumTriangles());
				}
			});
		}
	}

	public void addObject(float x, float y) {
		BaseObject3D obj = null;
		if (getCurrentScene().equals(mScene2)) {
			obj = new Sphere(1, 10, 10);
		} else if (getCurrentScene().equals(mScene1)) {
			obj = new Cube(1);
		}
		obj.addLight(mLight1);
		obj.addLight(mLight2);
		obj.setMaterial(mMaterial);
		obj.setShowBoundingVolume(true);
		obj.setScale(mRandom.nextFloat()*0.5f+0.1f);
		obj.setColor(new Number3D(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255)));
		boolean positive = mRandom.nextBoolean();
		int sign1 = 1;
		int sign2 = 1;
		if (positive) {sign1 = 1;} else {sign1 = -1;}
		positive = mRandom.nextBoolean();
		if (positive) {sign2 = 1;} else {sign2 = -1;}
		obj.setPosition(sign1*mRandom.nextFloat()*4, sign2*mRandom.nextFloat()*2, -mRandom.nextFloat()*10);
		obj.setRotation(45f, 45f, 45f);
		mSpheres.add(obj);
		addChild(obj);
	}
	
	public void removeObject() {
		if (getCurrentScene().equals(mScene2)) {
			BaseObject3D child = mSpheres.get(0);
			removeChild(child);
			mSpheres.remove(child);
		} else if (getCurrentScene().equals(mScene1)) {
			BaseObject3D child = mCubes.get(0);
			removeChild(child);
			mCubes.remove(child);
		}
	}
	
	public void nextCamera() {
		if (getCurrentCamera().equals(mCamera1)) {
			getCurrentScene().switchCamera(mCamera2);
		} else {
			getCurrentScene().switchCamera(mCamera1);
		}
	}
	
	public void nextScene() {
		if (getCurrentScene().equals(mScene1)) {
			switchScene(mScene2);
		} else {
			switchScene(mScene1);
		}
	}
}