FROM centos
RUN yum install -y java-1.8.0-openjdk
ADD target/miracleServer-0.0.1-SNAPSHOT.jar /opt
ENTRYPOINT ["java","-jar","/opt/miracleServer-0.0.1-SNAPSHOT.jar"]
CMD [""]

