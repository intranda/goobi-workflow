# Goobi workflow Docker Image

## Install Goobi workflow based on Docker

Please make sure than you have [Docker](https://www.docker.com/) installed on your machine to execute the following
commands to install Goobi in your Docker environment.

```bash
# download the docker-compose file from the repository e.g. using wget
mkdir goobi-workflow && wget -O goobi-workflow/docker-compose.yml https://raw.githubusercontent.com/intranda/goobi-workflow/master/docker-compose.yml

# go into the checked out goobi directory
cd goobi-workflow

# open the docker-compose.yml file in your favourite editor and change the default passwords to a value of your choice
gedit docker-compose.yml

# you can also choose to install the `slim` version without any plugins pre-installed from the docker-compose.yml file
image: intranda/goobi-workflow:latest
image: intranda/goobi-workflow:latest-slim 

# start the workflow application
docker compose up -d
```

Goobi workflow is now running and can get accessed through a web browser using the following data:

 Information | Description                                                
-------------|------------------------------------------------------------
 URL:        | <http://localhost:8080/workflow>                           
 Login:      | testadmin                                                  
 Password:   | ${PW_GOOBITESTUSER} (<i>value from docker-compose.yml</i>) 

Alternative login names are `testscanning`, `testqc`, `testmetadata`,`testprojectmanagement` and `testimaging`. The
password for all those accounts is also the value of PW_GOOBITESTUSER you specified in the docker-compose.yml file.

## Stop Goobi workflow and restart it later again

To stop a running Goobi workflow instance please make sure that you are inside of the directory `goobi-workflow` to
execute this command:

```bash
# stop Goobi workflow
docker compose stop
```

To start a stopped Goobi workflow instance later again please make sure that you are in the directory `goobi-workflow`
again as shown above. Then execute this command:

```bash
# restart Goobi workflow
docker compose start
```

## Uninstall Goobi workflow from Docker

To uninstall Goobi workflow from your system please execute the following commands from inside the
directory `goobi-workflow`:

```bash
cd goobi-workflow

# remove running containers and the network configuration
docker-compose down

# delete all the application data
cd .. && sudo rm -rf goobi-workflow

# cleanup the Goobi Docker image
docker image rm intranda/goobi-workflow:latest
docker image rm intranda/goobi-workflow:latest-slim

# cleanup the database Docker image
docker image rm mariadb:latest
```

## More information

### Application Configuration

#### goobi_config.properties
All properties of the [goobi_config.properties](https://docs.goobi.io/en/workflow/manual/admin/config_files/goobi_config.properties) can be set directly from the docker-compose.yml file using environment variables.
This requires the following schema:
```yaml
environment:
  - GOOBI_<propertyName>: Value
  # example; sets the value of the property 'ApplicationHeaderTitle':
  - GOOBI_ApplicationHeaderTitle: "Goobi workflow"
```
#### Other config files
All config files (including the goobi_config.properties) are located inside `goobi-workflow/goobi/config`. These can be edited which sets or overrides the default properties and the ones specified inside the docker-compose.yml <br>
You can either edit them directly on the system or via the [config editor](https://docs.goobi.io/en/workflow/manual/manager/goobi_configeditor) inside the admin tab after logging in. <br>
NOTE: The empty config files are created when starting the container for the first time, but existing files won't be overwritten in case you want to start the container directly with your custom configuration.

### Plugins

Plugins must be installed to the directory `goobi-workflow/goobi/plugins` identical to the native installation which uses the directory `/opt/digiversion/goobi/plugins`.

The image provided here contains an example configuration. To allow the local editing of configuration files you can
bind mount the Goobi configuration directory `/opt/digiverso/goobi/config` to a local directory containing the
configuration files, see the comments in `docker-compose.yml`.
The image currently does not contain any Goobi plugins. You can bind mount the plugins directory (
`/opt/digiverso/goobi/plugins`) to a local directory containing
compiled [Goobi plugins](https://docs.goobi.io/en/workflow/plugins).

### Information and Feedback

- Goobi Website: https://goobi.io
- Goobi Community: https://community.goobi.io/
- Goobi Documentation: https://docs.goobi.io
