package com.oracle.svm.hosted.analysis.absint;

import com.oracle.svm.shared.option.HostedOptionKey;
import jdk.graal.compiler.options.Option;
import jdk.graal.compiler.options.OptionType;

public class GraalIrJsonDumpOptions {

    @Option(help = "Enable Graal IR json dump to the specified file.", type = OptionType.Expert)
    static final HostedOptionKey<String> JsonDump = new HostedOptionKey<>(null);

}
