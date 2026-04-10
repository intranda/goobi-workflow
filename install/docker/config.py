#!/usr/bin/env python3

import os
import secrets
import string

def load_properties():
    lines = []
    initial_props = {}
    if not os.path.isfile(fp):
        return [], initial_props

    with open(fp) as f:
        for i, line in enumerate(f):
            line = line.strip()

            if not "=" in line:
                lines.append(line)
                continue

            active = True
            # skip comments and empty lines
            if line.startswith("#"):
                active = False
                line = line.replace("#", "")

            key, value = line.split("=", 1)
            lines.append(line)
            if key in initial_props.keys() and not active:
                #print(f"skipping duplicate {key}")
                lines[-1] = f"#{lines[-1]}"
                continue
            initial_props[key.strip()] = {"line_num": i, "value": value, "active": active}

    return lines, initial_props


def load_env() -> dict[str, str]:
    env_props = {}

    prefix = "GOOBI_"

    for key, value in os.environ.items():
        if not key.startswith(prefix):
            continue

        stripped = key[len(prefix):]

        env_props[stripped] = value

    if "dashboardPlugin" not in env_props.keys() and os.path.exists("/workflow-template/default-plugins"):
        env_props["dashboardPlugin"] = "intranda_dashboard_extended"

    return env_props

def merge_properties(lines: list, initial_props: dict, env_props: dict):
    for key, value in env_props.items():
        if key not in initial_props.keys():
            lines.append(f"{key}={value}\n")
            continue
        initial_props[key]["value"] = value
        initial_props[key]["active"] = True

    if "jwtSecret" in initial_props.keys() and not initial_props["jwtSecret"]["active"]:
        initial_props["jwtSecret"]["value"] = ''.join(secrets.choice(string.ascii_letters + string.digits) for _ in range(20))
        initial_props["jwtSecret"]["active"] = True

    for key, entry in initial_props.items():
        lines[entry["line_num"]] = f"{key}={entry["value"]}" if entry["active"] else f"#{key}={entry["value"]}"

    with open(fp, "w") as f:
        f.write("\n".join(lines))
        f.write("\n")


def main():
    lines, initial_props = load_properties()
    env_props = load_env()
    merge_properties(lines, initial_props, env_props)


if __name__ == "__main__":
    fp = "/usr/local/tomcat/webapps/workflow/WEB-INF/classes/goobi_config.properties"
    main()