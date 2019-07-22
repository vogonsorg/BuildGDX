package ru.m210projects.Build.Render;

import static com.badlogic.gdx.graphics.GL20.GL_LINEAR;
import static com.badlogic.gdx.graphics.GL20.GL_LINEAR_MIPMAP_LINEAR;
import static com.badlogic.gdx.graphics.GL20.GL_NEAREST;
import static ru.m210projects.Build.Engine.pow2long;
import static ru.m210projects.Build.OnSceenDisplay.Console.osd_argv;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Architecture.GLFrame;
import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.OnSceenDisplay.OSDCOMMAND;
import ru.m210projects.Build.OnSceenDisplay.OSDCVARFUNC;
import ru.m210projects.Build.Pattern.BuildConfig;
import ru.m210projects.Build.Render.Types.GLFilter;
import ru.m210projects.Build.Types.BuildSettings;
import ru.m210projects.Build.Types.BuildVariable;
import ru.m210projects.Build.Types.BuildVariable.RespondType;

public class GLSettings extends BuildSettings {

	public static GLFilter[] glfiltermodes = {
		new GLFilter("Retro", GL_NEAREST, GL_NEAREST), // 0
		new GLFilter("Bilinear", GL_LINEAR, GL_LINEAR), // 1
		new GLFilter("Trilinear", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR) // 2
	};

	public static BuildVariable<GLFilter> textureFilter;
	public static BuildVariable<Integer> textureAnisotropy;
	public static BuildVariable<Boolean> useHighTile;
	public static BuildVariable<Boolean> useModels;
	
	public static BuildVariable<Integer> gamma;
	public static BuildVariable<Integer> brightness;
	public static BuildVariable<Integer> contrast;

	public static BuildVariable<Boolean> animSmoothing;
	
	public static void init(final Engine engine, final BuildConfig cfg)
	{
		textureFilter = new BuildVariable<GLFilter>(cfg.glfilter < glfiltermodes.length ? glfiltermodes[cfg.glfilter] : glfiltermodes[0], "Changes the texture filtering settings") {
			@Override
			public void execute(GLFilter value) {
				GLRenderer gl = engine.glrender();
				if(gl != null)
					gl.gltexapplyprops();
				
				for(int i = 0; i < glfiltermodes.length; i++)
					if(value.equals(glfiltermodes[i])) {
						cfg.glfilter = i;
						break;
					}
			}
			
			@Override
			public GLFilter check(Object value) {
				if(value instanceof GLFilter) 
					return (GLFilter) value;
				return null;
			}
		};
		
		textureAnisotropy = new BuildVariable<Integer>(1, "Changes the texture anisotropy settings") {
			@Override
			public void execute(Integer value) { 
				GLRenderer gl = engine.glrender();
				if(gl != null)
					gl.gltexapplyprops();
				cfg.glanisotropy = value;
			}
			
			@Override
			public Integer check(Object value) {
				if(value instanceof Integer) {
					Integer anisotropy = (Integer) value;
					if (GLInfo.maxanisotropy > 1.0) {
						if (anisotropy <= 0 || anisotropy > GLInfo.maxanisotropy)
							anisotropy = (int) GLInfo.maxanisotropy;
					}
					return pow2long[checkAnisotropy(anisotropy)];
				}
				return null;
			}
			
			int checkAnisotropy(int anisotropy) {
				int anisotropysize = 0;
				for (int s = anisotropy; s > 1; s >>= 1)
					anisotropysize++;
				return anisotropysize;
			}
		};
		textureAnisotropy.set(cfg.glanisotropy);
		
		OSDCOMMAND R_texture = new OSDCOMMAND( "r_texturemode", "r_texturemode: " + GLSettings.textureFilter.getDescription(), new OSDCVARFUNC() { 
			@Override
			public void execute() {
				if (Console.osd_argc != 2) {
					Console.Println("Current texturing mode is " + GLSettings.textureFilter.get().name);
					return;
				}
				try {
					int value = Integer.parseInt(osd_argv[1]);
					if(GLSettings.textureFilter.set(glfiltermodes[value]) == RespondType.Success)
						Console.Println("Texture filtering mode changed to " + GLSettings.textureFilter.get().name);
					else Console.Println("Texture filtering mode out of range");
				} catch(Exception e)
				{
					Console.Println("r_texturemode: Out of range");
				}
			} });
		R_texture.setRange(0, 2);
		Console.RegisterCvar(R_texture);
		
		useHighTile = new BooleanVar(true, "Use true color textures from high resolution pack") {
			@Override
			public void execute(Boolean value) {
				GLRenderer gl = engine.glrender();
				if(gl != null)
					gl.gltexinvalidateall(1);
			}
		};
		useModels = new BooleanVar(true, "Use md2 / md3 models from high resolution pack");
		
		animSmoothing = new BooleanVar(true, "Use  model animation smoothing");
		
		gamma = new BuildVariable<Integer>((int) ((1 - cfg.gamma) * 4096), "Global gamma") {
			@Override
			protected void execute(Integer value) {
				cfg.gamma = (1 - (value / 4096.0f));
			}

			@Override
			protected Integer check(Object value) {
				if(value instanceof Integer) {
					float gamma = (Integer) value / 4096.0f;
					if (((GLFrame) BuildGdx.app.getFrame()).setDisplayConfiguration(1 - gamma, cfg.brightness, cfg.contrast))
						return (Integer) value;
				}
				return null;
			}
		};
		
		brightness = new BuildVariable<Integer>((int) (cfg.brightness * 4096), "Global brightness") {
			@Override
			protected void execute(Integer value) {
				cfg.brightness = value / 4096.0f;
			}

			@Override
			protected Integer check(Object value) {
				if(value instanceof Integer) {
					float brightness = (Integer) value / 4096.0f;
					if (((GLFrame) BuildGdx.app.getFrame()).setDisplayConfiguration(cfg.gamma, brightness, cfg.contrast))
						return (Integer) value;
				}
				return null;
			}
		};
		
		contrast = new BuildVariable<Integer>((int) (cfg.contrast * 4096), "Global contrast") {
			@Override
			protected void execute(Integer value) {
				cfg.contrast = value / 4096.0f;
			}

			@Override
			protected Integer check(Object value) {
				if(value instanceof Integer) {
					float contrast = (Integer) value / 4096.0f;
					if (((GLFrame) BuildGdx.app.getFrame()).setDisplayConfiguration(cfg.gamma, cfg.brightness, contrast))
						return (Integer) value;
				}
				return null;
			}
		};
	}
	
}
