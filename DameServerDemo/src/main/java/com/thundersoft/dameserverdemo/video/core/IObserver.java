package com.thundersoft.dameserverdemo.video.core;


public interface IObserver<Type> {

    void onCall(Type type);

}
