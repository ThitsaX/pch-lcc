# Build stage
FROM maven:3.6.3-jdk-11-slim AS build
RUN mkdir -p /opt/app
COPY implementation /opt/app/implementation/
RUN mvn clean -f /opt/app/implementation/mod_fspiop_interface/ -P local clean install
RUN mvn clean -f /opt/app/implementation/ install -DskipTests -P local
RUN mvn clean -f /opt/app/implementation/web_fsp_connector_api/ install -DskipTests -P local
RUN mvn clean -f /opt/app/implementation/web_hub_connector_api/ install -DskipTests -P local


FROM openjdk:17-jdk-alpine
RUN mkdir -p /opt/app
RUN mkdir -p /opt/app/fsp
RUN mkdir -p /opt/app/fsp/lib
RUN mkdir -p /opt/app/hub
RUN mkdir -p /opt/app/hub/lib

COPY --from=build /opt/app/implementation/web_fsp_connector_api/target/fsp_connector_api.jar opt/app/fsp/fsp_connector_api.jar
COPY --from=build /opt/app/implementation/web_hub_connector_api/target/hub_connector_api.jar opt/app/hub/hub_connector_api.jar
COPY --from=build /opt/app/implementation/web_fsp_connector_api/target/lib/* /opt/app/fsp/lib/
COPY --from=build /opt/app/implementation/web_hub_connector_api/target/lib/* /opt/app/hub/lib/
WORKDIR /opt/app/
COPY docker-entrypoint.sh /opt/app/
RUN ["chmod", "+x", "/opt/app/docker-entrypoint.sh"]
EXPOSE 8001
EXPOSE 8000

ENV FSP_IOP_BASE_URL="http://localhost:1001"
ENV REDIS_URL="redis://127.0.0.1:123"
ENV PUBLIC_KEY="dummy"
ENV ISIS_URL="http://localhost:1002"
ENV ISIS_USERNAME="dummy"
ENV ISIS_PASSWORD="dummy"
ENV ENABLE_JWS: "false"
ENV CACHE_LIFE_TIME: 50
ENV PUBLIC_KEY: "dummy"
ENV LOCALE_LANGUAGE: "eng"


#ENTRYPOINT java "-DfspiopBaseUrl=${fspiopBaseUrl}" "-DredisUrl=redis://redis:6379"  -cp fsp/fsp_connector_api.jar:fsp/lib/* com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.FspConnectorApiApplication 
ENTRYPOINT ["/opt/app/./docker-entrypoint.sh"]

#java -DfspiopBaseUrl="http://fspiop-api-svc.dev.sanbox.wynepayhubsanbox-pre.com" -DredisUrl="redis://127.0.0.1:6379" -DthitsawalletUrl="http://13.212.50.9:8081/" -DthitsaconnectKey="ThitsaconnectKey" -DflxUrl="" -DflxTenantId="" -DflxUserName="" -DflxMakerUserId="" -DflxPassword="" -DflxSettledGL=123 -DflxApiKey="" -DmusApiKey="yE2wPAq90Laa23jbNLo3K43Kjmu7y1VZ3Pe36sQR" -DmusUrl="https://api.demo.sing.musoniservices.com/v1/" -DmusPassword="zarchitun" -DmusRoundingValue="100" -DmusTenantId="thitsaworks" -DmusUserName="testuser" -cp "fsp_connector_api.jar;lib/*" com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.FspConnectorApiApplication