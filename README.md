iot-dashboard-app
==============

Vaadin application that only requires a Servlet 3.0 container to run.
The application displays a UI based on the items.xml configuration file (see com.jandebeule.iot.dashboard.ItemsPersistence.java for an example).

A dashboard consists of a tabsheet where you can define your own tabs with each tab contains its configured items.
Currently following items are supported to add to your dashboard (see also com.jandebeule.iot.dashboard.Item.java):
- HTTP_SUBSCRIBE : a HTML page that is refreshed at a fixed interval
- MQTT_SUBSCRIBE_LABEL : a Textfield showing the content from a MQTT topic
- MQTT_PUBLISH_SELECT : a dropdown/button which value is published to a MQTT topic
- MQTT_PUBLISH_SLIDER : a slider which value is published to a MQTT topic
- GRID : a nested grid that contains additional Items

The starting point for the Vaadin application is com.jandebeule.iot.dashboard.MyUI.java  


Workflow
========

To compile the entire project, run "mvn install".

To run the application, run "mvn jetty:run" and open http://localhost:8080/ .

To produce a deployable production mode WAR:
- change productionMode to true in the servlet class configuration (nested in the UI class)
- run "mvn clean package"
- test the war file with "mvn jetty:run-war"

Client-Side compilation
-------------------------

The generated maven project is using an automatically generated widgetset by default. 
When you add a dependency that needs client-side compilation, the maven plugin will 
automatically generate it for you. Your own client-side customisations can be added into
package "client".

Debugging client side code
  - run "mvn vaadin:run-codeserver" on a separate console while the application is running
  - activate Super Dev Mode in the debug window of the application

Developing a theme using the runtime compiler
-------------------------

When developing the theme, Vaadin can be configured to compile the SASS based
theme at runtime in the server. This way you can just modify the scss files in
your IDE and reload the browser to see changes.

To use the runtime compilation, open pom.xml and comment out the compile-theme 
goal from vaadin-maven-plugin configuration. To remove a possibly existing 
pre-compiled theme, run "mvn clean package" once.

When using the runtime compiler, running the application in the "run" mode 
(rather than in "debug" mode) can speed up consecutive theme compilations
significantly.

It is highly recommended to disable runtime compilation for production WAR files.

Using Vaadin pre-releases
-------------------------

If Vaadin pre-releases are not enabled by default, use the Maven parameter
"-P vaadin-prerelease" or change the activation default value of the profile in pom.xml .

Example dashboard
-----------------
![alt tag](https://github.com/jandebeule/iot-dashboard-app/blob/master/example1.png)
![alt tag](https://github.com/jandebeule/iot-dashboard-app/blob/master/example2.png)
![alt tag](https://github.com/jandebeule/iot-dashboard-app/blob/master/example3.png)


