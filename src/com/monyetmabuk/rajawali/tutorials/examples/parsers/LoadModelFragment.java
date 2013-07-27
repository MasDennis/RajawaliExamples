package com.monyetmabuk.rajawali.tutorials.examples.parsers;

import rajawali.BaseObject3D;
import rajawali.animation.Animation3D;
import rajawali.animation.Animation3D.RepeatMode;
import rajawali.animation.EllipticalOrbitAnimation3D;
import rajawali.animation.EllipticalOrbitAnimation3D.OrbitDirection;
import rajawali.animation.RotateAnimation3D;
import rajawali.lights.PointLight;
import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;
import rajawali.parser.AParser.ParsingException;
import rajawali.parser.ObjParser;
import android.content.Context;

import com.monyetmabuk.rajawali.tutorials.R;
import com.monyetmabuk.rajawali.tutorials.examples.AExampleFragment;

public class LoadModelFragment extends AExampleFragment {

	@Override
	protected AExampleRenderer createRenderer() {
		return new LoadModelRenderer(getActivity());
	}

	private final class LoadModelRenderer extends AExampleRenderer {
		private PointLight mLight;
		private BaseObject3D mObjectGroup;
		private Animation3D mCameraAnim, mLightAnim;

		public LoadModelRenderer(Context context) {
			super(context);
		}

		protected void initScene() {
			mLight = new PointLight();
			mLight.setPosition(0, 0, 4);
			mLight.setPower(3);
			getCurrentCamera().setZ(16);

			ObjParser objParser = new ObjParser(mContext.getResources(),
					mTextureManager, R.raw.multiobjects_obj);
			try {
				objParser.parse();
				mObjectGroup = objParser.getParsedObject();
				mObjectGroup.addLight(mLight);
				addChild(mObjectGroup);

				mCameraAnim = new RotateAnimation3D(Axis.Y, 360);
				mCameraAnim.setDuration(8000);
				mCameraAnim.setRepeatMode(RepeatMode.INFINITE);
				mCameraAnim.setTransformable3D(mObjectGroup);
			} catch (ParsingException e) {
				e.printStackTrace();
			}

			mLightAnim = new EllipticalOrbitAnimation3D(new Vector3(),
					new Vector3(0, 10, 0), Vector3.getAxisVector(Axis.Z), 0,
					360, OrbitDirection.CLOCKWISE);

			mLightAnim.setDuration(3000);
			mLightAnim.setRepeatMode(RepeatMode.INFINITE);
			mLightAnim.setTransformable3D(mLight);

			registerAnimation(mCameraAnim);
			registerAnimation(mLightAnim);

			mCameraAnim.play();
			mLightAnim.play();
		}

	}

}
