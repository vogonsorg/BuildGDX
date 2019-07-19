package ru.m210projects.Build.Types;

import java.util.ArrayList;
import java.util.List;

public class ParamLinker {
	
	public enum ItemType { Switch, Slider, Conteiner };
	
	private List<ParamItem<?>> params = new ArrayList<ParamItem<?>>();
	
	public <T> void registerSwitch(String name, BuildVariable<T> var)
	{
		params.add(new SwitchItem<T>(name, var));
	}
	
	public <T> void registerConteiner(String name, BuildVariable<T> var, T[] variants, String[] titles)
	{
		params.add(new ConteinerItem<T>(name, var, variants, titles));
	}
	
	public <T> void registerSlider(String name, BuildVariable<T> var, int min, int max)
	{
		params.add(new ParamItem<T>(name, var, ItemType.Slider));
	}
	
	public ParamItem<?> get(int index)
	{
		return params.get(index);
	}
	
	public List<ParamItem<?>> getList()
	{
		return params;
	}

	public class ParamItem<T> {
		private final String name;
		protected final BuildVariable<T> var;
		private final ItemType type;
		
		public ParamItem(String name, BuildVariable<T> var, ItemType type)
		{
			this.name = name;
			this.var = var;
			this.type = type;
		}
		
		public String getName() {
			return name;
		}
		
		public BuildVariable<?> getVariable()
		{
			return var;
		}
		
		public ItemType getType()
		{
			return type;
		}
	}
	
	public class SwitchItem<T> extends ParamItem<T> {
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
	}
	
	public class ConteinerItem<T> extends ParamItem<T> {
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
		
		public String getTitle(int i)
		{
			return title[i];
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
