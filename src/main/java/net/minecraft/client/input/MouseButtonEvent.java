package net.minecraft.client.input;

public class MouseButtonEvent {
    private final double x;
    private final double y;
    private final int button;
    private final int clickCount;

    public MouseButtonEvent(double x, double y, int button) {
        this(x, y, button, 1);
    }

    public MouseButtonEvent(double x, double y, int button, int clickCount) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.clickCount = clickCount;
    }

    public MouseButtonEvent(double x, double y, MouseButtonInfo info) {
        this(x, y, info == null ? 0 : info.button(), 1);
    }

    public double getX() {
        return this.x;
    }

    public double x() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double y() {
        return this.y;
    }

    public int getButton() {
        return this.button;
    }

    public int button() {
        return this.button;
    }

    public int getClickCount() {
        return this.clickCount;
    }

    public int clickCount() {
        return this.clickCount;
    }
}
