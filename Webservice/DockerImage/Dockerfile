FROM ubuntu:18.04
MAINTAINER adolphusenvy

USER root
ENV DISPLAY :0

RUN apt-get update && \
    apt-get -y install software-properties-common && \
    add-apt-repository -y ppa:webupd8team/java && \
    echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections && \
    apt-get update && \
    apt-get -y install openjdk-11-jdk iputils-ping net-tools python3
RUN apt-get -y install python3-pip
RUN pip3 install flask flask_restful
RUN apt-get -y install supervisor less sudo

RUN mkdir -p -m=777 /home/yhu310/log/supervisor
RUN mkdir -p -m=777 /opt/server
RUN  mkdir -p -m=777 /opt/server/log/supervisor && \
  mkdir -p -m=777 /etc/supervisor/conf.d
RUN mkdir -p -m=777 /opt/server/lib
#RUN apt-get -y autoremove python3-pip
ADD DockerImage/lib/* /opt/server/lib/
ADD DockerImage/ipl-server /opt/server/
ADD DockerImage/supervisor.conf /etc/supervisor.conf
ADD app.py /opt/server/
RUN chmod -R 777 /opt/server
ENTRYPOINT ["supervisord", "-c", "/etc/supervisor.conf"]
#CMD python3 app.py
#CMD  ./ipl-server &