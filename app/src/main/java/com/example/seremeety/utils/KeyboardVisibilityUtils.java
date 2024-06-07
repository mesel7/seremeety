package com.example.seremeety.utils;

import android.graphics.Rect;
import android.view.ViewTreeObserver;
import android.view.Window;

public class KeyboardVisibilityUtils {

    private static final int MIN_KEYBOARD_HEIGHT_PX = 150;

    private final Window window;
    private final OnShowKeyboard onShowKeyboard;
    private final OnHideKeyboard onHideKeyboard;

    private final Rect windowVisibleDisplayFrame = new Rect();
    private int lastVisibleDecorViewHeight;

    public interface OnShowKeyboard {
        void onShowKeyboard(int keyboardHeight);
    }

    public interface OnHideKeyboard {
        void onHideKeyboard();
    }

    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            window.getDecorView().getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
            int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

            if (lastVisibleDecorViewHeight != 0) {
                if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                    int currentKeyboardHeight = window.getDecorView().getHeight() - windowVisibleDisplayFrame.bottom;
                    if (onShowKeyboard != null) {
                        onShowKeyboard.onShowKeyboard(currentKeyboardHeight);
                    }
                } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                    if (onHideKeyboard != null) {
                        onHideKeyboard.onHideKeyboard();
                    }
                }
            }

            lastVisibleDecorViewHeight = visibleDecorViewHeight;
        }
    };

    public KeyboardVisibilityUtils(Window window, OnShowKeyboard onShowKeyboard, OnHideKeyboard onHideKeyboard) {
        this.window = window;
        this.onShowKeyboard = onShowKeyboard;
        this.onHideKeyboard = onHideKeyboard;
        window.getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void detachKeyboardListeners() {
        window.getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }
}
