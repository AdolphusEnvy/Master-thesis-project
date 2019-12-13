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
Start working now and expect to finish at June/July 2020. In the middle,from 15.1.2020-10.2.2020 back to China.  
* Form of project—internship or not
eScience center can make it as internship, it could also be possible not. My question: what Is the difference? Salary, different form of supervision or presence on office?
One thing should concern is that  since I will be back to China in the middle, I can not meet the requirement for compulsory presence at that time.
* Bigger and more well defined research question
To make it more acceptable for university , one formal research question covering our scenario should be made. 
Discuss it with supervisor from university. Set a goal for publishing.
* cluster for experiment(DAS5?) and account


<!--stackedit_data:
eyJoaXN0b3J5IjpbLTQyNzA5MDAzMV19
-->