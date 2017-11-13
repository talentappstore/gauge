Sample assessment app, as per http://talentappstore.github.io/tas-tenant-apis/doc/assessment-apps.html.

This app demonstrates a single assessment type, a basic math quiz.

When the user starts the assessment, the app fetches assessment details, fetches details about the candidate from the view, and then updates the assessment to the "In progress" status, and causes a link to the quiz to be sent to the candidate.


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







