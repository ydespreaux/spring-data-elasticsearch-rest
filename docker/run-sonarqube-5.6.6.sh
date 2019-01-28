#!/usr/bin/env bash
docker run -d --name sonarqube -p 9000:9000 -p 9095:9092 sonarqube:6.6