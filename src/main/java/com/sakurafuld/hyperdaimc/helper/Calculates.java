package com.sakurafuld.hyperdaimc.helper;

public class Calculates {
    private Calculates() {
    }

    public static double curve(double delta, double p1, double p2, double p3) {
        return Math.pow(1 - delta, 2) * p1
                +
                2 * (1 - delta) * delta * p2
                +
                Math.pow(delta, 2) * p3;
    }

    public static double curve(double delta, double p1, double p2, double p3, double p4) {
        return Math.pow(1 - delta, 3) * p1
                +
                3 * Math.pow(1 - delta, 2) * delta * p2
                +
                3 * (1 - delta) * Math.pow(delta, 2) * p3
                +
                Math.pow(delta, 3) * p4;
    }

    public static double curve(double delta, double p1, double p2, double p3, double p4, double p5) {
        return Math.pow(1 - delta, 4) * p1
                +
                4 * delta * Math.pow(1 - delta, 3) * p2
                +
                6 * Math.pow(delta, 2) * Math.pow(1 - delta, 2) * p3
                +
                4 * Math.pow(delta, 3) * (1 - delta) * p4
                +
                Math.pow(delta, 4) * p5;
    }
}
