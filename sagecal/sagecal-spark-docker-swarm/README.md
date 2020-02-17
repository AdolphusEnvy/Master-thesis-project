# SageCal on Apache Spark (WIP)

## About

The Docker swarm cluster with Apache Spark and HDFS filesystem to run [SageCal](https://github.com/nlesc-dirac/sagecal). The setup uses [Docker images](https://github.com/nlesc-dirac/spark-archsci-images) to build scaleble cluster.


## Included Software
| Name    | Version   |
| ------- | --------- |
| Hadoop  | 2.9       |
| Spark   | 2.2.0     |
| java    | OpenJDK-8 |
| SageCal | -         |

**Note:** The setup was only tested on Linux64 system.

## Docker images []()

docker pull fdiblen/spark-worker-dirac
docker pull fdiblen/spark-master-dirac
docker pull fdiblen/hadoop

## Instructions

Intallation instructions [INSTALL.md](docs/INSTALL.md)

Instructions to submit a job [JOBS.md](docs/JOBS.md)
