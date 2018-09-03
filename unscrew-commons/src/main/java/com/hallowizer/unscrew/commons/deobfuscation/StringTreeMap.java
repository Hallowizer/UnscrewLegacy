package com.hallowizer.unscrew.commons.deobfuscation;

import java.util.HashMap;
import java.util.Map;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class StringTreeMap extends HashMap<String,Map<String,String>> {
	private static final long serialVersionUID = 4252673409894295261L;
	
	public StringTreeMap(int size) {
		super(size);
	}
	
	@Override
	public Map<String,String> get(Object key) {
		if (!(key instanceof String))
			return null;
		
		return containsKey(key) ? super.get(key) : put((String) key, new HashMap<>());
	}
}
