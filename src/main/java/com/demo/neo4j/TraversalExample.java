package com.demo.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.io.IOException;

public class TraversalExample {

    private enum Rels implements RelationshipType {
        LIKES, KNOWS
    }

    private GraphDatabaseService DB;
    private TraversalDescription friendsTraversal;
    private static final File databaseDirectory = new File("D:\\Neo4J\\Data\\neo4jDatabases\\database-6579898b-7ea4-441c-a78e-262af3bf0769\\installation-3.5.14\\data\\databases\\graph.db");

    public static void main(String[] args) throws IOException {
        FileUtils.deleteRecursively(databaseDirectory);
        TraversalExample example = new TraversalExample();
        Node joe = example.createData();
        example.run(joe);
    }

    public TraversalExample() {
        this.DB = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
        friendsTraversal = DB.traversalDescription()
                .depthFirst()
                .relationships(Rels.KNOWS)
                .uniqueness(Uniqueness.RELATIONSHIP_GLOBAL);
    }

    private Node createData() {
        String query = "Create(joe{name:'Joe'}),(sara{name:'Sara'})," +
                "(lisa{name:'Lisa'}),(peter{name:'PETER'})," +
                "(dirk{name:'Dirk'}),(lars{name:'Lars'}),(ed{name:'Ed'})," +
                "(joe)-[:KNOWS]->(sara),(lisa)-[:LIKES]->(joe)," +
                "(peter)-[:KNOWS]->(sara),(dirk)-[:KNOWS]->(peter)," +
                "(lars)-[:KNOWS]->(dirk),(ed)-[:KNOWS]->(lars)," +
                "(lisa)-[:KNOWS]->(lars)" +
                "return joe";
            Result result = DB.execute(query);
            Object joe = result.columnAs("joe").next();
            if (joe instanceof Node) {
                return (Node) joe;
            } else {
                throw new RuntimeException("Joe is not a node!");
            }
    }

    private void run(Node joe) {
        try (Transaction tx = DB.beginTx()) {
            System.out.println(traverseBaseTraverser(joe));
        }
    }

    public String traverseBaseTraverser(Node node) {
        String output = "";
        for (Path path : friendsTraversal.traverse(node)) {
            output += path + "\n";
        }
        return output;
    }
}
