# Goobi workflow Docker Image

## Install Goobi workflow based on Docker

Please make sure than you have [Git](https://git-scm.com/) and [Docker](https://www.docker.com/) installed on your machine to execute the following commands to install Goobi in your Docker environment.

```bash
# checkout the source code of Goobi workflow from Github
git clone https://github.com/intranda/goobi-workflow.git

# go into the checked out goobi directory
cd goobi-workflow

# startup the database to have it running for Goobi
docker-compose up -d goobi-db

# startup the Goobi container
docker-compose up -d
```

Goobi workflow is now running and can get accessed through a web browser using the following data:

Information | Description
------------|----------------------------
URL:        | <http://localhost:8080/goobi>
Login:      | testadmin
Password:   | test

Alternative login names are `testscanning`, `testqc`, `testmetadata`,`testprojectmanagement` and `testimaging`. The password for all those accounts is `test`.

## Stop Goobi workflow and restart it later again

To stop a running Goobi workflow instance please make sure that you are inside of the directory `goobi-workflow` to execute this command:

```bash
# stop Goobi workflow
docker-compose stop
```

To start a stopped Goobi workflow instance later again please make sure that you are in the directory `goobi-workflow` again as shown above. Then execute this command:

```bash
# restart Goobi workflow
docker-compose start
```

## Uninstall Goobi workflow from Docker

To uninstall Goobi workflow from your system and your Docker environment again please make sure you have stopped the Goobi workflow containers first as shown above. Afterwards please execute the following commands from inside of the directory `goobi-workflow`:

```bash

# remove running containers and the network configuration
docker-compose down

# cleanup the created volumes that Goobi created
docker volume prune

# cleanup the Goobi Docker image
docker image rm intranda/goobi-workflow

# cleanup the database Docker image
docker image rm mariadb:10.1

# leave the goobi directory again
cd ..

# delete the source code of Goobi workflow from your hard disk entirely
rm -rf goobi-workflow
```

## More information

Pre-built images are available on [Docker Hub](https://hub.docker.com/r/intranda/goobi-workflow). The command [docker-compose](https://docs.docker.com/compose/) is documented quite detailed there too.
To manually build the Docker image locally, please switch from using `image` to using the `build` section in the `docker-compose.yml` file and run the follwing command:

```bash
docker-compose build
```

### Further Configuration

The image provided here contains an example configuration. To allow the local editing of configuration files you can bind mount the Goobi configuration directory `/opt/digiverso/goobi/config` to a local directory containing the configuration files, see the comments in `docker-compose.yml`.
The image currently does not contain any Goobi plugins. You can bind mount the plugins directory (`/opt/digiverso/goobi/plugins`) to a local directory containing compiled [Goobi plugins](https://docs.intranda.com/goobi-workflow-plugins-en/).

### Information and Feedback

- Goobi Website: https://goobi.io
- Goobi Community: https://community.goobi.io/
- Goobi Documentation: https://docs.intranda.com
