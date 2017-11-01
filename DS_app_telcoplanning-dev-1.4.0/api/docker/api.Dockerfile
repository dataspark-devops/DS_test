from dataspark-docker-releases.artifactory.datasparkanalytics.com/dataspark/oraclejava8:latest

LABEL description="Dataspark Telco Planning Api container"

#Build
ARG USER=api
ARG SPRING_HTTP_PORT=8080

#ENV variables for the image
#ENV foo bar

#add the user to $GROUP ->default value is dataspark
RUN adduser -D -G $GROUP -s /bin/sh $USER && \
    id $USER

#create the dirs for jar and conf
RUN mkdir -p $BASE_CONF_DIR/api/conf
RUN mkdir -p $BASE_CONF_DIR/api/lib
RUN mkdir -p $BASE_CONF_DIR/api/bin

COPY ./conf/* $BASE_CONF_DIR/api/conf/
COPY ./lib/api-telcoplanning*exec.jar $BASE_CONF_DIR/api/lib/
COPY ./bin/* $BASE_CONF_DIR/api/bin/
COPY ./read-me.md $BASE_CONF_DIR/api/

#Change ownership of the resources. BASE_CONF_DIR comes from the dataspark base image
RUN chown $USER:$GROUP -R $BASE_CONF_DIR/api

#Set the working directory
WORKDIR $BASE_CONF_DIR/api/bin

#Set the user to $USER
USER $USER

#create default entry point
ENTRYPOINT ["sh","api-run-telcoplanning.sh"]
EXPOSE $SPRING_HTTP_PORT

