package com.oracle.svm.hosted.analysis.absint;

import com.oracle.graal.pointsto.BigBang;
import com.oracle.graal.pointsto.PointsToAnalysis;
import com.oracle.graal.pointsto.reports.ReportUtils;
import com.oracle.svm.core.SubstrateOptions;
import com.oracle.svm.core.feature.InternalFeature;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.shared.feature.AutomaticallyRegisteredFeature;
import com.oracle.svm.shared.util.VMError;
import jdk.graal.compiler.util.json.JsonBuilder;
import jdk.graal.compiler.util.json.JsonPrettyWriter;
import jdk.graal.compiler.util.json.JsonWriter;
import org.graalvm.nativeimage.hosted.Feature;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

@AutomaticallyRegisteredFeature
public class GraalIRJsonDumpFeature implements InternalFeature {

    private StringWriter jsonOutput = null;
    private JsonWriter jsonWriter = null;
    private JsonBuilder.ObjectBuilder objectBuilder = null;

    private static Path getFile(String extension) {
        String fileName = GraalIrJsonDumpOptions.JsonDump.getValue();
        if (fileName == null) {
            fileName = SubstrateOptions.Name.getValue();
        }

        return new File(fileName + "." + extension).getAbsoluteFile().toPath();
    }

    @Override
    public void duringSetup(DuringSetupAccess access) {
        jsonOutput = new StringWriter();
        jsonWriter = new JsonPrettyWriter(jsonOutput);
        try {
            objectBuilder = jsonWriter.objectBuilder();
        } catch (IOException ex) {
            System.getLogger(GraalIRJsonDumpFeature.class.getName())
                    .log(System.Logger.Level.ERROR, "IOException during Graal IR json dump header", ex);
        }

        ReportUtils.report("Graal IR JSON dump header", getFile("dump"), false, _ -> {
        });
    }
    @Override
    public void onAnalysisExit(OnAnalysisExitAccess access) {
        FeatureImpl.OnAnalysisExitAccessImpl config = (FeatureImpl.OnAnalysisExitAccessImpl) access;
        BigBang bb = config.getBigBang();
        VMError.guarantee(bb instanceof PointsToAnalysis);
    }
}
