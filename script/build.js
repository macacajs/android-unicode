#!/usr/bin/env node

'use strict';

var path = require('path');
var antBuild = require('ant-build');

antBuild({
  cwd: path.join(__dirname, '..')
}).then(function(d) {
  console.log(d); 
}).catch(function(e) {
  setTimeout(function() {
    console.log(e);
    throw e;
  });
});
