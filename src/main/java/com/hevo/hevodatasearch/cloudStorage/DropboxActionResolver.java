package com.hevo.hevodatasearch.cloudStorage;

@FunctionalInterface
interface DropboxActionResolver<T> {

    T perform() throws Exception;

}
