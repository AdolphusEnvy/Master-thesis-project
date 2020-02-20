## Sept 30th, Meeting for picking topic

Participant: You Hu, Jason Maassen

In the meeting, we talked about three kinds of topics:
* develop the systems utilizing GPUs.
* the efficient computing
* modeling for pulsar searching on other systems for comparison with current approach
* plus fast serialization for spark

Later Jason provided more speific topics. Then I picked the calibration one.
The calibration project was originally formalized as below:

It is about calibration for radioastronomy observations. Today, such calibration is often done on a single machine with GPUs. To parallelize this, we are looking at two options in this project, a traditional MPI solution, and a solution using spark. Spark is not really a logical choice however, something like Ibis (developed at the VU) would make more sense. This project would be about porting the current spark version of the code to Ibis, and performing a comparison between this new solution and the spark and MPI ones. 

  
## Till Nov 16th, Reading literatures
Three literatures were :
* Ibis: a Flexible and Efﬁcient Java-based Grid Programming Environment
	* the technology selection and details of Ibis
* Real-World Distributed Computing  with Ibis
	* an overview of ibis
* Towards Jungle Computing with Ibis/Constellation
	* an use case of Ibis/constellation

Besides, because i took course parallel programming practical, I have already practiced a little on Ibis.


## Nov 27th, Meeting for calibration use case and procedure 
Participant: You Hu, Jason Maassen and Faruk Diblen
There are two parts:
### The project content
The idea of the project is to utilize Ibis/Constellation for calibration. The solution is to invoke calibration tool(implemented by C/C++, consider it as black box) parallelly. Three options are JMI, JNA and system call.

The existing implementations are based on Spark and MPI. We can make comparison among three of them.

To potential difficulties are data locality, job schedule, and perhaps fault tolerance. We can learn those points from Spark or any existing systems.

One point that I forgot to ask is that whether there is requirement for real time processing and streaming processing.

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
Keep debugging!
## Meeting on 20th of Feb
Participant: You Hu, Jason Maassen and Adam Belloum
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTkzNzk2MTA0MiwtMTk5Mzk4MDg3LC02NT
EyMTQxMjIsMTQzMzE1MDMwLC02MTA4NDA3OTIsLTEyMDAyMjIw
NzEsMTE5NDU4MzIzNSw2NDQ4NTg2NjgsLTEzNTk2NTIzMTYsMT
A0ODE0NjM0OF19
-->