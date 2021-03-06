package project.android.imageprocessing.filter.effect;

import project.android.imageprocessing.filter.BasicFilter;
import android.graphics.PointF;
import android.opengl.GLES20;

/**
 * Performs a vignetting effect, fading out the image at the edges
 * colour: The colour outside
 * center: The starting location for the vignetting effect
 * start, end: The directional intensity of the vignetting
 * @author Chris Batt
 */
public class VignetteFilter extends BasicFilter {
	protected static final String UNIFORM_CENTER = "u_Center";
	protected static final String UNIFORM_COLOUR = "u_Colour";
	protected static final String UNIFORM_START = "u_Start";
	protected static final String UNIFORM_END = "u_End";
	
	private int centerHandle;
	private int colourHandle;
	private int startHandle;
	private int endHandle;
	private PointF center;
	private float[] colour;
	private float start;
	private float end;
	
	public VignetteFilter(PointF center, float[] colour, float start, float end) {
		this.center = center;
		this.colour = colour;
		this.start = start;
		this.end = end;
	}
		
	@Override
	protected String getFragmentShader() {
		return 
				 "precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform vec2 "+UNIFORM_CENTER+";\n"
				+"uniform vec3 "+UNIFORM_COLOUR+";\n"
				+"uniform float "+UNIFORM_START+";\n"
				+"uniform float "+UNIFORM_END+";\n"
				
		  		+"void main(){\n"
		  		+"	lowp vec4 color = texture2D("+UNIFORM_TEXTURE0+", "+VARYING_TEXCOORD+");\n"
		  		+" 	mediump float d = distance("+VARYING_TEXCOORD+", "+UNIFORM_CENTER+");\n"
			    +" 	lowp float percent = smoothstep("+UNIFORM_START+", "+UNIFORM_END+", d);\n"
			    +" 	gl_FragColor = vec4(mix(color.rgb, "+UNIFORM_COLOUR+", percent), color.a);\n"
		  		+"}\n";
	}
	
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		centerHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_CENTER);
		colourHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_COLOUR);
		startHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_START);
		endHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_END);
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform2f(centerHandle, center.x, center.y);
		GLES20.glUniform3f(colourHandle, colour[0], colour[1], colour[2]);
		GLES20.glUniform1f(startHandle, start);
		GLES20.glUniform1f(endHandle, end);
	}
	     
	     
}
