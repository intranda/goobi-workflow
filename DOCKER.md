# Goobi Docker Image

Pre-built images are available on [Docker Hub](https://hub.docker.com/r/intranda/goobi-workflow).

The easiest way to run Goobi in Docker is to use [docker-compose](https://docs.docker.com/compose/).

You will need a local clone of the [Goobi git repository](https://github.com/intranda/goobi/) to run the Docker image and database. 
```bash
git clone https://github.com/intranda/goobi.git
cd goobi
```

## Prepare Database Container

You can start and initialize the database container, this avoids starting the application before the database is ready:
```bash
docker-compose up -d goobi-db
```

## Container Start

Start all containers in the background:
```bash
docker-compose up -d
```
The Goobi UI will be available at http://localhost:8080/goobi shortly after.

## Manually Build Images
To manually build the Docker image locally, please switch from using *image* to using the *build* section in the *docker-compose.yml* file and run
```bash
docker-compose build
```

## Further Configuration

The image contains an example configuration. To allow the local editing of configuration files you can bind mount the Goobi configuration directory (*/opt/digiverso/goobi/config*) to a local directory containing configuration files, s. comments in *docker-compose.yml*.
The image currently does not contain any Goobi plugins. You can bind mount the plugins directory (*/opt/digiverso/goobi/plugins*) to a local directory containing compiled [Goobi plugins](https://docs.intranda.com/goobi-workflow-plugins-en/).


## Information and Feedback

- Goobi Website: https://goobi.io
- Goobi Community: https://community.goobi.io/
- Goobi Documentation: https://docs.intranda.com
