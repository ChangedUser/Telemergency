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

### Setup the Elastic Server
