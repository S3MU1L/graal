package com.oracle.svm.hosted.analysis.absint;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.Collection;

import com.oracle.graal.pointsto.BigBang;
import com.oracle.graal.pointsto.PointsToAnalysis;
import com.oracle.graal.pointsto.flow.InvokeTypeFlow;
import com.oracle.graal.pointsto.flow.LoadFieldTypeFlow;
import com.oracle.graal.pointsto.flow.MethodFlowsGraph;
import com.oracle.graal.pointsto.flow.MethodTypeFlow;
import com.oracle.graal.pointsto.flow.StoreFieldTypeFlow;
import com.oracle.graal.pointsto.flow.TypeFlow;
import com.oracle.graal.pointsto.meta.AnalysisMethod;
import com.oracle.graal.pointsto.meta.PointsToAnalysisMethod;
import com.oracle.graal.pointsto.reports.ReportUtils;
import com.oracle.svm.core.SubstrateOptions;
import com.oracle.svm.core.feature.InternalFeature;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.shared.feature.AutomaticallyRegisteredFeature;
import com.oracle.svm.shared.util.VMError;

import jdk.graal.compiler.util.json.JsonBuilder;
import jdk.graal.compiler.util.json.JsonPrettyWriter;
import jdk.graal.compiler.util.json.JsonWriter;

@AutomaticallyRegisteredFeature
public class GraalIRJsonDumpFeature implements InternalFeature {

    private StringWriter jsonOutput;
    private JsonWriter jsonWriter;
    private JsonBuilder.ObjectBuilder objectBuilder;

    private static boolean isEnabled() {
        return GraalIrJsonDumpOptions.GraalIRJson.getValue();
    }

    private static Path getFile(String extension) {
        String fileName = GraalIrJsonDumpOptions.JsonDump.getValue();
        if (fileName == null) {
            fileName = SubstrateOptions.Name.getValue();
        }
        return new File(fileName + "." + extension).getAbsoluteFile().toPath();
    }

    @Override
    public boolean isInConfiguration(IsInConfigurationAccess access) {
        return isEnabled();
    }

    public GraalIRJsonDumpFeature() {
        if (!isEnabled()) {
            return;
        }
        jsonOutput = new StringWriter();
        jsonWriter = new JsonPrettyWriter(jsonOutput);
        try {
            objectBuilder = jsonWriter.objectBuilder();
        } catch (IOException ex) {
            System.getLogger(GraalIRJsonDumpFeature.class.getName())
                    .log(Level.ERROR, "IOException during Graal IR json dump header", ex);
        }
        // Touch the file early so a build failure before onAnalysisExit still leaves an (empty) artifact.
        ReportUtils.report("Graal IR JSON dump header", getFile("json"), false, _ -> {
        });
    }

    @Override
    public void onAnalysisExit(OnAnalysisExitAccess access) {
        if (!isEnabled() || objectBuilder == null) {
            return;
        }
        FeatureImpl.OnAnalysisExitAccessImpl config = (FeatureImpl.OnAnalysisExitAccessImpl) access;
        BigBang bb = config.getBigBang();
        VMError.guarantee(bb instanceof PointsToAnalysis);

        ReportUtils.report(
                "Graal IR JSON dump has been exited",
                getFile("json"),
                true,
                _ -> {
                    try {
                        try (JsonBuilder.ArrayBuilder methods = objectBuilder.append("methods").array()) {
                            dumpMethods(bb, methods);
                        }
                    } catch (IOException ex) {
                        config.getDebugContext().log("Dump of Graal IR failed with: %s", ex);
                        throw new RuntimeException(ex);
                    }
                });
    }

    private void dumpMethods(BigBang bb, JsonBuilder.ArrayBuilder methods) throws IOException {
        for (AnalysisMethod method : bb.getUniverse().getMethods()) {
            if (!(method instanceof PointsToAnalysisMethod) || !method.isReachable()) {
                continue;
            }
            PointsToAnalysisMethod ptMethod = (PointsToAnalysisMethod) method;
            MethodTypeFlow methodFlow = ptMethod.getTypeFlow();
            MethodFlowsGraph flowsGraph = methodFlow.getMethodFlowsGraph();
            if (flowsGraph == null) {
                // Method flow was never fully built (e.g. unreachable/stub) - nothing to dump.
                continue;
            }

            try (JsonBuilder.ObjectBuilder methodObj = methods.nextEntry().object()) {
                methodObj.append("id", method.getId());
                methodObj.append("qualifiedName", method.getQualifiedName());

                try (JsonBuilder.ArrayBuilder invokes = methodObj.append("invokes").array()) {
                    dumpInvokes(flowsGraph, invokes);
                }
                try (JsonBuilder.ArrayBuilder fieldAccesses = methodObj.append("fieldAccesses").array()) {
                    dumpFieldAccesses(flowsGraph, fieldAccesses);
                }
            }
        }
    }

    private void dumpInvokes(MethodFlowsGraph flowsGraph, JsonBuilder.ArrayBuilder invokes) throws IOException {
        for (InvokeTypeFlow invokeFlow : flowsGraph.getInvokes()) {
            Collection<AnalysisMethod> callees = invokeFlow.getAllCallees();
            try (JsonBuilder.ObjectBuilder invokeObj = invokes.nextEntry().object()) {
                invokeObj.append("bci", invokeFlow.getPosition().getBCI());
                invokeObj.append("targetMethod", invokeFlow.getTargetMethod().getQualifiedName());
                invokeObj.append("isDirect", invokeFlow.isDirectInvoke());
                try (JsonBuilder.ArrayBuilder calleeArray = invokeObj.append("callees").array()) {
                    for (AnalysisMethod callee : callees) {
                        calleeArray.append(callee.getQualifiedName());
                    }
                }
            }
        }
    }

    private void dumpFieldAccesses(MethodFlowsGraph flowsGraph, JsonBuilder.ArrayBuilder fieldAccesses) throws IOException {
        for (TypeFlow<?> flow : flowsGraph.flows()) {
            String kind = null;
            Object field = null;
            if (flow instanceof LoadFieldTypeFlow loadFlow) {
                field = loadFlow.field();
                kind = "load";
            } else if (flow instanceof StoreFieldTypeFlow storeFlow) {
                field = storeFlow.field();
                kind = "store";
            }
            if (field != null) {
                try (JsonBuilder.ObjectBuilder access = fieldAccesses.nextEntry().object()) {
                    access.append("kind", kind);
                    access.append("field", field.toString());
                }
            }
        }
    }

    @Override
    public void cleanup() {
        if (!isEnabled()) {
            return;
        }
        try {
            if (objectBuilder != null) {
                objectBuilder.close();
            }
            if (jsonWriter != null) {
                jsonWriter.close();
            }
        } catch (IOException ex) {
            System.getLogger(GraalIRJsonDumpFeature.class.getName())
                    .log(Level.ERROR, "IOException while closing Graal IR JSON dump", ex);
        }

        ReportUtils.report("Graal IR JSON dump", getFile("json"), true, os -> {
            try (PrintWriter pw = new PrintWriter(os)) {
                pw.print(jsonOutput.toString());
            }
        });
    }
}
