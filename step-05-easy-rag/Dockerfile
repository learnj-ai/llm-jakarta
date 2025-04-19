FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven && apt-get clean
RUN mvn clean package

FROM eclipse-temurin:21-jre
WORKDIR /opt

# Install necessary packages
RUN apt-get update && apt-get install -y curl unzip && apt-get clean

# Download and set up WildFly
RUN curl -L -o wildfly.zip https://github.com/wildfly/wildfly/releases/download/33.0.2.Final/wildfly-33.0.2.Final.zip && \
    unzip wildfly.zip && \
    mv wildfly-33.0.2.Final wildfly && \
    rm wildfly.zip

# Create wildfly user
RUN groupadd -r wildfly && useradd -r -g wildfly -d /opt/wildfly -s /sbin/nologin wildfly && \
    chown -R wildfly:wildfly /opt/wildfly

# Create directories for scripts and libs
RUN mkdir -p /opt/wildfly/scripts/
RUN mkdir -p /opt/wildfly/libs/

# Download PostgreSQL JDBC driver
RUN curl -L -o /opt/wildfly/libs/postgresql-42.7.3.jar https://jdbc.postgresql.org/download/postgresql-42.7.3.jar

# Copy script from build stage
COPY --from=build /app/scripts/configure-postgresql.cli /opt/wildfly/scripts/

# Copy war file from build stage
COPY --from=build /app/target/*.war /opt/wildfly/standalone/deployments/

# Only expose HTTP port
EXPOSE 8080

# Set the user
USER wildfly

# Start WildFly
CMD ["/bin/sh", "-c", "/opt/wildfly/bin/standalone.sh -c standalone-full.xml -b 0.0.0.0 --cli-script=/opt/wildfly/scripts/configure-postgresql.cli"]