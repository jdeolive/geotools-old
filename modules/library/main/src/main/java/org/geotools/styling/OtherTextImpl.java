package org.geotools.styling;

import org.opengis.filter.expression.Expression;

public class OtherTextImpl implements OtherText {

    String location;

    Expression text;

    public String getTarget() {
        return location;
    }

    public void setTarget(String location) {
        this.location = location;
    }

    public Expression getText() {
        return text;
    }

    public void setText(Expression text) {
        this.text = text;
    }

}
