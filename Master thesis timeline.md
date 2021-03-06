## Sept 30th, Meeting for picking topic

Participant: You Hu, Jason Maassen

In the meeting, we talked about three kinds of topics:
* develop the systems utilizing GPUs.
* the efficient computing
* modeling for pulsar searching on other systems for comparison with the current approach
* plus fast serialization for spark

Later Jason provided more specific topics. Then I picked the calibration one.
The calibration project was originally formalized as below:

It is about calibration for radioastronomy observations. Today, such calibration is often done on a single machine with GPUs. To parallelize this, we are looking at two options in this project, a traditional MPI solution, and a solution using spark. Spark is not really a logical choice however, something like Ibis (developed at the VU) would make more sense. This project would be about porting the current spark version of the code to Ibis, and performing a comparison between this new solution and the spark and MPI ones. 

  
## Till Nov 16th, Reading literature
Three kinds of literature were :
* Ibis: a Flexible and Efﬁcient Java-based Grid Programming Environment
	* The technology selection and details of Ibis
* Real-World Distributed Computing  with Ibis
	* An overview of ibis
* Towards Jungle Computing with Ibis/Constellation
	* An use case of Ibis/constellation

Besides, because I took course parallel programming practical, I have already practiced a little on Ibis.


## Nov 27th, Meeting for the calibration use case and procedure 
Participant: You Hu, Jason Maassen and Faruk Diblen
There are two parts:
### The project content
The idea of the project is to utilize Ibis/Constellation for calibration. The solution is to invoke the calibration tool(implemented by C/C++, consider it as black box) parallelly. Three options are JMI, JNA and system call.

The existing implementations are based on Spark and MPI. We can make comparison among the three of them.

To potential difficulties are data locality, job schedule, and perhaps fault tolerance. We can learn those points from Spark or any existing systems.

One point that I forgot to ask is whether there is a requirement for real-time processing and streaming processing.

### About the procedure
* Supervisor/examiner at university
* When start/end and  how long  
	* Start working now and expect to finish at June/July 2020. In the middle,from 15.1.2020-10.2.2020 back to China.  
* Form of project—internship or not
	* eScience center can make it as internship, it could also be possible not. My question: what Is the difference? Salary, different form of supervision or presence on office?
One thing should concern is that  since I will be back to China in the middle, I can not meet the requirement for compulsory presence at that time.
* Bigger and more well defined research question
	* To make it more acceptable for university , one formal research question covering our scenario should be made. Discuss it with supervisor from university. Set a goal for publishing.
* cluster for experiment(DAS5?) and account
## Dec 16th, meeting for more details 

Participant: You Hu, Jason Maassen and Adam Belloum
### Points
#### When and schedule
* From now on to Feb 10th , literature study. After Feb 10th , practical work
#### 	Research Question:
* Should be proposed after literature study, abstract it from a set of problems encountered in existed solutions
#### Literature study
*	First, experiment on existing MPI and Spark solutions to find the bottleneck/shortcome
*	Compare Ibis with MPI and Spark on their properties
*	Find as much related solutions as possible in multiple dimensions such as  dock for packing environment, kubernetes for organizing/auto provision
*	Meeting in the middle, 6th of Jan.
#### Ibis vs MPI and Spark
*	Ibis: Join/leave -> MPI: ×, Spark: √ but another way -> a place for auto provision
*	Ibis: Cross area -> MPI ×, Spark:× -> DAS5
#### Questions
*	Real time processing requirement? 
	*	There is a potential for real time processing  good for auto provisioning
	* Since there is a Spark version, we first consider it as batch task
*	Fault tolerance?
	*	Ibis does not provide it, it should be handle by ourselves
	*	We can achieve it in another way: provisioning/scalability/elasticity
*	Data locality?
	*	We need first to see the solution of  MPI and Spark versions
	*	I assume MPI one utilizes features from cluster and Spark uses Hadoop
	*	Ibis+Hadoop could be a solution
*	If we can apply auto provision, how can we pack calibrator?
	*	Use docker
	*	Assume calibrator has been already installed in each node
### What to do next
*	Wait for documents from Faruk Diblen
*	Try to test MIP and Spark implementations
*	Prepare for literature study
* 	(apply for an  account on DAS5 or surfsara)

