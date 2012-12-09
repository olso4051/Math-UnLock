package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EZ {
	public static <T> List<T> list(T... objects) {
		return new ArrayList<T>(Arrays.asList(objects));
	}

	public static <T> List<T> list(Collection<T> collection) {
		return new ArrayList<T>(collection);
	}

	public static <T> Set<T> set(T... objects) {
		return set(Arrays.asList(objects));
	}

	public static <T> Set<T> set(Collection<T> collection) {
		return new HashSet<T>(collection);
	}
}
