# spring-microservices

run these docker commands before starting the application:

#1
docker run --name zipkin -d -p 9411:9411 openzipkin/zipkin

#2
docker run --name keycloak -p 8181:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:22.0.1 start-dev

#3
docker run -- name mongodb -d --name mongodb -p 27017:27017 -e MONGO_INITDB_DATABASE=product-service mongo

#4
docker run --name MySQL_SERVER -e MYSQL_ROOT_PASSWORD=1234 -p 3306:3306 -d mysql

#5
docker compose up

