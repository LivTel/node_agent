#!/bin/sh

CATALINA_HOME=/usr/local/tomcat
export CATALINA_HOME
echo CATALINA_HOME : $CATALINA_HOME

SERVICE_NAME=org_estar_nodeagent
export SERVICE_NAME

WEB_APP_LIB=$CATALINA_HOME/webapps/$SERVICE_NAME/WEB-INF/lib
export WEB_APP_LIB

WEB_APP_CLASSPATH=$WEB_APP_LIB/activation.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/axis.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/axis-ant.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/commons-discovery-0.2.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/commons-logging-1.0.4.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/commons-configuration-1.2.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/dev_lt.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/jaxrpc.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/log4j-1.2.13.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/mail.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/ngat_astrometry.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/ngat_util_logging.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/ngat_util.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/org_estar_astrometry.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/org_estar_io.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/org_estar_nodeagent.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/org_estar_rtml.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/saaj.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/soap.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/tea.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/wsdl4j-1.5.1.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/xalan.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/xerces.jar
WEB_APP_CLASSPATH=$WEB_APP_CLASSPATH:$WEB_APP_LIB/xmlsec-1.2.1.jar

export WEB_APP_CLASSPATH

echo WEB_APP_CLASSPATH : $WEB_APP_CLASSPATH

JAVA_HOME=/usr/java/jdk1.5.0_07

$JAVA_HOME/bin/java -cp $WEB_APP_CLASSPATH org.apache.axis.client.AdminClient -lhttp://localhost:8080/$SERVICE_NAME/services/AdminService undeploy_nodeagent.wsdd
