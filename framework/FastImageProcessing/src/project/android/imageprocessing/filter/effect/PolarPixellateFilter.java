package project.android.imageprocessing.filter.effect;

import project.android.imageprocessing.filter.BasicFilter;
import android.graphics.PointF;
import android.opengl.GLES20;

/**
 * Applies a pixellation effect on an image or video in a spiral
 * fractionalWidthOfAPixel: How large the pixels are, as a fraction of the width and height of the image (0.0 - 1.0)
 * center: The point that the polar pixellization will begin at and spiral around.
 * @author Chris Batt
 */
public class PolarPixellateFilter extends BasicFilter {
	private static final String UNIFORM_CENTER = "u_Center";
	private static final String UNIFORM_FRACTIONAL_SIZE = "u_FractionalSize";
	
	private int fractionalSizeHandle;
	private int centerHandle;
	private PointF fractionalSize;
	private PointF center;
	
	public PolarPixellateFilter(PointF center, PointF fractionalSize) {
		this.center = center;
		this.fractionalSize = fractionalSize;
	}
	
	@Override
	protected String getFragmentShader() {
		return 
				"precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform vec2 "+UNIFORM_FRACTIONAL_SIZE+";\n"	
				+"uniform vec2 "+UNIFORM_CENTER+";\n"
				
		  		+"void main(){\n"
		  		+"  highp vec2 normCoord = 2.0 * "+VARYING_TEXCOORD+" - 1.0;\n"
			    +"  highp vec2 normCenter = 2.0 * "+UNIFORM_CENTER+" - 1.0;\n"
			    +"  normCoord -= normCenter;\n"
			    +"  highp float r = length(normCoord);\n" // to polar coords 
			    +"  highp float phi = atan(normCoord.y, normCoord.x);\n" // to polar coords 
			    +"  r = r - mod(r, "+UNIFORM_FRACTIONAL_SIZE+".x) + 0.03;\n"
			    +"  phi = phi - mod(phi, "+UNIFORM_FRACTIONAL_SIZE+".y);\n"
			    +"  normCoord.x = r * cos(phi);\n"
			    +"  normCoord.y = r * sin(phi);\n"
			    +"  normCoord += normCenter;\n"
			    +"  mediump vec2 textureCoordinateToUse = normCoord / 2.0 + 0.5;\n"
			    +"  gl_FragColor = texture2D("+UNIFORM_TEXTURE0+", textureCoordinateToUse);\n"
		  		+"}\n";	
	}
	
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		fractionalSizeHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_FRACTIONAL_SIZE);
		centerHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_CENTER);
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform2f(fractionalSizeHandle, fractionalSize.x, fractionalSize.y);
		GLES20.glUniform2f(centerHandle, center.x, center.y);
	}
}
