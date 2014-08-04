package org.wikipedia.events;

public class ShowToCEvent {
    private final boolean show;

    public ShowToCEvent(boolean show) {
        this.show = show;
    }

    public boolean getShow() {
        return show;
    }

}
