# GEO Upload command-line Tool

[![Build Status](https://travis-ci.com/qbicsoftware/geo-uploader-cli.svg?branch=development)](https://travis-ci.com/qbicsoftware/geo-uploader-cli)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/geo-uploader-cli/branch/development/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/geo-uploader-cli)

GEO Upload command-line Tool, version 0.2.0 - Command-line utility to upload data to GEO by parsing the information from openBis.
## Author
Created by Julian Späth (julian.spaeth@student.uni-tuebingen.de).

Further development by Timo Lucas (timo-niklas.lucas@student.uni-tuebingen.de)

## Description

 This tool aims to simplify the data upload process for the NCBI Gene Expression Omnibus by automatically parsing project metadata from openBis and creating an excel sheet using the GEO template.


## How to Install

Download the jar release as well as the template sheet from GEO (geo-template.xlsx) and put them in the same directory.

## How to Run

Use java -cp to run the program.
Usage:

```console
java -cp geo-uploader-cli.jar [Main Class] -u [ZDV Login] -p [Project Identifier] -o [Output Path]
```
Example usage:

```console
java -cp geo-uploader-cli.jar life.qbic.cli.main.MainEntryPoint -u [ZDV Login] -p [Projcet Identifier] -o output/
```

Main Class: The main class of the java project. You can use life.qbic.cli.main.MainEntryPoint

ZDV Login: Username for login to openBis. In my case this is the ZDV login ID.

Project Identifier: Identifier of the QBiC project

Output Path: Path to were the output should be generated

## OpenBis Identifiers

The following OpenBis identifiers need are used to parse the information that is needed for the tool to run.
If any of this information is not provided the program's output will be incomplete and will need manual adjustment.
This information is stored on the openBis server and you may need to contact the administrator to get access to it.

The following fields need to be filled out for each sample:

* Q_NGS_SINGLE_SAMPLE_RUN
* Q_BIOLOGICAL_ENTITY
* Q_BIOLOGICAL_SAMPLE
* Q_TEST_SAMPLE
* Q_SEQUENCING_MODE
* Q_SAMPLE_TYPE
* Q_SECONDARY_NAME
* Q_PROPERTIES qcategorical
* Q_PRIMARY_TISSUE
* Q_NCBI_ORGANISM

## Config.yaml

Many properties of this CLI can be changed. For this you need to provide a config file.
The config file needs to look like this:

```yaml
#Information needed to connect ot openbis
---
app: [openBis appserver adress]
dss: [openBis dss server adress]
username: [openBis login username]
password: [openBis login password]

# Then enter Information needed for parsing. These are the default identifiers that are parsed from openBis and writen to the excel sheet. Change the identifiers to access different information from openBis
organism: Q_NCBI_ORGANISM
source_name: Q_PRIMARY_TISSUE
source_name_detailed: Q_TISSUE_DETAILED
title: Q_SECONDARY_NAME
molecule: Q_SAMPLE_TYPE
characteristics: Q_PROPERTIES
property: qcategorical
experiment: Q_SEQUENCING_MODE
```
