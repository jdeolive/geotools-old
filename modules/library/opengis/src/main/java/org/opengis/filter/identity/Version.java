package org.opengis.filter.identity;

import java.util.Date;

/**
 * Union type class for the {@code Version} Union type in FES 2.0
 * 
 * @invariant {@code #getVersionAction() != null || #getIndex() != null || #getDateTime() != null}
 */
public final class Version {

    private final VersionAction versionAction;

    private final Integer index;

    private final Date dateTime;

    public Version(final VersionAction action) {
        if (action == null) {
            throw new IllegalArgumentException("action can't be null");
        }

        this.versionAction = action;
        this.index = null;
        this.dateTime = null;
    }

    public Version(final Integer index) {
        if (index == null) {
            throw new IllegalArgumentException("index can't be null");
        }

        this.versionAction = null;
        this.index = index;
        this.dateTime = null;
    }

    public Version(final Date dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime can't be null");
        }

        this.versionAction = null;
        this.index = null;
        this.dateTime = dateTime;
    }

    public VersionAction getVersionAction() {
        return versionAction;
    }

    public Integer getIndex() {
        return index;
    }

    public Date getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Version)) {
            return false;
        }
        Version v = (Version) o;
        return equals(versionAction, v.getVersionAction()) && equals(index, v.getIndex())
                && equals(dateTime, v.getDateTime());
    }

    private boolean equals(Object o1, Object o2) {
        return (o1 == o2) || (o1 != null && o1.equals(o2));
    }

    @Override
    public int hashCode() {
        return 17 * (versionAction != null ? versionAction.hashCode() : (index != null ? index
                .hashCode() : dateTime.hashCode()));
    }
}
