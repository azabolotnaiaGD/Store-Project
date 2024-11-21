FROM openjdk:17
ADD target/store-project-GD.jar store-project-GD.jar
ENTRYPOINT ["java","-jar","store-project-GD.jar"]