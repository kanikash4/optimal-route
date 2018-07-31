package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import models.Edge;
import models.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.*;

public class RouteController extends Controller {

    Logger logger = LoggerFactory.getLogger(RouteController.class);
    public static Map<String,Vertex> nodes;
    public static List<Edge> edges;
    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Double> distance;

    @Inject
    RouteController() {
    }

    public Result createGrid() {
        nodes = new HashMap<String,Vertex>();
        edges = new ArrayList<Edge>();

        try {
            JsonNode request = request().body().asJson();
            String id, length, speed, source, destination;
            Preconditions.checkArgument(request != null, "request is empty or null");

            Iterator<JsonNode> iterator = request.iterator();
            while (iterator.hasNext()) {
                JsonNode obj = iterator.next();
                id = utils.validate.validateAndReturn(obj, "id", "id can not be empty or null");
                length = utils.validate.validateAndReturn(obj, "length", "length can not be empty or null");
                speed = utils.validate.validateAndReturn(obj, "speed", "speed can not be empty or null");
                source = utils.validate.validateAndReturn(obj, "source", "speed can not be empty or null");
                destination = utils.validate.validateAndReturn(obj, "destination", "destination can not be empty or null");

                if (!nodes.containsKey(source) ) {
                    Vertex location = new Vertex("Node_" + source, "Node_" + source);
                    nodes.put(source,location);
                }
                if (!nodes.containsKey(destination)) {
                    Vertex location = new Vertex("Node_" + destination, "Node_" + destination);
                    nodes.put(destination,location);
                }

                addLane(id, source, destination, Double.valueOf(length) / Double.valueOf(speed));


            }
        } catch (IllegalArgumentException ie){
                    logger.warn("Incorrect Input request : cause: {}", ie.getMessage());
                    return badRequest("Incorrect Input request");
        } catch (Exception e) {
            logger.warn("Error while creating the grid");
            return badRequest("Error while creating the grid" + e.getMessage());
        }
        return ok("Route Created successfully");
    }

//    public Result updateGrid() {
//        return null;
//    }

    public Result getGrid () {
        if(edges == null) {
            return badRequest("grid not found");
        }
        ArrayNode gridArray = new ObjectMapper().createArrayNode();
        for (Edge edge:
        edges) {
            ObjectNode gridNode = Json.newObject();
            gridNode.put("Source", edge.getSource().getId());
            gridNode.put("Destination", edge.getDestination().getId());
            gridNode.put("Duration", edge.getWeight());
            gridArray.add(gridNode);
        }
        return ok(gridArray);
    }

    public  Result optimalRoute () {
        String src, dest;
        src = getRequestParam("source", "source should not be null or empty");
        dest = getRequestParam("destination", "destination should not be null or empty");

        if(edges == null) {
            return badRequest("No grid found to find optimal route!");
        }

        Vertex source = nodes.get(src);
        settledNodes = new HashSet<Vertex>();
        unSettledNodes = new HashSet<Vertex>();
        distance = new HashMap<Vertex, Double>();
        predecessors = new HashMap<Vertex, Vertex>();
        distance.put(source, 0.0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            Vertex node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
        LinkedList<Vertex> path =getPath(nodes.get(dest));

        String result = "";
        for (Vertex v: path
             ) {
            if(result== ""){
                result =v.getId().substring(5);
            } else {
                result += "-" + v.getId().substring(5);
            }
        }

        return ok(result);
    }

    private void findMinimalDistances(Vertex node) {
        List<Vertex> adjacentNodes = getNeighbors(node);
        for (Vertex target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node) + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }
    }

    private double getDistance(Vertex node, Vertex target) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(node) && edge.getDestination().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Runtime Exception occured!");
    }

    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> neighbors = new ArrayList<Vertex>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(node) && !isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    private Vertex getMinimum(Set<Vertex> vertexes) {
        Vertex minimum = null;
        for (Vertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(Vertex vertex) {
        return settledNodes.contains(vertex);
    }

    private double getShortestDistance(Vertex destination) {
        Double d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    private LinkedList<Vertex> getPath(Vertex target) {
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

    private String getRequestParam(String param, String msg) {
        String value[] = request().queryString().get(param);
        Preconditions.checkArgument(value != null && !value[0].isEmpty(), msg);
        return value[0].trim();
    }

    private void addLane(String laneId, String sourceLocNo, String destLocNo, double duration) {
        Edge lane = new Edge(laneId,nodes.get(sourceLocNo), nodes.get(destLocNo), duration );
        edges.add(lane);
    }

}