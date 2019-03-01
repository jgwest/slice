#!/bin/bash

cd code/

ftypes=$(find . -type f | grep -E ".*\.[a-zA-Z0-9]*$" | sed -e 's/.*\(\.[a-zA-Z0-9]*\)$/\1/' | sort | uniq)

for ft in $ftypes
do
    echo -n "$ft "
    find . -name "*${ft}" -exec ls -l {} \; | awk '{total += $5} END {print total}'
done


