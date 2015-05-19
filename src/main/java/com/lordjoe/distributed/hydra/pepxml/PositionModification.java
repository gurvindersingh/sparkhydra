package com.lordjoe.distributed.hydra.pepxml;

/**
 * com.lordjoe.distributed.hydra.pepxml.PositionModification
 *
 * @author Steve Lewis
 * @date 5/18/2015
 */
public class PositionModification {
    public final int position;
    public final double massChange;

    public PositionModification(int position, double massChange) {
        this.position = position;
        this.massChange = massChange;
    }

    @Override
    public String toString() {
        return "PositionModification{" +
                "position=" + position +
                ", massChange=" + massChange +
                '}';
    }

    public String toModString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(String.format("%10.3f", massChange).trim());
        sb.append("]");
        return sb.toString();
    }
}
