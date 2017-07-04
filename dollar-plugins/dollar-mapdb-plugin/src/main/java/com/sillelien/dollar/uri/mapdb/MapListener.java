package com.sillelien.dollar.uri.mapdb;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by neil on 04/07/2017.
 */
public interface MapListener<KEY, OLD, NEW> {
    void apply(@NotNull KEY key, @Nullable OLD oldValue, @Nullable NEW newValue);
}
