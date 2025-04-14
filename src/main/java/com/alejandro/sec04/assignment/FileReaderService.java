package com.alejandro.sec04.assignment;

import reactor.core.publisher.Flux;

import java.nio.file.Path;

/*
* - do the work only when it is subscribed
* - do the work based on demand
* - stop producing when subscriber cancels
* - produce only the requested items
* - file should be closed once
* */
public interface FileReaderService {

    Flux<String> read(Path path);
}
