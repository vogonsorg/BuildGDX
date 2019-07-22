package ru.m210projects.Build.Types;

import java.util.ArrayList;
import java.util.List;

import ru.m210projects.Build.Pattern.MenuItems.MenuProc;
import ru.m210projects.Build.Types.BuildVariable.RespondType;

public class ParamLinker {
	
	public enum ItemType { Switch, Slider, Conteiner, Button, Separator };
	
	private List<ParamItem<?>> params = new ArrayList<ParamItem<?>>();
	
	public <T> void registerSwitch(String name, BuildVariable<T> var)
	{
		params.add(new SwitchItem<T>(name, var));
	}
	
	public <T> void registerConteiner(String name, BuildVariable<T> var, T[] variants, String[] titles)
	{
		params.add(new ConteinerItem<T>(name, var, variants, titles));
	}
	
	public <T> void registerSlider(String name, BuildVariable<Integer> var, int min, int max, int step, Integer digitalMax)
	{
		params.add(new SliderItem<Integer>(name, var, min, max, step, digitalMax));
	}
	
	public <T> void registerButton(String name, MenuProc callback)
	{
		params.add(new ButtonItem<T>(name, callback));
	}
	
	public <T> void registerSeparator()
	{
		params.add(new ParamItem<T>(ItemType.Separator));
	}

	public List<ParamItem<?>> getParamList()
	{
		return params;
	}

	public class ParamItem<T> {
		private final ItemType type;
		
		public ParamItem(ItemType type)
		{
			this.type = type;
		}
		
		public ItemType getType()
		{
			return type;
		}
	}
	
	public class ParamChoosableItem<T> extends ParamItem<T> {
		private final String name;
		protected final BuildVariable<T> var;
		
		public ParamChoosableItem(String name, BuildVariable<T> var, ItemType type)
		{
			super(type);
			this.name = name;
			this.var = var;
		}
		
		public String getName() {
			return name;
		}
		
		public BuildVariable<?> getVariable()
		{
			return var;
		}
	}
	
	public class SliderItem<T> extends ParamChoosableItem<Integer> {
		private int min, max, step;
		private Integer digitalMax;
		public SliderItem(String name, BuildVariable<Integer> var, int min, int max, int step, Integer digitalMax) {
			super(name, var, ItemType.Slider);
			
			this.min = min;
			this.max = max;
			this.step = step;
			this.digitalMax = digitalMax;
		}
		
		public boolean setValue(int value)
		{
			return var.set(value) == RespondType.Success;
		}
		
		public Integer getDigitalMax() {
			return digitalMax;
		}
		
		public int getValue() {
			return var.get();
		}
		
		public int getMin() {
			return min;
		}
		
		public int getMax() {
			return max;
		}
		
		public int getStep() {
			return step;
		}
	}
	
	public class SwitchItem<T> extends ParamChoosableItem<T> {
		public SwitchItem(String name, BuildVariable<T> var) {
			super(name, var, ItemType.Switch);
		}
		
		public boolean getState()
		{
			T out = var.get();
			if(out instanceof Boolean)
				return (Boolean) out;
			if(out instanceof Number)
				return ((Number) out).intValue() == 1;
			return false;
		}
		
		public void setState(boolean state)
		{
			T out = var.get();
			if(out instanceof Boolean)
				var.set(state);
			if(out instanceof Number)
				var.set(state ? 1 : 0);
		}
	}
	
	public class ButtonItem<T> extends ParamChoosableItem<T> {
		private MenuProc callback;
		
		public ButtonItem(String name, MenuProc callback) {
			super(name, null, ItemType.Button);
			this.callback = callback;
		}

		public MenuProc getCallback()
		{
			return callback;
		}
	}
	
	public class ConteinerItem<T> extends ParamChoosableItem<T> {
		public T[] conteiner;
		public String[] title;
		
		public ConteinerItem (String name, BuildVariable<T> var, T[] conteiner, String[] title)
		{
			super(name, var, ItemType.Conteiner);
			this.conteiner = conteiner;
			this.title = title;
		}
		
		public T getObject(int i)
		{
			return conteiner[i];
		}

		public int getIndex()
		{
			for(int i = 0; i < conteiner.length; i++)
				if(var.get().equals(conteiner[i])) 
					return i;
			return -1;
		}
	}
}
