package com.pyrlong.data;

public class DataTypes {

	public static final int DATATABLE_STRING = 0;
	public static final int DATATABLE_DATE = 1;
	public static String getDataTypeName(int dataType) {
		if(DATATABLE_STRING == dataType) {
			return "string";
		}
		if(DATATABLE_DATE == dataType) {
			return "date";
		}
		return "string";
	}

}
