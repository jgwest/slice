#!/bin/bash

echo Deleting $1

find code/ -name "*$1" -type f -delete

