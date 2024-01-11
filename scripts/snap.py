#! /usr/bin/env python3

# Copies from src/MPWorking into src/<package_name> and replaces all instances of MPWorking with <package_name>
import sys
import shutil, errno
import os
import argparse

def copyanything(src, dst):
    try:
        shutil.copytree(src, dst)
    except OSError as exc: # python >2.5
        if exc.errno in (errno.ENOTDIR, errno.EINVAL):
            shutil.copy(src, dst)
        else: raise

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Snapshots the MPWorking directory into a new package')
    parser.add_argument('package_name', type=str)
    parser.add_argument('--submission', action='store_true', help='Copy for submission')
    args = parser.parse_args()

    snapshot_name = args.package_name

    if snapshot_name[:2] != "MP":
        print("Invalid package name:", snapshot_name)
        print("Package name must start with MP")
        quit()

    working_name = "MPWorking"
    src_dir = "src"

    working_src = src_dir + "/" + working_name
    snapshot_dst = src_dir + "/" + snapshot_name

    debug_verbose_on = "static final boolean VERBOSE = true;"
    debug_verbose_off = "static final boolean VERBOSE = false;"

    local_resign_off = "// localResign();"
    local_resign_on = "localResign();"
    resign_on = "rc.resign();"
    resign_off = "// rc.resign();"
    assert_on = "assert";
    assert_off = "// assert";

    copyanything(working_src, snapshot_dst)

    for r, d, f in os.walk(snapshot_dst):
        for file in f:
            file_path = os.path.join(r, file)
            # Read in the file
            with open(file_path, 'r') as file :
                filedata = file.read()

            # Replace the target string
            filedata = filedata.replace(working_name, snapshot_name)
            filedata = filedata.replace(debug_verbose_on, debug_verbose_off)

            if args.submission:
                filedata = filedata.replace(local_resign_on, local_resign_off)
                filedata = filedata.replace(resign_on, resign_off)
                filedata = filedata.replace(assert_on, assert_off)
            else:
                filedata = filedata.replace(local_resign_off, local_resign_on)
                # filedata = filedata.replace(resign_off, resign_on)

            # Write the file out again
            with open(file_path, 'w') as new_file:
                new_file.write(filedata)
