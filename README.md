#Perun application for SP registration.

##Build for devel
###Install necessary SW
- sudo apt install apache2 npm wget maven

###Set-up mapping for spreg.com
- vim /etc/hosts
- add following
```127.0.0.1  spreg.com```

###Set-up apache2:
- Enable necessary apache2 modules
- a2enmod proxy_http

#####Create web config for SPreg app
- cd /etc/apache2/sites-available
- vim spreg.conf
- Paste following: (and fill your EPPN - Slack message from dBucik)
```
<VirtualHost *:80>
        ServerName spreg.com
        ServerAdmin webmaster@localhost
        DocumentRoot /var/www/html

        # Always set these headers.
        Header always set Access-Control-Allow-Origin "http://localhost:4200"
        Header always set Access-Control-Allow-Credentials: true
        Header always set Access-Control-Allow-Methods "POST, GET, OPTIONS, DELETE, PUT"
        Header always set Access-Control-Max-Age "1000"
        Header always set Access-Control-Allow-Headers "x-requested-with, Content-Type, origin, authorization, accept, client-security-token"

        # Added a rewrite to respond with a 200 SUCCESS on every OPTIONS request.
        RewriteEngine On
        RewriteCond %{REQUEST_METHOD} OPTIONS
        RewriteRule ^(.*)$ $1 [R=200,L]

        ErrorLog ${APACHE_LOG_DIR}/error.log
        CustomLog ${APACHE_LOG_DIR}/access.log combined

        # Set fake attribute for login
        # TODO: fill your EPPN from external identity associated with https://login.cesnet.cz/idp/ ExtSource
        RequestHeader add FAKE-USR-HDR "yourEppn@einfa.cesnet.cz"

        # Proxy pass to tomcat
        ProxyRequests Off
        ProxyVia Off
        ProxyPreserveHost On
        ProxyStatus On

        ProxyPass           /spreg    http://localhost:8081/spreg
        ProxyPassReverse    /spreg    http://localhost:4200/spreg
        ProxyPassReverseCookiePath  /spreg /spreg

</VirtualHost>
# vim: syntax=apache ts=4 sw=4 sts=4 sr noet
```
#####Enable new site:
* a2ensite spreg.conf

###Run the project
- run backend (API):
  - if you want to run it from IDEA, open configuraiton of run environment (select box on left of "play" button), click on edit configurations and select "Application". There add following to the "override parameters" section (key -> value):
    -  server.port -> 8081
    - server.servlet.context-path -> /spreg
    - dev.enabled -> true
    - note: If you do not have the "Application" run environment there, navigate in project structure to "api/src/main/java/cz/metacentrum/perun/spRegistration" and right click on the Application.class file. Then hit "Run 'Application'"

  - if you want to run it from terminal, navigate to the directory with project files. In the project go to the "api" direcotry. Then execute following command: "mvn clean spring-boot:run -Dserver.port=8081 -Dserver.servlet.context-path=/spreg -Ddev.enabled=true". To kill the process hit Ctrl + C.

- run frontend (GUI):
  - navigate to the directory with project files. Then go to the "gui" directory. To run the frontend execute following command: "ng serve --public-host http://localhost:4200 --deployUrl=/spreg/ --baseHref=/spreg/".

###Access the page with application
- open your browser and navigate to the "http://localhost:4200/spreg".

##Build for production
- navigate to the project directory and execute "mvn clean package -Dprod=True"
- after the build has finished go to the api/target directory. You will have to place the "spreg.war" file to the /var/lib/tomcat8/webapps folder on the machine. Logs are available via "tail -f /var/log/tomcat8/catalina.out"


##Facilities in Perun
- If you want to add facility that should be displayed as yours, you have to create it in Perun (perun-dev.cesnet.cz/fed/gui). Go to the Facility Manager taband create facility. Important thing is to set attribute "ProxyIdentifiers" of the facility to value "identifier1" It should be then available in list of facilities



	


