# GEO Upload command-line Tool

[![Build Status](https://travis-ci.com/qbicsoftware/geo-uploader-cli.svg?branch=development)](https://travis-ci.com/qbicsoftware/geo-uploader-cli)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/geo-uploader-cli/branch/development/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/geo-uploader-cli)

GEO Upload command-line Tool - Command-line utility to upload data to GEO by parsing the information from openBis.
## Author
Created by Julian Späth (julian.spaeth@student.uni-tuebingen.de).

Further development by Timo Lucas (https://github.com/lucass122) (timo-niklas.lucas@student.uni-tuebingen.de)

## Description

 This tool aims to simplify the data upload process for the NCBI Gene Expression Omnibus by automatically parsing project metadata from openBis and creating an excel sheet using the GEO template.


## How to Install

A jar file including all the dependencies can be found under releases

## How to Run

Download the jar file and the config.yaml then use java -jar to run the program providing the project identifier, output path and the path to the config file.

Usage:

```console
java -jar geo-uploader-cli.jar -p [Project Identifier] -o [Output Path] -c [Path to config]
```
Example usage:

Project Identifier: Identifier of the QBiC project

Output Path: Path to were the output should be generated

# Optional Parameters

-md: Set this parameter to compute the md5 checksums of the raw files and write them to the excel file

## OpenBis Identifiers

The following OpenBis identifiers need are used to parse the information that is needed for the tool to run.
If any of this information is not provided the program's output will be incomplete and will need manual adjustment.

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

## Config File

-c [Path to config.yaml]: Provide a config file that includes login information as well as the openBis identifiers that are parsed to the excel file. All the fields except for the password have to be filled out in order for the program to run

Example Config file:

```yaml
app: [Application server adress] https://********/*****/****
dss: https://****.****.*******.**/[port]/********
username: [openBis login name]
password: [openBis password] #if this is empty the CLI will ask for the password at the start of the program

organism: Q_NCBI_ORGANISM
source_name: Q_PRIMARY_TISSUE
source_name_detailed: Q_TISSUE_DETAILED
title: Q_SECONDARY_NAME
molecule: Q_SAMPLE_TYPE
characteristics: Q_PROPERTIES
property: qcategorical
experiment: Q_SEQUENCING_MODE
```
