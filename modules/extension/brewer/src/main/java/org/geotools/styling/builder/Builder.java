package org.geotools.styling.builder;

interface Builder<T> {
	
	Builder<T> reset();
	
	T build();
}
