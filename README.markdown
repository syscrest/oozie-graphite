
# oozie-graphite

## About

`oozie-graphite` contains some useful glue for pushing operational data from your oozie bundles / coordinators 
and/or oozie-internal instrumentation into graphite. 

## Compatibility

* [Oozie 3.3.2](http://oozie.apache.org/) + [Graphite 0.9.x](http://graphite.readthedocs.org/en/0.9.10)
 
(see comments in `build.gradle` and `GraphiteMRCounterExecutor` on downgrading it to Oozie 3.1.3-incubating)

## How to build

[![Build Status](https://travis-ci.org/syscrest/oozie-graphite.png)](https://travis-ci.org/syscrest/oozie-graphite)

Use [gradle 1.4++](http://www.gradle.org/downloads)

## Installation

See [HowToInstallOozieGraphite](https://github.com/syscrest/oozie-graphite/wiki/HowToInstallOozieGraphite) on how to bundle and configure your oozie server with these extentions.

## Available modules

### GraphiteInstrumentationService

Monitor your oozie instance by pushing already available internal oozie metrics into your graphite installation. See [GraphiteInstrumentationService](https://github.com/syscrest/oozie-graphite/wiki/GraphiteInstrumentationService) for further details and how to configure this module.

__Examples:__

* [HowToMonitorActionExecutionTimes](https://github.com/syscrest/oozie-graphite/wiki/HowToMonitorActionExecutionTimes) : Monitor your map-reduce submission times, java action submission times, fs action execution times.
* [HowToMonitorCoordinatorActionTimeouts](https://github.com/syscrest/oozie-graphite/wiki/HowToMonitorCoordinatorActionTimeouts) : Monitor coordinator action timeouts


### GraphiteMRCounterExecutor


Graph your oozie coordinator runs / workflows by pushing map-reduce counters into graphite. See [GraphiteMRCounterExecutor](https://github.com/syscrest/oozie-graphite/wiki/GraphiteMRCounterExecutor) for further details and how to configure this module.

__Examples:__

 * TODO

