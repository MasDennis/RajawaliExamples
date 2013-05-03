/**
 * @author Andrew Jo (andrewjo@gmail.com)
 * www.andrewjo.com
 */
package com.monyetmabuk.rajawali.tutorials;

import javax.microedition.khronos.opengles.GL10;

import rajawali.BaseObject3D;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.TextureManager.TextureType;
import rajawali.primitives.Plane;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;

public class RajawaliTextureCompressionRenderer extends RajawaliRenderer {
	private BaseObject3D mMipmappedPlane;
	private BaseObject3D mPlane;
	
	public RajawaliTextureCompressionRenderer(Context context) {
		super(context);
		setFrameRate(60);
	}

	protected void initScene() {
		getCurrentCamera().setPosition(0, 0, 7);
		
		mPlane = new Plane(2, 2, 1, 1);
		mPlane.setMaterial(new SimpleMaterial());
		mPlane.setPosition(0, -1.25f, 0);
		mPlane.setDoubleSided(true);
		mPlane.setRotZ(90);
		mPlane.addTexture(mTextureManager.addEtc1Texture(mContext.getResources().openRawResource(R.raw.rajawali_tex_mip_0), null, TextureType.DIFFUSE));
		addChild(mPlane);
		
		mMipmappedPlane = new Plane(2, 2, 1, 1);
		mMipmappedPlane.setMaterial(new SimpleMaterial());
		mMipmappedPlane.setPosition(0, 1.25f, 0);
		mMipmappedPlane.setDoubleSided(true);
		mMipmappedPlane.setRotZ(90);
		mMipmappedPlane.addTexture(mTextureManager.addEtc1Texture(new int[] { 
				R.raw.rajawali_tex_mip_0,
				R.raw.rajawali_tex_mip_1,
				R.raw.rajawali_tex_mip_2,
				R.raw.rajawali_tex_mip_3,
				R.raw.rajawali_tex_mip_4,
				R.raw.rajawali_tex_mip_5,
				R.raw.rajawali_tex_mip_6,
				R.raw.rajawali_tex_mip_7,
				R.raw.rajawali_tex_mip_8,
				R.raw.rajawali_tex_mip_9}, TextureType.DIFFUSE));
		addChild(mMipmappedPlane);
	}
	
	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
		
		// Rotate the plane to showcase difference between a mipmapped
		// texture and non-mipmapped texture.
		mMipmappedPlane.setRotX(mMipmappedPlane.getRotX() - 0.1f);
		mMipmappedPlane.setRotY(mMipmappedPlane.getRotY() - 0.1f);
		mPlane.setRotX(mPlane.getRotX() + 0.1f);
		mPlane.setRotY(mPlane.getRotY() + 0.1f);
	}
}
