# Short review for literatures

## [PROCESS DOC USE CASE:Square Kilometre Array/LOFAR ](PUBLIC_PROCESS_D4.1_Initial_-state_of_the_art_and_requirement_analysis_initial_PROCESS_architecture_v1-1.pdf)
The PROCESS is a solution aiming to handle heterogenous exascale use cases driven by both the scientific research community and the industry. One use case it covers is to process the observation from LOFAR. 

In this case, the goal is to provide a user friendly system that astronomers can easily utilize by clicking and they only need to focus on their own field.The use case can be abstracted as it is shown in the figure below.The data is firstly staged and divided into multiple subsets.There are two different and separate calibration stages, the key is that whether it is direction dependent or independent.The output of DD calibration stage will be used for DI calibration, which means there's an order between two of them. 

DI uses the pre-FACTOR package to perform the initial calibration which can be parallel. DD is an iterative procedure on the target field, which is called self calibration. It is currently implemented using the DDF package on a single node (using all the cores of one high-end CPU). *Which one is the stage that this master project should concern? Or both of it?*

In this figure, the number of division is fixed and determined by the number of processors. (or division of sky map?). `one question is that whether each division is fixed whole job.` If so, there might be a bottleneck that the overall processing time is determined by the slowest one. It maybe a room for work stealing to solve load imbalance problem. `Another thing that should concern is that  whether it can be scaled horizontally.` Since the number of processors is very certain
![demo of use case](MD_source/RPOCESS_USE_CASE.jpg )

The PROCESS project focuses on computation at the high level. The workflow is based on the use of the Common Workflow Language (CWL) and Docker / Singularity. This workflow mentioned is part of project EOSCpilot.

### Important reference
*  EOSCpilot project 
*  pre-FACTOR [https://github.com/lofar-astron/prefactor] 
*  DDF [https://github.com/mhardcastle/ddf-pipeline] 

## [Unlocking the LOFAR LTA](/Literature/Unlocking&#32;the&#32;LOFAR&#32;LTA.pdf)

The LOFAR LAT  was set up to store all LOFAR observations. In this paper, how PROCESS tackles on the exscale data is covered. The implementation consists of several parts including: web interface, DDF pipeline, CWL & Xenon and data service. 

The Web interface is related to the interaction with users which is not what we should mainly concern. The DDF pipeline is taken charge by *prefactor* which is also mentioned in PROCESS document. I think this part is related to what we are going to do for calibration, however, this paper does not talk about it a lot. Maybe it is because this part is a black box or fixed package for the solution and it is not the main point of innovation. For our problem, this part probably can be viewed as black box as well, but we should go down to how this pipeline is invoked.

In section II.C, CWL & Xenon, how DDF pipeline is organized is explained. I don`t know whether this is the current solution for calibration in real practice. However, this can be also a good source to learn. According to the example, the DDF pipeline is decomposed into multiple phases vertically. The Ibis framework is good at scaling horizontally, which means the nodes basically are working with same tasks, after all nodes are sharing the same code (of course, Ibis can certainly be applied to this workflow; and Constellation may help as well). 

The last part is Data service. I think we should take care about it, as data locality is a vital part of performance and how data are stored and accessed is something we can not ignore. There are a lot of technologies mentioned, but it is not very clear to see the detail, meanwhile there is no reference talking about the decision making on technologies.

In the last paragraph of the paper, it mentions that the DI part can be parallelized while DD part of calibration not. I am not sure which part is the one that we talked about that using Ibis to replace MPI and Spark.

### Important reference

*  CWL & Xenon 

## Towards a new paradigm for programming scientiﬁc workﬂows

This paper aims to purpose a new programming paradigm for scientific computing.

## The data locality of Spark
The data locality of spark relies on RDD. The "executor code" will try to get as close as possible to the data. 

## Resource management for Infrastructure as a Service (IaaS) in cloud computing: A survey

>Resource management: At any time instant, resources are to be allocated to effectively handle workload fluctuations, while providing QoS guarantees to the end users. The computing and network resources are limited and have to be efficiently shared among the users in virtual manner. In order to perform effective resource management, we need to consider the issues such as resource mapping, resource provisioning, resource allocation and resource adaptation. The lack of mature virtualization tools and powerful processor's have prevented growth of cloud computing. Although relatively new, a fair amount of work by Urgaonkar et al. (2010a) and Vaquero et al. (2009) has been done to examine current and future challenges for both users and providers of cloud computing. However, little has been done to understand the range of operational challenge faced by users as they attempt to run applications in the cloud. Chase et al. (2010) have considered the problem of energy-efficient resource management of homogeneous resources in Internet hosting centres. The main challenge is to determine the resource demand of each application at its current request load level and to allocate resources in most efficient way.

>Metering of any kind of resource and service consumption is essential in order to offer elastic pricing, charging and billing. It is therefore a pre-condition for the elasticity of clouds. The issue here is to see that the users are charged only for the services that they use for the specific period of time. Cloud computing alone will not help an organization to determine who will pay for what resource, but it can help provide a platform for an infrastructure design that establishes a charge-back model for metering and billing.


This paper aims to point out the problems and solutions for resource management in IaaS. First, it defines what the resources are in IaaS and then simply lists the issues. After that, each issue and the related solution are presented.

* Resource provisioning
  * The issue here is to provide better quality of service in IaaS by provisioning the resources to the users or applications via load balancing mechan- ism, high availability mechanism, etc. In
  * predict the incoming demond, and prepare in advance to increase availability
  * >Warneke and Kao (2011) have discussed the challenges and opportunities for efficient parallel data processing in cloud envir- onments and presented Nephele, the first data processing frame- work to exploit the dynamic resource provisioning offered by today IaaS clouds. They have described Nephele basic architecture and presented a performance comparison to the well-established data processing framework Hadoop. The performance evaluation gives a first impression on the ability to assign specific virtual machine types to specific tasks of a processing job, as well as the possibility to automatically allocate/deallocate virtual machines in the course of a job execution. This can help in improving overall resource utilization. and consequently reduce the processing cost.
  * highlight:
    * Dynamic Resource Provisioning for Data Streaming Applications in a Cloud
Environment
    * Exploiting Dynamic Resource Allocation for
Efficient Parallel Data Processing in the Cloud
    * ADAPTIVE RESOURCES PROVISIONING FOR GRID APPLICATIONS
AND SERVICES (increase resource utilization)
* Resource allocation
  * 
