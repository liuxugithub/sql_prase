package com.sinux.parser.result;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.sinux.parser.column.ScistorColumn;
import com.sinux.parser.column.ScistorSelectColumn;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.table.ScistorTable;

import java.util.ArrayList;
import java.util.List;

public abstract class ScistorColumnResult extends ScistorResult{
	protected Boolean haveWhere;
	protected List<ScistorColumn> whereConditionColumns = new ArrayList<>();
	protected String limitStr;
	protected String groupStr;
	protected SQLStatement statement;
	protected List<ScistorTable> tables = new ArrayList<>();
	public abstract List<ScistorColumn> getConditionColumns() throws ScistorParserException;
	public abstract void addConditionColumn(ScistorColumn column) throws ScistorParserException;
	public abstract void replaceWherePart(ScistorSelectColumn column) throws ScistorParserException;
	public  void setHaveWhere(Boolean where) {
		this.haveWhere=where;
	}
	public  Boolean haveWhere() {
		return haveWhere;
	}
	public void addAllWhereColumns(List<ScistorColumn> whereColumns){
		this.whereConditionColumns.addAll(whereColumns);
	}


	public SQLStatement getStatement() {
		return statement;
	}

	public void setStatement(SQLStatement statement) {
		this.statement = statement;
	}

	public String getLimitStr() {
		return limitStr;
	}

	public String getGroupStr() {
		return groupStr;
	}

	public void setGroupStr(String groupStr) {
		this.groupStr = groupStr;
	}

	public void setLimitStr(String limitStr) {
		this.limitStr = limitStr;
	}

	public void addTables(List<ScistorTable> tables){
		this.tables.addAll(tables);
	}
	public List<ScistorColumn> whereConditionColumns(){
		return whereConditionColumns;
	}
	public List<ScistorTable> tables(){
		return tables;
	}
}
