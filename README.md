Sample assessment app, as per http://talentappstore.github.io/tas-tenant-apis/doc/assessment-apps.html.

This app demonstrates a single assessment type, a basic math quiz. This code:

- produces POST /tenants (when a customer installs the app, insert into account table)
- produces DELETE /tenants (when a customer uninstalls the app, clean up)
- produces POST /assessments/byID/{}/tenantDeltaPings (learns about assessments being started or restarted) 
    - checks for error conditions (insufficient credits or missing view fields)
    - grabs basic information about the assessment by consuming GET /assessments/byID/{}
    - grabs name, email, phone number from the assessment's view
    - generates 4 random addition problems
    - if insufficient credits, or missing details (name etc.) PATCHes the assessment to be "Error"
    - otherwise PATCHes the assessment to be "In progress", causing an email to be sent to the candidate with a link to the quiz
- produces unprotected web pages where:
    - candidate can complete the quiz
    - candidate can see their result
- and SSO-protected web pages where:
    - user can see account details, and get more credits
    - user can see that candidate hasn't yet completed the quiz
    - user can see there's an error preventing the assessment from starting
    - user can see completed candidate results 
 
To run (e.g. in spring tool suite (eclipse)):

Clone
-----
git clone etc.


Start up the image server
-----------------
Build docker image and start as per https://github.com/talentappstore/imageGen.

Start named tunnel to it (please use your own tunnel name and update application.properties :):
```` 
~/Devtools/ngrok http -subdomain=imageserver 8081
````


Start up a local mysql database
----------

````
# start mysql in docker container
docker run --restart=always --name gauge-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=frodo -d mysql:latest

# get ip address of container
docker ps
docker inspect 99ae59d88276

# connect to database
docker run -it --rm mysql sh -c 'exec mysql --host=172.17.0.2 --port=3306 -uroot --password=frodo'

# create database
mysql> create database gauge;
Query OK, 1 row affected (0.00 sec)
mysql> use gauge;
Database changed
mysql>

# now edit application.properties to reflect correct ip address 
````



