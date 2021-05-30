package com.bisoft.model;


import com.bisoft.navi.common.exceptions.GetTitleObjectException;
import com.bisoft.navi.common.exceptions.LoadStructureSourceException;
import com.bisoft.navi.common.interfaces.IModelObject;
import com.bisoft.navi.common.interfaces.ISavedFormat;
import com.bisoft.navi.common.interfaces.IStructureSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bisoft.navi.common.interfaces.IStructureSource.ElementType.*;

public class FileStructureSource implements IStructureSource {
	


	private final File folder;
	private final ISavedFormat format;
	
	public FileStructureSource(File folder, ISavedFormat format) {
		this.folder = folder;
		this.format = format;
	}
	
	private String removeExtension(Path fileName) {
		return fileName.toString().replaceAll("(?<!^)[.]" + "[^.]*$", "");
	}
	
	@Override
	public Iterator<IModelObject> objectCollection(ElementType elementType) throws LoadStructureSourceException{
		List<IModelObject> res = new ArrayList();
		List<File> files = Arrays.stream(folder.listFiles()).filter(v -> v.getName().contains(map.get(elementType))).collect(Collectors.toList());
		for(File file: files) {
			try {
				res.add(new FileModelObject(
						removeExtension(file.toPath().getFileName())
						, format.loadedTitles(new FileInputStream(file))));
			} catch (GetTitleObjectException | FileNotFoundException e) {
				e.printStackTrace();
				throw new LoadStructureSourceException("Load structure from files fail");
			}
		}
		return res.iterator();
	}
	
}