## Till 6th of Jan, Reading literatures
Read about three pieces of document, and write down reviews/summary on [markdown file]([https://github.com/AdolphusEnvy/Master-thesis-project/blob/master/Literature/reviews.md](https://github.com/AdolphusEnvy/Master-thesis-project/blob/master/Literature/reviews.md))(the following summary will be written here as well).

## Jan 6th, meeting for current solutions 

Participant: You Hu, Jason Maassen and Adam Belloum
### Points
#### Research question
Look into the current solution, in the same time, purpose as many questions as possible.
Try to find solutions for those questions by literatures, the questions remind could be the final research question(take Ibis+calibration as experiment sinario).
####  Current solution-SAGECAL-Spark version
The source code is on [github page](https://github.com/nlesc-dirac). 
The current solution is based on MPI and GPU, also another solution is based on  spark and docker container. The second one is more easier for me to test.
In the following days, I will look into this solution and have some try.

## Till Jan 14th, testing sagecal on spark
Meet problem on starting `spark_hadoop` contrainer. I followed the  [instratction](https://github.com/nlesc-dirac/sagecal-spark-docker-swarm/blob/master/docs/INSTALL.md). And the spark nodes failed to start. The original resaon is that spark_hadoop contrainer failed to start service. The error is shown below by command `docker service logs spark_hadoop`.
```spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:32,245 CRIT Supervisor is running as root.  Privileges were not dropped because no user is specified in the config file.  If you intend to run as root, you can set user=root in the config file to avoid this message.
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:32,249 INFO supervisord started with pid 6
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:33,251 INFO spawned: 'sshd' with pid 9
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:33,253 INFO spawned: 'hadoop-dfs' with pid 10
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:33,255 INFO spawned: 'hadoop' with pid 11
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:33,256 INFO spawned: 'hadoop-yarn' with pid 12
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:34,310 INFO success: sshd entered RUNNING state, process has stayed up for > than 1 seconds (startsecs)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:36,029 INFO exited: hadoop-yarn (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:37,821 INFO spawned: 'hadoop-yarn' with pid 506
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:39,143 INFO exited: hadoop-yarn (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:41,952 INFO spawned: 'hadoop-yarn' with pid 849
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:43,243 INFO exited: hadoop-yarn (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:47,170 INFO spawned: 'hadoop-yarn' with pid 1101
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:47,903 INFO exited: hadoop-yarn (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:47,903 INFO gave up: hadoop-yarn entered FATAL state, too many start retries too quickly
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:48,906 INFO success: hadoop-dfs entered RUNNING state, process has stayed up for > than 15 seconds (startsecs)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:48,906 INFO success: hadoop entered RUNNING state, process has stayed up for > than 15 seconds (startsecs)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:55,279 INFO exited: hadoop-dfs (exit status 0; expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:55,291 INFO spawned: 'hadoop-dfs' with pid 1417
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:59,932 INFO exited: hadoop (exit status 0; expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:34:59,934 INFO spawned: 'hadoop' with pid 1788
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:00,187 INFO exited: hadoop-dfs (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:01,189 INFO spawned: 'hadoop-dfs' with pid 1842
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:06,494 INFO exited: hadoop-dfs (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:08,797 INFO spawned: 'hadoop-dfs' with pid 2293
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:13,989 INFO exited: hadoop-dfs (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:14,848 INFO exited: hadoop (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:15,851 INFO spawned: 'hadoop' with pid 2687
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:17,014 INFO spawned: 'hadoop-dfs' with pid 2741
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:22,263 INFO exited: hadoop-dfs (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:23,021 INFO gave up: hadoop-dfs entered FATAL state, too many start retries too quickly
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:28,305 INFO exited: hadoop (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:30,309 INFO spawned: 'hadoop' with pid 3285
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:41,434 INFO exited: hadoop (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:44,442 INFO spawned: 'hadoop' with pid 3586
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:55,670 INFO exited: hadoop (exit status 0; not expected)
spark_hadoop.0.lnbb3vjxwx7k@ubuntu    | 2020-01-14 12:35:56,671 INFO gave up: hadoop entered FATAL state, too many start retries too quickly
```
~~Keep debugging!~~
**Solved**
## Meeting on 20th of Feb
Participant: You Hu, Jason Maassen, Faruk Diblen and Adam Belloum

The slide for this meeting is uploaded to [github repo](Docs/pre2.20.pptx).

In this meeting we discussed:
* Spark version: features and possible issues
	* HDFS is just a temporary choice for demonstration 
	* The cost of each stage is unknown yet
	* data locality issue, related to HDFS, can be solved by replacing HDFS
* What Ibis could achieve:
	* cross region--IPL provides easy way to connect 
	* move compute instead of moving data
	* combine two of them above
	* auto-provision 
* TODO(It can be traced in file [TODOList](Docs/TODOList.md)):
	* run hello world application on spark plus JNI to get familiar to them
	* (Faruk would fix the issues on current version first) testing the performance of spark version

In last few pages of the presentation slides, the issues are summarized.
First is about the volume, It has been solved. Now the problem is that the excon dir is not found. I tried to build image from github repo, but failed. Probably the docker image is out of date on docker hub. Hope Faruk can fix it. Thanks in advance!

## Feb 25th
The hello world case of JNI on spark has been finished.
The C++ function can be invoked by JAVA code and paramneter can be passed to C++ function to print.

## 4th of March 
Participant: You Hu, and Adam Belloum

In this meeting, we first discussed the shortcoming of MPI and Spark.
And then we talked about three kinds of dimensions that Ibis could achieve beyond the current versions.

The shortcoming of current versions is that the computation resources can not be scaled at the demand and adjusted to the environment.

The details are shown below: 
* The resource granularity of the MPI job is fixed. A cluster with 16 nodes won't run any job requesting over 6 nodes when a job using 10 nodes. This may cause a waste of usage of resources.
* Spark system requires pre-allocated resources, and it is not easy for scaling( the current spark version based on docker swarm meets the auto-scaling on task level, not resource level)
* MPI has a problem on accessing cross-region computation recourse; Spark is not clear yet, I assume it is not easy to do that.  
* Both two versions view the file as remote data and do not try to get close to data( spark’s data locality strategy is based on RDD or  DataSet APIs, not valid on Driver)
* Kubernetes cluster along has the same problem as spark system, we need a higher level to manage the resources.

The three dimensions of features are：
* self-adjust auto-scaling
	* Master requests/release node according to the task quantity and job queue of the cluster
	* Requesting and releasing rely on cluster API like Prun on DAS
	* Tt can be managed by the master or by a server keeping running
* cross region data locality 
	* Long Term Archive has multiple sites store the different data set
	* The data can be processed at cluster cloth to its archive site
	* It can be evaluated on DAS
* staging processing separation & parallelization.
	* Not sure yet, maybe the downloading data from archive and processing can be run in parallel, not sequentially to save time.
	* Should talk to Onno

The idea of my solution is that for each cluster, we assume there is one node never crash convey ibis-server and service for: job submitting; computation node allocation and destroy; communication with other clusters.
Once a node is assigned, it will join/create the docker swarm or kubernetes cluster.  The containers will execute the same code, and elect ibis master.
The master receives job list and allocates tasks to workers. It also monitor the job list to send scaling up or down demand to the server( this part can be discussed in detail later).
The job list or task arrangement can be persisted by something like Zookeeper to reduce the cost of the crash of master.

## 11th of March 
Participant: You Hu, and Jason Maassen

A short recap:
* Issues of MPI and spark – resources can be not able to be completely used
* A prototype design and features to tackle on – self adjusted provisioning, cross region and data  locality
* Plan for the project

There are few feedback and self thinking:
* VMs is the straight forward solution for resources management, what kind of strategy are used for resource management on VMs
* IPL is designed for communication between the clusters, whether this feature can help in our case
* This most key point is the strategy on managing tasks, we can start from a simple one
* With this framework, we can replace sagecal with any computation efficient program
* Xenon can be used for communication with cluster

## 19th of March
Participant: You Hu, Adam Belloum

* presented detailed proposal
* literature study: read the guide on canvas
	* architecture or resource management?
* design need for more literature
* proposal: a more clear statement of research questions
	* plan: a draft before next Monday(23th of March)
* Plan: too rough
	* need a plan for every week and few milestones 

## 25th of March
Participant: You Hu, Adam Belloum

Today we talked about the project proposal, and literature study.
Here are few points:

* The terminology: system or framework, auto-scaling or auto-provisioning
* Plan could be Coarse-grained like 2-3 weeks per block
* For the paper writing, build up skeleton at first and fill in along with the progress
* We compared the demand of common auto-scaling policy and our solution. We should find  more materials from our side.

## 8th of April
Participant: You Hu, Adam Belloum

Today, We firstly discussed one paper with similar assumption of the case: limited amount of resources; batch and auto-scaling jobs
The summary of the paper can be checked by [Resource Provision for Batch and Interactive Workloads in Data Centers](Literature/reviews.md)

Todo: talk to Jason and prepare slides and concrete questions;
state the problem to solve(to each research questions)

## 15th of April
Participant: You Hu, Adam Belloum, Jason Maassen

Online meeting for three of us. The main topic is reporting progress to Jason and few questions about Ibis and Xenon.

[The sides](Docs/meeting%204.15.pptx) is archived in Docs folder. It is consists of :
* Recap of background and our special role in cloud industry
* Questions statement in detail: the outline for literature study
* current implementation: Ibis+JNI+contianer; Xenon on DAS5
* current related work literature: batch and interactive jobs provision based on SLA; overview on resource management on cloud.
* few remoarks from Jason: Ibis is meant to support communication for system nodes join and leave; Xenon provides simple iterface for manage jobs.
* Future work: systematicly literature study; combline Ibis and Xenon experiments.