/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package me.neilellis.dollar.learner.orientdb;

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
import me.neilellis.dollar.script.SourceSegment;
import me.neilellis.dollar.script.TypeLearner;
import me.neilellis.dollar.types.prediction.CountBasedTypePrediction;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class OrientDBTypeLearner implements TypeLearner {

    private OServer oServer;
    private OrientGraphFactory factory;

    public OrientDBTypeLearner() {
        try {
            final File orientdb = new File(System.getProperty("user.home") + "/.dollar", "orientdb");
            orientdb.mkdirs();
            oServer = OServerMain.create();
            oServer.startup(OrientDBTypeLearner.class.getResourceAsStream("/orientdb.config.xml"));
            factory = new OrientGraphFactory("plocal:" + orientdb.getAbsolutePath() + "/types");

        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException
                | IllegalAccessException e) {
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

    @NotNull @Override public TypeLearner copy() {
        return this;
    }

    @Override public void learn(String name, SourceSegment source, @NotNull List<var> inputs, @NotNull Type type) {
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

    @Nullable @Override public TypePrediction predict(String name, SourceSegment source, @NotNull List<var> inputs) {
        CountBasedTypePrediction prediction = new CountBasedTypePrediction(name);
        OrientGraph graph = factory.getTx();
        try {
            Vertex vertex = createVertex("operation-" + name, graph);
            if (vertex == null) {
                return new CountBasedTypePrediction(name);
            }
            int count = 0;
            ArrayList<String> perms = perms(inputs);
//            System.out.println("Getting "+name + " " + perms);

            for (String type : perms) {
                final Iterable<Edge>
                        edges =
                        vertex.getEdges(Direction.OUT, getEdgeKey(type));
                for (Edge edge : edges) {
                    long countForType = (long) edge.getProperty("count");
                    final String typePrediction = edge.getVertex(Direction.IN).getProperty("type");
                    prediction.addCount(Type.valueOf(typePrediction), countForType);
                    System.out.println("Count for " +
                                       name +
                                       " type " +
                                       type +
                                       " with prediction " +
                                       typePrediction +
                                       " was " +
                                       countForType);
                }
            }

            graph.rollback();
            return prediction;
        } catch (Exception e) {
            e.printStackTrace();
            graph.rollback();
            return null;
        }
    }

    private Vertex createVertex(String name, @NotNull OrientGraph graph) {
        final Iterable<Vertex> vertices = graph.getVertices("name", name);
        Vertex vertex;
        if (vertices.iterator().hasNext()) {
            vertex = vertices.iterator().next();
        } else {
            vertex = graph.addVertex(null, "name", name);
        }
        return vertex;
    }

    @NotNull private static ArrayList<String> perms(@NotNull List<var> inputs) {
        ArrayList<String> perms = TypeLearner.perms(inputs);
        return perms;
    }

    private Edge createEdge(String name, @NotNull Vertex src, Vertex dest) {
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

    @NotNull private static String getEdgeKey(String t) {return "input-" + t;}

    @Nullable public TypePrediction predict(String name, SourceSegment source, Object[] args) {
        List<var> inputs = (List<var>) args[0];
        return predict(name, source, inputs);
    }

    @Override public void start() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        oServer.activate();

    }

    @Override public void stop() {
        oServer.shutdown();

    }
}
