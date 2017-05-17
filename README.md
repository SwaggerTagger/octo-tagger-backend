Backend Application Server for Yolo-Tagger
==========================================

This server provides the Rest-Api for the Yolo-Tagging-Service. It was created using the [Play 2.5 Silhouette 4.0 Slick PostgreSQL seed](https://github.com/dpitkevics/play-silhouette-4.0-slick-postgres-seed) that provides Authorization and User Management, but customized to be accessible via a Rest-Api.

## Notable technologies/libraries used
 - [Slick](http://slick.lightbend.com/), a database abstraction layer
 - [Akka](http://akka.io/), an actor-based messaging system inside our application, that is for example used to connect to kafka
 - [Play](https://www.playframework.com/), a framework for web applications

## Tools
You can use Postman to directly communicate with the Api.
You can either:
  - Go to the [published documentation](https://documenter.getpostman.com/collection/view/12316-7a9d5d39-d5ff-280e-88d6-f48e3775836f)
  - Use the exported version here in the repository (file octo-tagger.postman_collection.json), but you have to create the correct environment yourself