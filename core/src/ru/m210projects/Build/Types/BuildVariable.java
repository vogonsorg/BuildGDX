package ru.m210projects.Build.Types;

public abstract class BuildVariable<T> {
	
	public static enum RespondType { Success, Fail, Description }
	
	private T value;
	private String description;
	
	protected abstract void execute(T value);
	
	protected abstract T check(Object value);

	public BuildVariable(T set, String description)
	{
		this.value = set;
		this.description = description;
	}
	
	public RespondType set(Object i)
	{
		if(i == null) return RespondType.Description;
		
		T val = check(i);
		if(val != null) {
			execute(value = val);
			return RespondType.Success;
		} 
		
		return RespondType.Fail;
	}
	
	public T get() {
		return value;
	}

	public String getDescription()
	{
		return description;
	}
}
