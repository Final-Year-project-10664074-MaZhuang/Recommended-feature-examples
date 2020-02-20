package com.demo.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

public class Neo4jInit {

    public enum MyLabels implements Label {
        Person
    }

    private static enum RelTypes implements RelationshipType {
        KNOWS
    }

    private static final File databaseDirectory = new File("D:\\Neo4J\\Data\\neo4jDatabases\\database-6579898b-7ea4-441c-a78e-262af3bf0769\\installation-3.5.14\\data\\databases\\graph.db");

    GraphDatabaseService graphDb;

    Node firstNode;
    Node secondNode;
    Relationship relationship;

    public static void main(String[] args) {
        Neo4jInit hello = new Neo4jInit();
        hello.createDb();
        hello.searchData();
        hello.shutDown();
    }

    void createDb() {
        System.out.println("------------------------------");
        System.out.println("start database ...");
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);

        //ACID
        try (Transaction tx = graphDb.beginTx()) {
            firstNode = graphDb.createNode();
            firstNode.setProperty("message", "Hello, ");
            firstNode.addLabel(MyLabels.Person);
            secondNode = graphDb.createNode();
            secondNode.setProperty("message", "World");

            relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
            relationship.setProperty("message", "brave Neo4j");

            //commit ACID
            tx.success();
        }
    }

    void searchData() {
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> persions = graphDb.findNodes(MyLabels.Person);
            persions.forEachRemaining((entityTypeGraphNode) -> {
                System.out.println("ID: "+entityTypeGraphNode.getId()+", Property: "+entityTypeGraphNode.getProperties("message"));

                Iterable<Relationship> typeObjectsRelationships = entityTypeGraphNode.getRelationships();
                typeObjectsRelationships.forEach(
                        (relationship)->{
                            Node entityGraphNode = relationship.getEndNode();
                            System.out.println("ID: "+entityGraphNode.getId()+", Property: "+entityGraphNode.getProperties("message"));
                        }
                );
            });
            tx.success();
        }
    }

    void removeData() {
        try (Transaction tx = graphDb.beginTx()) {
            firstNode.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();
            firstNode.delete();
            secondNode.delete();

            tx.success();
        }
    }

    void shutDown() {
        System.out.println("------------------------------");
        System.out.println("shutdown database ...");
        graphDb.shutdown();
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        //用于数据库正确关闭的回调方法
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }
}
