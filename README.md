# 52°North Sensor Event Service

<<<<<<< HEAD
For information on this project visit [http://52north.org/ses](http://52north.org/ses).
=======
The Sensor Event Service (SES) is used to provide a publish/subscribe
based access to not only sensor measurements but several other data
formats (e.g. aviation-specific data). It also provides methods to
dynamically register new data publishers and send notifications to
the service. The SES specification is currently available as an OGC
discussion paper (OGC 08-133).

## Code Compilation

This project is managed with Maven3. Simply run `mvn clean install`
to create a deployable .WAR file. `mvn clean install -P integration-test`
additionally enables integration tests.
>>>>>>> releasing-1.2.0

## Branches

This project follows the  [Gitflow branching model](http://nvie.com/posts/a-successful-git-branching-model/). "master" reflects the latest stable release.
Ongoing development is done in branch [develop](../../tree/develop) and dedicated feature branches (feature-*).
<<<<<<< HEAD
=======

## Deployment Hints

If you need UTF-8 capabilities (e.g. special characters in subscriptions
and messages) you must ensure that the `file.encoding` JVM property is
set to "UTF-8" within your servlet container. E.g. Tomcat accepts it to
be set in the `CATALINA_OPTS` (-Dfile.encoding="UTF-8").

## More Information

[http://52north.org/ses](http://52north.org/ses)
>>>>>>> releasing-1.2.0
