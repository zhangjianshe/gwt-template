FROM harbor.cangling.cn:22002/cangling/gdal-base:v4.1.6
LABEL authors="satway"

WORKDIR /app
COPY target/gwt-template-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar","-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport","app.jar"]