/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package org.jvyamlb.nodes;

import org.jvyamlb.Position;
import org.jvyamlb.Positionable;
import java.util.List;

/**
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class PositionedSequenceNode extends SequenceNode implements Positionable {
    private Position.Range range;

    public PositionedSequenceNode(final String tag, final List value, final boolean flowStyle, final Position.Range range) {
        super(tag, value, flowStyle);
        assert range != null;
        this.range = range;
    }

    public Position getPosition() {
        return range.start;
    }

    public Position.Range getRange() {
        return range;
    }

    public void setRange(Position.Range range) {
        this.range = range;
    }

    public boolean equals(Object other) {
        boolean ret = this == other;
        if(!ret && (other instanceof PositionedSequenceNode)) {
            PositionedSequenceNode o = (PositionedSequenceNode)other;
            ret = 
                this.getValue().equals(o.getValue()) &&
                (null == this.getTag() ? null == o.getTag() : this.getTag().equals(o.getTag())) &&
                this.getFlowStyle() == o.getFlowStyle() &&
                this.getRange().equals(o.getRange());
        }
        return ret;
    }

    public String toString() {
        return "#<" + this.getClass().getName() + " value=\"" + getValue() + "\" tag="+getTag()+" flowStyle="+getFlowStyle()+" range=" + getRange() + ">";
    }
}// PositionedSequenceEvent
