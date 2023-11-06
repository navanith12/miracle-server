FROM adoptopenjdk/openjdk11
RUN apt-get update
ADD target/miracleServer-0.0.1-SNAPSHOT.jar /opt
ENTRYPOINT ["java","-jar","/opt/miracleServer-0.0.1-SNAPSHOT.jar"]
CMD [""]

