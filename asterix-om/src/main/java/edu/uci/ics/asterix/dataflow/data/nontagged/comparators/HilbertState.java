package edu.uci.ics.asterix.dataflow.data.nontagged.comparators;

public class HilbertState {
    public final int[] nextState;
    public final int[] sn; //sequence number in a state

    public HilbertState(int[] nextState, int[] sn) {
        this.nextState = nextState;
        this.sn = sn;
    }
}
