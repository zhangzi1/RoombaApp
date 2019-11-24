package com.example.roombaapp;

import android.app.Application;

public class MyApplication extends Application {
    private TCP sender = null;
    private TCP checker = null;

    public void setSender(TCP sender) {
        this.sender = sender;
    }

    public void setChecker(TCP checker) {
        this.checker = checker;
    }

    public TCP getSender() {
        return sender;
    }

    public TCP getChecker() {
        return checker;
    }
}
