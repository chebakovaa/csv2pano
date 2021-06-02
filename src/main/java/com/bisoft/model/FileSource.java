package com.bisoft.model;

import com.bisoft.navi.common.exceptions.GetObjectNamesException;
import com.bisoft.navi.common.exceptions.LoadStructureSourceException;
import com.bisoft.navi.common.interfaces.IModelObject;
import com.bisoft.navi.common.interfaces.ISavedFormat;
import com.bisoft.navi.common.interfaces.IStructureSource;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	public Iterator<IModelObject> objectCollection() throws GetObjectNamesException {
		
		Arrays
			.stream(folder.listFiles())
			.map(v ->
				new FileModelObject(
					removeExtension(v.toPath().getFileName())
				, new FileInputStream(v).)
			);
		
		
		
		
		try {


			ResultSet tables = openedConnection.Query(collectionQuery);
			return new ModelObjectDBCollection(openedConnection, tables, objectQuery);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GetObjectNamesException("Get Object Names Fail");
		}
	}
	
	@Override
	public Iterator<IModelObject> objectCollection(ElementType et) throws LoadStructureSourceException {
		return null;
	}
}
