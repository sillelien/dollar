/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar.script;

import com.codahale.metrics.MetricRegistry;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import me.neilellis.dollar.Type;
import me.neilellis.dollar.TypePrediction;
import me.neilellis.dollar.var;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TypeLearner {

    static final MetricRegistry metrics = new MetricRegistry();
    private static OServer oServer;
    private static OrientGraphFactory factory;

    static {
        try {
            final File orientdb = new File(System.getProperty("user.home") + "/.dollar", "orientdb");
            orientdb.mkdirs();
            oServer = OServerMain.create();
            oServer.startup(TypeLearner.class.getResourceAsStream("/orientdb.config.xml"));
            oServer.activate();
            factory = new OrientGraphFactory("plocal:" + orientdb.getAbsolutePath() + "/types");

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        OrientGraphNoTx noTxGraph = factory.getNoTx();
        try {
            noTxGraph.createVertexType("Type");
            noTxGraph.createVertexType("Operation");
            noTxGraph.createKeyIndex("name", Vertex.class, new Parameter("type", "UNIQUE"));
        } catch (OSchemaException e) {

        }
    }


    public static TypePrediction predict(String name, Source source, Object[] args) {
        List<var> inputs = (List<var>) args[0];
        return predict(name, source, inputs);
    }

    public static TypePrediction predict(String name, Source source, List<var> inputs) {
        TypePrediction prediction = new TypePrediction(name);
        OrientGraph graph = factory.getTx();
        try {
            Vertex vertex = createVertex("operation-" + name, graph);
            if (vertex == null) {
                return new TypePrediction(name);
            }
            int count = 0;
            ArrayList<String> perms = perms(inputs);
//            System.out.println("Getting "+name + " " + perms);

            for (String type : perms) {
                long countForType = 0;
                final Iterable<Edge>
                        edges =
                        vertex.getEdges(Direction.OUT, getEdgeKey(type));
                for (Edge edge : edges) {
                    countForType += (long) edge.getProperty("count");
                    final String typePrediction = edge.getVertex(Direction.IN).getProperty("type");
                    prediction.addCount(typePrediction, (long) countForType);
//                    System.out.println("Count for " + name + " type " + type +" with prediction "+typePrediction+ "
// was " + countForType);
                }
            }

            graph.commit();
            return prediction;
        } catch (Exception e) {
            e.printStackTrace();
            graph.rollback();
            return null;
        }
    }

    public static Vertex createVertex(String name, OrientGraph graph) {
        final Iterable<Vertex> vertices = graph.getVertices("name", name);
        Vertex vertex;
        if (vertices.iterator().hasNext()) {
            vertex = vertices.iterator().next();
        } else {
            vertex = graph.addVertex(null, "name", name);
        }
        return vertex;
    }

    public static ArrayList<String> perms(List<var> inputs) {
        ArrayList<String> perms = new ArrayList<>();
        boolean first = true;
        for (var input : inputs) {
            TypePrediction inputPrediction = input._predictType();
            if (inputPrediction == null) {
                continue;
            }
            final Set<String> types = inputPrediction.types();
            if (first) {
                perms.addAll(types);
                first = false;
            } else {
                for (String type : types) {
                    ArrayList<String> newPerms = new ArrayList<>();
                    for (String perm : perms) {
                        newPerms.add(perm + "-" + type);
                    }
                    perms = newPerms;
                }
            }
        }
        return perms;
    }

    public static String getEdgeKey(String t) {return "input-" + t;}

    public static String calculateKey(String name, Source source, List<var> inputs, String result) {
        return result +

               inputs.stream().map(i -> i.$type()).collect(Collectors.toList()) + " " + name + " ";
    }

    public static void learn(String name, Source source, List<var> inputs, Type type) {
        OrientGraph graph = factory.getTx();
        try {
            Vertex operationVertex = createVertex("operation-" + name, graph);
            Vertex resultVertex = createVertex("type-" + type.toString(), graph);
            resultVertex.setProperty("type", type.toString());
            ArrayList<String> perms = perms(inputs);
//            System.out.println("Setting "+name + " " + perms);
            for (String t : perms) {

                final Edge
                        edge =
                        createEdge(getEdgeKey(t), operationVertex, resultVertex);
                if (edge.getProperty("count") == null) {
                    edge.setProperty("count", 1L);

                } else {
                    final long newVal = 1L + (long) edge.getProperty("count");
                    edge.setProperty("count", newVal);
                }
            }

            graph.commit();
        } catch (Exception e) {
            e.printStackTrace();
            graph.rollback();
        }
    }

    public static Edge createEdge(String name, Vertex src, Vertex dest) {
        final Iterable<Edge>
                edges =
                src.getEdges(Direction.OUT, name);

        Edge edge;
        if (edges.iterator().hasNext()) {
            edge = edges.iterator().next();
        } else {
            edge = src.addEdge(name, dest);
//            System.out.println("Created "+name);
        }
        return edge;
    }
}
