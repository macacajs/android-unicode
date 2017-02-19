'use strict';

const path = require('path');

exports.apkPath = path.join(__dirname, '..', 'bin', 'android_unicode_ime-debug.apk');
exports.package = 'macaca.unicode.ime';
exports.activity = '.Utf7ImeService';
