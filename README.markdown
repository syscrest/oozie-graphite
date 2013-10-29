
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

## Releases

2013-10-28 - v1.0.0 - [oozie-graphite-1.0.0.jar](https://github.com/syscrest/oozie-graphite/releases/download/v1.0.0/oozie-graphite-1.0.0.jar)

## Installation

See [HowToInstallOozieGraphite](https://github.com/syscrest/oozie-graphite/wiki/HowToInstallOozieGraphite) on how to bundle and configure your oozie server with these extentions.

## Available modules

### GraphiteInstrumentationService

Monitor your oozie instance by pushing already available internal oozie metrics into your graphite installation. See [GraphiteInstrumentationService](https://github.com/syscrest/oozie-graphite/wiki/GraphiteInstrumentationService) for further details and how to configure this module.

_Related blog posts with example use cases:_

* Blog post on [syscrest.com](http://www.syscrest.com) : [Installing GraphiteInstrumentationService](http://www.syscrest.com/2013/08/oozie-monitoring-installing-graphiteinstrumentationservice/)
* Blog post on [syscrest.com](http://www.syscrest.com) : [Monitoring action performance (mapreduce/java/fs actions) with GraphiteInstrumentationService](http://www.syscrest.com/2013/08/oozie-monitoring-action-performance/)
* Blog post on [syscrest.com](http://www.syscrest.com) : [Visualizing coordinator actions timeouts](http://www.syscrest.com/2013/09/oozie-monitoring-coordinator-action-timeouts-graphite/)

### GraphiteMRCounterExecutor


Graph your oozie coordinator runs / workflows by pushing map-reduce counters into graphite. See [GraphiteMRCounterExecutor](https://github.com/syscrest/oozie-graphite/wiki/GraphiteMRCounterExecutor) for further details and how to configure this module.

_Related blog posts with example use cases:_

 * Blog post on [syscrest.com](http://www.syscrest.com) : [Installing GraphiteMRCounterExecutor](http://www.syscrest.com/2013/10/oozie-bundle-coordinator-monitoring-installing-graphitemrcounterexecutor/)
 * Blog post on [syscrest.com](http://www.syscrest.com) : [Tapping into hadoop counters with GraphiteMRCounterExecutor](http://www.syscrest.com/2013/10/oozie-bundle-monitoring-tapping-into-hadoop-counters/)



