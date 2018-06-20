#!/bin/bash

DISK=/dev/sda # <===ADJUST THIS===
sync
echo 3 > /proc/sys/vm/drop_caches
blockdev --flushbufs $DISK
hdparm -F $DISK


