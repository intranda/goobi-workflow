# Goobi Docker Image Creation

## Build Images and Prepare Database

The easiest way to build and run Goobi in Docker is to use [docker-compose](https://docs.docker.com/compose/).

You will need a local clone of the [Goobi git repository](https://github.com/intranda/goobi/) to build the Docker image. The Goobi Docker image creation is then started in the local git repository:
```bash
git clone https://github.com/intranda/goobi.git
cd goobi
docker-compose build
```
This will use a Docker container to compile Goobi and then assemble the final image.

Afterwards you can start and initialize the database container:
```bash
docker-compose up -d goobi-db
```

## Container Start

Start all containers in the background:
```bash
docker-compose up -d
```
The Goobi UI will be available at http://localhost:8080/goobi shortly after.

## Further Configuration

The image contains an example configuration. To allow the local editing of configuration files you can bind mount the Goobi configuration directory (*/opt/digiverso/goobi/config*) to a local directory containing configuration files, s. comments in *docker-compose.yml*.
The image currently does not contain any Goobi plugins. You can bind mount the plugins directory (*/opt/digiverso/goobi/plugins*) to a local directory containing compiled [Goobi plugins](https://docs.intranda.com/goobi-workflow-plugins-en/).


## Information and Feedback

- Goobi Website: https://goobi.io
- Goobi Community: https://community.goobi.io/
- Goobi Documentation: https://docs.intranda.com
