# Telemergency
A medical healthcare app for emergency requests 

## Prerequisites 
### Setup the ICD-11 Server
  - Have Docker installed
    - https://docs.docker.com/desktop/install/windows-install/ (Windows)
    - https://docs.docker.com/desktop/install/linux-install/ (Linux)
    - https://docs.docker.com/desktop/install/mac-install/ (Mac)
  - Acquire the official Docker Image for the ICD API
    - https://hub.docker.com/r/whoicd/icd-api
  - Run it on a local 80:80 port with the acceptLicense flag set to true:
      > docker run -p 80:80 -e acceptLicense=true whoicd/icd-api
      - It should now run on either:
          > http://localhost/swagger/index.html for the Swagger Index Page or
          > http://localhost/browse/2024-01/mms/en for the local Browser 
  - For further information regarding the ICD-API:
      - https://id.who.int/swagger/index.html (Swagger Page)
      - https://icd.who.int/docs/icd-api/APIDoc-Version2/ (Docs) 

### Setup the Elastic and Kibana Server

In order to run docker-compose with one elasticsearch and one kibana container a docker-compose configuration File (docker-compose.yml) needs to be created with images for the raspberry pi, e.g.:

version: "2.0"

services:
  es01:
    image: comworkio/elasticsearch:7.9.1-1.8-arm
    container_name: es01
    ports:
      - 9200:9200
      - 9300:9300
    networks:
      - elastic
    volumes:
      - data01:/usr/share/elasticsearch/data
  kib01:
    image: comworkio/kibana:7.9.1-1.9-arm
    container_name: kib01
    ports:
      - 5601:5601
    environment:
      - ES_PROTO=http
      - ES_HOST=es01
      - ES_PORT=9200
    networks:
      - elastic
    depends_on: 
      - es01

volumes:
  data01:
    driver: local

networks:
  elastic:
    driver: bridge

After the docker-compose file is created with the following command the elastic and kibana container will start up: docker-compose up


