package com.ganderson.exploreni.entities.data

/**
 * Wrapper class for API and DB functions using LiveData. The primary motivation behind this class
 * was to allow exceptions to be passed to the UI classes.
 */
data class DataResult<T>(val data: T?, val error: Throwable?)