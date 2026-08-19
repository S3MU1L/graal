package com.oracle.svm.hosted.analysis.absint;

import com.oracle.svm.shared.option.HostedOptionKey;
import jdk.graal.compiler.options.Option;
import jdk.graal.compiler.options.OptionType;

class GraalIrJsonDumpOptions {
    @Option(help = "Enable Graal IR + points-to JSON dump.", type = OptionType.Expert)//
    static final HostedOptionKey<Boolean> GraalIRJson = new HostedOptionKey<>(false);

    @Option(help = "File name (without extension) for the Graal IR JSON dump.", type = OptionType.Expert)//
    static final HostedOptionKey<String> JsonDump = new HostedOptionKey<>(null);
}
