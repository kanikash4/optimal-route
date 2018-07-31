package models;

import java.io.Serializable;

public class Edge implements Serializable {

    private final Vertex source;
    private final Vertex destination;
    private final double weight;
    private  String length;
    private  String speed;

    public Edge(Vertex source, Vertex destination, int weight, String length, String speed) {

        this.source = source;
        this.destination = destination;
        this.weight = weight;
        this.length = length;
        this.speed = speed;
    }

    public Edge(Vertex source, Vertex destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }
    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }

}
