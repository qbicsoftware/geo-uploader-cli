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

# Mandatory Parameters:

Project Identifier: Identifier of the project

Output Path: Path to were the output should be generated

Path to config: A path to a config file. This file needs to be created manually based on the information given in this readme. See section Config File for more information. Future releases will include an example 'config.yaml' that needs to be adapted by the user himself.


# Optional Parameters

-md5: Required to calculate MD5 checksums. Path to a text file (.tsv) containing identifiers for the samples. The text file needs to contain a sample source identifier in each row. If this is set the geo-uploader will download the sample files and calculate the checksums for them. An example file can be found under the example folder at the root of this repository.


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

IMPORTANT:

The complete config file is not included with the software!
In order for the geo-uploader-cli to work you have to manually create a 'config.yaml' based on the given information! Future releases will include an example 'config.yaml' that can be adapted by the user.

-c [Path to config.yaml]: Provide a config file that includes login information as well as the openBis identifiers that are parsed to the excel file. All the fields except for the password have to be filled out in order for the program to run. If the password field is left empty you will be asked to insert it when running the geo-uploader-cli.

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
