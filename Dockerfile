FROM java:8-jdk-alpine
COPY out/artifacts/sensordata_jar/sensordata.jar /usr/app/
# COPY log4j.xml /usr/app/
WORKDIR /usr/app
ENTRYPOINT ["java", "-jar", "sensordata.jar"]