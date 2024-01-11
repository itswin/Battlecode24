#!/usr/bin/env bash
./scripts/snap.py MPSubmit --submission
cd src
zip -r MPSubmit.zip MPSubmit/*
mv MPSubmit.zip ../
rm -rf MPSubmit
cd ..
