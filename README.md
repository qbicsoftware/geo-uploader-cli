# GEO Upload command-line Tool

[![Build Status](https://travis-ci.com/qbicsoftware/geo-uploader-cli.svg?branch=development)](https://travis-ci.com/qbicsoftware/geo-uploader-cli)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/geo-uploader-cli/branch/development/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/geo-uploader-cli)

GEO Upload command-line Tool, version 1.1.0 - Command-line utility to upload data to GEO by parsing the information from openBis.
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
java -cp geo-uploader-cli.jar life.qbic.cli.main.MainEntryPoint -u zxmvi59 -p QGVIN -o output/
```

Main Class: The main class of the java project. You can use life.qbic.cli.main.MainEntryPoint

ZDV Login: Username for login to openBis. In my case this is the ZDV login ID.

Project Identifier: Identifier of the QBiC project e.g. QMCKA

Output Path: Path to were the output should be generated

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

