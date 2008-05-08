package org.geotools.filter;

import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

public class SortByImpl implements SortBy {

	PropertyName propertyName;
	
	SortOrder sortOrder;
	
	public SortByImpl( PropertyName propertyName, SortOrder sortOrder ) {
		this.propertyName = propertyName;
		this.sortOrder = sortOrder;
	}
	
	public PropertyName getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(PropertyName propertyName) {
		this.propertyName = propertyName;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

}
