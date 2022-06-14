#!/usr/bin/env python3

import lxml.etree as ET
import semver
import sys
import re

ns={"pom": "http://maven.apache.org/POM/4.0.0"}

def update_pom(filename, new_version, child=False):
    tree = ET.parse(filename)
    new_public_version = ""
    if not child:
        public_version_node = tree.find("/pom:properties/pom:publicVersion", namespaces=ns)
        public_version_node.text = new_version
        
    version_node = tree.find("/pom:version", namespaces=ns)
    version_node.text = new_version
    if child:
        parent_version = tree.find("/pom:parent/pom:version", namespaces=ns)
        parent_version.text = new_version
    with open(filename, "wb") as f:
        tree.write(f, pretty_print=True)

def get_new_version(old_version, bump_month=False):
    if not bump_month and old_version.endswith("-SNAPSHOT"):
        return old_version[:-9]
    fields = re.findall(r"\d+", old_version)
    year, month = fields[:2]
    if bump_month:
        if int(month) == 12:
            year = int(year)+1
            month = 1
        else:
            year = int(year)
            month = int(month)+1
        new_version = "{:02d}.{:02d}".format(year, month)
    else:
        minor = "1" if len(fields) < 3 else int(fields[2])+1
        new_version = "{:02d}.{:02d}.{}".format(int(year), int(month), int(minor)) + "-SNAPSHOT"
    return new_version

def get_old_version(filename):
    tree = ET.parse(filename)
    public_version_node = tree.find("/pom:properties/pom:publicVersion", namespaces=ns)
    return public_version_node.text


def main():
    bump_month = len(sys.argv) > 1 and sys.argv[1] == "--bump_month"
    old_version = get_old_version("Goobi/pom.xml")
    new_version = get_new_version(old_version, bump_month=bump_month)
    update_pom("Goobi/pom.xml", new_version)
    update_pom("Goobi/module-ci/pom.xml", new_version, child=True)
    update_pom("Goobi/module-jar/pom.xml", new_version, child=True)
    update_pom("Goobi/module-war/pom.xml", new_version, child=True)

    print(new_version, end='')

def test():
    old_versions = ["20.12", "20.12.1", "21.01", "21.01.1", "21.01.1-SNAPSHOT", "21.01-SNAPSHOT", "21.12.01-SNAPSHOT", "21.12-SNAPSHOT"]
    bump_month = False
    for old_version in old_versions:
        print(bump_month, old_version, "->", get_new_version(old_version, bump_month=bump_month))
    print()
    bump_month = True
    for old_version in old_versions:
        print(bump_month, old_version, "->", get_new_version(old_version, bump_month=bump_month))

    return


if __name__ == "__main__":
    main()
