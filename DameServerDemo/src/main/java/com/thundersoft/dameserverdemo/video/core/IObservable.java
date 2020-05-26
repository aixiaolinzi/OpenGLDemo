package com.thundersoft.dameserverdemo.video.core;


public interface IObservable<Type> {

    void addObserver(IObserver<Type> observer);

    void notify(Type type);

}
