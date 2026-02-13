#!/usr/bin/env python3

import os

def load_properties() -> dict[str, str]:
    props = {}
    if not os.path.isfile("/opt/digiverso/goobi/config/goobi_config.user.properties"):
        return props

    with open("/opt/digiverso/goobi/config/goobi_config.user.properties") as f:
        for line in f:
            line = line.strip()

            # skip comments and empty lines
            if not line or line.startswith("#"):
                continue

            if "=" not in line:
                continue

            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()

    return props


def load_env() -> dict[str, str]:
    env_props = {}

    prefix = "GOOBI_"

    for key, value in os.environ.items():
        if not key.startswith(prefix):
            continue

        stripped = key[len(prefix):]

        env_props[stripped] = value

    return env_props

def write_properties(props: dict):
    with open("/opt/digiverso/goobi/config/goobi_config.properties", "w") as f:
        f.write(
            "# --------------------------------------------------\n"
            "# GENERATED FILE - DO NOT EDIT\n"
            "# SOURCE: Either edit the 'goobi_config.user.properties' file or add an environment variables like 'GOOBI_<property>=<value>'\n"
            "# IMPORTANT: File properties are discarded over environment variables\n"
            "# --------------------------------------------------\n\n"
        )

        for key in sorted(props):
            f.write(f"{key}={props[key]}\n")

if __name__ == "__main__":

    props = load_properties()
    props.update(load_env())
    write_properties(props)
