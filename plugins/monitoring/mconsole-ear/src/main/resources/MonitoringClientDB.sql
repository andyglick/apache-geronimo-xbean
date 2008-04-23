/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * server_id        ID number for server - auto generated
 * enabled          Enable/disable this server - defaults to 1
 * name             Name for this server - alphanum
 * ip               IP address of this server
 * port             Port number to connect on
 * protocol         Protocol to use, 1 for ejb, 2 for jmx
 * username         username to use for connecting
 * password         password to use for connecting
 * added            Timestamp when this server was added
 * modified         Timestamp when this record was changed
 * last_seen        Timestamp when this server was last seen
 */
CREATE TABLE servers(
    server_id   INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
    enabled SMALLINT DEFAULT 1 NOT NULL,
    name VARCHAR(128) DEFAULT NULL,
    ip   VARCHAR(128) UNIQUE NOT NULL,
    port INTEGER NOT NULL,
    protocol INTEGER DEFAULT 1 NOT NULL,
    username    VARCHAR(128) NOT NULL,
    password    VARCHAR(1024) NOT NULL,
    added       TIMESTAMP NOT NULL,
    modified    TIMESTAMP NOT NULL,
    last_seen   TIMESTAMP NOT NULL
);
/*
 * graph_id         Id number for graph - auto generated
 * enabled          Enable/disable display of graph defaults 1
 * server_id        server id graph is associated with
 * name             name for graph - alphanumeric for js.
 * description      Description for the graph
 * timeframe        Timeframe for graph in minutes, defaults to 60
 * mbean            Mbean for graph
 * data1operation   Operation to be performed on data 1
 *                  D indicates delta (subtracts i-1 from i)
 * dataname1        Stats name for data1
 * operation        Operation between data1 and 2 done in JS
 *                  Simple math.. for example.. /10/ would divide data1 by 10, then divide result by data2
 *                  Simply / would divide data1 by data2
 * data2operation   Operation to be performed on data 2
 *                  D indicates delta (subtracts i-1 from i)
 * dataname2        Name for data2
 * xlabel           xLabel at the bottom of the graph
 * ylabel           yLabel on the side of the graph
 * warninglevel1    Level at which graph turns to yellow (float)
 * warninglevel2    Level at which graph turns to red (float)
 * color            Default color of the graph
 * last_js          Most recently generated JS source
 * added            Timestamp when this graph was added
 * modified         Timestamp when this graph was last changed
 * last_seen        Timestamp when this graph was last generated
 */
CREATE TABLE graphs(
    graph_id    INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
    enabled     SMALLINT NOT NULL DEFAULT 1,
    server_id   INTEGER NOT NULL DEFAULT 0,
    name        VARCHAR(64) UNIQUE NOT NULL,
    description LONG VARCHAR DEFAULT NULL,
    timeframe   INTEGER NOT NULL DEFAULT 60,
    mbean       VARCHAR(512) NOT NULL,
    data1operation  CHAR DEFAULT NULL,
    dataname1   VARCHAR(128) NOT NULL,
    operation   VARCHAR(128) DEFAULT NULL,
    data2operation  CHAR DEFAULT NULL,
    dataname2   VARCHAR(128) DEFAULT NULL,
    xlabel      VARCHAR(128) DEFAULT NULL,
    ylabel      VARCHAR(128) DEFAULT NULL,
    warninglevel1   FLOAT DEFAULT NULL,
    warninglevel2   FLOAT DEFAULT NULL,
    color       VARCHAR(6) NOT NULL DEFAULT '1176c2',
    last_js     LONG VARCHAR DEFAULT NULL,
    added       TIMESTAMP NOT NULL,
    modified    TIMESTAMP NOT NULL,
    last_seen   TIMESTAMP NOT NULL
);
/*
 * view_id          ID number for the view, auto generated
 * enabled          Enable or disable of showing this view defaults 1
 * name             Name for this view
 * description      Longer description for this view
 * graph_count      Number of graphs in this view
 * graph_ids        Id numbers for graphs in this view
 * added            Timestamp when this was created
 * modified         Timestamp when this was last changed
 */
CREATE TABLE views(
    view_id     INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
    enabled     SMALLINT NOT NULL DEFAULT 1,
    name        VARCHAR(128) NOT NULL,
    description LONG VARCHAR DEFAULT NULL,
    graph_count INTEGER NOT NULL DEFAULT 0,
    graph_ids   LONG VARCHAR NOT NULL,
    added       TIMESTAMP NOT NULL,
    modified    TIMESTAMP NOT NULL
);



