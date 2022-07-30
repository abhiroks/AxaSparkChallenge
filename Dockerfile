FROM openjdk:8-alpine

RUN apk --update add git curl tar bash ncurses && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*

ARG SBT_VERSION=1.2.8
ARG SBT_HOME=/usr/local/sbt
RUN curl -sL "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar -xz -C /usr/local

ARG SPARK_VERSION=2.4.1
ARG SPARK_HOME=/usr/local/spark-$SPARK_VERSION-bin-hadoop2.7
RUN curl -sL "https://archive.apache.org/dist/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop2.7.tgz" | tar -xz -C /usr/local

ENV PATH $PATH:$SBT_HOME/bin:$SPARK_HOME/bin

ENV SPARK_MASTER local[*]

ENV SPARK_DRIVER_PORT 5001
ENV SPARK_UI_PORT 5002
ENV SPARK_BLOCKMGR_PORT 5003
EXPOSE $SPARK_DRIVER_PORT $SPARK_UI_PORT $SPARK_BLOCKMGR_PORT
RUN mkdir /app
COPY run.sh /app
WORKDIR /app
RUN sed -i 's/\r$//' run.sh && chmod +x run.sh
RUN mkdir data
RUN wget http://www.sec.gov/dera/data/Public-EDGAR-log-file-data/2017/Qtr1/log20170201.zip && \
    unzip log20170201.zip -d /app/data && \
    rm log20170201.zip
CMD ./run.sh