package project.android.imageprocessing.filter.colour;

import android.opengl.GLES20;
import project.android.imageprocessing.filter.BasicFilter;

/**
 * A image levels filter extension of BasicFilter.
 * This filter works like the levels filter in photoshop and can be use to adjust histographs as well
 * as gamma levels.  
 * Values for min and max levels of both input and output should be in [0, 1]. 
 * The gamma value works like the {@link ImageGammaFilter} and should be in [0, 3] for normal use case.
 * @author Chris Batt
 */
public class ImageLevelsFilter extends BasicFilter {
	private static final String UNIFORM_GAMMA = "u_Gamma";
	private static final String UNIFORM_MAXIN = "u_MaxIn";
	private static final String UNIFORM_MAXOUT = "u_MaxOut";
	private static final String UNIFORM_MININ = "u_MinIn";
	private static final String UNIFORM_MINOUT = "u_MinOut";
	private float gamma;
	private float minIn;
	private float maxIn;
	private float minOut;
	private float maxOut;
	private int gammaHandle;
	private int minInHandle;
	private int maxInHandle;
	private int minOutHandle;
	private int maxOutHandle;
	
	/**
	 * Creates a ImageLevelsFilter with the given min, gamma, max input levels and min and max output levels.
	 * @param minIn
	 * The minimum level of the input image.
	 * @param maxIn
	 * The maximum level of the input image.
	 * @param gamma
	 * The gamma adjust value.
	 * @param minOut
	 * The minimum level of the output image.
	 * @param maxOut
	 * The maximum level of the output image.
	 */
	public ImageLevelsFilter(float minIn, float maxIn, float gamma, float minOut, float maxOut) {
		if(gamma < 0) {
			gamma = 0;
		}
		this.gamma = gamma;
		this.minIn = minIn;
		this.minOut = minOut;
		this.maxIn = maxIn;
		this.maxOut = maxOut;
	}
	
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		gammaHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_GAMMA);
		minInHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MININ);
		maxInHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MAXIN);
		minOutHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MINOUT);
		maxOutHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MAXOUT);
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform1f(gammaHandle, gamma);
		GLES20.glUniform1f(minInHandle, minIn);
		GLES20.glUniform1f(maxInHandle, maxIn);
		GLES20.glUniform1f(minOutHandle, minOut);
		GLES20.glUniform1f(maxOutHandle, maxOut);
	} 
	
	/*
	 ** Gamma correction
	 ** Details: http://blog.mouaif.org/2009/01/22/photoshop-gamma-correction-shader/
	 */

	/*
	 ** Levels control (input (+gamma), output)
	 ** Details: http://blog.mouaif.org/2009/01/28/levels-control-shader/
	 */
	@Override
	protected String getFragmentShader() {
		return 
				 "#define GammaCorrection(color, gamma)												pow(color, 1.0 / gamma)\n"
				+"#define LevelsControlInputRange(color, minInput, maxInput)						min(max(color - minInput, vec3(0.0)) / (maxInput - minInput), vec3(1.0))\n"
				+"#define LevelsControlInput(color, minInput, gamma, maxInput)						GammaCorrection(LevelsControlInputRange(color, minInput, maxInput), gamma)\n"
				+"#define LevelsControlOutputRange(color, minOutput, maxOutput) 					mix(minOutput, maxOutput, color)\n"
				+"#define LevelsControl(color, minInput, gamma, maxInput, minOutput, maxOutput) 	LevelsControlOutputRange(LevelsControlInput(color, minInput, gamma, maxInput), minOutput, maxOutput)\n"
				
				+"precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform float "+UNIFORM_GAMMA+";\n"
				+"uniform float "+UNIFORM_MININ+";\n"
				+"uniform float "+UNIFORM_MAXIN+";\n"
				+"uniform float "+UNIFORM_MINOUT+";\n"
				+"uniform float "+UNIFORM_MAXOUT+";\n"
				
		  		+"void main(){\n"
		  		+"   vec4 color = texture2D("+UNIFORM_TEXTURE0+", "+VARYING_TEXCOORD+");\n"
				+"   gl_FragColor = vec4(LevelsControl(color.rgb, vec3("+UNIFORM_MININ+"), vec3("+UNIFORM_GAMMA+"), vec3("+UNIFORM_MAXIN+"), vec3("+UNIFORM_MINOUT+"), vec3("+UNIFORM_MAXOUT+")), color.a);\n"
				+"}\n";
	}
}