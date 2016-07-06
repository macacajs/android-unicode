# android-unicode

[![NPM version][npm-image]][npm-url]
[![build status][travis-image]][travis-url]
[![Test coverage][coveralls-image]][coveralls-url]
[![node version][node-image]][node-url]
[![npm download][download-image]][download-url]

[npm-image]: https://img.shields.io/npm/v/android-unicode.svg?style=flat-square
[npm-url]: https://npmjs.org/package/android-unicode
[travis-image]: https://img.shields.io/travis/xudafeng/android-unicode.svg?style=flat-square
[travis-url]: https://travis-ci.org/xudafeng/android-unicode
[coveralls-image]: https://img.shields.io/coveralls/xudafeng/android-unicode.svg?style=flat-square
[coveralls-url]: https://coveralls.io/r/xudafeng/android-unicode?branch=master
[node-image]: https://img.shields.io/badge/node.js-%3E=_0.10-green.svg?style=flat-square
[node-url]: http://nodejs.org/download/
[download-image]: https://img.shields.io/npm/dm/android-unicode.svg?style=flat-square
[download-url]: https://npmjs.org/package/android-unicode

> Android unicode [UTF-7](https://tools.ietf.org/html/rfc2152) input apk

[uiautomator-unicode-input-helper](https://github.com/sumio/uiautomator-unicode-input-helper) fork to Node.js wrapper.

## Installment

```shell
$ npm i android-unicode --save
```

## Use in adb shell
Ref: <https://github.com/senzhk/ADBKeyBoard>


1. Sending text input

	```
	adb shell am broadcast -a ADB_INPUT_TEXT --es msg '你好Hello'
	adb shell am broadcast -a ADB_INPUT_TEXT --es format base64 --es msg 'K1RpMWxody1jbg=='
	```

	`K1RpMWxody1jbg==` is encode with `UTF-7` and `Base64`

	Use python code like this.

	```python
	import base64

	s = u'你好''
	print base64.b64encode(s.encode("utf-7"))
	```

2. Sending keyevent code  (67 = KEYCODE_DEL)

	```
	adb shell am broadcast -a ADB_INPUT_CODE --ei code 67
	# repeat 10 times
	adb shell am broadcast -a ADB_INPUT_CODE --ei code 67 --ei repeat 10
	```

3. Sending editor action (2 = IME_ACTION_GO)

	```
	adb shell am broadcast -a ADB_EDITOR_CODE --ei code 2
	```

4. Sending unicode characters

	```
	# To send 😸 Cat
	adb shell am broadcast -a ADB_INPUT_CHARS --eia chars '128568,32,67,97,116'
	```

Interact with python

```py
import base64

s = u'中文cn'.encode('utf-7')
print s
# expect: +Ti1lhw-cn
print base64.b64encode(s)
# expect: K1RpMWxody1jbg==
```

Open in command

```
adb shell am broadcast -a ADB_INPUT_TEXT --es msg 'K1RpMWxody1jbg==' --es format base64
```

## Usage
Switch to Utf7ImeService

```sh
adb shell ime set android.unicode.ime/.Utf7ImeService
```

- KeyEvent Code Ref: <http://developer.android.com/reference/android/view/KeyEvent.html>
- Editor Action Code Ref: <http://developer.android.com/reference/android/view/inputmethod/EditorInfo.html>

## License

The MIT License (MIT)
