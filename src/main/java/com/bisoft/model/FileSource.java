package com.bisoft.model;

import com.bisoft.navi.common.exceptions.GetObjectNamesException;
import com.bisoft.navi.common.exceptions.LoadStructureSourceException;
import com.bisoft.navi.common.interfaces.IModelObject;
import com.bisoft.navi.common.interfaces.ISavedFormat;
import com.bisoft.navi.common.interfaces.IStructureSource;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

public class FileSource implements IStructureSource {
	
	
	private final File folder;
	private final ISavedFormat format;
	
	public FileSource(File folder, ISavedFormat format) {
		this.folder = folder;
		this.format = format;
	}
	
	private String removeExtension(Path fileName) {
		return fileName.toString().replaceAll("(?<!^)[.]" + "[^.]*$", "");
	}
	
	@Override
	public Iterator<IModelObject> objectCollection(final String prefix) throws LoadStructureSourceException {
		Arrays
			.stream(folder.listFiles())
			.filter(v -> v.isFile() && v.toString().contains(prefix))
			.map(v ->
				new FileModelObject(
					removeExtension(v.toPath().getFileName())
					, new FileInputStream(v).)
			);
	}
}
