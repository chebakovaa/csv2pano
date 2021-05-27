package com.bisoft.model;

import com.bisoft.exeptions.GetObjectNamesException;
import com.bisoft.interfaces.IModelObject;
import com.bisoft.interfaces.IStructureSource;
import com.bisoft.models.ModelObjectDBCollection;
import com.bisoft.navi.common.interfaces.ISavedFormat;
import com.bisoft.navi.common.model.CSVFormat;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
	
}
