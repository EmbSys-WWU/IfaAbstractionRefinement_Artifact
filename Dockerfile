# Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy pom.xml and source
COPY pom.xml .
COPY SC2AST ./SC2AST
COPY IfaAbstractionRefinement ./IfaAbstractionRefinement

# Build the project
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

# Install gcc and other build tools
RUN apt-get update && apt-get install -y gcc g++ && rm -rf /var/lib/apt/lists/*

# Copy ressources
COPY IfaAbstractionRefinement/config ./config
COPY IfaAbstractionRefinement/testdata ./testdata
COPY SC2AST/release/sc2ast.jar /app/sc2ast.jar

# Copy built artifacts from builder
#COPY --from=builder /app/SC2AST/target/*.jar /app/SC2AST.jar
COPY --from=builder /app/IfaAbstractionRefinement/target/AbstractionRefinement.jar /app/AbstractionRefinement.jar

# Copy the runner scripts into the image and make them executable
COPY run_examples eval_examples ./
RUN chmod +x run_examples eval_examples

# Set the entrypoint to a shell
ENTRYPOINT ["/bin/bash"]