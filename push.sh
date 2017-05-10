#!/bin/sh
sbt docker:publishLocal
docker tag tagger-backend:1.0.0 webeng.azurecr.io/tagger-backend:latest
docker push webeng.azurecr.io/tagger-backend:latest
