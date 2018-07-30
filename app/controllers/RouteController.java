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
import scala.util.parsing.json.JSONObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RouteController extends Controller {

    Logger logger = LoggerFactory.getLogger(RouteController.class);
    public static List<Vertex> nodes;
    public static List<Edge> edges;
    @Inject
    RouteController(){
    }

    public Result createGrid() {
        nodes = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();

        for (int i = 0; i < 11; i++) {
            Vertex location = new Vertex("Node_" + i, "Node_" + i);
            nodes.add(location);
        }
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
                addLane(id, source, destination, Double.valueOf(length) / Double.valueOf(speed));
            }
        }catch (IllegalArgumentException ie){
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
//
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
//
    public  Result optimalRoute () {
        String source, destination;
        source = getRequestParam("source", "source should not be null or empty");
        destination = getRequestParam("destination", "destination should not be null or empty");
        System.out.println(source + " : " + destination);
        if(edges == null) {
            return badRequest("No grid found!");
        }

        return ok();
    }

    private String getRequestParam(String param, String msg) {
        String value[] = request().queryString().get(param);
        Preconditions.checkArgument(value != null && !value[0].isEmpty(), msg);
        return value[0].trim();
    }

    private void addLane(String laneId, String sourceLocNo, String destLocNo, double duration) {
        Edge lane = new Edge(laneId,nodes.get(Integer.parseInt(sourceLocNo)), nodes.get(Integer.parseInt(destLocNo)), duration );
        edges.add(lane);
    }

}