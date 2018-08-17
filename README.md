Thanks for interesting in the advance event management tool build on HCP (Hana Cloud Platform) NEO. It was designed as requested by SAP Labs and used by several projects successfully: kid@sap, autumn@sap, marathon@sap ...

It provides following features:
* Flexible and powerful form design
* Support maximum registration limitation check. After reach the limitation, new register will enter to waiting list. When one register cancel his registration, then the first one will move from the waiting list to the registered status
* Two level security:  project level (public or private), registration level( public, protected, private)
* Authenticated integrated with SAP Global Domain User repository


You can try the version in [https://projectui5-p2000536732trial.dispatcher.hanatrial.ondemand.com/mng/index.html](https://projectui5-p2000536732trial.dispatcher.hanatrial.ondemand.com/mng/index.html)

# How to setup in NEO environment. 
   Please first create one DB according to this guideline [steps-to-create-database-tables-in-sap-hana-cloud-platform-formerly-hcp.](https://blogs.sap.com/2017/05/31/steps-to-create-database-tables-in-sap-hana-cloud-platform-formerly-hcp./)

## Deploy Java application projectMng
   1. Download the project projectui5 from github, and compile it by run "mvn install" in the root project. 
      How to check whether compile successful or not: It will create projectMng.war in the target directory
   2. Deploy the java application.  Please choose run time name "**Java EE 7 Web Profile TomEE 7**" or "**Java Web Tomcat 7**"
   3. Create a new Data Source Bindings for the new deployed java application
      Select the java app, go to "Configuration"-->Data Source Bindings, click "New Binding" to create a new Data Source

## Deploy UI5 application
   1. Download the project projectui5 from github, go to the root directory and create a zip file
   2. Deploy a HTML5 application
   3. Create a destination named "projectodata" with following properties:
           type: HTTP   Proxy Type: Internet         Authentication: AppToAppSSO
           URL: The url of your java application, such as https://projectmngi068108trial.hanatrial.ondemand.com/projectMng

# User manual 
  Detail user manual will be provided soon, actually it is so easy to use and only need to understand following basic concept:

Two roles: 
    Project Manager: who will create a project (set up important property such as time/location) and need manage the registration (approve/reject, explore)
    Employee: who will register to a project 

Typical process:
 1. Project manager create a project by access the HTML5 application projectui5 URL, such as https://projectui5-i068108trial.dispatcher.hanatrial.ondemand.com/
2.  After finish design, click the button "Employee Registration Window" at the footer bar.  It will open a new browser with the url similar https://projectui5-i068108trial.dispatcher.hanatrial.ondemand.com/?hc_reset&projectId=1.   It is the URL for employee to register
3.  Employee use the above register URL to register
