/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.caching.firstdraft.quadtree;

import java.util.ArrayList;
import org.geotools.caching.firstdraft.CacheEntry;


public class NodeCacheEntry implements CacheEntry {
    protected ArrayList linkedFeatures = new ArrayList();
    private final Node node;
    private final Integer key;
    private int hits;
    private long creationTime;
    private long lastAccessTime;
    private long oldestChildAccessTime;
    private boolean valid = false;

    public NodeCacheEntry(Node node) {
        this.node = node;
        key = new Integer(node.getIdentifier());
        hits = 0;
        creationTime = System.currentTimeMillis();
        lastAccessTime = creationTime;
        oldestChildAccessTime = -1;
    }

    public long getCost() {
        // TODO Auto-generated method stub
        return -1;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getExpirationTime() {
        // TODO Auto-generated method stub
        return -1;
    }

    public int getHits() {
        return hits;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public long getLastUpdateTime() {
        return -1;
    }

    public long getVersion() {
        // TODO Auto-generated method stub
        return -1;
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        valid = false;
        linkedFeatures.clear();
    }

    public void setValid() {
        valid = true;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return node;
    }

    public Object setValue(Object arg0) {
        throw new UnsupportedOperationException();
    }

    public void hit() {
        hits++;

        if ((node.parent != null) && (lastAccessTime == node.parent.entry.oldestChildAccessTime)) {
            Node current = node.parent;

            while (current != null) {
                current.entry.oldestChildAccessTime = -1;
                current = current.parent;
            }
        }

        lastAccessTime = System.currentTimeMillis();
    }

    public long getOldestChildAccess() {
        if (oldestChildAccessTime == -1) {
            updateOldestChildAccess();
        }

        return oldestChildAccessTime;
    }

    protected void updateOldestChildAccess() {
        if (node.isLeaf()) {
            oldestChildAccessTime = lastAccessTime;
        } else {
            assert (node.getChildrenCount() > 1);

            long latest = node.getSubNode(0).entry.getOldestChildAccess();

            for (int i = 1; i < node.getChildrenCount(); i++) {
                long nextlatest = node.getSubNode(i).entry.getOldestChildAccess();

                if (nextlatest < latest) {
                    latest = nextlatest;
                }
            }

            oldestChildAccessTime = latest;
        }
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("Level=" + node.getLevel() + " Parent="
            + ((node.parent == null) ? 0 : node.parent.id) + " Node=" + node.id);
        ret.append(" Hits: " + hits);
        ret.append(" lastAccess: " + lastAccessTime);
        ret.append(" oldestChildAccess: " + oldestChildAccessTime);

        return ret.toString();
    }
}
