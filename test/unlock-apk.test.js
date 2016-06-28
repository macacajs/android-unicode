/* ================================================================
 * unlock-apk by xdf(xudafeng[at]126.com)
 *
 * first created at : Mon Dec 21 2015 23:54:59 GMT+0800 (CST)
 *
 * ================================================================
 * Copyright 2013 xdf
 *
 * Licensed under the MIT License
 * You may not use this file except in compliance with the License.
 *
 * ================================================================ */

'use strict';

var unlockApk = require('..');

describe('test', function() {
  it('should be ok', function() {
    unlockApk.apkPath.should.be.ok;
  });
});
