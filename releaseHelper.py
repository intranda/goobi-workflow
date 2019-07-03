#!/usr/bin/env python3

import lxml.etree as ET
import semver

ns={"pom": "http://maven.apache.org/POM/4.0.0"}

def fix_node(node):
    if node.text.endswith("-SNAPSHOT"):
        node.text = node.text[:-9]
    else:
        node.text = semver.bump_patch(node.text)+"-SNAPSHOT"

def update_pom(filename, child=False):
    tree = ET.parse(filename)
    version_node = tree.find("/pom:version", namespaces=ns)
    fix_node(version_node)
    if child:
        parent_version = tree.find("/pom:parent/pom:version", namespaces=ns)
        fix_node(parent_version)
    with open(filename, "wb") as f:
        tree.write(f, pretty_print=True)
    return version_node.text

def main():
    new_version = update_pom("Goobi/pom.xml")
    update_pom("Goobi/module-jar/pom.xml", child=True)
    update_pom("Goobi/module-war/pom.xml", child=True)

    print(new_version, end='')

if __name__ == "__main__":
    main()
