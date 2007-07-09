package org.regenstrief.linkage.io;

import org.regenstrief.linkage.*;
import org.regenstrief.linkage.util.*;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Class reads lines from a character delimited file until the end of the file is
 * reached.  The input file is first re-written to create a file of only the columns
 * of interest.  This file is then read and Records created using column
 * information in the LinkDataSource object.
 * 
 * As of July, 2007, class is hardwired to just parse pipe delimited files.
 *
 */

public class CharDelimFileReader extends DataSourceReader{
	
	protected File switched_file;
	protected BufferedReader file_reader;
	protected Record next_record;
	
	
	/**
	 * The constructor sorts the file according to the blocking variables and opens
	 * a BufferedReader.  If there's an error with sorting the file, the file reader
	 * is set to null.
	 * 
	 * @param lds	the LinkDataSource with information of a character delimited file
	 */
	public CharDelimFileReader(LinkDataSource lds){
		super(lds);
		
		File raw_file = new File(lds.getName());
		switched_file = switchColumns(raw_file);
		char raw_file_sep = lds.getAccess().charAt(0);
		
		try{
			file_reader = new BufferedReader(new FileReader(switched_file));
			next_record = line2Record(file_reader.readLine());
		}
		catch(IOException ioe){
			file_reader = null;
			next_record = null;
		}
	}
	
	/*
	 * Method switches columns.  Gets information from LinkDataSource object.
	 * 
	 * @param f	the file to modify
	 * @return	the resulting file
	 */
	private File switchColumns(File f){
		List<DataColumn> dcs1 = data_source.getDataColumns();
		int[] order1 = new int[data_source.getIncludeCount()];
		
		// iterate over the  DataColumn list and store the data position value
		// in order arrays at the index given by display_position, as long as
		// display positioin is not NA
		Iterator<DataColumn> it1 = dcs1.iterator();
		while(it1.hasNext()){
			DataColumn dc = it1.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				order1[dc.getIncludePosition()] = Integer.parseInt(dc.getColumnID());
			}
		}
		
		File switched = new File(data_source.getName() + ".switched");
		try{
			ColumnSwitcher cs = new ColumnSwitcher(f, switched, order1, data_source.getAccess().charAt(0));
			cs.switchColumns();
		}
		catch(IOException ioe){
			return null;
		}
		
		return switched;
	}
	
	
	
	/**
	 * Returns whether there are more records to be read from the reader.
	 */
	public boolean hasNextRecord(){
		return next_record != null;
	}
	
	/**
	 * Returns the next Record in the data source.  If there are no more recurds, returns null
	 */
	public Record nextRecord(){
		if(file_reader == null){
			return null;
		}
		Record ret = next_record;
		try{
			String line = file_reader.readLine();
			if(line != null){
				next_record = line2Record(line);
			} else {
				next_record = null;
			}
		}
		catch(IOException ioe){
			next_record = null;
		}
		return ret;
	}
	
	/**
	 * Converts a character delimted String into a Record object based on the 
	 * data source information in this object's LinkDataSource.
	 * 
	 * @param line	character-delimited line to convert to a Record object
	 * @return	the Record object with the data from that line
	 */
	public Record line2Record(String line){
		String[] split_line = line.split("\\|", -1);
		Record ret = new Record();
		List<DataColumn> cols = data_source.getDataColumns();
		for(int i = 0; i < cols.size(); i++){
			int include_index = cols.get(i).getIncludePosition();
			if(include_index != -1){
				ret.addDemographic(cols.get(i).getName(), split_line[include_index]);
			}
			
		}
		
		return ret;
	}
	
	/**
	 * Returns a boolean indicating if the reset of the reader was successful.
	 */
	public boolean reset(){
		try {
			file_reader.close();
			file_reader = new BufferedReader(new FileReader(switched_file));
			next_record = line2Record(file_reader.readLine());			
			return true;
		} catch (Exception e1) {
			return false;
		}
		
	}
}