'use strict';

const path = require('path');

exports.apkPath = path.join(__dirname, '..', 'app', 'build', 'outputs', 'apk', 'app-debug.apk');
exports.package = 'macaca.unicode.ime';
exports.activity = '.Utf7ImeService';
