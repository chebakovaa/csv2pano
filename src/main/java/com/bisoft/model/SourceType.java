package com.bisoft.model;

import com.bisoft.interfaces.INeoQuery;

public final record SourceType(String prefix, INeoQuery query) { }
