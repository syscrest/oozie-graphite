
# oozie-graphite

## About

`oozie-graphite` contains some useful glue for pushing operational data from your oozie bundles / coordinators 
and/or oozie-internal instrumentation into graphite. 

## Compatibility

* [Oozie 3.3.0](http://oozie.apache.org/) + [Graphite 0.9.x](http://graphite.readthedocs.org/en/0.9.10)
 
(see comments in `build.gradle` and `GraphiteMRCounterExecutor` on downgrading it to Oozie 3.1.3-incubating)

## How to build

Downloads have been build with [gradle 1.6](http://www.gradle.org/downloads)

## Installation

See [HowToInstallOozieGraphite](https://github.com/syscrest/oozie-graphite/wiki/HowToInstallOozieGraphite) on how to bundle and configure your oozie server with these extentions.

## Available modules

* graph your oozie coordinator runs / workflows: see [GraphiteMRCounterExecutor](https://github.com/syscrest/oozie-graphite/wiki/GraphiteMRCounterExecutor)
* graph your oozie server (internal instrumentation): see [GraphiteInstrumentationService](https://github.com/syscrest/oozie-graphite/wiki/GraphiteInstrumentationService)


