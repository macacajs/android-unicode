#!/usr/bin/env node

'use strict';

var path = require('path');
var spawn = require('cross-spawn');

const cwd = path.join(__dirname, '..');
const gradleFile = path.join(cwd, 'gradlew');

var proc = spawn.sync(gradleFile, ['assembleDebug'], {
  stdio: 'inherit'
});

process.exit(proc.status);
