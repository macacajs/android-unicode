#!/usr/bin/env node

/* ================================================================
 * unlock-apk by xdf(xudafeng[at]126.com)
 *
 * first created at : Wed Aug 26 2015 11:55:14 GMT+0800 (CST)
 *
 * ================================================================
 * Copyright  xdf
 *
 * Licensed under the MIT License
 * You may not use this file except in compliance with the License.
 *
 * ================================================================ */

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
