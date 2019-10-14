#!/usr/bin/env python3

import lxml.etree as ET
import semver
import sys
import re

ns={"pom": "http://maven.apache.org/POM/4.0.0"}

def fix_node(node):
    if node.text.endswith("-SNAPSHOT"):
        node.text = node.text[:-9]
    else:
        node.text = semver.bump_patch(node.text)+"-SNAPSHOT"

def update_pom(filename, child=False, bump_month=False):
    tree = ET.parse(filename)
    new_public_version = ""
    if not child:
        public_version_node = tree.find("/pom:properties/pom:publicVersion", namespaces=ns)
        fields = re.findall(r"\d+", public_version_node.text)
        year, month = fields[:2]
        if bump_month:
            if int(month) == 12:
                year = int(year)+1
                month = 1
            else:
                year = int(year)
                month = int(month)+1
            public_version_node.text = "{:02d}.{:02d}".format(year, month)
        else:
            minor = "1" if len(fields) < 3 else int(fields[2])+1
            public_version_node.text = "{:02d}.{:02d}-{}".format(int(year), int(month), int(minor))
        new_public_version = public_version_node.text
        
    version_node = tree.find("/pom:version", namespaces=ns)
    fix_node(version_node)
    if child:
        parent_version = tree.find("/pom:parent/pom:version", namespaces=ns)
        fix_node(parent_version)
    with open(filename, "wb") as f:
        tree.write(f, pretty_print=True)
    return new_public_version

def main():
    bump_month = len(sys.argv) > 1 and sys.argv[1] == "--bump_month"
    new_version = update_pom("Goobi/pom.xml", bump_month=bump_month)
    update_pom("Goobi/module-jar/pom.xml", child=True)
    update_pom("Goobi/module-war/pom.xml", child=True)

    print(new_version, end='')

if __name__ == "__main__":
    main()
