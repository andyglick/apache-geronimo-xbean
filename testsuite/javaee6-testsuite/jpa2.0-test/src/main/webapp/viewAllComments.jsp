<!--

	Licensed to the Apache Software Foundation (ASF) under one or more
	contributor license agreements. See the NOTICE file distributed with
	this work for additional information regarding copyright ownership.
	The ASF licenses this file to You under the Apache License, Version
	2.0 (the "License"); you may not use this file except in compliance
	with the License. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0 Unless required by
	applicable law or agreed to in writing, software distributed under the
	License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
	CONDITIONS OF ANY KIND, either express or implied. See the License for
	the specific language governing permissions and limitations under the
	License.
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<%@page import="org.apache.geronimo.javaee6.jpa20.bean.Facade" %>
<%@page import="org.apache.geronimo.javaee6.jpa20.entities.Course" %>
<%@page import="org.apache.geronimo.javaee6.jpa20.entities.Evaluation" %>
<%@page import="java.util.List" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>View Comment</title>
    </head>
    <body>
        This page displays comments on Course.<br>

        <%Facade facade = new Facade();
                    String cidString = request.getParameter("cid");
                    int cid = Integer.parseInt(cidString);
                    Course course = facade.findCourse(cid);

        %>
                <hr>
        Course Name:<%=course.getCname()%>,totally there are <%=course.getEvaluation().size()%> comments.<br>
        <%
        List<Evaluation> all = course.getEvaluation();
        %>
        <h2>All Comments</h2><hr>
        <ol>
            <%
            for (Evaluation e : all) {
                //System.out.println("in viewAllComments!!!!!!!!"+e.getComment());
            %>
            <li>
                Comment:<%=e.getComment()%> from viewAllComments.jsp
            </li>
            <%
             }%>
        </ol>
    </body>
</html>
