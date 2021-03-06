/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.data.state;

import java.util.HashMap;
import java.util.Map;

public class StateEvidence<S> extends HashMap<S, Integer> {
    private static final long serialVersionUID = 4276706788994272957L;
    private long totalEvidence;

    public long getTotalEvidence() {
        return this.totalEvidence;
    }

    public double getPercentage(final S key) {
        final Integer stateEvidence = this.get(key);
        if (stateEvidence == null)
            return 0.0;
        else
            return this.get(key).doubleValue() / (this.totalEvidence);
    }

    @Override
    public Integer put(final S key, final Integer value) {
        final Integer old = super.put(key, value);
        if (old != null)
            this.totalEvidence -= old;
        this.totalEvidence += value;

        return old;
    }

    @Override
    public void putAll(final Map<? extends S, ? extends Integer> map) {
        final Map<S, Integer> oldMap = new HashMap<>(this);
        super.putAll(map);

        map
                .entrySet().stream().forEach((entry) -> {
            final Integer oldEvidence = oldMap.get(entry.getKey());
            final Integer newEvidence = this.get(entry.getKey());
            this.totalEvidence = (this.totalEvidence - oldEvidence)
                    + newEvidence;
        });
    }

    @Override
    public Object clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
