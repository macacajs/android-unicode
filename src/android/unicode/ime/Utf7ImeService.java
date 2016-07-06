/*
 *    Copyright 2013 TOYAMA Sumio <jun.nama@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.unicode.ime;

import java.nio.charset.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.util.Log;

import android.inputmethodservice.InputMethodService;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * <p>
 * Utf7ImeService enables users to input any Unicode character by using only the
 * hardware keyboard. The selection of word candidates is not necessary. <br />
 * Using automated testing tools such as Uiautomator, it is impossible to input
 * non-ASCII characters directly. Utf7ImeService helps you to input any
 * characters by using Uiautomator.
 * </p>
 * <p>
 * String that is input from the keyboard, must be encoded in Modified UTF-7
 * (see RFC 3501).
 * </p>
 *
 * @author TOYAMA Sumio
 */
public class Utf7ImeService extends InputMethodService {

    private static final String TAG = "Utf7ImeService";

    /**
     * Expected encoding for hardware key input.
     */
    private static final String UTF7 = "UTF-7";

    private static final String ASCII = "US-ASCII";

    /**
     * Special character to shift to Modified BASE64 in modified UTF-7.
     */
    private static final char UTF7_SHIFT = '+';

    /**
     * Special character to shift back to US-ASCII in modified UTF-7.
     */
    private static final char UTF7_UNSHIFT = '-';

    /**
     * Indicates if current UTF-7 state is Modified BASE64 or not.
     */
    private boolean mIsShifted;
    private long mMetaState;
    private StringBuilder mComposing;
    private Charset mUtf7Charset;

    private String IME_MESSAGE = "ADB_INPUT_TEXT";
    private String IME_CHARS = "ADB_INPUT_CHARS";
    private String IME_KEYCODE = "ADB_INPUT_CODE";
    private String IME_EDITORCODE = "ADB_EDITOR_CODE";
    private BroadcastReceiver mReceiver = null;


    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        if (!restarting) {
            mMetaState = 0;
            mIsShifted = false;
            mUtf7Charset = Charset.forName(UTF7);
        }
        mComposing = null;
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        mUtf7Charset = null;
        mComposing = null;
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    // @Override
    // public boolean onEvaluateInputViewShown() {
    //     return false;
    // }

    /**
     * Translates key events encoded in modified UTF-7 into Unicode text.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, String.format("onKeyDown(): keyCode = %x", keyCode));
        int c = getUnicodeChar(keyCode, event);

        if (c == 0) {
            return super.onKeyDown(keyCode, event);
        }

        // Log.d(TAG, String.format("onKeyDown(): char = %c [%x]", c, c));

        if (!mIsShifted) {
            if (c == UTF7_SHIFT) {
                toShifted();
                return true;
            } else if (isAsciiPrintable(c)) {
                commitCharacter(c);
                return true;
            }
            // In unshifted (direct encoding) state, any non-printable character
            // is sent directly.
            return super.onKeyDown(keyCode, event);
        }

        // Shifted State
        if (c == UTF7_UNSHIFT) {
            toUnshifted();
        } else if (!isAlphanumeric(c)) {
            toUnshifted();
            commitCharacter(c);
        } else {
            appendComposing(c);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Log.d(TAG, String.format("onKeyUp (%x)", keyCode));
        mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState, keyCode, event);
        return super.onKeyUp(keyCode, event);
    }

    private void toShifted() {
        // Log.d(TAG, "SHIFTED!!");
        mIsShifted = true;
        mComposing = new StringBuilder();
        appendComposing(UTF7_SHIFT);
    }

    private void toUnshifted() {
        // Log.d(TAG, "toUnshifted()");
        mIsShifted = false;
        mComposing.append(UTF7_UNSHIFT);
        String decoded = decodeUtf7(mComposing.toString());
        InputConnection ic = getCurrentInputConnection();
        ic.commitText(decoded, 1);
        mComposing = null;
    }

    private int getUnicodeChar(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState, keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        return c;
    }

    private void commitCharacter(int c) {
        getCurrentInputConnection().commitText(String.valueOf((char) c), 1);
    }

    private void appendComposing(int c) {
        mComposing.append((char) c);
        getCurrentInputConnection().setComposingText(mComposing, 1);
    }

    private String decodeUtf7(String encStr) {
        byte[] encoded = encStr.getBytes(Charset.forName(ASCII));
        return new String(encoded, mUtf7Charset);
    }

    private boolean isAsciiPrintable(int c) {
        return c >= 0x20 && c <= 0x7E;
    }

    private static boolean isAlphanumeric(int c) {
        // reference: http://www.asciitable.com/
        return (c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a);
    }

    @Override
    public View onCreateInputView() {
        Log.d(TAG, "onCreateInputView()");
        View mInputView = getLayoutInflater().inflate(R.layout.keyboard, null);

        if (mReceiver == null) {
            IntentFilter filter = new IntentFilter(IME_MESSAGE);
            filter.addAction(IME_CHARS);
            filter.addAction(IME_KEYCODE);
            filter.addAction(IME_EDITORCODE);
            mReceiver = new AdbReceiver();
            registerReceiver(mReceiver, filter);
        }

        return mInputView; 
    } 
    
    public void onDestroy() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.onDestroy();      
    }

    class AdbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IME_MESSAGE)) {
                String msg = intent.getStringExtra("msg");
                if (msg == null) {
                    return;
                }
                String format = intent.getStringExtra("format");
                Log.d(TAG, "Input Format: " + format);
                if (format != null && format.equals("base64")) {
                    String utf7msg = new String(Base64.decode(msg));
                    msg = decodeUtf7(utf7msg);
                }

                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    Log.d(TAG, "Input message: " + msg);
                    ic.commitText(msg, 1);
                }
            }
            
            if (intent.getAction().equals(IME_CHARS)) {
                int[] chars = intent.getIntArrayExtra("chars");             
                if (chars != null) {                    
                    String msg = new String(chars, 0, chars.length);
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.commitText(msg, 1);
                    }
                }
            }
            
            if (intent.getAction().equals(IME_KEYCODE)) {               
                int code = intent.getIntExtra("code", -1);
                int repeat = intent.getIntExtra("repeat", 1);
                if (code != -1) {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        for (int i=0; i < repeat; i++) {
                            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
                        }
                    }
                }
            }
            
            if (intent.getAction().equals(IME_EDITORCODE)) {                
                int code = intent.getIntExtra("code", -1);              
                if (code != -1) {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.performEditorAction(code);
                    }
                }
            }
        }
    }
}
