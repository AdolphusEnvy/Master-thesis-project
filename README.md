# Master project: self-adjusted auto-provisioning distributed system
Author: You Hu, Master Computer Science student @VU & UvA

Supervisor: Prof. Adam Belloum, Dr. Jason Maassen

Support institution: the Netherlands eScience Center, VU Amsterdam

## Introduction

### Issues
The MPI and Spark versions have drawback on resource utilization
### Research question
How to increase the resource utilization level from the user side?
### Core idea
Use calibration application to fill in the blank. Higher resource utilization rate enables acceleration.

### Components
* [Webservice](Webservice) -- Packs Ibis service and Flask web service ( job submission entry point)
* [Test_Xenon](Test_Xenon) -- Auto scaling layer
* [DrnPrvDriver](DynPrvDriver) -- Elastic distributed computing layer
* [PerformanceMeasure](PerformanceMeasure) -- processing log files to measure the performance


### Other project related resources
* [Docs](Docs) -- Slides / template / Final thesis / Literature study
* [Literature](Literature) -- Collected literatures